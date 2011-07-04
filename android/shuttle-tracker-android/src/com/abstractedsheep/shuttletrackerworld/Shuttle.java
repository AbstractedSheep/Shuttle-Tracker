package com.abstractedsheep.shuttletrackerworld;

import java.util.ArrayList;
import java.util.List;

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

public class Shuttle
{
    private int m_speed;
    private List<Integer> m_pastSpeeds;

    public int m_nextRouteCoordinate;
    public int m_bearing;
    public String m_cardinalPoint;
    public int m_id;
    public Coordinate m_location;
    public long m_lastUpdateTime;
    public String m_name;
    public int m_averageSpeed;
    public Route m_currentRoute;
    public Coordinate m_snappedCoordinate;
    
    Shuttle()
    {
        this.m_bearing = 0;
        this.m_cardinalPoint = "";
        this.m_id = -1;
        this.m_location = null;
        this.m_lastUpdateTime = -1;
        this.m_name = "";
        this.m_speed = -1;
        this.m_pastSpeeds = new ArrayList<Integer>(10);
        this.m_averageSpeed = -1;
    }

    void SnapToRoute()
    {
        if (this.m_currentRoute != null && this.m_location != null)
        {
            Coordinate c1, c2;
            Coordinate closestPoint = null, tempClosestPoint;
            int nextPointId = -1;
            double shortestDistance = 10000, tempShortestDistance = 10000;

            for (int i = 0; i < this.m_currentRoute.getCoordinates().size(); i++)
            {
                if (i == 0)
                    c1 = this.m_currentRoute.getCoordinates().get(this.m_currentRoute.getCoordinates().size() - 1);
                else
                    c1 = this.m_currentRoute.getCoordinates().get(i - 1);

                c2 = this.m_currentRoute.getCoordinates().get(i);

                tempClosestPoint = this.m_location.ClosestPoint(c1, c2);
                tempShortestDistance = tempClosestPoint.DistanceTo(this.m_location);

                if (tempShortestDistance < shortestDistance)
                {
                    shortestDistance = tempShortestDistance;
                    closestPoint = tempClosestPoint;
                    nextPointId = i + 1 == this.m_currentRoute.getCoordinates().size() ? 0 : i + 1;
                }
            }

            this.m_snappedCoordinate = closestPoint;
            this.m_nextRouteCoordinate = nextPointId;
        }
    }

    
    
    public int getSeed() {
		return m_speed;
	}

	void setSpeed(int speed) {
		this.m_speed = speed;
        if (this.m_pastSpeeds.size() == 10)
            this.m_pastSpeeds.remove(0);

        this.m_pastSpeeds.add(speed);

        int sum = 0;
        for (int i = 0; i < m_pastSpeeds.size(); i++)
        	sum += m_pastSpeeds.get(i);
        
        this.m_averageSpeed = sum / this.m_pastSpeeds.size();
	}

	public int getBearing() {
		return m_bearing;
	}

	void setBearing(int bearing) {
		this.m_bearing = bearing;
	}

	public String geCardinalPoint() {
		return m_cardinalPoint;
	}

	void setCardinalPoint(String cardinalPoint) {
		this.m_cardinalPoint = cardinalPoint;
	}

	public int getId() {
		return m_id;
	}

	void setId(int id) {
		this.m_id = id;
	}

	public Coordinate getLocation() {
		return m_location;
	}

	void setLocation(Coordinate location) {
		this.m_location = location;
	}

	public long getLastUpdateTime() {
		return m_lastUpdateTime;
	}

	void setLastUpdateTime(long lastUpdateTime) {
		this.m_lastUpdateTime = lastUpdateTime;
	}

	public String getName() {
		return m_name;
	}

	void setName(String name) {
		this.m_name = name;
	}

	public Route getCurrentRoute() {
		return m_currentRoute;
	}

	void setCurrentRoute(Route currentRoute) {
		this.m_currentRoute = currentRoute;
	}

	public List<Integer> getPastSpeeds() {
		return m_pastSpeeds;
	}

	public int getNextRouteCoordinate() {
		return m_nextRouteCoordinate;
	}

	public int getAverageSpeed() {
		return m_averageSpeed;
	}

	public Coordinate getSnappedCoordinate() {
		return m_snappedCoordinate;
	}

	@Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;

        try {
        	Shuttle s = (Shuttle)obj;
        	return this.m_id == s.m_id;
        } catch (ClassCastException e) {
        	return false;
        }
    }

    @Override
    public int hashCode()
    {
        return this.m_id;
    }
}
