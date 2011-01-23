package com.abstractedsheep.shuttletracker.shared;

import java.util.HashMap;
/**
 * sample output
latitude 42.7302712352
longitude -73.6765441399
name Student Union
short_name union
id 1
name West Route
id 2
name East Campus

 * @author jonnau
 *
 */
public class Stop {
	private double lon, lat;
	private String name, shortName;
	/**
	 * The routes that this stop is on is stored within another array in the json and the route contains
	 * an integer id number as well as a name (e.g. West Route)
	 */
	private HashMap<Integer, String> routeMap;
	
	public Stop(){
		this.lon = 42.7302712352;
		this.lat = -73.6765441399;
		this.name = "Union";
		this.shortName = "union";
		this.routeMap = new HashMap<Integer, String>();
	}
	
	public Stop(double longitude, double latitude, String fullName, String shortN, HashMap<Integer, String> map){
		this.lon = longitude;
		this.lat = latitude;
		this.name = fullName;
		this.shortName = shortN;
		this.routeMap = map;
	}

	/**
	 * @return the lon
	 */
	public double getLon() {
		return lon;
	}

	/**
	 * @param lon the lon to set
	 */
	public void setLon(double lon) {
		this.lon = lon;
	}

	/**
	 * @return the lat
	 */
	public double getLat() {
		return lat;
	}

	/**
	 * @param lat the lat to set
	 */
	public void setLat(double lat) {
		this.lat = lat;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the shortName
	 */
	public String getShortName() {
		return shortName;
	}

	/**
	 * @param shortName the shortName to set
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	/**
	 * @return the routeMap
	 */
	public HashMap<Integer, String> getRouteMap() {
		return routeMap;
	}

	/**
	 * @param routeMap the routeMap to set
	 */
	public void setRouteMap(HashMap<Integer, String> routeMap) {
		this.routeMap = routeMap;
	}
}
