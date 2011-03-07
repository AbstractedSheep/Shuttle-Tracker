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

import android.os.SystemClock;
import android.util.Log;

public class ShuttleDataService {
	private ObjectMapper mapper = new ObjectMapper();
	private Set<IShuttleDataUpdateCallback> callbacks = new HashSet<IShuttleDataUpdateCallback>();
	public AtomicBoolean active = new AtomicBoolean(true);
	private ArrayList<VehicleJson> vehicles;
	private ArrayList<EtaJson> etas;
	private RoutesJson routes;
	
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
			URL shuttlesJson;
			shuttlesJson = new URL(url);
			URLConnection shuttleJsonConnection = shuttlesJson.openConnection();
			
			// ObjectMapper doesn't like the stream, but it works if converted into a string first
			long start = System.currentTimeMillis();
			String json = convertStreamToString(shuttleJsonConnection.getInputStream());		
			Log.d("Tracker", "Converted " + generic.getName() + " stream to string in " + String.valueOf(System.currentTimeMillis() - start) + "ms");
			start = System.currentTimeMillis();
			parsedClass = mapper.readValue(json, generic);
			Log.d("Tracker", generic.getName() + " mapping complete in " + String.valueOf(System.currentTimeMillis() - start) + "ms");
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
   
    	return parsedClass;
    }
    
    public Runnable updateRoutes = new Runnable() {
		public void run() {
			synchronized (this) {
				RoutesJson tempRoutes;
				
				do {
					tempRoutes = parseJson("http://shuttles.rpi.edu/displays/netlink.js", RoutesJson.class);
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

	private String convertStreamToString(InputStream is) throws IOException
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
