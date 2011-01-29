/*
 * This class is currently set up for demonstration.
 * It is heavily commented for educational purposes.
 */

package com.abstractedsheep.extractor;

import java.util.ArrayList;
import java.util.HashMap;

public class Shuttle {
	private int shuttleId;
	private int routeId;
	private HashMap<String, Shuttle.Point> stops;
	private String cardinalPoint;
	private int speed;
	private Point currentLocation;
	
	// Jackson requires a constructor with no parameters to be available
	// Also notice 'this.' preceding the variables, this makes it clear that the variable
	// is a global variable and although it is not necessary to use 'this.' if you do not
	// have a local variable by the same name, it is still a good idea to include it
	public Shuttle() {
		this.shuttleId = -1;
		this.routeId = -1;
		this.stops = new HashMap<String, Shuttle.Point>();
		this.cardinalPoint = "North";
		this.speed = 0;
		this.currentLocation = new Point();
	}
	
	// This constructor is not required by Jackson, but it makes manually creating a new point a
	// one line operation.
	public Shuttle(int shuttleId, int routeId) {
		// Here, 'this.' is necessary because we have a local variable named the same as the global
		this.shuttleId = shuttleId;
		this.routeId = routeId;
		this.stops = new HashMap<String, Shuttle.Point>();
		this.cardinalPoint = "North";
		this.speed = 0;
		this.currentLocation = new Point();
	}
	
	
	// Jackson will not work unless all of the variables have accessors and mutators
	// Since these usually only have one line of code in them, put the entire method
	// on a single line to increase readability
	public int getShuttleId() { return this.shuttleId; }
	public void setShuttleId(int shuttleId) { this.shuttleId = shuttleId; }
	
	public void setRouteId(int routeId) { this.routeId = routeId; }
	public int getRouteId() { return routeId; }
	
	public HashMap<String, Point> getStops() { return stops; }
	public void setStops(HashMap<String, Point> stops) { this.stops = stops; }
	
	public int getSpeed() { return speed; }
	public void setSpeed(int newSpd) { this.speed = newSpd; }
	
	public Point getCurrentLocation() { return this.currentLocation; }
	public void setCurrentLocation(Point newLocation) { this.currentLocation = newLocation; }
	
	
	public String getCardinalPoint() { return cardinalPoint; }
	public void setCardinalPoint(String cardinalPoint) { this.cardinalPoint = cardinalPoint; }

	// These next two methods are not required by Jackson
	// They are here to add data to stops
	public void addStop(String stopName, Point p) { 
		stops.put(stopName, p);
	}
	
	public void addStop(String stopName, double lat, double lon) {
		addStop(stopName, new Point(lat, lon));
	}
	
	/**
	 * Method determines ETA to the given stop based on the current speed and the
	 * distance to the stop based on the given route information.
	 * @param stopName - desired stop name
	 * @param route - contains a list of coordinates for the route
	 * @return time to reach destination or -1 if shuttle is not on this route
	 */
	public int getETAToStop(String stopName, ArrayList<Route> routeList) {
		return 0;
	}
	
	// If you create a class within a class, make sure it is static
	// This class follows the same rules as above
	public static class Point {
		private double lat;
		private double lon;
			
		public Point() {
			lat = 0;
			lon = 0;
		}
				
		public Point(double lat, double lon) {
			this.lat = lat;
			this.lon = lon;
		}	
				
		public double getLat() { return lat; }	
		public void setLat(double lat) { this.lat = lat; }
		
		public double getLon() { return lon; }
		public void setLon(double lon) { this.lon = lon; }
		
		public String toString() { return "(" + this.lat + ", " + this.lon + ")"; }
	}
}
