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

package com.abstractedsheep.shuttletracker.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.abstractedsheep.shuttletracker.json.EtaArray;
import com.abstractedsheep.shuttletracker.json.EtaJson;
import com.abstractedsheep.shuttletracker.json.RoutesJson;
import com.abstractedsheep.shuttletracker.json.VehicleArray;
import com.abstractedsheep.shuttletracker.json.VehicleJson;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class ShuttleDataService extends Service {
	private ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
	private String oldShuttleJson = "";
	private String oldEtaJson = "";
	private Set<IShuttleDataUpdateCallback> callbacks = new HashSet<IShuttleDataUpdateCallback>();
	private final Binder binder = new LocalBinder();
	private AtomicBoolean active = new AtomicBoolean(true);
	private ArrayList<VehicleJson> vehicles;
	private ArrayList<EtaJson> etas;
	private RoutesJson routes;

	@Override
	public void onCreate() {
		super.onCreate();
		
		new Thread(updateShuttles).start();
		new Thread(updateRoutes).start();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	private ArrayList<VehicleJson> parseShuttleJson(String url) {
    	VehicleArray vehicles = null;
	
		try {
			URL shuttlesJson;
			shuttlesJson = new URL(url);
			URLConnection shuttleJsonConnection = shuttlesJson.openConnection();
			
			// ObjectMapper doesn't like the stream, but it works if converted into a string first
			String json = convertStreamToString(shuttleJsonConnection.getInputStream());
			
			if(!json.equals(oldShuttleJson)) {
				oldShuttleJson = json;
				vehicles = mapper.readValue(json, VehicleArray.class);
			}
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
   
    	return vehicles;
    }
    
    private ArrayList<EtaJson> parseEtaJson(String url) {
    	EtaArray etas = null;
	
		try {
			URL shuttlesJson;
			shuttlesJson = new URL(url);
			URLConnection shuttleJsonConnection = shuttlesJson.openConnection();
			
			// ObjectMapper doesn't like the stream, but it works if converted into a string first
			String json = convertStreamToString(shuttleJsonConnection.getInputStream());
			if (!json.equals(oldEtaJson)) {
				oldEtaJson = json;
				etas = mapper.readValue(json, EtaArray.class);
			}
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
   
    	return etas;
    }
    
    private RoutesJson parseRoutesJson(String url) {		
    	RoutesJson routes = null;
	
		try {
			URL shuttlesJson;
			shuttlesJson = new URL(url);
			URLConnection shuttleJsonConnection = shuttlesJson.openConnection();
			
			Log.d("Tracker", String.valueOf(System.currentTimeMillis()));
			// ObjectMapper doesn't like the stream, but it works if converted into a string first
			String json = convertStreamToString(shuttleJsonConnection.getInputStream());
			Log.d("Tracker", String.valueOf(System.currentTimeMillis()));
			routes = mapper.readValue(json, RoutesJson.class);
			Log.d("Tracker", String.valueOf(System.currentTimeMillis()));
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
   
    	return routes;
    }
    
    private Runnable updateRoutes = new Runnable() {
		public void run() {
			synchronized (this) {
				RoutesJson tempRoutes;
				
				do {
					tempRoutes = parseRoutesJson("http://shuttles.rpi.edu/displays/netlink.js");
					if (tempRoutes != null) {
						routes = tempRoutes;
					}
					
					SystemClock.sleep(5000);
				} while (tempRoutes == null);
				
				
				if (tempRoutes != null)
					notifyRoutesUpdated(routes);
			}	
		}
	};

    private Runnable updateShuttles = new Runnable() {
		public void run() {
			while (active.get()) {
				synchronized (this) {
					ArrayList<VehicleJson> tempVehicles = parseShuttleJson("http://www.abstractedsheep.com/~ashulgach/data_service.php?action=get_shuttle_positions");
					if (tempVehicles != null) {
						vehicles = tempVehicles;
					}
					
					ArrayList<EtaJson> tempEtas = parseEtaJson("http://www.abstractedsheep.com/~ashulgach/data_service.php?action=get_next_eta");
					if (tempEtas != null) {
						etas = tempEtas;
					}
					
					if (tempVehicles != null || tempEtas != null)
						notifyShuttlesUpdated(vehicles, etas);
				}	
				
				SystemClock.sleep(5000);
			}
		}
	};
	
	private synchronized void notifyShuttlesUpdated(ArrayList<VehicleJson> vehicles, ArrayList<EtaJson> etas) {
		for (IShuttleDataUpdateCallback c : callbacks) {
			c.dataUpdated(vehicles, etas);
		}
	}
	
	private synchronized void notifyRoutesUpdated(RoutesJson routes) {
		for (IShuttleDataUpdateCallback c : callbacks) {
			c.routesUpdated(routes);
		}
	}
	
	public class LocalBinder extends Binder implements IShuttleDataMonitor {

		public synchronized void registerCallback(IShuttleDataUpdateCallback callback) {
			callbacks.add(callback);		
			callback.dataUpdated(vehicles, etas);
			callback.routesUpdated(routes);
		}

		public synchronized void unregisterCallback(IShuttleDataUpdateCallback callback) {
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
	}
	
	public String convertStreamToString(InputStream is) throws IOException
	{
		/*
		* To convert the InputStream to String we use the
		* Reader.read(char[] buffer) method. We iterate until the
		* Reader return -1 which means there's no more data to
		* read. We use the StringWriter class to produce the string.
		*/
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(
				new InputStreamReader(is, "UTF-8"), 20000);
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {        
			return "";
		}
	}

}
