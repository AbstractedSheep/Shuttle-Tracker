/* Copyright 2011 Austin Wagner
 *     
 * This file is part of Mobile Shuttle Tracker.
 *
 *  Mobile Shuttle Tracker is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Mobile Shuttle Tracker is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Mobile Shuttle Tracker.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.abstractedsheep.shuttletrackerworld;

import java.util.*;

public class Stop
{
    final Map<Integer, Route> routes;
    final Map<Integer, Double> precedingCoordinateDistance;
    final Map<Integer, Coordinate> snappedCoordinate;
    final Map<Integer, Integer> precedingCoordinate;
    final List<Route> routeList;


    private final Map<Integer, Route> ro_routes;
    private final Map<Integer, Double> ro_precedingCoordinateDistance;
    private final Map<Integer, Coordinate> ro_snappedCoordinate;
    private final Map<Integer, Integer> ro_precedingCoordinate;
    private final List<Route> ro_routeList;
    private final String name;
    private final String id;
    private final Coordinate location;

    Stop(String id, String name, Coordinate location)
    {
        this.id = id;
        this.location = location;
        this.name = name;
        this.routes = Collections.synchronizedMap(new HashMap<Integer, Route>());
        this.snappedCoordinate = Collections.synchronizedMap(new HashMap<Integer, Coordinate>());
        this.precedingCoordinate = Collections.synchronizedMap(new HashMap<Integer, Integer>());
        this.precedingCoordinateDistance = Collections.synchronizedMap(new HashMap<Integer, Double>());
        this.routeList = Collections.synchronizedList(new ArrayList<Route>());
        this.ro_routes = Collections.unmodifiableMap(this.routes);
        this.ro_snappedCoordinate = Collections.unmodifiableMap(this.snappedCoordinate);
        this.ro_precedingCoordinate = Collections.unmodifiableMap(this.precedingCoordinate);
        this.ro_precedingCoordinateDistance = Collections.unmodifiableMap(this.precedingCoordinateDistance);
        this.ro_routeList = Collections.unmodifiableList(this.routeList);
    }

    void snapToRoute(Route r)
	{
		Coordinate c1, c2;
		Coordinate closestPoint = null, tempClosestPoint = null;
		int precedingPointId = -1;
		double shortestDistance = 10000, tempShortestDistance = 10000;
		
		for (int i = 0; i < r.getCoordinates().size(); i++)
		{
			if (i == 0)
				c1 = r.getCoordinates().get(r.getCoordinates().size() - 1);
			else
				c1 = r.getCoordinates().get(i - 1);
			
			c2 = r.getCoordinates().get(i);

            tempClosestPoint = this.location.closestPoint(c1, c2);
            tempShortestDistance = tempClosestPoint.distanceTo(this.location);
			
			if (tempShortestDistance < shortestDistance)
			{
				shortestDistance = tempShortestDistance;
				closestPoint = tempClosestPoint;
				precedingPointId = i == 0 ? r.getCoordinates().size() - 1 : i;
			}
		}

        this.snappedCoordinate.put(r.getId(), closestPoint);
        this.precedingCoordinate.put(r.getId(), precedingPointId);
        this.precedingCoordinateDistance.put(r.getId(), r.getCoordinates().get(precedingPointId).distanceTo(location));
	}
    
    

    public Map<Integer, Route> getRoutes() {
		return ro_routes;
	}

	public Map<Integer, Double> getPrecedingCoordinateDistance() {
		return ro_precedingCoordinateDistance;
	}

	public Map<Integer, Coordinate> getSnappedCoordinate() {
		return ro_snappedCoordinate;
	}

	public Map<Integer, Integer> getPrecedingCoordinate() {
		return ro_precedingCoordinate;
	}

    public List<Route> getRouteList() {
        return ro_routeList;
    }

    public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public Coordinate getLocation() {
		return location;
	}

	@Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;

        try {
        	Stop s = (Stop) obj;
        	return this.id.equals(s.id);
        } catch (ClassCastException e) {
        	return false;
        }   
    }

    @Override
    public int hashCode()
    {
        return this.id.hashCode();
    }
}
