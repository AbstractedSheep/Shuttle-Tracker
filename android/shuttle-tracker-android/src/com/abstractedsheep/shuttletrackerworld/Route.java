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

public class Route
{
    final List<Double> distanceToNextCoord;
    final List<Coordinate> coordinates;
    final Map<Integer, Shuttle> shuttles;
    final Map<String, Stop> stops;
    final List<Shuttle> shuttleList;
    final List<Stop> stopList;
	private final String name;
    private double length;
    private final int id;
    private final int color;
    
    private final List<Double> ro_distanceToNextCoord;
    private final List<Coordinate> ro_coordinates;
    private final Map<Integer, Shuttle> ro_shuttles;
    private final Map<String, Stop> ro_stops;
    private final List<Shuttle> ro_shuttleList;
    private final List<Stop> ro_stopList;
    
    
    Route(int id, String name, int color, List<Coordinate> coords)
    {
        this.id = id;
		this.name = name;
        this.color = color;
        this.coordinates = new ArrayList<Coordinate>(coords);
        this.stops = Collections.synchronizedMap(new HashMap<String, Stop>());
		this.distanceToNextCoord = new ArrayList<Double>();
        this.shuttles = Collections.synchronizedMap(new HashMap<Integer, Shuttle>());
        this.ro_distanceToNextCoord = Collections.unmodifiableList(distanceToNextCoord);
        this.ro_coordinates = Collections.unmodifiableList(coordinates);
        this.ro_shuttles = Collections.unmodifiableMap(shuttles);
        this.ro_stops = Collections.unmodifiableMap(stops);
        
        this.shuttleList = Collections.synchronizedList(new ArrayList<Shuttle>());
        this.stopList = Collections.synchronizedList(new ArrayList<Stop>());
        this.ro_shuttleList = Collections.unmodifiableList(shuttleList);
        this.ro_stopList = Collections.unmodifiableList(stopList);
		
		for(int i = 0; i < this.coordinates.size(); i++)
		{
			Coordinate c1, c2;
			if (i == 0)
				c1 = this.coordinates.get(this.coordinates.size() - 1);
			else
				c1 = this.coordinates.get(i - 1);
			
			c2 = this.coordinates.get(i);
			
			this.distanceToNextCoord.add(c1.distanceTo(c2));
		}
    }
    
    public String getName() {
		return name;
	}

	public double getLength() {
		return length;
	}

	public int getId() {
		return id;
	}

	public List<Double> getDistanceToNextCoord() {
		return ro_distanceToNextCoord;
	}

	public List<Coordinate> getCoordinates() {
		return ro_coordinates;
	}

	public Map<Integer, Shuttle> getShuttles() {
		return ro_shuttles;
	}

	public Map<String, Stop> getStops() {
		return ro_stops;
	}
	
	public List<Shuttle> getShuttleList() {
		return ro_shuttleList;
	}
	
	public List<Stop> getStopList() {
		return ro_stopList;
	}

    public int getColor() {
        return color;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;

        try {
        	Route r = (Route) obj;
        	return this.id == r.id;
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
