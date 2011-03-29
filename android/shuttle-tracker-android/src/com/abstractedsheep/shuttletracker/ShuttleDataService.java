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

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.abstractedsheep.shuttletracker.json.EtaArray;
import com.abstractedsheep.shuttletracker.json.EtaJson;
import com.abstractedsheep.shuttletracker.json.RoutesJson;
import com.abstractedsheep.shuttletracker.json.VehicleArray;
import com.abstractedsheep.shuttletracker.json.VehicleJson;
import com.abstractedsheep.shuttletracker.sql.DatabaseHelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class ShuttleDataService implements OnSharedPreferenceChangeListener {
	private ObjectMapper mapper = new ObjectMapper();
	private Set<IShuttleServiceCallback> callbacks = new HashSet<IShuttleServiceCallback>();
	public AtomicBoolean active = new AtomicBoolean(true);
	private ArrayList<VehicleJson> vehicles;
	private ArrayList<EtaJson> etas;
	private RoutesJson routes;
	private boolean informedNoConnection = false;
	private Context ctx;
	private SharedPreferences prefs;
	private AtomicInteger updateRate = new AtomicInteger(5000);
	
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
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
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
    private <T> T parseJson(String url, Class<T> generic) {		
    	T parsedClass = null;
	
		try {
			URL jsonUrl = new URL(url);
			URLConnection jsonConnection = jsonUrl.openConnection();
			
			parsedClass = mapper.readValue(jsonConnection.getInputStream(), generic);
			informedNoConnection = false;
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
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
    
    public Runnable updateRoutes = new Runnable() {
		public void run() {
			synchronized (this) {
				RoutesJson tempRoutes;
				DatabaseHelper db = new DatabaseHelper(ctx);
				
				do {
					if (db.hasRoutes()) {
						tempRoutes = db.getRoutes();
						routes = tempRoutes;
						notifyRoutesUpdated(routes);
					} else {
						tempRoutes = parseJson("http://shuttles.rpi.edu/displays/netlink.js", RoutesJson.class);
						if (tempRoutes != null) {
							routes = tempRoutes;
							db.putRoutes(routes);
							notifyRoutesUpdated(routes);
						}
					}
					
					SystemClock.sleep(5000);
				} while (tempRoutes == null);	
				
				db.close();
			}	
		}
	};

    public Runnable updateShuttles = new Runnable() {
		public void run() {
			while (active.get()) {
				synchronized (this) {
					ArrayList<VehicleJson> tempVehicles = parseJson("http://www.abstractedsheep.com/~ashulgach/data_service.php?action=get_shuttle_positions", VehicleArray.class);
					if (tempVehicles != null) {
						vehicles = tempVehicles;
					}
					
					ArrayList<EtaJson> tempEtas = parseJson("http://www.abstractedsheep.com/~ashulgach/data_service.php?action=get_all_eta", EtaArray.class);
					if (tempEtas != null) {
						etas = tempEtas;
					}
					
					if (tempVehicles != null || tempEtas != null) {
						notifyShuttlesUpdated(vehicles, etas);
					}
				}	
				
				SystemClock.sleep(updateRate.get());
			}
		}
	};
	
	private synchronized void notifyShuttlesUpdated(ArrayList<VehicleJson> vehicles, ArrayList<EtaJson> etas) {
		for (IShuttleServiceCallback c : callbacks) {
			c.dataUpdated(vehicles, etas);
		}
	}
	
	private synchronized void notifyRoutesUpdated(RoutesJson routes) {
		for (IShuttleServiceCallback c : callbacks) {
			c.routesUpdated(routes);
		}
	}
	
	private synchronized void notifyError(int errorCode) {
		for (IShuttleServiceCallback c : callbacks) {
			c.dataServiceError(errorCode);
		}
	}
	
	public synchronized void registerCallback(IShuttleServiceCallback callback) {
		callbacks.add(callback);		
		callback.dataUpdated(vehicles, etas);
		callback.routesUpdated(routes);
	}

	public synchronized void unregisterCallback(IShuttleServiceCallback callback) {
		callbacks.remove(callback);		
	}
	
	public synchronized ArrayList<VehicleJson> getCurrentShuttleLocations() {
		return vehicles;
	}
	
	public synchronized ArrayList<EtaJson> getCurrentEtas() {
		return etas;
	}

	public synchronized RoutesJson getRoutes() {
		return routes;
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		updateRate.set(Integer.parseInt(sharedPreferences.getString(TrackerPreferences.UPDATE_RATE, "5000")));
	}	
}
