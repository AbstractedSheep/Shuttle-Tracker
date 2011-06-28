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
using AbstractedSheep.ShuttleTrackerWorld;

namespace AbstractedSheep.ShuttleTrackerService
{
    class EtaCalculator
    {
        #region Global Variables

        public World world;
        public List<Eta> etas;
		private Dictionary<RouteStopKey, double> nextStopDistance;

        #endregion

        #region Structs

        public struct Eta
        {
			public readonly int ShuttleId;
            public readonly string StopId;
			public readonly int RouteId;
			public readonly int Time;
            public Eta(int shuttleId, int routeId, string stopId, int eta)
            {
                ShuttleId = shuttleId;
                StopId = stopId;
				Time = eta;
				RouteId = routeId;
            }
        }
		
		private struct RouteStopKey
        {
			public readonly int RouteId;
            public readonly string StopId;
            public RouteStopKey(int routeId, string stopId)
            {
                RouteId = routeId;
                StopId = stopId;
            }
        }

        #endregion

        #region Constructors

        public EtaCalculator(World w)
        {
            this.world = w;
            this.etas = new List<Eta>();
        }

        #endregion

        #region Public Methods

        public void Recalculate()
        {
			this.etas.Clear();
			foreach (Shuttle s in this.world.Shuttles.Values)
			{
				Route r = s.CurrentRoute;
				
				foreach (Stop st in r.Stops.Values)
				{
					int i = s.NextRouteCoordinate;
					int j;
					double distance;
                    int stopPrecedingCoordinate = st.PrecedingCoordinate[r.Id];

                    // If the shuttle and stop are between the same two route points, the distance is
                    // simply the distance from the shuttle to the stop
                    if ((s.NextRouteCoordinate == 0 && stopPrecedingCoordinate == r.Coordinates.Count - 1) ||
                        (s.NextRouteCoordinate == stopPrecedingCoordinate + 1))
					{
						distance = s.Location.DistanceTo(s.Location);
					}
					else
					{
                        // Start with the distance from the shuttle to the next route point
						distance = s.Location.DistanceTo(r.Coordinates[s.NextRouteCoordinate]);
 
                        // Go through each route point until we reach the one just before
                        // the stop and sum the distances
                        while (i != stopPrecedingCoordinate)
						{
							if (i == r.Coordinates.Count)
								j = 0;
							else
								j = i + 1;
							
                            // TODO: use the nextStopDistance map to remove the need to recalculate
                            // the distance each time. Check execution times for both. Dictionary lookup
                            // may actually be slower than recalculating
							distance += r.Coordinates[i].DistanceTo(r.Coordinates[j]);
							
							i++;
							if (i == r.Coordinates.Count)
								i = 0;	
						}
						
                        // Finish by adding the distance from the stop to the last route point that the shuttle
                        // will go through
						distance += st.PrecedingCoordinateDistance[r.Id];
					}
					
					this.etas.Add(new Eta(s.Id, s.CurrentRoute.Id, st.Id, (int)(distance / s.AverageSpeed)));
				}
			}
        }

        #endregion
    }
}
