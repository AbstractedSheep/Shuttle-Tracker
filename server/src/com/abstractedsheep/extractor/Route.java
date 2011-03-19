package com.abstractedsheep.extractor;

import java.util.ArrayList;
import com.abstractedsheep.extractor.Shuttle.Point;

/**
 * Sample output from JSONExtractor.java color #E1501B id 1 name West Route
 * width 4 latitude 42.72276 longitude -73.67982 latitude 42.72326 longitude
 * -73.68052
 * 
 * @author jonnau
 * 
 */

public class Route {
	private int idNum;
	private String routeName;
	private ArrayList<Point> coordinateList;
	/**
	 * list of initial bearings for each route point.
	 */
	private ArrayList<Double[]> bearingList;

	public Route() {
		idNum = 0;
		routeName = "West";
		this.coordinateList = new ArrayList<Point>();
		calculateBearings();
	}

	public Route(int idNum, String routeName) {
		this.idNum = idNum;
		this.routeName = routeName;
		this.coordinateList = new ArrayList<Point>();
		calculateBearings();
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

	private Double[] getBearing(Point current, Point prev, Point next) {
		Double[] array = new Double[2];
		double deltaLon1 = (current.getLonInRadians() - prev.getLonInRadians()),
			   deltaLon2 = (next.getLonInRadians() - current.getLonInRadians());
		
		double x1 = Math.sin(deltaLon1) * Math.cos(current.getLatInRadians()),
			   x2 = Math.sin(deltaLon2) * Math.cos(next.getLatInRadians());
		
		double y1 = Math.cos(prev.getLatInRadians()) * Math.sin(current.getLatInRadians())
					- Math.sin(prev.getLatInRadians()) *
					Math.cos(current.getLatInRadians())*Math.cos(deltaLon1);
		double y2 = Math.cos(current.getLatInRadians()) * Math.sin(next.getLatInRadians())
					- Math.sin(current.getLatInRadians()) *
					Math.cos(next.getLatInRadians())*Math.cos(deltaLon2);
		double bearing1 = Math.toDegrees(Math.atan2(y1, x1));
		double bearing2 = Math.toDegrees(Math.atan2(y2, x2));
		array[0] = (bearing1 < 0) ? (bearing1 + 360) : bearing1;
		array[1] = (bearing2 < 0) ? (bearing2 + 360) : bearing2;
		return array;
	}
	
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
	
	public Double[] getBearingsForPoint(int index) {
		return this.bearingList.get(index);
	}

	public void putCoordinate(double lon, double lat) {
		Point p = new Shuttle.Point(lat, lon);
		this.coordinateList.add(p);
	}

	public void putCoordinate(Point coordinate) {
		this.coordinateList.add(coordinate);
	}

	public ArrayList<Point> getCoordinateList() {
		return this.coordinateList;
	}
}
