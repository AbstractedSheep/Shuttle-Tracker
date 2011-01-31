/*
 * This class is currently set up for demonstration.
 * It is heavily commented for educational purposes.
 */

package com.abstractedsheep.shuttletracker.android;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.MapTileDownloader;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.HandlerBase;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.abstractedsheep.kml.ObjectFactory;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

import de.micromata.opengis.kml.v_2_2_0.Kml;

import android.app.Activity;
import android.graphics.Color;
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
        
        LinearLayout ll = (LinearLayout)findViewById(R.id.mapLayout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        ll.addView(map, lp);     
        
        //PathOverlay poWest = new PathOverlay(Color.argb(180, 255, 127, 39), this);
        //PathOverlay poEast = new PathOverlay(Color.argb(180, 0, 255, 0), this);
        
        List<PathOverlay> routes = parseRoutes("http://shuttles.rpi.edu/displays/netlink.kml", map);

        for (PathOverlay po : routes) {
        	map.getOverlays().add(po);
        }
        
        ScaleBarOverlay sbo = new ScaleBarOverlay(this);
        sbo.setImperial();
        
        map.getOverlays().add(sbo);
    }
    
    private void initMap() {
    	MapTileProviderBasic mtp = new MapTileProviderBasic(this, TileSourceFactory.MAPNIK);
        map = new MapView(this, null, 256, mtp);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setUseDataConnection(true);
        map.getController().setZoom(15);
        map.getController().setCenter(new GeoPoint(42729640, -73681280));
    }
    
    private List<PathOverlay> parseRoutes(String kmlUrl, MapView map) {
    	VTDGen vg = new VTDGen();
    	if (vg.parseHttpUrl(kmlUrl, true)) {
    		VTDNav vn = vg.getNav();
    		
    		if (vn.matchElement("Folder")) {
    			
    		}
    	}
    	
    	

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
    }
        
    

}