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
    private int speed;
    private final List<Integer> pastSpeeds;

    public int nextRouteCoordinate;
    public int bearing;
    public String cardinalPoint;
    public int id;
    public Coordinate location;
    public long lastUpdateTime;
    public String name;
    public int averageSpeed;
    public Route currentRoute;
    public Coordinate snappedCoordinate;
    
    Shuttle()
    {
        this.bearing = 0;
        this.cardinalPoint = "";
        this.id = -1;
        this.location = null;
        this.lastUpdateTime = -1;
        this.name = "";
        this.speed = -1;
        this.pastSpeeds = new ArrayList<Integer>(10);
        this.averageSpeed = -1;
    }

    void snapToRoute()
    {
        if (this.currentRoute != null && this.location != null)
        {
            Coordinate c1, c2;
            Coordinate closestPoint = null, tempClosestPoint;
            int nextPointId = -1;
            double shortestDistance = 10000, tempShortestDistance = 10000;

            for (int i = 0; i < this.currentRoute.getCoordinates().size(); i++)
            {
                if (i == 0)
                    c1 = this.currentRoute.getCoordinates().get(this.currentRoute.getCoordinates().size() - 1);
                else
                    c1 = this.currentRoute.getCoordinates().get(i - 1);

                c2 = this.currentRoute.getCoordinates().get(i);

                tempClosestPoint = this.location.closestPoint(c1, c2);
                tempShortestDistance = tempClosestPoint.distanceTo(this.location);

                if (tempShortestDistance < shortestDistance)
                {
                    shortestDistance = tempShortestDistance;
                    closestPoint = tempClosestPoint;
                    nextPointId = i + 1 == this.currentRoute.getCoordinates().size() ? 0 : i + 1;
                }
            }

            this.snappedCoordinate = closestPoint;
            this.nextRouteCoordinate = nextPointId;
        } else {
            this.snappedCoordinate = null;
        }
    }

    
    
    public int getSeed() {
		return speed;
	}

	void setSpeed(int speed) {
		this.speed = speed;
        if (this.pastSpeeds.size() == 10)
            this.pastSpeeds.remove(0);

        this.pastSpeeds.add(speed);

        int sum = 0;
        for (int i = 0; i < pastSpeeds.size(); i++)
        	sum += pastSpeeds.get(i);
        
        this.averageSpeed = sum / this.pastSpeeds.size();
	}

	public int getBearing() {
		return bearing;
	}

	void setBearing(int bearing) {
		this.bearing = bearing;
	}

	public String geCardinalPoint() {
		return cardinalPoint;
	}

	void setCardinalPoint(String cardinalPoint) {
		this.cardinalPoint = cardinalPoint;
	}

	public int getId() {
		return id;
	}

	void setId(int id) {
		this.id = id;
	}

	public Coordinate getLocation() {
		return location;
	}

	void setLocation(Coordinate location) {
		this.location = location;
	}

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	public Route getCurrentRoute() {
		return currentRoute;
	}

	void setCurrentRoute(Route currentRoute) {
		this.currentRoute = currentRoute;
	}

	public List<Integer> getPastSpeeds() {
		return pastSpeeds;
	}

	public int getNextRouteCoordinate() {
		return nextRouteCoordinate;
	}

	public int getAverageSpeed() {
		return averageSpeed;
	}

	public Coordinate getSnappedCoordinate() {
        if (snappedCoordinate == null)
            return location;
        else
		    return snappedCoordinate;
	}

	@Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;

        try {
        	Shuttle s = (Shuttle)obj;
        	return this.id == s.id;
        } catch (ClassCastException e) {
        	return false;
        }
    }

    @Override
    public int hashCode()
    {
        return this.id;
    }
}
