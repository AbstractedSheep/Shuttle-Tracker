package com.abstractedsheep.world;

import java.util.ArrayList;
import java.util.HashMap;

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
	private ArrayList<Coordinate> coordinateList;
	private ArrayList<Double> distanceToNextCoordinateList;
	private HashMap<Integer, Shuttle> shuttleList;
	private HashMap<Integer, Stop> stopList;
	private double roundTripDistance;
	/**
	 * list of initial bearings for each route point.
	 */
	private ArrayList<Double[]> bearingList;

	public Route() {
		idNum = 0;
		routeName = "West";
		this.coordinateList = new ArrayList<Coordinate>();
		this.stopList = new HashMap<Integer, Stop>();
		this.shuttleList = new HashMap<Integer, Shuttle>();
		this.distanceToNextCoordinateList = new ArrayList<Double>();
		this.roundTripDistance = 0.0;
	}

	public Route(int idNum, String routeName, ArrayList<Coordinate> list) {
		this.idNum = idNum;
		this.routeName = routeName;
		this.coordinateList = list;
		this.roundTripDistance = 0.0;
		this.stopList = new HashMap<Integer, Stop>();
		this.shuttleList = new HashMap<Integer, Shuttle>();
		this.distanceToNextCoordinateList = new ArrayList<Double>();
		this.computeDistances();
	}
	
	private void computeDistances() {
		int size = coordinateList.size();
		Coordinate c1 = null, c2 = null;
		double distance = 0.0;
		for (int i = 0; i < coordinateList.size(); i++) {
			if (i == 0)
				c1 = coordinateList.get(size - 1);
			else
				c1 = coordinateList.get(i - 1);
			c2 = coordinateList.get(i);
			distance = c1.distanceFromCoordiante(c2);
			this.distanceToNextCoordinateList.add(distance);
			this.roundTripDistance += distance;
		}
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
	public ArrayList<Coordinate> getCoordinateList() {
		return this.coordinateList;
	}
	
	public void setCoordinateList(ArrayList<Coordinate> list) {
		this.distanceToNextCoordinateList.clear();
		this.roundTripDistance = 0.0;
		this.coordinateList = list;
		this.computeDistances();
	}

	/**
	 * @return the shuttleList
	 */
	public HashMap<Integer, Shuttle> getShuttleList() {
		return shuttleList;
	}

	/**
	 * @param shuttleList the shuttleList to set
	 */
	public void setShuttleList(HashMap<Integer, Shuttle> shuttleList) {
		this.shuttleList = shuttleList;
	}

	/**
	 * @return the stopList
	 */
	public HashMap<Integer, Stop> getStopList() {
		return stopList;
	}

	/**
	 * @param stopList the stopList to set
	 */
	public void setStopList(HashMap<Integer, Stop> stopList) {
		this.stopList = stopList;
	}
}
