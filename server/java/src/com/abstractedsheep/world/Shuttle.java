package com.abstractedsheep.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
public class Shuttle implements IRouteFinder{
	private int shuttleId;
	private HashMap<String, Stop> stops;
	private HashMap<String, ArrayList<Integer>> stopETA;
	private ArrayList<Integer> speedList;
	private String cardinalPoint;
	private String shuttleName;
	private int speed;
	private Coordinate currentLocation;
	private long lastUpdateTime;
	private int bearing;
	private Coordinate SnappedCoordinate;
	private int NextRouteCoordinate;
	private Route currentRoute;

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
		this.stopETA = new HashMap<String, ArrayList<Integer>>();
		this.shuttleName = "Bus 42";
		this.cardinalPoint = "North";
		this.speed = 0;
		this.currentLocation = new Coordinate();
		this.speedList = new ArrayList<Integer>();
		this.lastUpdateTime = System.currentTimeMillis();
		this.bearing = 0;
		this.currentRoute = new Route();
	}

	// This constructor is not required by Jackson, but it makes manually
	// creating a new point a
	// one line operation.
	public Shuttle(int shuttleId, int routeId, Route rt) {
		// Here, 'this.' is necessary because we have a local variable named the
		// same as the global
		this.shuttleId = shuttleId;
		this.stops = new HashMap<String, Stop>();
		this.stopETA = new HashMap<String, ArrayList<Integer>>();
		this.speedList = new ArrayList<Integer>();
		this.shuttleName = "Bus 42";
		this.cardinalPoint = "North";
		this.speed = 0;
		this.currentLocation = new Coordinate();
		this.lastUpdateTime = System.currentTimeMillis();
		this.bearing = 0;
		this.currentRoute = new Route();
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
		return this.currentRoute.getIdNum();
	}
	public HashMap<String, Stop> getStops() {
		return stops;
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
	public  Coordinate getCurrentLocation() {
		return currentLocation;
	}
	
	/**
	 * Change the current state of the shuttle's location
	 * and change value of the most recent update time to time.
	 * @param newLocation - new shuttle location
	 * @param time - most recent update time
	 */
	public void setCurrentLocation(Coordinate newLocation, long time) {
		if(!this.currentLocation.equals(newLocation))
			this.lastUpdateTime = time;
		this.currentLocation = newLocation;
		
	}
	public long getLastUpdateTime() { return this.lastUpdateTime; }
	public String getCardinalPoint() { return cardinalPoint; }
	public void setCardinalPoint(String cardinalPoint) { this.cardinalPoint = cardinalPoint;}
	public String getName() { return shuttleName; }
	public void setName(String newName) { this.shuttleName = newName; }
	public HashMap<String, ArrayList<Integer>> getStopETA() { return stopETA; }

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
		
		this.addTimeDelayToStops();
	}
	
	/**
	 * added a 30 second time delay for each stop (except the first one).
	 */
	private void addTimeDelayToStops() {
		ArrayList<ArrayList<Integer>> valueList = new ArrayList<ArrayList<Integer>>(stopETA.values());
		ArrayList<Integer> indexedValueList = null;
		int index = 0;
		
		HashMap<String, ArrayList<Integer>> tempList = new HashMap<String, ArrayList<Integer>>(stopETA);
		
		for(index = 0; index < valueList.size(); index++) {
			indexedValueList = returnValueList(index, valueList);
			Collections.sort(indexedValueList);
			
			for(String name : tempList.keySet()) {
				for(int i = 0; i < indexedValueList.size(); i++) {
					if(tempList.get(name).get(index) == indexedValueList.get(i)) {
						tempList.get(name).set(index, (int) Math.abs(indexedValueList.get(i) + (1000 * (30 * i)) - 
								(System.currentTimeMillis() - this.lastUpdateTime)));
						stopETA.put(name, tempList.get(name));
						break;
					}
				}
			}
		}
	}
	
	private ArrayList<Integer> returnValueList(int index, ArrayList<ArrayList<Integer>> valueList) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		for(ArrayList<Integer> temp : valueList) {
			list.add(temp.get(index));
		}
		return list;
	}

	@Override
	public boolean equals(Object obj) {
		Shuttle s = (Shuttle) obj;
		return this.shuttleId == s.getShuttleId();

	}

	@Override
	public void snapToRoute(Route r) {
		if (this.currentRoute != null && this.currentLocation != null) {
			Coordinate c1, c2;
            Coordinate closestPoint = null, tempClosestPoint;
            int nextPointId = -1;
            double shortestDistance = 10000, tempShortestDistance = 10000;
            int size = this.currentRoute.getCoordinateList().size();
            for (int i = 0; i < size; i++)
            {
                if (i == 0)
                    c1 = this.currentRoute.getCoordinateList().get(size - 1);
                else
                    c1 = this.currentRoute.getCoordinateList().get(i - 1);

                c2 = this.currentRoute.getCoordinateList().get(i);

                tempClosestPoint = this.currentLocation.closestPoint(c1, c2);
                tempShortestDistance = tempClosestPoint.distanceFromCoordiante(this.currentLocation);

                if (tempShortestDistance < shortestDistance)
                {
                    shortestDistance = tempShortestDistance;
                    closestPoint = tempClosestPoint;
                    nextPointId = (size >= (i + 1))? 0 : i + 1;
                }
            }

            this.SnappedCoordinate = closestPoint;
            this.NextRouteCoordinate = nextPointId;
        }
	}
}