package com.abstractedsheep.kml;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.GeoPoint;

public class Placemark {
	public String name;
	public String description;
	public Style style;
	public List<GeoPoint> coords;
}
