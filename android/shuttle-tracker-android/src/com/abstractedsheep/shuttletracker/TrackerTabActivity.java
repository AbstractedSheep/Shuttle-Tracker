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

import java.util.ArrayList;
import java.util.Date;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import com.abstractedsheep.shuttletracker.json.EtaJson;
import com.abstractedsheep.shuttletracker.json.ExtraEtaJson;
import com.abstractedsheep.shuttletracker.json.Style;
import com.abstractedsheep.shuttletracker.mapoverlay.*;
import com.abstractedsheep.shuttletrackerworld.Coordinate;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;
import com.abstractedsheep.shuttletrackerworld.Route;
import com.abstractedsheep.shuttletrackerworld.Shuttle;
import com.abstractedsheep.shuttletrackerworld.World;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class TrackerTabActivity extends MapActivity implements IShuttleServiceCallback, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int DEFAULT_LAT = 42729640;
	private static final int DEFAULT_LON = -73681280;
	private static final int DEFAULT_ZOOM = 15;
    private final static int MENU_REMOVE_FAV = 1;
	private final static int MENU_ADD_FAV = 2;
    private final static String MAP_TAB = "map";
    private final static String ETA_TAB = "eta";

	private ArrayList<EtaJson> etas;
	private TabHost tabHost;
    private MapView map;
    private ShuttleItemizedOverlay shuttlesOverlay;
	private LocationOverlay myLocationOverlay;
	private StopsItemizedOverlay stopsOverlay;
	private ShuttleDataService dataService;
	private boolean hasRoutes;
	private SharedPreferences prefs;
	private TimestampOverlay timestampOverlay;
	private ExpandableListView etaListView;
	private EtaListAdapter etaAdapter;
    private Dialog splashDialog;
    private Handler handler;
	
	@Override
	protected void onPause() {
		super.onPause();

        myLocationOverlay.disableMyLocation();
        dataService.stopAllUpdates();
		dataService.unregisterCallback(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		dataService.registerCallback(this);
		dataService.startShuttleUpdates();

        handler.post(onResumeTasks);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		getWindow().setFormat(PixelFormat.RGBA_8888); 
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

		setContentView(R.layout.tab);

        // Initialize the tab host
		this.tabHost = (TabHost) findViewById(android.R.id.tabhost);
		this.tabHost.setup();

		// Add the ETA activity as a tab
		TabSpec tab = this.tabHost.newTabSpec(ETA_TAB);
		tab.setContent(R.id.eta_list);
		tab.setIndicator("ETA", getResources().getDrawable(R.drawable.clock));
		this.tabHost.addTab(tab);
		
		// Add the map activity as a tab
		tab = this.tabHost.newTabSpec(MAP_TAB);
		tab.setContent(initMap());
		tab.setIndicator("Map", getResources().getDrawable(R.drawable.map));
		this.tabHost.addTab(tab);
		
		if (savedInstanceState != null)
			this.tabHost.setCurrentTab(savedInstanceState.getInt("open_tab", 0));

        etaListView = (ExpandableListView) findViewById(R.id.eta_list);
		etaAdapter = new EtaListAdapter((TrackerTabActivity)this.getParent(), etaListView, this, getLayoutInflater(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
		etaListView.setAdapter(etaAdapter);
		etaListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
					ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
					int type = ExpandableListView.getPackedPositionType(info.packedPosition);
					int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);

					//Only create a context menu for child items
					if (type == 1) {
						if (group == 0 && etaAdapter.favoritesVisible())
							menu.add(0, MENU_REMOVE_FAV, 0, getString(R.string.remove_favorite));
						else
							menu.add(0, MENU_ADD_FAV, 0, getString(R.string.add_favorite));

					}
			}
		});
		etaListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				if (etaAdapter.expandChild(groupPosition, childPosition)) {
					dataService.setExtraEtaToGet(etaAdapter.getStopId(groupPosition, childPosition), etaAdapter.getRouteId(groupPosition, childPosition));
				} else {
					dataService.setExtraEtaToGet(null, -1);
				}
				return true;
			}
		});

        dataService = ShuttleDataService.getInstance();
        dataService.setApplicationContext(this.getApplicationContext());

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.registerOnSharedPreferenceChangeListener(this);

        handler = new Handler();

        MyStateSaver data = (MyStateSaver) getLastNonConfigurationInstance();
        if (data != null) {
            // Show splash screen if still loading
            if (data.showSplashScreen) {
                showSplashScreen();
            }
        } else {
            showSplashScreen();
            dataService.startRouteUpdate();
        }
	}

    private final Runnable onResumeTasks = new Runnable() {
        @Override
        public void run() {
            if (prefs.getBoolean(TrackerPreferences.MY_LOCATION, true))
                myLocationOverlay.enableMyLocation();

            if (shuttlesOverlay != null)
                shuttlesOverlay.hide();
            map.invalidate();

            routesUpdated(dataService.getWorld());
            dataUpdated(dataService.getWorld(), dataService.getCurrentEtas());

            etaAdapter.loadFavorites();
        }
    };

    private TabHost.TabContentFactory mapTabFactory = new TabHost.TabContentFactory() {
        @Override
        public View createTabContent(String s) {
            map.setTag(s);
            return map;
        }
    };
    private TabHost.TabContentFactory initMap() {
        if (map == null) {
            // API must be generated and added to a new class called MapsApiKey
            // MAPS_API_KEY must be a string constant in that class
            map = new MapView(this, MapsApiKey.MAPS_API_KEY);
            map.getController().setZoom(DEFAULT_ZOOM);
            map.getController().setCenter(new GeoPoint(DEFAULT_LAT, DEFAULT_LON));
            map.setClickable(true);
            map.setFocusable(true);
            map.setBuiltInZoomControls(true);
            map.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

            map.getOverlays().add(new NullOverlay());

            myLocationOverlay = new LocationOverlay(this, map, R.drawable.glyphish_location_arrow);
            map.getOverlays().add(myLocationOverlay);
        }
        return mapTabFactory;
    }
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("open_tab", this.tabHost.getCurrentTab());
		
		super.onSaveInstanceState(outState);
	}
	
	final Runnable hideIndeterminateProgress = new Runnable() {
		public void run() {
			setProgressBarIndeterminateVisibility(false);
		}
	};

	private class MakeToast implements Runnable {
		private final int errorCode;
		public MakeToast(int errorCode) {
			this.errorCode = errorCode;
		}
		public void run() {
			switch (errorCode) {
			case IShuttleServiceCallback.NO_CONNECTION_ERROR:
				Toast.makeText(TrackerTabActivity.this, R.string.no_conn, 10000).show();
				break;
			}
		}
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

        etaAdapter.setRoutes(world);

        if (etaAdapter.favoritesVisible())
            etaListView.expandGroup(0);
    }

	@Override
	protected boolean isRouteDisplayed() {
		return hasRoutes;
	}

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (prefs.getBoolean(TrackerPreferences.MY_LOCATION, true))
            myLocationOverlay.enableMyLocation();
        else
            myLocationOverlay.disableMyLocation();
        if (timestampOverlay != null)
            timestampOverlay.set24Hour(prefs.getBoolean(TrackerPreferences.USE_24_HOUR, false));
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
            if (world != null)
			    setWorld(world);
		}
	}


	public void dataUpdated(World world, ArrayList<EtaJson> etas) {

        if (etas != null) {
            this.etas = etas;
            runOnUiThread(updateList);
        }

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
        if (world != null) {
			runOnUiThread(hideIndeterminateProgress);
            removeSplashScreen();
        }

		if (world != null && !hasRoutes) {
            runOnUiThread(new SetWorld(world));
			map.postInvalidate();
		}
	}

    @Override
    public void extraEtasUpdated(ExtraEtaJson etas) {
        etaAdapter.setExtraEtas(etas);
    }

    public void dataServiceError(int errorCode) {
        runOnUiThread(new MakeToast(errorCode));
		switch (errorCode) {
		case (IShuttleServiceCallback.NO_CONNECTION_ERROR):
			// Make the shuttle display clear when the connection is lost
            if (shuttlesOverlay != null)
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
        if (tabHost.getCurrentTabTag().equals(MAP_TAB))
		    getMenuInflater().inflate(R.menu.map_options, menu);
        else
            getMenuInflater().inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        if (tabHost.getCurrentTabTag().equals(MAP_TAB)) {
            switch (item.getItemId()) {
            case R.id.options:
                startActivity(new Intent(this, TrackerPreferences.class));
                break;
            case R.id.center_map:
                if (myLocationOverlay.isMyLocationEnabled())
                    map.getController().animateTo(myLocationOverlay.getMyLocation());
                else
                    map.getController().animateTo(new GeoPoint(DEFAULT_LAT, DEFAULT_LON));
                map.getController().setZoom(DEFAULT_ZOOM);
                break;
            }
        } else {
            switch (item.getItemId()) {
            case R.id.options:
                startActivity(new Intent(this, TrackerPreferences.class));
                break;
            }
        }

        return super.onOptionsItemSelected(item);
	}
	
	public void showMap(String stopId) {
		tabHost.setCurrentTab(1);
		stopsOverlay.displayStop(stopId);
	}

    public boolean onContextItemSelected(MenuItem menuItem) {
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuItem.getMenuInfo();
		int groupPos = -1;
		int childPos = -1;
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
		}

		switch (menuItem.getItemId()) {
		case MENU_REMOVE_FAV:
			etaAdapter.removeFavorite(childPos);
			return true;
		case MENU_ADD_FAV:
			etaAdapter.addFavorite(groupPos, childPos);
			return true;
		default:
			return super.onContextItemSelected(menuItem);
		}
	}

    private final Runnable updateList = new Runnable() {
		public void run() {
            if (etas != null)
			    etaAdapter.putEtas(etas);
		}
	};

    @Override
    public Object onRetainNonConfigurationInstance() {
        MyStateSaver data = new MyStateSaver();
        // Save your important data here

        if (splashDialog != null) {
            data.showSplashScreen = true;
            removeSplashScreen();
        }
        return data;
    }

    /**
     * Removes the Dialog that displays the splash screen
     */
    protected void removeSplashScreen() {
        if (splashDialog != null) {
            splashDialog.dismiss();
            splashDialog = null;
        }
    }

    /**
     * Shows the splash screen over the full Activity
     */
    protected void showSplashScreen() {
        splashDialog = new Dialog(this);
        splashDialog.setContentView(R.layout.splash);
        splashDialog.setCancelable(false);
        splashDialog.show();
    }

    /**
     * Simple class for storing important data across config changes
     */
    private class MyStateSaver {
        public boolean showSplashScreen = false;
        // Your other important fields here
    }
}
