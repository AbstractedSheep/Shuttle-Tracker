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


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.ObjectMapper;

import com.abstractedsheep.kml.Placemark;
import com.abstractedsheep.kml.Style;
import com.abstractedsheep.shuttletracker.json.VehicleArray;
import com.abstractedsheep.shuttletracker.json.VehicleJson;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
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
import android.util.Log;
import android.view.animation.BounceInterpolator;

public class Tracker extends MapActivity {
	public static String MAPS_API_KEY = "01JOmSJBxx1vR0lM4z_VkVIYfWwZcOgZ6q1VAaQ"; //"01JOmSJBxx1voRKERKRP3C2v-43vBsKl74-b9Og"; "01JOmSJBxx1vR0lM4z_VkVIYfWwZcOgZ6q1VAaQ";
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
        
        LocationOverlay mlo = new LocationOverlay(this, map, R.drawable.shuttle_marker);
        mlo.enableMyLocation();
        mlo.enableCompass();
        
        map.getOverlays().add(mlo);
        
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
    
    private ArrayList<VehicleJson> parseShuttleJson(InputStream is) {
    	ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
    	VehicleArray vehicles = null;
	
		try {
			// ObjectMapper doesn't like the stream, but it works if converted into a string first
			String json = convertStreamToString(is);
			vehicles = mapper.readValue(json, VehicleArray.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   
    	return vehicles;
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
					Thread.sleep(2500);
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
		
		try {
			URL shuttlesJson;
			shuttlesJson = new URL("http://shuttles.rpi.edu/vehicles/current.js");
			URLConnection shuttleJsonConnection = shuttlesJson.openConnection();

			ArrayList<VehicleJson> vehicles = parseShuttleJson(shuttleJsonConnection.getInputStream());
			if (vehicles != null) {
				shuttlesOverlay.removeAllOverlays();
	        
	        	for (VehicleJson v : vehicles) {
	        		shuttlesOverlay.addOverlay(v.toOverlayItem());
	        	}
	        	                
	        	runOnUiThread(invalidateMap);
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        
        
        threadLock = false;
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
