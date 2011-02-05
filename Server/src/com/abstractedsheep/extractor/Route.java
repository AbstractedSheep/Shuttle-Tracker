package com.abstractedsheep.extractor;

import java.awt.Point;
import java.util.ArrayList;

/**
 * Sample output from JSONExtractor.java
color #E1501B
id 1
name West Route
width 4
latitude 42.72276
longitude -73.67982
latitude 42.72326
longitude -73.68052
 * @author jonnau
 *
 */

public class Route {
	int idNum;
	String routeName;
	ArrayList<Point> coordinateList;
	
	public Route() {
		idNum = 0;
		routeName = "West";
		this.coordinateList = new ArrayList<Point>();
	}
	
	public Route(int idNum, String routeName) {
		this.idNum = idNum;
		this.routeName = routeName;
		this.coordinateList = new ArrayList<Point>();
	}
	
	/**
	 * @return the idNum
	 */
	public int getIdNum() {
		return idNum;
	}

	/**
	 * @param idNum the idNum to set
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
	
	public void putCoordinate(double lon, double lat) {
		Point p = new Point();
		p.setLocation(lon, lat);
		this.coordinateList.add(p);
	}
	
	public void putCoordinate(Point coordinate) {
		this.coordinateList.add(coordinate);
	}
}
