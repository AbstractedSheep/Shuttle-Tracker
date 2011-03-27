package com.abstractedsheep.extractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * The purpose of this class is to hold information about a shuttle from
 * the current.js file being read in by JSONExtractor. Each shuttle contains
 * information about its current location, when that shuttle was last updated,
 * its name, current heading, the route it is on and the stops it will visit.
 * This information is then used to determine how long it will take the shuttle
 * to reach each stop on its list, while accommodating enough time for when the shuttle
 * might wait at each stop.
 * @author saiumesh
 * @author wagnea
 */
public class Shuttle {
	private int shuttleId;
	private HashMap<String, Stop> stops;
	private HashMap<String, Integer> stopETA;
	private ArrayList<Integer> speedList;
	private String cardinalPoint;
	private String shuttleName;
	private int speed;
	private Point currentLocation;
	private RouteFinder finder;
	private long lastUpdateTime;
	private int bearing;

	// Jackson requires a constructor with no parameters to be available
	// Also notice 'this.' preceding the variables, this makes it clear that the
	// variable
	// is a global variable and although it is not necessary to use 'this.' if
	// you do not
	// have a local variable by the same name, it is still a good idea to
	// include it
	public Shuttle(ArrayList<Route> rt) {
		this.shuttleId = -1;
		this.stops = new HashMap<String, Stop>();
		this.stopETA = new HashMap<String, Integer>();
		this.shuttleName = "Bus 42";
		this.cardinalPoint = "North";
		this.speed = 0;
		this.currentLocation = new Point();
		this.speedList = new ArrayList<Integer>();
		finder = new RouteFinder(rt);
		this.lastUpdateTime = System.currentTimeMillis();
		this.bearing = 0;
	}

	// This constructor is not required by Jackson, but it makes manually
	// creating a new point a
	// one line operation.
	public Shuttle(int shuttleId, int routeId, ArrayList<Route> rt) {
		// Here, 'this.' is necessary because we have a local variable named the
		// same as the global
		this.shuttleId = shuttleId;
		this.stops = new HashMap<String, Stop>();
		this.stopETA = new HashMap<String, Integer>();
		this.speedList = new ArrayList<Integer>();
		this.shuttleName = "Bus 42";
		this.cardinalPoint = "North";
		this.speed = 0;
		this.currentLocation = new Point();
		finder = new RouteFinder(rt);
		this.lastUpdateTime = System.currentTimeMillis();
		this.bearing = 0;
	}	
	
	/**
	 * updates the current state of the shuttle object.
	 * @param newShuttle
	 */
	public void updateShuttle(Shuttle newShuttle) {
		this.setCurrentLocation(newShuttle.getCurrentLocation(), System.currentTimeMillis());
	}
	
	// Jackson will not work unless all of the variables have accessors and
	// mutators
	// Since these usually only have one line of code in them, put the entire
	// method
	// on a single line to increase readability
	public int getShuttleId() {
		return this.shuttleId;
	}
	public void setShuttleId(int shuttleId) {
		this.shuttleId = shuttleId;
	}
	public int getRouteId() {
		return finder.getRouteID();
	}
	public HashMap<String, Stop> getStops() {
		return stops;
	}
	/**
	 * changes the state of the stop collection if the given route ID matches that for this
	 * shuttle and the shuttle also knows which route it is on.
	 * @param stops - new collection
	 * @param id - route id
	 */
	public void setStops(HashMap<String, Stop> stops, RouteFinder newFinder) {
		if(newFinder.hasFoundRoute()) {
			this.stops = stops;
			this.finder = newFinder;
		}
		this.stopETA.clear();
	}
	/**
	 * @return average speed of shuttle
	 */
	public int getSpeed() {
		return speed;
	}
	/**
	 * given a new speed value, calculate the average speed by this shuttle
	 * and set that value as the speed of the shuttle.
	 * @param newSpd - new instantaneous speed value.
	 */
	public void setSpeed(int newSpd) {
		if(speedList.size() > 10)
			speedList.remove(0);
		speedList.add((newSpd < 10) ? 10 : newSpd);
		int count = 0;
		
		for(int s : speedList) {
			count += s;
		}
		this.speed = count / speedList.size();
	}
	public  Point getCurrentLocation() {
		return currentLocation;
	}
	
	/**
	 * Change the current state of the shuttle's location
	 * and change value of the most recent update time to time.
	 * @param newLocation - new shuttle location
	 * @param time - most recent update time
	 */
	public void setCurrentLocation(Point newLocation, long time) {
		this.currentLocation = newLocation;
		finder.changeCurrentLocation(currentLocation);
		this.lastUpdateTime = time;
	}
	public long getLastUpdateTime() { return this.lastUpdateTime; }
	public String getCardinalPoint() { return cardinalPoint; }
	public void setCardinalPoint(String cardinalPoint) { this.cardinalPoint = cardinalPoint;}
	public String getName() { return shuttleName; }
	public void setName(String newName) { this.shuttleName = newName; }
	public HashMap<String, Integer> getStopETA() { return stopETA; }
	public void setBearing(int newBearing) { finder.setBearing(newBearing); }
	public String getRouteName() { return finder.getRouteName(); }
	public RouteFinder getFinder() {return this.finder; }
	//these two methods get values from the inner RouteFinder class.
	public boolean hasFoundRoute() { return finder.hasFoundRoute(); }
	public boolean isTooFarFromRoute() { return finder.isShuttleOnRoute(); }

	// These next two methods are not required by Jackson
	// They are here to add data to stops
	public void addStop(String stopName, Stop p) {
		if (p.getRouteMap().containsKey(this.getRouteId()))
			stops.put(stopName, p);
	}

	/**
	 * Method determines ETA to the given stop based on the current speed and
	 * the distance to the stop based on the given route information.
	 * 
	 * @param stopName
	 *            - desired stop name
	 * @param routeList
	 *            - contains a list of coordinates for the route
	 * @return time to reach destination or -1 if the stop does not exist on the
	 *         shuttle's route
	 */
	public void getETAToStop() {
		// If only to get the ETA to a particular stop, return the time, but for
		// all general intentions
		// it might be better to save the times in a HashMap as it may make
		// writing to a file easier.
		Point p = null;
		int count = 0;

		for (String name : stops.keySet()) {
			p = stops.get(name).getLocation();
			double distance = finder.getDistanceToStop(stops.get(name));
			int time = (int) ((distance / (double)this.speed) * 3600000) - 1000;
//			System.out.println(this.getName() + " " + (double) ((double)time * (1.667 * Math.pow(10, -5))));
			this.stopETA.put(name, time);
			count++;
		}
		
		this.addTimeDelayToStops();
	}
	
	/**
	 * added a 30 second time delay for each stop (except the first one).
	 */
	private void addTimeDelayToStops() {
		ArrayList<Integer> valueList = new ArrayList<Integer>(stopETA.values());
		Collections.sort(valueList);
		HashMap<String, Integer> tempList = new HashMap<String, Integer>(stopETA);
		
		for(String name : tempList.keySet()) {
			for(int i = 0; i < valueList.size(); i++) {
				if(tempList.get(name) == valueList.get(i)) {
					stopETA.put(name, (int) Math.abs(valueList.get(i) + (1000 * (30 * i)) - 
							(System.currentTimeMillis() - this.lastUpdateTime)));
					break;
				}
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		Shuttle s = (Shuttle) obj;
		return this.shuttleId == s.getShuttleId();

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
		/**
		 * @return latitude of point in degrees
		 */
		public double getLat() {
			return lat;
		}
		/**
		 * @return latitude of point in radians
		 */
		public double getLatInRadians() {
			return Math.toRadians(lat);
		}

		public void setLat(double lat) {
			this.lat = lat;
		}
		/**
		 * @return longitude of point in degrees
		 */
		public double getLon() {
			return lon;
		}
		/**
		 * @return longitude of point in radians
		 */
		public double getLonInRadians() {
			return Math.toRadians(lon);
		}

		public void setLon(double lon) {
			this.lon = lon;
		}

		public String toString() {
			return "(" + this.lat + ", " + this.lon + ")";
		}

		@Override
		public boolean equals(Object obj) {
			Point p = (Point) obj;

			if ((p.getLat() == this.getLat()) && (p.getLon() == this.getLon()))
				return true;
			return false;
		}
	}

	/**
	 * The purpose of this inner class is to calculate the distance and time for
	 * the shuttle to get to the desired stop.
	 * 
	 * @author jonnau
	 * 
	 */
	private class RouteFinder {
		ArrayList<Route> routeList;
		ArrayList<Point> locList;
		// this value is allowable error in degrees (~5-10 feet)
		private boolean foundRoute;
		// this is the route coordinate closest to the shuttle's position.
		private Point closestRouteCoor;
		private double closestDistanceToRoute;
		private int indexOfClosestCoordinate;
		private boolean isBeforeRoutePoint;
		private double currentBearing;

		/**
		 * 
		 * @param r
		 *            - shuttle's route
		 * @param loc
		 *            - current location of shuttle
		 */
		public RouteFinder(Route r, Point loc) {
			this.routeList = new ArrayList<Route>();
			this.routeList.add(r);
			this.locList = new ArrayList<Point>();
			locList.add(loc);
			foundRoute = false;
			closestRouteCoor = new Point();
			indexOfClosestCoordinate = 0;
			this.isBeforeRoutePoint = false;
		}

		public RouteFinder(ArrayList<Route> rt) {
			routeList = new ArrayList<Route>(rt);
			this.locList = new ArrayList<Point>();
			foundRoute = false;
			closestRouteCoor = new Point();
			indexOfClosestCoordinate = 0;
			this.isBeforeRoutePoint = false;
		}

		// TDO: might not be necessary to store the locations, but perhaps
		// necessary to store the speed
		public void changeCurrentLocation(Point pt) {
			if (locList.size() > 1)
				locList.remove(0);
			locList.add(pt);
			determineRouteOfShuttle();
		}
		
		//a shuttle is considered on a route if it is no more than a quarter
		//mile away from the closest route coordinate.
		public boolean isShuttleOnRoute() {
			return (this.closestDistanceToRoute >= .07);
		}
		
		//these getters and setters return values related to the shuttle
		public int getRouteID() { return routeList.get(0).getIdNum();}
		public String getRouteName() { return routeList.get(0).getRouteName();}
		public boolean hasFoundRoute() { return this.foundRoute; }
		public void setBearing(int bearing) {this.currentBearing = bearing; }
		
		/**
		 * This method determines the route that the shuttle is on by going through
		 * all of the coordinate lists in the route list and computing the distance between
		 * the shuttle's location and all of the coordinates composing each route. The shortest
		 * distances is between all of the routes is then compared and the route with the 
		 * shortest distance measurement to the shuttle's current location is presumed to be
		 * the route the shuttle is currently on. It should be noted that the difference between the
		 * distances has to be considerable (greater than .01 miles), else an assumption will be made
		 * with regards to the shuttle's current route until the difference is noticeable. The reason for this
		 * is because the shuttle might travel along an area that houses multiple routes.
		 */
		private void determineRouteOfShuttle() {
			
			// using the given routes, determine which route the
			// shuttle is following
			ArrayList<Shuttle.Point> list = null;
			Point p1 = null, p2 = null;
			double[] distanceArray = { 999, 999 }; // TODO: to make the code
													// more robust, turn it into
													// an arraylist?
			Point[] locationArray = { new Point(), new Point() };
			int[] indexArray = { 0, 0 };
			int index = 0;
			double distance = 0.0;

			for (Route route : routeList) {
				list = route.getCoordinateList();
				for (int i = indexOfClosestCoordinate, count = 0; count < list.size(); i++) {
					if(i > list.size() - 1)
						i = 0;
					p1 = list.get(i);
					distance = calculateDistance(p1);
					//if the new distance value is shorter than the old one, then
					//change it.
					if (distanceArray[index] > distance) {
						Double[] array = route.getBearingsForPoint(i);
						if(((array[0] + 15.0 >= this.currentBearing) &&
							(array[0] - 15.0 <= this.currentBearing)) ||
							((array[1] + 15.0 >= this.currentBearing) &&
									(array[1] - 15.0 <= this.currentBearing))) {
							distanceArray[index] = distance;
							locationArray[index] = p1;
							indexArray[index] = i;
						}
					}
					count++;
				}
				index++;
			}
			
			if(routeList.size() < 2) {
				this.closestRouteCoor = locationArray[0];
				this.indexOfClosestCoordinate = indexArray[0];
				this.closestDistanceToRoute = distanceArray[0];
				return;
			}
			
			//the shuttle's route has only been determined iff the difference
			//between the closest points on the East and West routes is greater
			//than ~32 feet...
			if (Math.abs((distanceArray[0] - distanceArray[1])) >= .01) {
				this.foundRoute = true;
				this.routeList.remove((distanceArray[0] < distanceArray[1]) ? 1 : 0);
			}
			//Since the overlapped region is still part of both routes,
			//the shuttle can still give valid ETAs.
			this.closestRouteCoor = (distanceArray[0] < distanceArray[1]) ?
									locationArray[0] : locationArray[1];
			//this.routeList.remove((distanceArray[0] < distanceArray[1]) ? 1 : 0);
			this.indexOfClosestCoordinate = indexArray[this.getRouteID() - 1];
			this.closestDistanceToRoute = distanceArray[this.getRouteID() - 1];
		}

		/**
		 * calculates distance from stop
		 * 
		 * @param stop - desired stop
		 * @return distance to stop.
		 */
		public double getDistanceToStop(Stop stop) {
			ArrayList<Point> list = null;
			double distance = 0.0, distanceToTravel = 0.0;
			int distanceMultiplier = isBearingbeforeClosestRoutePoint();
			//ideally, routeList should have only one element
			for (Route rt : routeList) {
				list = rt.getCoordinateList();
				int index = indexOfClosestCoordinate + 1;
				int count = 0;
				distanceToTravel = calculateDistance(list.get(index - 1)) * distanceMultiplier;
				for (count = 0; count <= list.size(); count++, index++) {
					if (index >= list.size())
						index = 1;
					//calculate distance between the currently viewed point in the list
					//and the stop's position.
					distance = calculateDistance(list.get(index - 1), stop.getLocation());
					
					if (stop.isClosestRoutePoint(list.get(index - 1), this.getRouteID()))
						return distanceToTravel + distance;
					distanceToTravel += calculateDistance(list.get(index),
							list.get(index - 1));
				}
			}
			return distanceToTravel;
		}
		
		//returns -1 if shuttle is before the closest route point
		//or 1 if is past it.
		private int isBearingbeforeClosestRoutePoint() {
			Double[] array = routeList.get(0).getBearingsForPoint(indexOfClosestCoordinate);
			Double before = (array[0] <= 0) ? 360 - 15 : (array[0] - 15.0);
			Double after = (array[0] >= 360) ? 15 : (array[0] + 15.0);
			//current bearing is equal to the initial bearing
			//to the closest route point +/- 15 degrees
			if(currentBearing <= after && (currentBearing >= before))
				return -1;
			return 1;
		}
		
		/**
		 * calculates the straight line distance between the given stop location and
		 * the shuttle's location The formula used to calculate this distance is the
		 * haversine formula {@link movable-type.co.uk/scripts/latlong.html}
		 * 
		 * @param p - stop's location
		 * @return distance to stop
		 */
		private double calculateDistance(Point p) {
			return calculateDistance(p, getCurrentLocation());
		}

		private double calculateDistance(Point p, Point curr) {
			double earthRadius = 3956;
			
			double dlong = Math.toRadians((curr.lon - p.lon));
		    double dlat = Math.toRadians((curr.lat - p.lat));
		    double a = Math.pow(Math.sin(dlat/2.0), 2) +
		    		   Math.cos(Math.toRadians(p.lat)) * Math.cos(Math.toRadians(curr.lon)) * Math.pow(Math.sin(dlong/2.0), 2);
		    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		    double d = earthRadius * c; 

		    return d;
		}
	}
}