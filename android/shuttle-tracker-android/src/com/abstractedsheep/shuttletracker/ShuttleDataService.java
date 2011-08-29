/* 
 * Copyright 2011 Austin Wagner
 *     
 * This file is part of Mobile Shuttle Tracker.
 *
 *  Mobile Shuttle Tracker is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Mobile Shuttle Tracker is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Mobile Shuttle Tracker.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package com.abstractedsheep.shuttletracker;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.abstractedsheep.shuttletrackerworld.Coordinate;
import com.abstractedsheep.shuttletrackerworld.Netlink;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.abstractedsheep.shuttletracker.json.EtaArray;
import com.abstractedsheep.shuttletracker.json.EtaJson;
import com.abstractedsheep.shuttletracker.json.ExtraEtaJson;
import com.abstractedsheep.shuttletracker.json.VehicleArray;
import com.abstractedsheep.shuttletracker.json.VehicleJson;
import com.abstractedsheep.shuttletracker.sql.DatabaseHelper;
import com.abstractedsheep.shuttletrackerworld.World;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

public class ShuttleDataService implements OnSharedPreferenceChangeListener {
	private final ObjectMapper mapper = new ObjectMapper();
	private final Set<IShuttleServiceCallback> callbacks = new HashSet<IShuttleServiceCallback>();
	public final AtomicBoolean active = new AtomicBoolean(true);
	private ArrayList<EtaJson> etas;
	private World world;
	private boolean informedNoConnection = false;
	private Context ctx;
	private final AtomicInteger updateRate = new AtomicInteger(5000);
	private String extraEtaStopId = null;
	private int extraEtaRouteId = -1;
	
	// Private constructor prevents instantiation from other classes
	private ShuttleDataService() {
	}

	/**
	* SingletonHolder is loaded on the first execution of Singleton.getInstance() 
	* or the first access to SingletonHolder.INSTANCE, not before.
	*/
	private static class SingletonHolder { 
		public static final ShuttleDataService INSTANCE = new ShuttleDataService();
	}

	public static ShuttleDataService getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public void setApplicationContext(Context context) {
		ctx = context;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		prefs.registerOnSharedPreferenceChangeListener(this);
		updateRate.set(prefs.getInt(TrackerPreferences.UPDATE_RATE, 5000));
	}
	 
    /**
     * Uses the Jackson object mapper to read the JSON from a URL into a Java Bean.
     * 
     * @param url The URL to retrieve the JSON from.
     * @param generic The class type of the JSON in the form of a Java Bean.
     * @return The parsed JSON in a new instance of the Java Bean.
     */
    public <T> T parseJson(String url, Class<T> generic) {		
    	T parsedClass = null;
	
		try {
			URL jsonUrl = new URL(url);
			URLConnection jsonConnection = jsonUrl.openConnection();
			
			parsedClass = mapper.readValue(jsonConnection.getInputStream(), generic);
			informedNoConnection = false;
		} catch (JsonParseException e) {
			Log.w("Tracker", "Error Parsing URL: " + url);
			Log.w("Tracker", "Data type: " + generic.getName());
			e.printStackTrace();
		} catch (JsonMappingException e) {
			Log.w("Tracker", "Error Parsing URL: " + url);
			Log.w("Tracker", "Data type: " + generic.getName());
			e.printStackTrace();
		} catch (IOException e) {
			if (!informedNoConnection) {
				informedNoConnection = true;
				notifyError(IShuttleServiceCallback.NO_CONNECTION_ERROR);
			}
			e.printStackTrace();
		}
  
    	return parsedClass;
    }
    
    public final Runnable updateRoutes = new Runnable() {
		public void run() {
			synchronized (this) {
				Netlink tempRoutes = null;
				DatabaseHelper db = new DatabaseHelper(ctx);
				
				while (tempRoutes == null) {
					if (db.hasRoutes()) {
						world = World.generateWorld(db.getRoutes());
						notifyRoutesUpdated();
					} else {
						tempRoutes = parseJson("http://shuttles.rpi.edu/displays/netlink.js", Netlink.class);
						if (tempRoutes != null) {
							world = World.generateWorld(tempRoutes);
							db.putRoutes(tempRoutes);
							notifyRoutesUpdated();
						}
					}
					
					SystemClock.sleep(5000);
				}
				
				db.close();
			}	
		}
	};

    public final Runnable updateShuttles = new Runnable() {
		public void run() {
			while (active.get()) {
				synchronized (this) {
					ArrayList<VehicleJson> tempVehicles = parseJson("http://shuttles.abstractedsheep.com/data_service.php?action=get_shuttle_positions", VehicleArray.class);
                    if (tempVehicles != null) {
                        for (VehicleJson v : tempVehicles) {
                            world.addOrUpdateShuttle(v.getShuttle_id(), new Coordinate((int)(v.getLatitude() * 1e6), (int)(v.getLongitude() * 1e6)), v.getName(),
                                    v.getHeading(), v.getCardinal_point(), v.getSpeed(), v.getRoute_id());
                        }
                    }

					ArrayList<EtaJson> tempEtas = parseJson("http://shuttles.abstractedsheep.com/data_service.php?action=get_all_eta", EtaArray.class);
					if (tempEtas != null) {
						etas = tempEtas;
					}
					
					if (tempVehicles != null || tempEtas != null) {
						notifyShuttlesUpdated();
					}
					
					if (extraEtaStopId != null && extraEtaRouteId != -1) {
						ExtraEtaJson exEtas = parseJson("http://shuttles.abstractedsheep.com/data_service.php?action=get_all_extra_eta&rt=" + extraEtaRouteId + "&st=" + extraEtaStopId, ExtraEtaJson.class);
						notifyExtraEtasUpdated(exEtas);
					}
				}	
				
				SystemClock.sleep(updateRate.get());
			}
		}
	};


	public synchronized void setExtraEtaToGet(String extraEtaStopId, int extraEtaRouteId) {
		this.extraEtaRouteId = extraEtaRouteId;
		this.extraEtaStopId = extraEtaStopId;
		new Thread(new Runnable() {
			public void run() {
				notifyExtraEtasUpdated(parseJson("http://shuttles.abstractedsheep.com/data_service.php?action=get_all_extra_eta&rt=" +
						ShuttleDataService.this.extraEtaRouteId + "&st=" + ShuttleDataService.this.extraEtaStopId, ExtraEtaJson.class));
			}
		}).start();
	}
	
	public World getWorld() {
		return world;
	}
	
	private synchronized void notifyExtraEtasUpdated(ExtraEtaJson etas) {
		for (IShuttleServiceCallback c : callbacks) {
			c.extraEtasUpdated(etas);
		}
	}	
	
	private synchronized void notifyShuttlesUpdated() {
		for (IShuttleServiceCallback c : callbacks) {
			c.dataUpdated(world, etas);
		}
	}
	
	private synchronized void notifyRoutesUpdated() {
		for (IShuttleServiceCallback c : callbacks) {
			c.routesUpdated(world);
		}
	}
	
	private synchronized void notifyError(int errorCode) {
		for (IShuttleServiceCallback c : callbacks) {
			c.dataServiceError(errorCode);
		}
	}
	
	public synchronized void registerCallback(IShuttleServiceCallback callback) {
		callbacks.add(callback);		
		callback.dataUpdated(world, etas);
		callback.routesUpdated(world);
	}

	public synchronized void unregisterCallback(IShuttleServiceCallback callback) {
		callbacks.remove(callback);		
	}
	
	public synchronized ArrayList<EtaJson> getCurrentEtas() {
		return etas;
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		updateRate.set(Integer.parseInt(sharedPreferences.getString(TrackerPreferences.UPDATE_RATE, "5000")));
	}	
}
