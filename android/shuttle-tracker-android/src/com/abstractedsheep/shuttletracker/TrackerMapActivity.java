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

import com.abstractedsheep.shuttletracker.json.EtaJson;
import com.abstractedsheep.shuttletracker.json.ExtraEtaJson;
import com.abstractedsheep.shuttletracker.json.Style;
import com.abstractedsheep.shuttletracker.mapoverlay.LocationOverlay;
import com.abstractedsheep.shuttletracker.mapoverlay.NullOverlay;
import com.abstractedsheep.shuttletracker.mapoverlay.PathOverlay;
import com.abstractedsheep.shuttletracker.mapoverlay.StopsItemizedOverlay;
import com.abstractedsheep.shuttletracker.mapoverlay.TimestampOverlay;
import com.abstractedsheep.shuttletracker.mapoverlay.ShuttleItemizedOverlay;
import com.abstractedsheep.shuttletrackerworld.Coordinate;
import com.abstractedsheep.shuttletrackerworld.Route;
import com.abstractedsheep.shuttletrackerworld.World;
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
	private ShuttleItemizedOverlay shuttlesOverlay;
	private LocationOverlay myLocationOverlay;
	private StopsItemizedOverlay stopsOverlay;
	private ShuttleDataService dataService;
	private boolean hasRoutes;
	private SharedPreferences prefs;
	private TimestampOverlay timestampOverlay;
	
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
    

    private void setWorld(World world) {
    	hasRoutes = true;

        PathOverlay routeOverlay;
        Style style;
        ArrayList<GeoPoint> points;

        synchronized (world.getRouteList()) {
            for (Route r : world.getRouteList()) {
                style = new Style();
                style.setColor(r.getColor());
                style.setWidth(4);
                routeOverlay = new PathOverlay(style);
                points = new ArrayList<GeoPoint>();
                for (Coordinate c: r.getCoordinates()) {
                    points.add(new GeoPoint(c.getLatitudeE6(), c.getLongitudeE6()));
                }

                routeOverlay.setPoints(points);
                routeOverlay.setVisiblity(true);
                map.getOverlays().add(routeOverlay);
            }
        }

        
        shuttlesOverlay = new ShuttleItemizedOverlay(getResources().getDrawable(R.drawable.shuttle_color), map, world, PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        map.getOverlays().add(shuttlesOverlay);
        
        stopsOverlay = new StopsItemizedOverlay(this, getResources().getDrawable(R.drawable.stop_marker), map, world, PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
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
    		shuttlesOverlay.hide();
    	map.invalidate();
    	
        routesUpdated(dataService.getWorld());
    	dataUpdated(dataService.getWorld(), dataService.getCurrentEtas());
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
	
	/** Calls stopsOverlay.putEtas(). For use with runOnUiThread() */
	private class PutEtas implements Runnable {
		private final ArrayList<EtaJson> etas;
		public PutEtas(ArrayList<EtaJson> etas) {
			this.etas = etas;
		}
		public void run() {
			stopsOverlay.putEtas(etas);
		}
	}

	/** Calls setWorld(). For use with runOnUiThread() */
	private class SetWorld implements Runnable {
		private final World world;
		public SetWorld(World world) {
			this.world = world;
		}
		public void run() {
			setWorld(world);
		}
	}


	public void dataUpdated(World world, ArrayList<EtaJson> etas) {
		if (etas != null && stopsOverlay != null) {
			runOnUiThread(new PutEtas(etas));
		}
		
		if (world != null && shuttlesOverlay != null && timestampOverlay != null) {
        	timestampOverlay.setLastUpdateTime(new Date());
        	timestampOverlay.setStatusText(getResources().getString(R.string.status_ok));
        	
        	runOnUiThread(vehiclesUpdated);
		}
		
		map.postInvalidate();
	}
	
	final Runnable vehiclesUpdated = new Runnable() {
		public void run() {
			shuttlesOverlay.shuttlesUpdated();
            shuttlesOverlay.show();
		}
	};

	public void routesUpdated(World world) {
		if (world != null && !hasRoutes) {
			runOnUiThread(new SetWorld(world));
			map.postInvalidate();
		}
	}

	public void dataServiceError(int errorCode) {
		switch (errorCode) {
		case (IShuttleServiceCallback.NO_CONNECTION_ERROR):
			// Make the shuttle display clear when the connection is lost
			shuttlesOverlay.hide();
			if (timestampOverlay != null)
				timestampOverlay.setStatusText(getResources().getString(R.string.status_no_conn));
			map.postInvalidate();
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
