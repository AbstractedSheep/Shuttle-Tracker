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
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.OverlayItem;
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
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class Tracker extends Activity implements OnItemGestureListener<OverlayItem> {
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
        
        
        List<Placemark> placemarks = parsePlacemarks("http://shuttles.rpi.edu/displays/netlink.kml");
        List<OverlayItem> items = new ArrayList<OverlayItem>();
        OverlayItem oi;
        PathOverlay po;
        
        for (Placemark p : placemarks) {
        	if (p.type == Placemark.LINE_STRING) {
        		po = new PathOverlay(p.style.color, this);
        		for (GeoPoint gp : p.coords) {
        			po.addPoint(gp);
        		}
        		map.getOverlays().add(po);
        	} else if (p.type == Placemark.POINT) {
        		oi = new OverlayItem(p.name, p.description, p.coords.get(0));
        		items.add(oi);
        	}
        }
                
        ItemizedOverlay<OverlayItem> io = new ItemizedOverlay<OverlayItem>(this, items, this);
        map.getOverlays().add(io);
        
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
    			if (vn.toElement(VTDNav.FC, "Style"))
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

	public boolean onItemLongPress(int index, OverlayItem item) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onItemSingleTapUp(int index, OverlayItem item) {
		// TODO Auto-generated method stub
		return false;
	}
        
    

}