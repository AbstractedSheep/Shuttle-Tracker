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
import java.util.Map.Entry;

import com.abstractedsheep.shuttletrackerworld.Netlink.RouteJson;
import com.abstractedsheep.shuttletrackerworld.Netlink.StopJson;
import com.abstractedsheep.shuttletrackerworld.Netlink.RouteJson.RouteCoordinateJson;
import com.abstractedsheep.shuttletrackerworld.Netlink.StopJson.StopRouteJson;


public class World
{
    private static final long SHUTTLE_EXPIRATION_TIME = 60000;

    private final Map<Integer, Shuttle> shuttles;
    private final Map<Integer, Route> routes;
    private final Map<String, Stop> stops;
    private final List<Shuttle> shuttleList;
    private final List<Route> routeList;
    private final List<Stop> stopList;
    
    private final Map<Integer, Shuttle> ro_shuttles;
    private final Map<Integer, Route> ro_routes;
    private final Map<String, Stop> ro_stops;
    private final List<Shuttle> ro_shuttleList;
    private final List<Route> ro_routeList;
    private final List<Stop> ro_stopList;

    private World()
    {
        this.shuttles = Collections.synchronizedMap(new HashMap<Integer, Shuttle>());
        this.routes = Collections.synchronizedMap(new HashMap<Integer, Route>());
        this.stops = Collections.synchronizedMap(new HashMap<String, Stop>());
        this.ro_shuttles = Collections.unmodifiableMap(shuttles);
        this.ro_routes = Collections.unmodifiableMap(routes);
        this.ro_stops = Collections.unmodifiableMap(stops);
        
        this.shuttleList = Collections.synchronizedList(new ArrayList<Shuttle>());
        this.routeList = Collections.synchronizedList(new ArrayList<Route>());
        this.stopList = Collections.synchronizedList(new ArrayList<Stop>());
        this.ro_shuttleList = Collections.unmodifiableList(shuttleList);
        this.ro_routeList = Collections.unmodifiableList(routeList);
        this.ro_stopList = Collections.unmodifiableList(stopList);
    }

    /// <summary>
	/// Create a world object from a Netlink object.
	/// </summary>
	/// <param name="n">The Netlink class that represents the Netlink JSON.</param>
	/// <returns>A world generated from the Netlink.</returns>
	public static World generateWorld(Netlink n)
	{
		World w = new World();
		
	    for (RouteJson r : n.getRoutes())
	    {
	        w.addRoute(r);
	    }
	
	    for (StopJson s : n.getStops())
	    {
	        w.addStop(s);
	    }

		return w;
	}
	
	/// <summary>
	/// Removes all shuttles older than SHUTTLE_EXPIRATION_TIME
	/// </summary>
	public void removeOldShuttles()
	{
        Set<Entry<Integer, Shuttle>> tempEntrySet = shuttles.entrySet();

        synchronized (shuttleList) {
            for (Entry<Integer, Shuttle> e : tempEntrySet)
            {
                if (System.currentTimeMillis() - e.getValue().getLastUpdateTime() > SHUTTLE_EXPIRATION_TIME)
                    shuttleList.remove(shuttles.remove(e.getKey()));
            }
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
    public void addOrUpdateShuttle(int shuttleId, Coordinate location, String name, int bearing, String cardinalPoint, int speed, int route)
    {
        Shuttle s = this.shuttles.get(shuttleId);
        Route r = this.routes.get(route);

        if (s == null)
        {     
            s = new Shuttle();
            s.id = shuttleId;
            s.location = location;
            s.name = name;
            s.bearing = bearing;
            s.cardinalPoint = cardinalPoint;
            s.setSpeed(speed);
            s.lastUpdateTime = System.currentTimeMillis();
                           
            this.shuttles.put(s.id, s);
            this.shuttleList.add(s);

            if (r != null)
            {
                s.currentRoute = r;
                r.shuttles.put(s.id, s);
                r.shuttleList.add(s);
            }
			
			s.snapToRoute();
        }
        else
        {
            s.lastUpdateTime = System.currentTimeMillis();
            s.location = location;
            s.setSpeed(speed);
            s.bearing = bearing;
            s.cardinalPoint = cardinalPoint;
            s.name = name;

            if (r == null && s.currentRoute != null)
            {
                s.currentRoute.shuttleList.remove(s.currentRoute.shuttles.remove(s.id));
                s.currentRoute = null;
            }
            else if (r != null && s.currentRoute != null && s.currentRoute != r)
            {
                s.currentRoute.shuttleList.remove(s.currentRoute.shuttles.remove(s.id));
                s.currentRoute = r;
                r.shuttles.put(s.id, s);
                r.shuttleList.add(s);
            }
            else if (r != null && s.currentRoute == null)
            {
                s.currentRoute = r;
                r.shuttles.put(s.id, s);
                r.shuttleList.add(s);
            }
			
			s.snapToRoute();
        }
    }

    private void addRoute(RouteJson route)
    {
        List<Coordinate> coords = new ArrayList<Coordinate>();
        for (RouteCoordinateJson rc : route.getCoords())
        {
            coords.add(new Coordinate((int)(rc.getLatitude() * 1E6), (int)(rc.getLongitude() * 1E6)));
        }
        addRoute(route.getId(), route.getName(), route.getColorInt(), coords);
    }

    private void addRoute(int routeId, String name, int color, List<Coordinate> coords)
    {
        Route r = new Route(routeId, name, color, coords);
        this.routes.put(r.getId(), r);
        this.routeList.add(r);
    }

    private void addStop(StopJson stop)
    {
        List<Integer> routes = new ArrayList<Integer>();
        for (StopRouteJson sj : stop.getRoutes())
        {
            routes.add(sj.getId());
        }
        addStop(stop.getShort_name(), new Coordinate((int)(stop.getLatitude() * 1E6), (int)(stop.getLongitude() * 1E6)), stop.getName(), routes);
    }

    private void addStop(String stopId, Coordinate location, String name, List<Integer> routes)
    {
        Stop s = new Stop(stopId, name, location);
        this.stops.put(s.getId(), s);
        this.stopList.add(s);

        for (Integer i : routes)
        {
            Route r = this.routes.get(i);
            s.routes.put(r.getId(), r);
            s.routeList.add(r);
            r.stops.put(s.getId(), s);
            r.stopList.add(s);
            s.snapToRoute(r);
        }
    }
    

    public Map<Integer, Shuttle> getShuttles() {
		return ro_shuttles;
	}

	public Map<Integer, Route> getRoutes() {
		return ro_routes;
	}

	public Map<String, Stop> getStops() {
		return ro_stops;
	}
	
	public List<Shuttle> getShuttleList() {
		return ro_shuttleList;
	}
	
	public List<Route> getRouteList() {
		return ro_routeList;
	}
	
	public List<Stop> getStopList() {
		return ro_stopList;
	}
}
