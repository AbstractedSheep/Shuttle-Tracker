package com.abstractedsheep.shuttletracker.android;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.abstractedsheep.kml.Placemark;
import com.abstractedsheep.kml.Style;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.LayoutParams;
import com.google.android.maps.OverlayItem;
import com.ximpleware.EOFException;
import com.ximpleware.EncodingException;
import com.ximpleware.EntityException;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.animation.BounceInterpolator;

public class Tracker extends MapActivity {
	public static String MAPS_API_KEY = "01JOmSJBxx1voRKERKRP3C2v-43vBsKl74-b9Og";//"01JOmSJBxx1vR0lM4z_VkVIYfWwZcOgZ6q1VAaQ";
	private MapView map;
	private UpdateShuttlesTask updateTask;
	public static boolean threadLock = false;
	private StopsItemizedOverlay shuttlesOverlay;
	
	private Runnable invalidateMap = new Runnable() {
		public void run() {
			map.invalidate();
		}
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initMap();
        setContentView(map);
        
        // Parse routes and stops
        List<Placemark> placemarks = parsePlacemarks("http://shuttles.rpi.edu/displays/netlink.kml");
        StopsItemizedOverlay stopsOverlay = new StopsItemizedOverlay(getResources().getDrawable(R.drawable.stop_marker), map);
        PathOverlay routesOverlay;
        
        for (Placemark p : placemarks) {
        	if (p.type == Placemark.LINE_STRING) {
        		routesOverlay = new PathOverlay(p.style);
        		for (GeoPoint gp : p.coords) {
        			routesOverlay.addPoint(gp);
        		}
        		map.getOverlays().add(routesOverlay);
        	} else if (p.type == Placemark.POINT) {
        		stopsOverlay.addOverlay(p.toOverlayItem());
        	}
        }
        
        map.getOverlays().add(stopsOverlay);
        
        shuttlesOverlay = new StopsItemizedOverlay(getResources().getDrawable(R.drawable.shuttle_marker), map);
        map.getOverlays().add(shuttlesOverlay);
        
        updateTask = new UpdateShuttlesTask();
        updateTask.execute((Void[])null);
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
    }
    
    /**
     * Parse the shuttle routes out of the KML
     * 
     * @param kmlUrl The HTTP path to the KML route file
     * @return A list of PathOverlays that can be added to a map view
     */
    private List<Placemark> parsePlacemarks(String kmlUrl) {
    	HashMap<String, Style> styles = new HashMap<String, Style>();
    	List<Placemark> placemarks = new ArrayList<Placemark>();
    	Placemark tempPlacemark;
    	Style tempStyle;
    	String id;
    	String temp;
    	byte[] doc = new byte[32000];
    	   	
    	try {
    		// Open a connection to the server
    		URL url = new URL(kmlUrl);
        	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        	InputStream is = conn.getInputStream();
        	
        	// Read the KML file
        	int i = 0;
        	int b = 0;
        	while ((b = is.read()) != -1) {
        		doc[i] = (byte) b;
        		i++;
        	}
        	
        	// Initialize the parser
	    	VTDGen vg = new VTDGen();
	    	vg.setDoc(doc, 0, i);
	    	vg.parse(true);
    		VTDNav vn = vg.getNav();
    		
    		// Quick and dirty parsing code, only parses LineStyles, LineStrings, and Points
    		if (vn.matchElement("Folder")) {
    			// Style parsing
    			if (vn.toElement(VTDNav.FC, "Style")) {
	    			do {
	    				id = vn.toString(vn.getAttrVal("id"));
	    				tempStyle = new Style();
	    				if (vn.toElement(VTDNav.FC, "LineStyle")) {
	    					if (vn.toElement(VTDNav.FC)) {
	    						do {
	    							tempStyle.setAttribute(vn.toString(vn.getCurrentIndex()), vn.toString(vn.getText()));
	    						} while (vn.toElement(VTDNav.NS));
	    						vn.toElement(VTDNav.P);
	    					}
	    					vn.toElement(VTDNav.P);
	    				}
	    				styles.put(id, tempStyle);
	    			} while (vn.toElement(VTDNav.NS, "Style"));
	    			vn.toElement(VTDNav.P);
    			}
    			
    			// Placemark parsing
    			if (vn.toElement(VTDNav.FC, "Placemark")) {
        			do {
        				tempPlacemark = new Placemark();
        				tempPlacemark.id = vn.toString(vn.getAttrVal("id"));

    					if (vn.toElement(VTDNav.FC)) {
    						do {
    							temp = vn.toString(vn.getCurrentIndex());
    							if ((temp.equalsIgnoreCase("styleUrl"))) {
    								tempPlacemark.style = styles.get(vn.toString(vn.getText()).substring(1));
    							} else if (temp.equalsIgnoreCase("LineString") || temp.equalsIgnoreCase("Point")) {
    								tempPlacemark.setAttribute("type", temp);
    								if (vn.toElement(VTDNav.FC, "coordinates")) {
    									tempPlacemark.parseCoordinates(vn.toString(vn.getText()));
    									vn.toElement(VTDNav.P);
    								}
    							} else {
    								tempPlacemark.setAttribute(temp, vn.toString(vn.getText()));
    							}
    						} while (vn.toElement(VTDNav.NS));
    						vn.toElement(VTDNav.P);
    					}
    					placemarks.add(tempPlacemark);
        			} while (vn.toElement(VTDNav.NS, "Placemark"));
        			vn.toElement(VTDNav.P);
    			}
    		}
    		
    	} catch (NavException e) {
    		e.printStackTrace();
    	} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (EncodingException e) {
			e.printStackTrace();
		} catch (EOFException e) {
			e.printStackTrace();
		} catch (EntityException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
    	return placemarks;
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	private class UpdateShuttlesTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			while (true) {
				updateShuttles();
				try {
					Thread.sleep(1000);
					while (threadLock) {
						Thread.sleep(100);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private void updateShuttles() {
		threadLock = true;
        List<Placemark> placemarks = parsePlacemarks("http://shuttles.rpi.edu/vehicles/current.kml");
        
        shuttlesOverlay.removeAllOverlays();
        
        for (Placemark p : placemarks) {
    		shuttlesOverlay.addOverlay(p.toOverlayItem());
        }
        
                
        runOnUiThread(invalidateMap);
        threadLock = false;
	}
}