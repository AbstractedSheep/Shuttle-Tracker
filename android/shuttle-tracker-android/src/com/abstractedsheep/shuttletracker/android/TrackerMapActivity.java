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

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

public class TrackerMapActivity extends MapActivity implements IShuttleDataUpdateCallback {
	public static String MAPS_API_KEY = "01JOmSJBxx1vR0lM4z_VkVIYfWwZcOgZ6q1VAaQ"; //"01JOmSJBxx1voRKERKRP3C2v-43vBsKl74-b9Og"; "01JOmSJBxx1vR0lM4z_VkVIYfWwZcOgZ6q1VAaQ";
	private MapView map;
	private VehicleItemizedOverlay shuttlesOverlay;
	private LocationOverlay myLocationOverlay;
	private StopsItemizedOverlay stopsOverlay;
	
	private IShuttleDataMonitor service = null;
	private ServiceConnection svcConn = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			service = (IShuttleDataMonitor)binder;
	    	
	    	service.registerCallback(TrackerMapActivity.this);
		}

		public void onServiceDisconnected(ComponentName className) {
			service = null;
		}
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initMap();
        setContentView(map);
              
        getApplicationContext().bindService(new Intent(this, ShuttleDataService.class), svcConn, BIND_AUTO_CREATE);
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	if (service != null)
    		getApplicationContext().unbindService(svcConn);
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
    private void addRoutes(RoutesJson routes) {
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
        
        shuttlesOverlay = new VehicleItemizedOverlay(getResources().getDrawable(R.drawable.shuttle_color));
        shuttlesOverlay.putRoutes(routes.getRoutes());
        map.getOverlays().add(shuttlesOverlay);
        
        stopsOverlay.addAllStops(routes.getStops());
        map.getOverlays().add(stopsOverlay);
    }
    
   
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	myLocationOverlay.enableMyLocation();
    	
    	if (service != null)
    		service.registerCallback(this);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	myLocationOverlay.disableMyLocation();
    	
    	if (service != null)
    		service.unregisterCallback(this);
    }


	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	private Runnable invalidateMap = new Runnable() {
		public void run() {
			map.invalidate();
		}
	};

	public void dataUpdated(ArrayList<VehicleJson> vehicles, ArrayList<EtaJson> etas) {
		if (etas != null && stopsOverlay != null) {
			stopsOverlay.putEtas(etas);
		}
		
		if (vehicles != null && shuttlesOverlay != null) {
			shuttlesOverlay.removeAllVehicles();
        
        	for (VehicleJson v : vehicles) {
        		shuttlesOverlay.addVehicle(v);
        	}
		}
		
		runOnUiThread(invalidateMap);
	}

	public void routesUpdated(RoutesJson routes) {
		if (routes != null)
			addRoutes(routes);
	}
}
