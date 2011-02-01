package com.abstractedsheep.shuttletracker.android;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import com.abstractedsheep.kml.Placemark;
import com.abstractedsheep.kml.Style;
import com.ximpleware.EOFException;
import com.ximpleware.EncodingException;
import com.ximpleware.EntityException;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class Tracker extends Activity {
	private MapView map;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
         
        initMap();
        
        // Add the map to the layout
        LinearLayout ll = (LinearLayout)findViewById(R.id.mapLayout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        ll.addView(map, lp);     
        
        //PathOverlay poWest = new PathOverlay(Color.argb(180, 255, 127, 39), this);
        //PathOverlay poEast = new PathOverlay(Color.argb(180, 0, 255, 0), this);
        
        List<PathOverlay> routes = parseRoutes("http://shuttles.rpi.edu/displays/netlink.kml");

        for (PathOverlay po : routes) {
        	map.getOverlays().add(po);
        }
        
        ScaleBarOverlay sbo = new ScaleBarOverlay(this);
        sbo.setImperial();
        
        map.getOverlays().add(sbo);
    }
    
    /**
     * Set up the map view with the default configuration
     */
    private void initMap() {
    	MapTileProviderBasic mtp = new MapTileProviderBasic(this, TileSourceFactory.MAPNIK);
        map = new MapView(this, null, 256, mtp);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setUseDataConnection(true);
        map.getController().setZoom(15);
        map.getController().setCenter(new GeoPoint(42729640, -73681280));
    }
    
    /**
     * Parse the shuttle routes out of the KML
     * 
     * @param kmlUrl The HTTP path to the KML route file
     * @return A list of PathOverlays that can be added to a map view
     */
    private List<PathOverlay> parseRoutes(String kmlUrl) {
    	List<PathOverlay> routes = new ArrayList<PathOverlay>();
    	HashMap<String, Style> styles = new HashMap<String, Style>();
    	List<Placemark> placemarks = new ArrayList<Placemark>();
    	Placemark tempPlacemark;
    	Style tempStyle;
    	String id;
    	byte[] doc = new byte[32000];
    	   	
    	try {
    		
    		URL url = new URL(kmlUrl);
        	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        	InputStream is = conn.getInputStream();
        	
        	int i = 0;
        	int b = 0;
        	while ((b = is.read()) != -1) {
        		doc[i] = (byte) b;
        		i++;
        	}
        	
        	FileOutputStream fos = new FileOutputStream("/sdcard/kml");
        	for (int j = 0; j <= i; j++) {
        		fos.write((int) doc[j]);
        	}
        	
        	fos.close();
        	
	    	VTDGen vg = new VTDGen();
	    	vg.setDoc(doc, 0, i);
	    	vg.parse(true);
    		VTDNav vn = vg.getNav();
    		
    		if (vn.matchElement("Folder")) {
    			if (vn.toElement(VTDNav.FC, "Style"))
    			do {
    				id = vn.toString(vn.getAttrVal("id"));
    				tempStyle = new Style();
    				if (vn.toElement(VTDNav.FC, "LineStyle")) {
    					if (vn.toElement(VTDNav.FC)) {
    						do {
    							tempStyle.setAttribute(vn.toString(vn.getCurrentIndex()), vn.toString(vn.getText()));
    						} while (vn.toElement(VTDNav.NS));
    					}
    				}
    				styles.put(id, tempStyle);
    			} while (vn.toElement(VTDNav.NS, "Style"));
    		}
    		
    	} catch (NavException e) {
    		e.printStackTrace();
    	} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EOFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return routes;

		/*
		NodeList nl = ele.getElementsByTagName("coordinates");
		String s = nl.item(0).getChildNodes().item(0).getNodeValue();
		
		String[] lines = s.split("\n");
		for (String line : lines) {
			if (line.contains(",")) {
				String[] coords = line.split(",");
				poWest.addPoint(new GeoPoint(Double.parseDouble(coords[1]), Double.parseDouble(coords[0])));
			}
		}
		
		s = nl.item(1).getChildNodes().item(0).getNodeValue();
		
		lines = s.split("\n");
		for (String line : lines) {
			if (line.contains(",")) {
				String[] coords = line.split(",");
				poEast.addPoint(new GeoPoint(Double.parseDouble(coords[1]), Double.parseDouble(coords[0])));
			}
		}
		*/
    }
        
    

}