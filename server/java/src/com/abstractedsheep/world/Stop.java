/*
 * Copyright 2011
 *
 *   This file is part of Mobile Shuttle Tracker.
 *
 *   Mobile Shuttle Tracker is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Mobile Shuttle Tracker is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Mobile Shuttle Tracker.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.abstractedsheep.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class is designed to hold information about a stop from netlink.js.
 * Each stop object will have a location, a name and an instance of the RouteFinder
 * class. This inner class is similar to the one used in the Shuttle class, and helps
 * the Shuttle class determine the distance to each stop.
 *
 * @author saiumesh
 */
public class Stop implements IRouteFinder {
    private Coordinate location;
    private String name, shortName;
    /**
     * The routes that this stop is on is stored within another array in the
     * json and the route contains an integer id number as well as a name (e.g.
     * West Route)
     */
    private HashMap<Integer, Route> routeMap;
    private HashMap<Integer, Coordinate> snappedCoordinate;
    private HashMap<Integer, Double> precedingCoordinateDistance;
    private HashMap<Integer, Integer> precedingCoordinate;
    private ArrayList<Integer> routesToAdd = null;

    /**
     * @return the snappedCoordinate
     */
    public HashMap<Integer, Coordinate> getSnappedCoordinate() {
        return snappedCoordinate;
    }

    /**
     * @return the precedingCoordinateDistance
     */
    public HashMap<Integer, Double> getPrecedingCoordinateDistance() {
        return precedingCoordinateDistance;
    }

    /**
     * @return the precedingCoordinate
     */
    public HashMap<Integer, Integer> getPrecedingCoordinate() {
        return precedingCoordinate;
    }

    public Stop(double longitude, double latitude, String fullName,
                String shortN, HashMap<Integer, Route> map) {
        snappedCoordinate = new HashMap<Integer, Coordinate>();
        precedingCoordinateDistance = new HashMap<Integer, Double>();
        precedingCoordinate = new HashMap<Integer, Integer>();
        this.location = new Coordinate(latitude, longitude);
        this.name = fullName;
        this.shortName = shortN;
        this.routeMap = map;
        for (Route r : map.values()) {
            this.snapToRoute(r);
        }
    }

    public Stop(Coordinate coordinate, String shortName, String fullName) {
        snappedCoordinate = new HashMap<Integer, Coordinate>();
        precedingCoordinateDistance = new HashMap<Integer, Double>();
        precedingCoordinate = new HashMap<Integer, Integer>();
        this.location = coordinate;
        this.name = fullName;
        this.shortName = shortName;
        this.routeMap = new HashMap<Integer, Route>();
    }

    public ArrayList<Integer> getRoutesToAdd() {
        return routesToAdd;
    }

    public void setRoutesToAdd(ArrayList<Integer> array) {
        this.routesToAdd = array;
    }

    /**
     * @return the lon
     */
    public double getLongitude() {
        return location.getLongitude();
    }

    /**
     * @param lon the lon to set
     */
    public void setLongitude(double lon) {
        this.location.setLongitude(lon);
    }

    /**
     * @return the lat
     */
    public double getLatitude() {
        return this.location.getLatitude();
    }

    public Coordinate getLocation() {
        return this.location;
    }

    /**
     * @param lat the lat to set
     */
    public void setLatitude(double lat) {
        this.location.setLatitude(lat);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
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
     * @param shortName the shortName to set
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * @return the routeMap
     */
    public HashMap<Integer, Route> getRouteMap() {
        return (HashMap<Integer, Route>) Collections.unmodifiableMap(routeMap);
    }

    public void addRoute(Route r) {
        if (r.getIdNum() == 1) {
            ArrayList list = r.getCoordinateList();
            Collections.reverse(list);
            r.setCoordinateList(list);
        }
        this.routeMap.put(r.getIdNum(), r);
        snapToRoute(r);
    }

    @Override
    public void snapToRoute(Route r) {
        Coordinate c1, c2;
        Coordinate closestPoint = null, tempClosestPoint = null;
        int precedingPointId = -1;
        double shortestDistance = 10000, tempShortestDistance = 10000;
        int size = r.getCoordinateList().size();
        for (int i = 0; i < r.getCoordinateList().size(); i++) {
            if (i == 0)
                c1 = r.getCoordinateList().get(size - 1);
            else
                c1 = r.getCoordinateList().get(i - 1);

            c2 = r.getCoordinateList().get(size - 1);

            tempClosestPoint = location.closestPoint(c1, c2);
            tempShortestDistance = tempClosestPoint.distanceFromCoordiante(location);

            if (tempShortestDistance < shortestDistance) {
                shortestDistance = tempShortestDistance;
                closestPoint = tempClosestPoint;
                precedingPointId = (i == 0) ? (size - 1) : i;
            }
        }
        this.snappedCoordinate.put(r.getIdNum(), closestPoint);
        this.precedingCoordinate.put(r.getIdNum(), precedingPointId);
        this.precedingCoordinateDistance.put(r.getIdNum(), r.getCoordinateList().get(precedingPointId).distanceFromCoordiante(location));
    }
}
