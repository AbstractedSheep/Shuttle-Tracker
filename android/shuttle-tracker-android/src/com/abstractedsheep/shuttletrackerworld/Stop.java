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

import java.util.HashMap;

public class Stop
{
    HashMap<Integer, Route> m_routes;
    HashMap<Integer, Double> m_precedingCoordinateDistance;
    HashMap<Integer, Coordinate> m_snappedCoordinate;
    HashMap<Integer, Integer> m_precedingCoordinate;


    private ReadOnlyMap<Integer, Route> ro_routes;
    private ReadOnlyMap<Integer, Double> ro_precedingCoordinateDistance;
    private ReadOnlyMap<Integer, Coordinate> ro_snappedCoordinate;
    private ReadOnlyMap<Integer, Integer> ro_precedingCoordinate;
    private String m_name;
    private String m_id;
    private Coordinate m_location;

    Stop(String id, String name, Coordinate location)
    {
        this.m_id = id;
        this.m_location = location;
        this.m_name = name;
        this.m_routes = new HashMap<Integer, Route>();
        this.m_snappedCoordinate = new HashMap<Integer, Coordinate>();
        this.m_precedingCoordinate = new HashMap<Integer, Integer>();
        this.m_precedingCoordinateDistance = new HashMap<Integer, Double>();
        this.ro_routes = new ReadOnlyMap<Integer, Route>(this.m_routes);
        this.ro_snappedCoordinate = new ReadOnlyMap<Integer, Coordinate>(this.m_snappedCoordinate);
        this.ro_precedingCoordinate = new ReadOnlyMap<Integer, Integer>(this.m_precedingCoordinate);
        this.ro_precedingCoordinateDistance = new ReadOnlyMap<Integer, Double>(this.m_precedingCoordinateDistance);
    }

    void SnapToRoute(Route r)
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

            tempClosestPoint = this.m_location.ClosestPoint(c1, c2);
            tempShortestDistance = tempClosestPoint.DistanceTo(this.m_location);
			
			if (tempShortestDistance < shortestDistance)
			{
				shortestDistance = tempShortestDistance;
				closestPoint = tempClosestPoint;
				precedingPointId = i == 0 ? r.getCoordinates().size() - 1 : i;
			}
		}

        this.m_snappedCoordinate.put(r.getId(), closestPoint);
        this.m_precedingCoordinate.put(r.getId(), precedingPointId);
        this.m_precedingCoordinateDistance.put(r.getId(), r.getCoordinates().get(precedingPointId).DistanceTo(m_location));
	}
    
    

    public ReadOnlyMap<Integer, Route> getRoutes() {
		return ro_routes;
	}

	public ReadOnlyMap<Integer, Double> getPrecedingCoordinateDistance() {
		return ro_precedingCoordinateDistance;
	}

	public ReadOnlyMap<Integer, Coordinate> getSnappedCoordinate() {
		return ro_snappedCoordinate;
	}

	public ReadOnlyMap<Integer, Integer> getPrecedingCoordinate() {
		return ro_precedingCoordinate;
	}

	public String getName() {
		return m_name;
	}

	public String getId() {
		return m_id;
	}

	public Coordinate getLocation() {
		return m_location;
	}

	@Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;

        try {
        	Stop s = (Stop) obj;
        	return this.m_id == s.m_id;
        } catch (ClassCastException e) {
        	return false;
        }   
    }

    @Override
    public int hashCode()
    {
        return this.m_id.hashCode();
    }
}
