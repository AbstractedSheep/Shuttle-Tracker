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
import java.util.Map.Entry;

import com.abstractedsheep.shuttletrackerworld.Netlink.RouteJson;
import com.abstractedsheep.shuttletrackerworld.Netlink.StopJson;
import com.abstractedsheep.shuttletrackerworld.Netlink.RouteJson.RouteCoordinateJson;
import com.abstractedsheep.shuttletrackerworld.Netlink.StopJson.StopRouteJson;


public class World
{
    private static final long SHUTTLE_EXPIRATION_TIME = 60000;

    private HashMap<Integer, Shuttle> m_shuttles;
    private HashMap<Integer, Route> m_routes;
    private HashMap<String, Stop> m_stops;
    
    private ReadOnlyMap<Integer, Shuttle> ro_shuttles;
    private ReadOnlyMap<Integer, Route> ro_routes;
    private ReadOnlyMap<String, Stop> ro_stops;

    private World()
    {
        this.m_shuttles = new HashMap<Integer, Shuttle>();
        this.m_routes = new HashMap<Integer, Route>();
        this.m_stops = new HashMap<String, Stop>();
        this.ro_shuttles = new ReadOnlyMap<Integer, Shuttle>(m_shuttles);
        this.ro_routes = new ReadOnlyMap<Integer, Route>(m_routes);
        this.ro_stops = new ReadOnlyMap<String, Stop>(m_stops);
    }

    /// <summary>
	/// Create a world object from a Netlink object.
	/// </summary>
	/// <param name="n">The Netlink class that represents the Netlink JSON.</param>
	/// <returns>A world generated from the Netlink.</returns>
	public static World GenerateWorld(Netlink n)
	{
		World w = new World();
		
	    for (RouteJson r : n.getRoutes())
	    {
	        w.AddRoute(r);
	    }
	
	    for (StopJson s : n.getStops())
	    {
	        w.AddStop(s);
	    }
	
		return w;
	}
	
	/// <summary>
	/// Removes all shuttles older than SHUTTLE_EXPIRATION_TIME
	/// </summary>
	public void RemoveOldShuttles()
	{
	    for (Entry<Integer, Shuttle> e : m_shuttles.entrySet())
	    {
	        if (System.currentTimeMillis() - e.getValue().getLastUpdateTime() > SHUTTLE_EXPIRATION_TIME)
	            m_shuttles.remove(e.getKey());
	    }
	}
	
	/// <summary>
	/// Adds a shuttle to the world or updates the position of an existing shuttle.
	/// </summary>
	/// <param name="shuttleId">The ID number of the shuttle.</param>
	/// <param name="location">The shuttle's current location.</param>
	/// <param name="name">The name of the shuttle.</param>
	/// <param name="bearing">The heading of the shuttle in degrees from north.</param>
	/// <param name="cardinalPoint">The heading of the shuttle as a cardinal direcation (e.g. Northwest).</param>
	/// <param name="speed">The speed of the shuttle in miles per hour.</param>
	/// <param name="route">The id of the shuttle route. -1 indicates that the shuttle is not on a route.</param>
    public void AddOrUpdateShuttle(int shuttleId, Coordinate location, String name, int bearing, String cardinalPoint, int speed, int route)
    {
        Shuttle s = this.m_shuttles.get(shuttleId);
        Route r = this.m_routes.get(route);

        if (s == null)
        {     
            s = new Shuttle();
            s.m_id = shuttleId;
            s.m_location = location;
            s.m_name = name;
            s.m_bearing = bearing;
            s.m_cardinalPoint = cardinalPoint;
            s.setSpeed(speed);
            s.m_lastUpdateTime = System.currentTimeMillis();
                           
            this.m_shuttles.put(s.m_id, s);

            if (r != null)
            {
                s.m_currentRoute = r;
                r.m_shuttles.put(s.m_id, s);
            }
			
			s.SnapToRoute();
        }
        else
        {
            s.m_lastUpdateTime = System.currentTimeMillis();
            s.m_location = location;
            s.setSpeed(speed);
            s.m_bearing = bearing;
            s.m_cardinalPoint = cardinalPoint;
            s.m_name = name;

            if (r == null && s.m_currentRoute != null)
            {
                s.m_currentRoute.m_shuttles.remove(s.m_id);
                s.m_currentRoute = null;
            }
            else if (r != null && s.m_currentRoute != null && s.m_currentRoute != r)
            {
                s.m_currentRoute.m_shuttles.remove(s.m_id);
                s.m_currentRoute = r;
                r.m_shuttles.put(s.m_id, s);
            }
            else if (r != null && s.m_currentRoute == null)
            {
                s.m_currentRoute = r;
                r.m_shuttles.put(s.m_id, s);
            }
			
			s.SnapToRoute();
        }	
    }

    private void AddRoute(RouteJson route)
    {
        List<Coordinate> coords = new ArrayList<Coordinate>();
        for (RouteCoordinateJson rc : route.getCoords())
        {
            coords.add(new Coordinate((int)(rc.getLatitude() * 1E6), (int)(rc.getLongitude() * 1E6)));
        }
        AddRoute(route.getId(), route.getName(), coords);
    }

    private void AddRoute(int routeId, String name, List<Coordinate> coords)
    {
        Route r = new Route(routeId, name, coords);
        this.m_routes.put(r.getId(), r);
    }

    private void AddStop(StopJson stop)
    {
        List<Integer> routes = new ArrayList<Integer>();
        for (StopRouteJson sj : stop.getRoutes())
        {
            routes.add(sj.getId());
        }
        AddStop(stop.getShort_name(), new Coordinate((int)(stop.getLatitude() * 1E6), (int)(stop.getLongitude() * 1E6)), stop.getName(), routes);
    }

    private void AddStop(String stopId, Coordinate location, String name, List<Integer> routes)
    {
        Stop s = new Stop(stopId, name, location);
        m_stops.put(s.getId(), s);

        for (Integer i : routes)
        {
            Route r = this.m_routes.get(i);
            s.m_routes.put(r.getId(), r);
            r.m_stops.put(s.getId(), s);
            s.SnapToRoute(r);
        }
    }
    
    

    public ReadOnlyMap<Integer, Shuttle> getShuttles() {
		return ro_shuttles;
	}

	public ReadOnlyMap<Integer, Route> getRoutes() {
		return ro_routes;
	}

	public ReadOnlyMap<String, Stop> getStops() {
		return ro_stops;
	}
}
