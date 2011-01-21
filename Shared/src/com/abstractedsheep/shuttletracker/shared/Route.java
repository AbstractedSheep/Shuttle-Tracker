package com.abstractedsheep.shuttletracker.shared;

import java.awt.Color;
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
	Color color;
	int idNum;
	String routeName;
	ArrayList<Double> latList;
	ArrayList<Double> longList;
	
	public Route() {
		this.color = Color.white;
		idNum = 0;
		routeName = "West";
		this.latList = new ArrayList<Double>();
		this.longList = new ArrayList<Double>();
	}
	
	public Route(Color color, int idNum, String routeName) {
		this.color = color;
		this.idNum = idNum;
		this.routeName = routeName;
		this.latList = new ArrayList<Double>();
		this.longList = new ArrayList<Double>();
	}

	public Route(Color color, int idNum, String routeName,
			ArrayList<Double> latList, ArrayList<Double> longList) {
		super();
		this.color = color;
		this.idNum = idNum;
		this.routeName = routeName;
		this.latList = latList;
		this.longList = longList;
	}

	/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(Color color) {
		this.color = color;
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

	/**
	 * @return the latList
	 */
	public ArrayList<Double> getLatList() {
		return latList;
	}

	/**
	 * @return the longList
	 */
	public ArrayList<Double> getLongList() {
		return longList;
	}

	public void putCoordinate(double lon, double lat) {
		latList.add(lat);
		longList.add(lon);
	}
	
	public void putCoordinate(Point coordinate) {
		latList.add(coordinate.getY());
		longList.add(coordinate.getX());
	}
}
