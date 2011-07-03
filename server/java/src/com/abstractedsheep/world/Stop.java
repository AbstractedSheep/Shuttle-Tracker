package com.abstractedsheep.world;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is designed to hold information about a stop from netlink.js.
 * Each stop object will have a location, a name and an instance of the RouteFinder
 * class. This inner class is similar to the one used in the Shuttle class, and helps
 * the Shuttle class determine the distance to each stop.
 * 
 * @author saiumesh
 * 
 */
public class Stop implements IRouteFinder{
	private Coordinate location;
	private String name, shortName;
	/**
	 * The routes that this stop is on is stored within another array in the
	 * json and the route contains an integer id number as well as a name (e.g.
	 * West Route)
	 */
	private HashMap<Integer, String> routeMap;
	private HashMap<Integer, Coordinate> snappedCoordinate;
	private HashMap<Integer, Double> precedingCoordinateDistance;
	private HashMap<Integer, Integer> precedingCoordinate;

	public Stop() {
		this.location = new Coordinate(-73.6765441399, 42.7302712352);
		this.name = "Union";
		this.shortName = "union";
		this.routeMap = new HashMap<Integer, String>();
	}

	public Stop(double longitude, double latitude, String fullName,
			String shortN, HashMap<Integer, String> map, ArrayList<Route> routeList) {
		this.location = new Coordinate(latitude, longitude);
		this.name = fullName;
		this.shortName = shortN;
		this.routeMap = map;
	}

	/**
	 * @return the lon
	 */
	public double getLongitude() {
		return location.getLongitude();
	}

	/**
	 * @param lon
	 *            the lon to set
	 */
	public void setLongitude(double lon) {
		this.location.setLongitude(lon);
	}

	/**
	 * @return the lat
	 */
	public double getLatitude() {
		return this.location.getLatitude();
	}

	public Coordinate getLocation() {
		return this.location;
	}

	/**
	 * @param lat
	 *            the lat to set
	 */
	public void setLatitude(double lat) {
		this.location.setLatitude(lat);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
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
	 * @param shortName
	 *            the shortName to set
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
	 * @param routeMap
	 *            the routeMap to set
	 */
	public void setRouteMap(HashMap<Integer, String> routeMap) {
		this.routeMap = routeMap;
	}

	@Override
	public void snapToRoute(Route r) {
		Coordinate c1, c2;
		Coordinate closestPoint = null, tempClosestPoint = null;
		int precedingPointId = -1;
		double shortestDistance = 10000, tempShortestDistance = 10000;
		int size = r.getCoordinateList().size();
		for (int i = 0; i < r.getCoordinateList().size(); i++)
		{
			if (i == 0)
				c1 = r.getCoordinateList().get(size - 1);
			else
				c1 = r.getCoordinateList().get(i - 1);
	
			c2 = r.getCoordinateList().get(size);
			
			tempClosestPoint = location.closestPoint(c1, c2);
			tempShortestDistance = tempClosestPoint.distanceFromCoordiante(location);
	
			if (tempShortestDistance < shortestDistance) {
				shortestDistance = tempShortestDistance;
				closestPoint = tempClosestPoint;
				precedingPointId = (i == 0) ? (size - 1) : i;
			}
		}
		this.snappedCoordinate.put(r.getIdNum(), closestPoint);
		this.precedingCoordinate.put(r.getIdNum(), precedingPointId);
		this.precedingCoordinateDistance.put(r.getIdNum(), r.getCoordinateList().get(precedingPointId).distanceFromCoordiante(location));
	}
}
