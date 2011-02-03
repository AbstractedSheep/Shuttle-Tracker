package com.abstractedsheep.kml;

import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class Placemark {
	public static int POINT = 1;
	public static int LINE_STRING = 2;
	
	public String id;
	public String name;
	public String description;
	public Style style;
	public List<GeoPoint> coords;
	public int type;
	
	public Placemark() {
		coords = new ArrayList<GeoPoint>();
	}
	
	/**
	 * Sets field based on KML tag name
	 * @param name The name of the KML tag
	 * @param value The value inside the KML tag
	 */
	public void setAttribute(String name, String value) {
		if (name.equalsIgnoreCase("name")) {
			this.name = value;
		} else if (name.equalsIgnoreCase("description")) {
			this.description = value;
		} else if (name.equalsIgnoreCase("type")) {
			if (value.equalsIgnoreCase("LineString")) {
				this.type = LINE_STRING;
			} else if (value.equalsIgnoreCase("Point")) {
				this.type = POINT;
			}
		}
	}
	
	/**
	 * Parses the string of coordinates from a Point or LineString into the coords list
	 * @param coordinates The string of cooordinates from the <coordinate> tag
	 */
	public void parseCoordinates(String coordinates) {

		String[] lines = coordinates.split("\n");
		for (String line : lines) {
			if (line.contains(",")) {
				String[] splitCoords = line.split(",");		
				this.coords.add(new GeoPoint((int)(Double.parseDouble(splitCoords[1]) * 1e6), (int)(Double.parseDouble(splitCoords[0]) * 1e6)));
			}
		}
	}
	
	public OverlayItem toOverlayItem() {
		OverlayItem oi;
		if (this.coords.size() > 0) {
			oi = new OverlayItem(this.coords.get(0), this.name, this.description);
		} else {
			oi = new OverlayItem(new GeoPoint(0, 0), this.name, this.description);
		}
		
		return oi;
	}
}
