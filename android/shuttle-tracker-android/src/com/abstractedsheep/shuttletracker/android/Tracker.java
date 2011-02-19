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

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.abstractedsheep.kml.Style;
import com.abstractedsheep.shuttletracker.json.EtaArray;
import com.abstractedsheep.shuttletracker.json.EtaJson;
import com.abstractedsheep.shuttletracker.json.RoutesJson;
import com.abstractedsheep.shuttletracker.json.RoutesJson.Route;
import com.abstractedsheep.shuttletracker.json.RoutesJson.Route.Coord;
import com.abstractedsheep.shuttletracker.json.VehicleArray;
import com.abstractedsheep.shuttletracker.json.VehicleJson;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.LayoutParams;

import android.os.Bundle;

public class Tracker extends MapActivity {
	public static String MAPS_API_KEY = "01JOmSJBxx1voRKERKRP3C2v-43vBsKl74-b9Og"; //"01JOmSJBxx1voRKERKRP3C2v-43vBsKl74-b9Og"; "01JOmSJBxx1vR0lM4z_VkVIYfWwZcOgZ6q1VAaQ";
	private MapView map;
	private Thread updateThread;
	public boolean threadLock = false;
	private VehicleItemizedOverlay shuttlesOverlay;
	private LocationOverlay myLocationOverlay;
	private StopsItemizedOverlay stopsOverlay;
	private boolean runUpdateShuttles;
	ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initMap();
        setContentView(map);
              
        addRoutes();  
    }
    
    /**
     * Set up the map view with the default configuration
     */
    private void initMap() {
    	map = new MapView(this, MAPS_API_KEY);
    	LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, new GeoPoint(0, 0), 0);
        map.setLayoutParams(lp);
        map.getController().setZoom(15);
        map.getController().setCenter(new GeoPoint(42729640, -73681280));
        map.setClickable(true);
        map.setFocusable(true);
        map.setBuiltInZoomControls(true);
        
        myLocationOverlay = new LocationOverlay(this, map, R.drawable.shuttle);
        map.getOverlays().add(myLocationOverlay);
    }
    
    /**
     * Add routes and stops to map from the KML file as a PathOverlay and ItemizedOverlay
     */
    private void addRoutes() {
    	RoutesJson routes = getRoutes();
        stopsOverlay = new StopsItemizedOverlay(getResources().getDrawable(R.drawable.stop_marker), map);
        PathOverlay routesOverlay;
        Style style;
        
        for (Route r : routes.getRoutes()) {
        	style = new Style();
        	style.setColor(r.getColor());
        	style.setWidth(r.getWidth());
    		routesOverlay = new PathOverlay(style);
    		for (Coord c : r.getCoords()) {
    			routesOverlay.addPoint(new GeoPoint((int)(c.getLatitude() * 1e6), (int)(c.getLongitude() * 1e6)));
    		}
    		map.getOverlays().add(routesOverlay);
        }
        
        shuttlesOverlay = new VehicleItemizedOverlay(getResources().getDrawable(R.drawable.shuttle_color), map);
        map.getOverlays().add(shuttlesOverlay);
        
        stopsOverlay.addAllStops(routes.getStops());
        map.getOverlays().add(stopsOverlay);
    }
    
    private RoutesJson getRoutes() {		
    	RoutesJson routes = null;
	
		try {
			URL shuttlesJson;
			shuttlesJson = new URL("http://shuttles.rpi.edu/displays/netlink.js");
			URLConnection shuttleJsonConnection = shuttlesJson.openConnection();
			
			// ObjectMapper doesn't like the stream, but it works if converted into a string first
			String json = convertStreamToString(shuttleJsonConnection.getInputStream());
			routes = mapper.readValue(json, RoutesJson.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
   
    	return routes;
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	myLocationOverlay.enableMyLocation();
    	runUpdateShuttles = true;
        updateThread = new Thread(updateShuttles);
    	updateThread.start();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	myLocationOverlay.disableMyLocation();
    	runUpdateShuttles = false;
    	threadLock = false;
    }
    
 
    private ArrayList<VehicleJson> parseShuttleJson(String url) {
    	VehicleArray vehicles = null;
	
		try {
			URL shuttlesJson;
			shuttlesJson = new URL(url);
			URLConnection shuttleJsonConnection = shuttlesJson.openConnection();
			
			// ObjectMapper doesn't like the stream, but it works if converted into a string first
			String json = convertStreamToString(shuttleJsonConnection.getInputStream());
			vehicles = mapper.readValue(json, VehicleArray.class);
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
			etas = mapper.readValue(json, EtaArray.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
   
    	return etas;
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	private Runnable updateShuttles = new Runnable() {
		
		public void run() {
			while (runUpdateShuttles) {
				threadLock = true;
				
				ArrayList<VehicleJson> vehicles = parseShuttleJson("http://shuttles.rpi.edu/vehicles/current.js");
				ArrayList<EtaJson> etas = parseEtaJson("http://www.abstractedsheep.com/~ashulgach/data_service.php?action=get_next_eta");
				
				if (etas != null) {
					stopsOverlay.putEtas(etas);
				}
				
				if (vehicles != null) {
					shuttlesOverlay.removeAllVehicles();
		        
		        	for (VehicleJson v : vehicles) {
		        		shuttlesOverlay.addVehicle(v.getVehicle());
		        	}
				}
				
				runOnUiThread(invalidateMap);
	        
		        threadLock = false;
			}
			
		}
	};
	
	private Runnable invalidateMap = new Runnable() {
		public void run() {
			map.invalidate();
		}
	};
	
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
		                  new InputStreamReader(is, "UTF-8"), 4000);
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
