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

import java.util.ArrayList;

import com.abstractedsheep.kml.Style;
import com.abstractedsheep.shuttletracker.json.EtaJson;
import com.abstractedsheep.shuttletracker.json.RoutesJson;
import com.abstractedsheep.shuttletracker.json.RoutesJson.Route;
import com.abstractedsheep.shuttletracker.json.RoutesJson.Route.Coord;
import com.abstractedsheep.shuttletracker.json.VehicleJson;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.LayoutParams;

import android.os.Bundle;
import android.util.Log;

public class TrackerMapActivity extends MapActivity implements IShuttleServiceCallback {
	public static String MAPS_API_KEY = "01JOmSJBxx1vR0lM4z_VkVIYfWwZcOgZ6q1VAaQ"; //"01JOmSJBxx1voRKERKRP3C2v-43vBsKl74-b9Og"; "01JOmSJBxx1vR0lM4z_VkVIYfWwZcOgZ6q1VAaQ";
	private MapView map;
	private VehicleItemizedOverlay shuttlesOverlay;
	private LocationOverlay myLocationOverlay;
	private StopsItemizedOverlay stopsOverlay;
	private ShuttleDataService dataService;
	private boolean hasRoutes;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initMap();
        setContentView(map);
              
        dataService = ShuttleDataService.getInstance();
    }
    
    /** Set up the MapView with the default configuration */
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
     * Add routes and stops to map from the JSON as a PathOverlay and StopsItemizedOverlay. Also adds the shuttles overlay so it sits below the stops.
     * 
     * @param routes The list of routes parsed from the JSON
     */
    private void addRoutes(RoutesJson routes) {
    	Log.d("Tracker", "addRoutes()");
    	hasRoutes = true;
        stopsOverlay = new StopsItemizedOverlay(getResources().getDrawable(R.drawable.stop_marker), map);
        PathOverlay routesOverlay;
        Style style;
        ArrayList<GeoPoint> points;
        
        
        for (Route r : routes.getRoutes()) {
        	Log.d("Tracker", "Creating route " + r.getName());
        	style = new Style();
        	style.setColor(r.getColor());
        	style.setWidth(r.getWidth());
    		routesOverlay = new PathOverlay(style);
    		points = new ArrayList<GeoPoint>();
    		for (Coord c : r.getCoords()) {
    			points.add(new GeoPoint((int)(c.getLatitude() * 1e6), (int)(c.getLongitude() * 1e6)));
    		}
    		
    		routesOverlay.setPoints(points);
    		Log.d("Tracker", "Adding route " + r.getName());
    		map.getOverlays().add(routesOverlay);
        }        
        
        shuttlesOverlay = new VehicleItemizedOverlay(getResources().getDrawable(R.drawable.shuttle_color), map);
        shuttlesOverlay.putRoutes(routes.getRoutes());
        map.getOverlays().add(shuttlesOverlay);
        
        stopsOverlay.addAllStops(routes.getStops());
        map.getOverlays().add(stopsOverlay);
    }
    
   
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	myLocationOverlay.enableMyLocation();
    	
    	if (shuttlesOverlay != null)
    		shuttlesOverlay.removeAllVehicles();
    	map.invalidate();
    	
        routesUpdated(dataService.getRoutes());
    	dataUpdated(dataService.getCurrentShuttleLocations(), dataService.getCurrentEtas());
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	myLocationOverlay.disableMyLocation();
    }


	@Override
	protected boolean isRouteDisplayed() {
		return hasRoutes;
	}
	
	/** Calls map,invalidate(). For use with runOnUiThread() */
	private Runnable invalidateMap = new Runnable() {
		public void run() {
			map.invalidate();
		}
	};
	
	/** Calls stopsOverlay.putEtas(). For use with runOnUiThread() */
	private class PutEtas implements Runnable {
		private ArrayList<EtaJson> etas;
		public PutEtas(ArrayList<EtaJson> etas) {
			this.etas = etas;
		}
		public void run() {
			stopsOverlay.putEtas(etas);
		}
	}
	
	/** Calls addRoutes(). For use with runOnUiThread() */
	private class AddRoutes implements Runnable {
		private RoutesJson routes;
		public AddRoutes(RoutesJson routes) {
			this.routes = routes;
		}
		public void run() {
			addRoutes(routes);
		}
	}


	/**
	 * Updates the MapView with the latest shuttle positions and ETAs in a thread safe manner.
	 * 
	 * @param vehicles The list of current vehicle positions, null value indicates no change.
	 * @param etas The list of ETAs to the stops, null value indicates no change.
	 */
	public void dataUpdated(ArrayList<VehicleJson> vehicles, ArrayList<EtaJson> etas) {
		if (etas != null && stopsOverlay != null) {
			runOnUiThread(new PutEtas(etas));
		}
		
		if (vehicles != null && shuttlesOverlay != null) {
			shuttlesOverlay.removeAllVehicles();
        
        	for (VehicleJson v : vehicles) {
        		shuttlesOverlay.addVehicle(v);
        	}
        	
        	runOnUiThread(vehiclesUpdated);
		}
		
		runOnUiThread(invalidateMap);
	}
	
	Runnable vehiclesUpdated = new Runnable() {
		public void run() {
			shuttlesOverlay.vehiclesUpdated();
		}
	};

	/**
	 * Sets up the MapView with the shuttle routes in a thread safe manner. Will not work a second time unless hasRoutes is manually changed to false.
	 * 
	 *  @param routes The list of routes parsed from the JSON, null will cause the function to do nothing.
	 */
	public void routesUpdated(RoutesJson routes) {
		if (routes != null && !hasRoutes)
			runOnUiThread(new AddRoutes(routes));
	}

	public void dataServiceError(int errorCode) {
	}
}
