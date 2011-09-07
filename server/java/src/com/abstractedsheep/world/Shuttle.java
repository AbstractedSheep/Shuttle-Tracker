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
import java.util.HashMap;

/**
 * The purpose of this class is to hold information about a shuttle from
 * the current.js file being read in by JSONExtractor. Each shuttle contains
 * information about its current location, when that shuttle was last updated,
 * its name, current heading, the route it is on and the stops it will visit.
 * This information is then used to determine how long it will take the shuttle
 * to reach each stop on its list, while accommodating enough time for when the shuttle
 * might wait at each stop.
 *
 * @author saiumesh
 * @author wagnea
 */
public class Shuttle implements IRouteFinder {
    private int shuttleId;
    private HashMap<String, Stop> stops;
    private ArrayList<Integer> speedList;
    private String cardinalPoint;
    private String shuttleName;
    private int heading;
    private int speed;
    private Coordinate currentLocation;
    private long lastUpdateTime;
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
    public Shuttle() {
        this.shuttleId = -1;
        this.stops = new HashMap<String, Stop>();
        new HashMap<String, ArrayList<Integer>>();
        this.shuttleName = "Bus 42";
        this.cardinalPoint = "North";
        this.speed = 0;
        this.currentLocation = new Coordinate();
        this.speedList = new ArrayList<Integer>();
        this.lastUpdateTime = System.currentTimeMillis();
        this.currentRoute = new Route();
    }

    // This constructor is not required by Jackson, but it makes manually
    // creating a new point a
    // one line operation.
    public Shuttle(int shuttleId, ArrayList<Route> rt) {
        // Here, 'this.' is necessary because we have a local variable named the
        // same as the global
        this.shuttleId = shuttleId;
        this.stops = new HashMap<String, Stop>();
        new HashMap<String, ArrayList<Integer>>();
        this.speedList = new ArrayList<Integer>();
        this.shuttleName = "Bus 42";
        this.cardinalPoint = "North";
        this.speed = 0;
        this.currentLocation = new Coordinate();
        this.lastUpdateTime = System.currentTimeMillis();
        this.currentRoute = new Route();
    }

    /**
     * updates the current state of the shuttle object.
     *
     * @param newShuttle
     */
    public void updateShuttle(Shuttle newShuttle) {
        this.setCurrentLocation(newShuttle.getCurrentLocation(), System.currentTimeMillis());
        this.setHeading(newShuttle.getHeading());

        if (newShuttle.getClosestPoint().distanceFromCoordiante(this.getClosestPoint()) > .1 &&
                newShuttle.getRouteId() != this.getRouteId()) {
            this.setCurrentRoute(newShuttle.getCurrentRoute());
        }
    }

    public int getHeading() {
        return heading;
    }

    public void setHeading(int newHeading) {
        this.heading = newHeading;
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
     *
     * @param newSpd - new instantaneous speed value.
     */
    public void setSpeed(int newSpd) {
        if (speedList.size() > 10)
            speedList.remove(0);
        speedList.add((newSpd < 10) ? 10 : newSpd);
        int count = 0;

        for (int s : speedList) {
            count += s;
        }
        this.speed = count / speedList.size();
    }

    public Coordinate getCurrentLocation() {
        return currentLocation;
    }

    /**
     * Change the current state of the shuttle's location
     * and change value of the most recent update time to time.
     *
     * @param newLocation - new shuttle location
     * @param time        - most recent update time
     */
    public void setCurrentLocation(Coordinate newLocation, long time) {
        if (!this.currentLocation.equals(newLocation))
            this.lastUpdateTime = time;
        this.currentLocation = newLocation;
        snapToRoute();

    }

    public void setCurrentLocation(Coordinate newLoc) {
        setCurrentLocation(newLoc, System.currentTimeMillis());
    }

    public String getCardinalPoint() {
        return cardinalPoint;
    }

    public void setCardinalPoint(String cardinalPoint) {
        this.cardinalPoint = cardinalPoint;
    }

    public String getName() {
        return shuttleName;
    }

    public void setName(String newName) {
        this.shuttleName = newName;
    }

    public Coordinate getClosestPoint() {
        return this.SnappedCoordinate;
    }

    public double getDistanceToClosestPoint() {
        if (SnappedCoordinate != null)
            return this.SnappedCoordinate.distanceFromCoordiante(currentLocation);
        else
            return Double.MAX_VALUE;
    }

    /**
     * @return the nextRouteCoordinate
     */
    public int getNextRouteCoordinate() {
        return NextRouteCoordinate;
    }

    public void setCurrentRoute(Route rt) {
        this.currentRoute = rt;
        this.snapToRoute(rt);
    }

    public Route getCurrentRoute() {
        return this.currentRoute;
    }

    /**
     * @return - the age of this shuttle.
     */
    public long getAge() {
        return System.currentTimeMillis() - this.lastUpdateTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Shuttle))
            return false;
        Shuttle s = (Shuttle) obj;
        return this.shuttleId == s.getShuttleId();

    }

    @Override
    public void snapToRoute(Route r) {
        if (this.currentRoute != null && this.currentLocation != null) {
            Coordinate c1, c2;
            Coordinate closestPoint = null, tempClosestPoint;
            int nextPointId = -1;
            Double shortestDistance = 10000.0, tempShortestDistance = 10000.0;
            double bearingDiff = 0.0;
            int size = this.currentRoute.getCoordinateList().size();
            for (int i = 0; i < size; i++) {
                if (i == 0)
                    c1 = this.currentRoute.getCoordinateList().get(size - 1);
                else
                    c1 = this.currentRoute.getCoordinateList().get(i - 1);

                c2 = this.currentRoute.getCoordinateList().get(i);

                tempClosestPoint = this.currentLocation.closestPoint(c1, c2);
                tempShortestDistance = tempClosestPoint.distanceFromCoordiante(this.currentLocation);
                if (tempShortestDistance.equals(Double.NaN)) {
                    tempClosestPoint = c2;
                    tempShortestDistance = c2.distanceFromCoordiante(this.currentLocation);
                }
                double b = Math.abs(currentLocation.getBearing(tempClosestPoint) - 0);
                bearingDiff = Math.abs(heading - b);

                if (tempShortestDistance < shortestDistance && b <= 45) {
                    shortestDistance = tempShortestDistance;
                    closestPoint = tempClosestPoint;
                    nextPointId = (size <= (i + 1)) ? 1 : i + 1;
                }
            }

            this.SnappedCoordinate = closestPoint;
            this.NextRouteCoordinate = nextPointId;
        }
    }

    public void snapToRoute() {
        this.snapToRoute(this.currentRoute);
    }

    public long getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    public void setRouteId(int routeId) {
        this.currentRoute.setIdNum(routeId);
    }
}