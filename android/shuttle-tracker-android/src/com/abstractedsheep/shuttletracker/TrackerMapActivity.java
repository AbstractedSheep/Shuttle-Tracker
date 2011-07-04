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

import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;

import com.abstractedsheep.shuttletracker.MapsApiKey;
import com.abstractedsheep.shuttletracker.R;
import com.abstractedsheep.shuttletracker.json.EtaJson;
import com.abstractedsheep.shuttletracker.json.ExtraEtaJson;
import com.abstractedsheep.shuttletracker.json.Style;
import com.abstractedsheep.shuttletracker.json.VehicleJson;
import com.abstractedsheep.shuttletracker.mapoverlay.LocationOverlay;
import com.abstractedsheep.shuttletracker.mapoverlay.NullOverlay;
import com.abstractedsheep.shuttletracker.mapoverlay.PathOverlay;
import com.abstractedsheep.shuttletracker.mapoverlay.StopsItemizedOverlay;
import com.abstractedsheep.shuttletracker.mapoverlay.TimestampOverlay;
import com.abstractedsheep.shuttletracker.mapoverlay.VehicleItemizedOverlay;
import com.abstractedsheep.shuttletrackerworld.Netlink;
import com.abstractedsheep.shuttletrackerworld.Netlink.RouteJson;
import com.abstractedsheep.shuttletrackerworld.Netlink.StopJson;
import com.abstractedsheep.shuttletrackerworld.Netlink.RouteJson.RouteCoordinateJson;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.LayoutParams;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class TrackerMapActivity extends MapActivity implements IShuttleServiceCallback {
	private static final int DEFAULT_LAT = 42729640;
	private static final int DEFAULT_LON = -73681280;
	private static final int DEFAULT_ZOOM = 15;
	private static final int PREFERENCES = 1;
	private MapView map;
	private VehicleItemizedOverlay shuttlesOverlay;
	private LocationOverlay myLocationOverlay;
	private StopsItemizedOverlay stopsOverlay;
	private ShuttleDataService dataService;
	private boolean hasRoutes;
	private SharedPreferences prefs;
	private TimestampOverlay timestampOverlay;
	private HashMap<Integer, PathOverlay> routeOverlays;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initMap();
        setContentView(map);
              
        dataService = ShuttleDataService.getInstance();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }
    
    /** Set up the MapView with the default configuration */
    private void initMap() {
    	map = new MapView(this, MapsApiKey.MAPS_API_KEY);
    	LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, new GeoPoint(0, 0), 0);
        map.setLayoutParams(lp);
        map.getController().setZoom(DEFAULT_ZOOM);
        map.getController().setCenter(new GeoPoint(DEFAULT_LAT, DEFAULT_LON));
        map.setClickable(true);
        map.setFocusable(true);
        map.setBuiltInZoomControls(true);
        
        map.getOverlays().add(new NullOverlay());
        
        myLocationOverlay = new LocationOverlay(this, map, R.drawable.glyphish_location_arrow);
        map.getOverlays().add(myLocationOverlay);
    }
    
    /**
     * Add routes and stops to map from the JSON as a PathOverlay and StopsItemizedOverlay. Also adds the shuttles overlay so it sits below the stops.
     * 
     * @param routes The list of routes parsed from the JSON
     */
    private void addRoutes(Netlink routes) {
    	hasRoutes = true;
        stopsOverlay = new StopsItemizedOverlay(this, getResources().getDrawable(R.drawable.stop_marker), map, PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        routeOverlays = new HashMap<Integer, PathOverlay>();
        PathOverlay routeOverlay;
        Style style;
        ArrayList<GeoPoint> points;
        
        
        for (RouteJson r : routes.getRoutes()) {
        	style = new Style();
        	style.setColor(r.getColor());
        	style.setWidth(r.getWidth());
    		routeOverlay = new PathOverlay(style);
    		points = new ArrayList<GeoPoint>();
    		for (RouteCoordinateJson c : r.getCoords()) {
    			points.add(new GeoPoint((int)(c.getLatitude() * 1e6), (int)(c.getLongitude() * 1e6)));
    		}
    		
    		routeOverlay.setPoints(points);
    		routeOverlay.setVisiblity(r.getVisible());
    		routeOverlays.put(r.getId(), routeOverlay);
    		map.getOverlays().add(routeOverlays.get(r.getId()));
        }        
        
        shuttlesOverlay = new VehicleItemizedOverlay(getResources().getDrawable(R.drawable.shuttle_color), map, PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        shuttlesOverlay.putRoutes(routes.getRoutes());
        map.getOverlays().add(shuttlesOverlay);
        
        boolean cont = false;
        
        for (StopJson s : routes.getStops()) {
        	for (RouteJson r : routes.getRoutes()) {
        		for (StopJson.StopRouteJson sr : s.getRoutes()) {
        			if (r.getVisible() && sr.getId() == r.getId()) {
        				stopsOverlay.addStop(s);
        				cont = true;
        				continue;
        			}
        		}
        		if (cont) { cont = false; continue; }
        	}
        }
        map.getOverlays().add(stopsOverlay);
        
        timestampOverlay = new TimestampOverlay(prefs.getBoolean(TrackerPreferences.USE_24_HOUR, false));
        map.getOverlays().add(timestampOverlay);
    }
    
   
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	if (prefs.getBoolean(TrackerPreferences.MY_LOCATION, true))
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
		private Netlink routes;
		public AddRoutes(Netlink routes) {
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
		
		if (vehicles != null && shuttlesOverlay != null && timestampOverlay != null) {
			shuttlesOverlay.removeAllVehicles();
        
        	for (VehicleJson v : vehicles) {
        		shuttlesOverlay.addVehicle(v);
        	}

        	timestampOverlay.setLastUpdateTime(new Date());
        	timestampOverlay.setStatusText(getResources().getString(R.string.status_ok));
        	
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
	public void routesUpdated(Netlink routes) {
		if (routes != null && !hasRoutes) {
			runOnUiThread(new AddRoutes(routes));
			runOnUiThread(invalidateMap);
		}
	}

	public void dataServiceError(int errorCode) {
		switch (errorCode) {
		case (IShuttleServiceCallback.NO_CONNECTION_ERROR):
			// Make the shuttle display clear when the connection is lost
			dataUpdated(new ArrayList<VehicleJson>(), null);
			if (timestampOverlay != null)
				timestampOverlay.setStatusText(getResources().getString(R.string.status_no_conn));
			runOnUiThread(invalidateMap);
			break;
		default:
			break;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_options, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.options:
			startActivityForResult(new Intent(this, TrackerPreferences.class), PREFERENCES);
			return true;
		case R.id.center_map:
			if (myLocationOverlay.isMyLocationEnabled())
				map.getController().animateTo(myLocationOverlay.getMyLocation());
			else
				map.getController().animateTo(new GeoPoint(DEFAULT_LAT, DEFAULT_LON));
			map.getController().setZoom(DEFAULT_ZOOM);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
		case PREFERENCES:
			if (prefs.getBoolean(TrackerPreferences.MY_LOCATION, true))
	    		myLocationOverlay.enableMyLocation();
			else
				myLocationOverlay.disableMyLocation();
			if (timestampOverlay != null)
				timestampOverlay.set24Hour(prefs.getBoolean(TrackerPreferences.USE_24_HOUR, false));
		}
	}

	public void extraEtasUpdated(ExtraEtaJson etas) {	
	}
	
	public void displayStop(String stopId) {
		stopsOverlay.displayStop(stopId);
	}
}
