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

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using ExtensionMethods;

namespace AbstractedSheep.ShuttleTrackerWorld
{
    public class World
    {
        private const long SHUTTLE_EXPIRATION_TIME = 60000;

        #region Global Variables

        private Dictionary<int, Shuttle> shuttles;
        private Dictionary<int, Route> routes;
        private Dictionary<string, Stop> stops;

        #endregion

        #region Properties

        public ReadOnlyDictionary<int, Shuttle> Shuttles { get; private set; }
        public ReadOnlyDictionary<int, Route> Routes { get; private set; }
        public ReadOnlyDictionary<string, Stop> Stops { get; private set; }

        #endregion

        #region Constructors

        private World()
        {
            this.shuttles = new Dictionary<int, Shuttle>();
            this.routes = new Dictionary<int, Route>();
            this.stops = new Dictionary<string, Stop>();
            this.Shuttles = this.shuttles.AsReadOnly<int, Shuttle>();
            this.Routes = this.routes.AsReadOnly<int, Route>();
            this.Stops = this.stops.AsReadOnly<string, Stop>();
        }

        #endregion

        #region Public Methods

        /// <summary>
        /// Create a world object from a Netlink object.
        /// </summary>
        /// <param name="n">The Netlink class that represents the Netlink JSON.</param>
        /// <returns>A world generated from the Netlink.</returns>
        public static World GenerateWorld(Netlink n)
        {
			World w = new World();
			
            foreach (RouteJson r in n.routes)
            {
                w.AddRoute(r);
            }

            foreach (StopJson s in n.stops)
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
            foreach (KeyValuePair<int, Shuttle> kvp in shuttles)
            {
                if (CurrentTimeMillis() - kvp.Value.LastUpdateTime > SHUTTLE_EXPIRATION_TIME)
                    shuttles.Remove(kvp.Key);
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
        public void AddOrUpdateShuttle(int shuttleId, Coordinate location, string name, int bearing, string cardinalPoint, int speed, int route = -1)
        {
            Shuttle s = this.shuttles[shuttleId];
            Route r = this.routes[route];

            if (s == null)
            {     
                s = new Shuttle();
                s.Id = shuttleId;
                s.Location = location;
                s.Name = name;
                s.Bearing = bearing;
                s.CardinalPoint = cardinalPoint;
                s.Speed = speed;
                s.LastUpdateTime = CurrentTimeMillis();
                               
                this.shuttles.Add(s.Id, s);

                if (r != null)
                {
                    s.CurrentRoute = r;
                    r.shuttles.Add(s.Id, s);
                }
				
				s.SnapToRoute();
            }
            else
            {
                s.LastUpdateTime = CurrentTimeMillis();
                s.Location = location;
                s.Speed = speed;
                s.Bearing = bearing;
                s.CardinalPoint = cardinalPoint;
                s.Name = name;

                if (r == null && s.CurrentRoute != null)
                {
                    s.CurrentRoute.shuttles.Remove(s.Id);
                    s.CurrentRoute = null;
                }
                else if (r != null && s.CurrentRoute != null && s.CurrentRoute != r)
                {
                    s.CurrentRoute.shuttles.Remove(s.Id);
                    s.CurrentRoute = r;
                    r.shuttles.Add(s.Id, s);
                }
                else if (r != null && s.CurrentRoute == null)
                {
                    s.CurrentRoute = r;
                    r.shuttles.Add(s.Id, s);
                }
				
				s.SnapToRoute();
            }	
        }

        #endregion

        #region Private Methods

        private void AddRoute(RouteJson route)
        {
            List<Coordinate> coords = new List<Coordinate>();
            foreach (RouteCoordinateJson rc in route.coords)
            {
                coords.Add(new Coordinate((int)(rc.latitude * (decimal)1E6), (int)(rc.longitude * (decimal)1E6)));
            }
            AddRoute(route.id, route.name, coords);
        }

        private void AddRoute(int routeId, string name, List<Coordinate> coords)
        {
            Route r = new Route(routeId, name, coords);
            this.routes.Add(r.Id, r);
        }

        private void AddStop(StopJson stop)
        {
            List<int> routes = new List<int>();
            foreach (StopRouteJson sj in stop.routes)
            {
                routes.Add(sj.id);
            }
            AddStop(stop.short_name, new Coordinate((int)(stop.latitude * (decimal)1E6), (int)(stop.longitude * (decimal)1E6)), stop.name, routes);
        }

        private void AddStop(string stopId, Coordinate location, string name, List<int> routes)
        {
            Stop s = new Stop(stopId, name, location);
            stops.Add(s.Id, s);

            foreach (int i in routes)
            {
                Route r = this.routes[i];
                s.routes.Add(r.Id, r);
                r.stops.Add(s.Id, s);
                s.SnapToRoute(r);
            }
        }

        private static readonly DateTime Jan1st1970 = new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);
        private long CurrentTimeMillis()
        {
            return (long)((DateTime.UtcNow - Jan1st1970).TotalMilliseconds);
        }

#endregion
    }
}
