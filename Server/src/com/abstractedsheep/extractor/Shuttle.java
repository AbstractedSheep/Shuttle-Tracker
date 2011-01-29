/*
 * This class is currently set up for demonstration.
 * It is heavily commented for educational purposes.
 */

package com.abstractedsheep.extractor;

import java.util.ArrayList;

public class Shuttle {
	private int shuttleId;
	private int routeId;
	private ArrayList<Point> stops;
	
	// Jackson requires a constructor with no parameters to be available
	// Also notice 'this.' preceding the variables, this makes it clear that the variable
	// is a global variable and although it is not necessary to use 'this.' if you do not
	// have a local variable by the same name, it is still a good idea to include it
	public Shuttle() {
		this.shuttleId = -1;
		this.routeId = -1;
		this.stops = new ArrayList<Shuttle.Point>();
	}
	
	// This constructor is not required by Jackson, but it makes manually creating a new point a
	// one line operation.
	public Shuttle(int shuttleId, int routeId) {
		// Here, 'this.' is necessary because we have a local variable named the same as the global
		this.shuttleId = shuttleId;
		this.routeId = routeId;
		this.stops = new ArrayList<Shuttle.Point>();
	}
	
	
	// Jackson will not work unless all of the variables have accessors and mutators
	// Since these usually only have one line of code in them, put the entire method
	// on a single line to increase readability
	public int getShuttleId() { return this.shuttleId; }
	public void setShuttleId(int shuttleId) { this.shuttleId = shuttleId; }
	
	public void setRouteId(int routeId) { this.routeId = routeId; }
	public int getRouteId() { return routeId; }
	
	public ArrayList<Point> getStops() { return stops; }
	public void setStops(ArrayList<Point> stops) { this.stops = stops; }
	
	
	// These next two methods are not required by Jackson
	// They are here to add data to stops
	public void addStop(Point p) { 
		stops.add(p);
	}
	
	public void addStop(double lat, double lon) {
		addStop(new Point(lat, lon));
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
