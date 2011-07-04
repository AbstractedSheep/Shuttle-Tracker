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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Route
{
    List<Double> m_distanceToNextCoord;
    List<Coordinate> m_coordinates;
    HashMap<Integer, Shuttle> m_shuttles;
    HashMap<String, Stop> m_stops;
	private String m_name;
    private double m_length;
    private int m_id;
    
    private ReadOnlyList<Double> ro_distanceToNextCoord;
    private ReadOnlyList<Coordinate> ro_coordinates;
    private ReadOnlyMap<Integer, Shuttle> ro_shuttles;
    private ReadOnlyMap<String, Stop> ro_stops;
    
    
    Route(int id, String name, List<Coordinate> coords)
    {
        this.m_id = id;
		this.m_name = name;
        this.m_coordinates = new ArrayList<Coordinate>(coords);
        this.m_stops = new HashMap<String, Stop>();
		this.m_distanceToNextCoord = new ArrayList<Double>();
        this.m_shuttles = new HashMap<Integer, Shuttle>();
        this.ro_distanceToNextCoord = new ReadOnlyList<Double>(m_distanceToNextCoord);
        this.ro_coordinates = new ReadOnlyList<Coordinate>(m_coordinates);
        this.ro_shuttles = new ReadOnlyMap<Integer, Shuttle>(m_shuttles);
        this.ro_stops = new ReadOnlyMap<String, Stop>(m_stops);
		
		for(int i = 0; i < this.m_coordinates.size(); i++)
		{
			Coordinate c1, c2;
			if (i == 0)
				c1 = this.m_coordinates.get(this.m_coordinates.size() - 1);
			else
				c1 = this.m_coordinates.get(i - 1);
			
			c2 = this.m_coordinates.get(i);
			
			this.m_distanceToNextCoord.add(c1.distanceTo(c2));
		}
    }
    
    public String getName() {
		return m_name;
	}

	public double getLength() {
		return m_length;
	}

	public int getId() {
		return m_id;
	}

	public ReadOnlyList<Double> getDistanceToNextCoord() {
		return ro_distanceToNextCoord;
	}

	public ReadOnlyList<Coordinate> getCoordinates() {
		return ro_coordinates;
	}

	public ReadOnlyMap<Integer, Shuttle> getShuttles() {
		return ro_shuttles;
	}

	public ReadOnlyMap<String, Stop> getStops() {
		return ro_stops;
	}

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;

        try {
        	Route r = (Route) obj;
        	return this.m_id == r.m_id;
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
