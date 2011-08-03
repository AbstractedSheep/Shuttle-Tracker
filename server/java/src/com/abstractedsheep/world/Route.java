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
import java.util.Map;

/**
 * This class is designed to hold information about a shuttle route.
 * The route would contain a list of geographical coordinates composing the route,
 * its name and ID number. Also, the bearing between each route point, the one before it
 * as well as the one after it is done in order to give a sense of traveling direction
 * along the route.
 *
 * @author saiumesh
 */

public class Route {
    private int idNum;
    private String routeName;
    private ArrayList<Coordinate> coordinateList;
    private ArrayList<Double> distanceToNextCoordinateList;
    private HashMap<Integer, Shuttle> shuttleList;
    private HashMap<String, Stop> stopList;
    private double roundTripDistance;

    public Route() {
        idNum = 0;
        routeName = "West";
        this.coordinateList = new ArrayList<Coordinate>();
        this.stopList = new HashMap<String, Stop>();
        this.shuttleList = new HashMap<Integer, Shuttle>();
        this.distanceToNextCoordinateList = new ArrayList<Double>();
        this.roundTripDistance = 0.0;
    }

    public Route(int idNum, String routeName, ArrayList<Coordinate> list) {
        this.idNum = idNum;
        this.routeName = routeName;
        this.coordinateList = list;
        this.roundTripDistance = 0.0;
        this.stopList = new HashMap<String, Stop>();
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
        return (HashMap<Integer, Shuttle>) Collections.unmodifiableMap(shuttleList);
    }

    /**
     * @return the distanceToNextCoordinateList
     *         NOTE: the distance between the coordinateList[0] and coordinateList[1]
     *         is distanceToNext[1].
     */
    public ArrayList<Double> getDistanceToNextCoordinateList() {
        return (ArrayList<Double>) Collections.unmodifiableList(distanceToNextCoordinateList);
    }

    /**
     * @return the roundTripDistance
     */
    public double getRoundTripDistance() {
        return roundTripDistance;
    }

    /**
     * @return the stopList
     */
    public Map<String, Stop> getStopList() {
        return Collections.unmodifiableMap(stopList);
    }

    public void addStop(Stop s) {
        this.stopList.put(s.getShortName(), s);
    }
}
