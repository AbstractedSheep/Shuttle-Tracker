package com.abstractedsheep.world;

import java.util.ArrayList;

import com.abstractedsheep.world.Shuttle.Point;

/**
 * This class is designed to hold information about a shuttle route.
 * The route would contain a list of geographical coordinates composing the route,
 * its name and ID number. Also, the bearing between each route point, the one before it
 * as well as the one after it is done in order to give a sense of traveling direction
 * along the route.
 * @author saiumesh
 * 
 */

public class Route {
	private int idNum;
	private String routeName;
	private ArrayList<Point> coordinateList;
	private double roundTripDistance;
	/**
	 * list of initial bearings for each route point.
	 */
	private ArrayList<Double[]> bearingList;

	public Route() {
		idNum = 0;
		routeName = "West";
		this.coordinateList = new ArrayList<Point>();
		calculateBearings();
		computeRoundTripDistance();
	}

	public Route(int idNum, String routeName) {
		this.idNum = idNum;
		this.routeName = routeName;
		this.coordinateList = new ArrayList<Point>();
		calculateBearings();
		computeRoundTripDistance();
	}
	
	//computes the round trip distance for this route
	private void computeRoundTripDistance() {
		Point p1 = null, p2 = null;
		this.roundTripDistance = 0;
		for(int i = 0; i < this.coordinateList.size(); i++) {
			p1 = this.coordinateList.get(i);
			p2 = (i == 0) ? coordinateList.get(coordinateList.size() - 1) : coordinateList.get(i - 1);
			
			this.roundTripDistance += this.calculateDistance(p2, p1);
		}
	}
	//computes the distance between two points (in miles)
	private double calculateDistance(Shuttle.Point p, Shuttle.Point curr) {
		double earthRadius = 3956; //radius in miles
		
		double dlong = Math.toRadians((curr.getLon() - p.getLon()));
	    double dlat = Math.toRadians((curr.getLat() - p.getLat()));
	    double a = Math.pow(Math.sin(dlat/2.0), 2) +
	    		   Math.cos(Math.toRadians(p.getLat())) * Math.cos(Math.toRadians(curr.getLon())) * Math.pow(Math.sin(dlong/2.0), 2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double d = earthRadius * c; 

	    return d;
	}

	/**
	 * calculates the initial bearing between a route position and the position
	 * both before and after it.
	 */
	public void calculateBearings() {
		this.bearingList = new ArrayList<Double[]>();
		Point p1 = null, p2 = null, p3 = null;
		
		for(int i = 0; i < coordinateList.size(); i++) {
			p1 = coordinateList.get(i);
			
			//ArrayIndexOutOfBoundsException will be thrown if i = 0
			try {
				p2 = coordinateList.get(i - 1);
			} catch(IndexOutOfBoundsException ex) {
				int index = coordinateList.size() - 1;
				p2 = coordinateList.get(index);
			}
			//ArrayIndexOutOfBoundsException will be thrown if i >= size() - 1
			try {
				p3 = coordinateList.get(i + 1);
			} catch(IndexOutOfBoundsException ex) {
				System.err.println(i + " / " + coordinateList.size());
				p3 = coordinateList.get(0);
			}
			
			this.bearingList.add(getConstantBearing(p1, p2, p3));
		}
	}
	
	/**
	 * Method calculates the constant bearing between the current route point and the one
	 * immediately following it as well as the bearing between the current route point
	 * and the one immediately before it.
	 * @param current - route point in the coordinate list at index n
	 * @param prev - route point in the coordinate list at index n-1 
	 * @param next - route point in the coordinate list at index n+1
	 * @return Method returns an array containing the bearing between current and prev
	 * 		   as well as the bearing between current and next
	 */
	private Double[] getConstantBearing(Point current, Point prev, Point next) {
		Double[] array = new Double[2];
		double deltaLat1 = (current.getLatInRadians() - prev.getLatInRadians()),
		   	   deltaLat2 = (next.getLatInRadians() - current.getLatInRadians());
		double deltaLon1 = (current.getLonInRadians() - prev.getLonInRadians()),
		       deltaLon2 = (next.getLonInRadians() - current.getLonInRadians());
		
		double dPhi1 = Math.log(Math.tan(current.getLatInRadians()/2+Math.PI/4)/
					  Math.tan(prev.getLatInRadians()/2+Math.PI/4));
		double dPhi2 = Math.log(Math.tan(next.getLatInRadians()/2+Math.PI/4)/
				  	  Math.tan(current.getLatInRadians()/2+Math.PI/4));
		
		if(deltaLon1 > Math.PI) {
			deltaLon1 = (deltaLon1 > 0) ? -(2*Math.PI - deltaLon1) : (2*Math.PI + deltaLon1);
		}
		
		if(deltaLon2 > Math.PI) {
			deltaLon2 = (deltaLon2 > 0) ? -(2*Math.PI - deltaLon2) : (2*Math.PI + deltaLon2);
		}
		
		double bearing1 = Math.toDegrees(Math.atan2(deltaLon1, dPhi1));
		double bearing2 = Math.toDegrees(Math.atan2(deltaLon2, dPhi2));
		array[0] = (bearing1 < 0) ? (bearing1 + 360) : bearing1;
		array[1] = (bearing2 < 0) ? (bearing2 + 360) : bearing2;
		return array;
	}
	
	/**
	 * returns route trip distance around the route.
	 */
	public double getRoundTripDistance() {
		return this.roundTripDistance;
	}

	/**
	 * @return the idNum
	 */
	public int getIdNum() {
		return idNum;
	}

	/**
	 * @param idNum
	 *            the idNum to set
	 */
	public void setIdNum(int idNum) {
		this.idNum = idNum;
	}

	/**
	 * @return the routeName
	 */
	public String getRouteName() {
		return routeName;
	}
	/**
	 * returns the constant bearings for the route point
	 * at the given index
	 * @param index - idnex of the desired route point
	 * @return the bearings between the desired route point
	 * 		   and the one before it as well as the bearing between
	 * 		   the desired route point and the one after it
	 */
	public Double[] getBearingsForPoint(int index) {
		return this.bearingList.get(index);
	}
	//place coordinate in list as two double values
	public void putCoordinate(double lon, double lat) {
		Point p = new Shuttle.Point(lat, lon);
		this.coordinateList.add(p);
		this.computeRoundTripDistance();
	}
	//place coordinate in list as a Point object
	public void putCoordinate(Point coordinate) {
		this.coordinateList.add(coordinate);
		this.computeRoundTripDistance();
	}

	public ArrayList<Point> getCoordinateList() {
		return this.coordinateList;
	}
}
