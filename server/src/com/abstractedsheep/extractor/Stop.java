package com.abstractedsheep.extractor;

import java.util.ArrayList;
import java.util.HashMap;

import com.abstractedsheep.extractor.Shuttle.Point;

/**
 * sample output latitude 42.7302712352 longitude -73.6765441399 name Student
 * Union short_name union id 1 name West Route id 2 name East Campus
 * 
 * @author jonnau
 * 
 */
public class Stop {
	private double lon, lat;
	private String name, shortName;
	private RouteFinder finder;
	/**
	 * The routes that this stop is on is stored within another array in the
	 * json and the route contains an integer id number as well as a name (e.g.
	 * West Route)
	 */
	private HashMap<Integer, String> routeMap;

	public Stop() {
		this.lon = 42.7302712352;
		this.lat = -73.6765441399;
		this.name = "Union";
		this.shortName = "union";
		this.routeMap = new HashMap<Integer, String>();
		finder = new RouteFinder();
	}

	public Stop(double longitude, double latitude, String fullName,
			String shortN, HashMap<Integer, String> map, ArrayList<Route> routeList) {
		this.lon = longitude;
		this.lat = latitude;
		this.name = fullName;
		this.shortName = shortN;
		this.routeMap = map;
		finder = new RouteFinder(routeList);
	}

	/**
	 * @return the lon
	 */
	public double getLon() {
		return lon;
	}

	/**
	 * @param lon
	 *            the lon to set
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

	public Shuttle.Point getLocation() {
		return new Shuttle.Point(lat, lon);
	}

	/**
	 * @param lat
	 *            the lat to set
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
	
	public void addRoutesToFinder(ArrayList<Route> routeList) {
		for(Route route : routeList) {
			if(routeMap.containsKey(route.getIdNum()))
					finder.addRoute(route);
		}
		
		finder.determineClosestRoutePointOfStop();
	}
	
	public boolean isClosestRoutePoint(Shuttle.Point coordinate, int routeID) {
		return finder.isSamePosition(coordinate, routeID);
	}
	
	//TODO: this is basically the same class as in Shuttle.java, so
	//		move this to a separate class for sake of it.
	private class RouteFinder {
		ArrayList<Route> routeList;
		ArrayList<Shuttle.Point> locList;
		
		public RouteFinder() {
			this.routeList = new ArrayList<Route>();
			this.locList = new ArrayList<Shuttle.Point>();
		}

		public RouteFinder(ArrayList<Route> rt) {
			this.routeList = new ArrayList<Route>(rt);
			this.locList = new ArrayList<Shuttle.Point>();
		}
		
		public void addRoute(Route route) {
			if(!routeList.contains(route))
				this.routeList.add(route);
		}
		
		//a shuttle is considered on a route if it is no more than a quarter
		//mile away from the closest route coordinate.
		public boolean isSamePosition(Shuttle.Point coordinate, int routeID) {
			int index = 0;
			if(locList.size() > 1)
				index = routeID - 1;
			Shuttle.Point pt = locList.get(index);
			
			if(coordinate.getLat() == pt.getLat() &&
					coordinate.getLon() == pt.getLon())
				return true;
			return false;
		}

		private void determineClosestRoutePointOfStop() {
			
			// using the given routes, determine which route the
			// shuttle is following
			ArrayList<Shuttle.Point> list = null;
			Point p1 = null, p2 = null;
			double[] distanceArray = { 999, 999 }; // TODO: to make the code
													// more robust, turn it into
													// an arraylist?
			Shuttle.Point[] locationArray = { new Shuttle.Point(), new Shuttle.Point() };
			int[] indexArray = { 0, 0 };
			int index = 0;
			double distance = 0.0;

			for (Route route : routeList) {
				list = route.getCoordinateList();
				for (int i = 0; i < list.size(); i++) {
					if(i > list.size() - 1)
						i = 0;
					p1 = list.get(i);
					distance = calculateDistance(p1);

					if (distanceArray[index] > distance) {
						distanceArray[index] = distance;
						locationArray[index] = p1;
						indexArray[index] = i;
					}
				}
				index++;
			}
			
			for(int i = 0; i < index; i++)
				this.locList.add(locationArray[i]);
		}
		
		// TODO: delete the first calculateDistance method and move the second one
		// to RouteFinder
		/**
		 * calculates the straight line distance between the given stop location and
		 * the shuttle's location The formula used to calculate this distance is the
		 * haversine formula {@link http
		 * ://www.movable-type.co.uk/scripts/latlong.html}
		 * 
		 * @param p
		 *            - stop's location
		 * @return distance to stop
		 */
		private double calculateDistance(Point p) {
			return calculateDistance(p, getLocation());
		}

		private double calculateDistance(Shuttle.Point p, Shuttle.Point curr) {
			double earthRadius = 3956;
			
			double dlong = Math.toRadians((curr.getLon() - p.getLon()));
		    double dlat = Math.toRadians((curr.getLat() - p.getLat()));
		    double a = Math.pow(Math.sin(dlat/2.0), 2) +
		    		   Math.cos(Math.toRadians(p.getLat())) * Math.cos(Math.toRadians(curr.getLon())) * Math.pow(Math.sin(dlong/2.0), 2);
		    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		    double d = earthRadius * c; 

		    return d;
		}
	}
}
