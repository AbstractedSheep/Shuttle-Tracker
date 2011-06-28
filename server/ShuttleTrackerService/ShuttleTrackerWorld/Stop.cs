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
    public class Stop
    {
        #region Global Variables

        internal Dictionary<int, Route> routes;
        internal Dictionary<int, double> precedingCoordinateDistance;
        internal Dictionary<int, Coordinate> snappedCoordinate;
        internal Dictionary<int, int> precedingCoordinate;

        #endregion

        #region Properties

        public ReadOnlyDictionary<int, Route> Routes { get; private set; }
        public ReadOnlyDictionary<int, double> PrecedingCoordinateDistance { get; private set; }
        public ReadOnlyDictionary<int, Coordinate> SnappedCoordinate { get; private set; }
        public ReadOnlyDictionary<int, int> PrecedingCoordinate { get; private set; }
        public string Name { get; private set; }
        public string Id { get; private set; }
        public Coordinate Location { get; private set; }

        #endregion

        #region Constructors

        internal Stop(string id, string name, Coordinate location)
        {
            this.Id = id;
            this.Location = location;
            this.Name = name;
            this.routes = new Dictionary<int, Route>();
            this.snappedCoordinate = new Dictionary<int, Coordinate>();
            this.precedingCoordinate = new Dictionary<int, int>();
            this.precedingCoordinateDistance = new Dictionary<int, double>();
            this.Routes = this.routes.AsReadOnly<int, Route>();
            this.SnappedCoordinate = this.snappedCoordinate.AsReadOnly<int, Coordinate>();
            this.PrecedingCoordinate = this.precedingCoordinate.AsReadOnly<int, int>();
            this.PrecedingCoordinateDistance = this.precedingCoordinateDistance.AsReadOnly<int, double>();
        }

        #endregion

        #region Internal Methods

        internal void SnapToRoute(Route r)
		{
			Coordinate c1, c2;
			Coordinate closestPoint = null, tempClosestPoint = null;
			int precedingPointId = -1;
			double shortestDistance = 10000, tempShortestDistance = 10000;
			
			for (int i = 0; i < r.coordinates.Count; i++)
			{
				if (i == 0)
					c1 = r.coordinates.Last();
				else
					c1 = r.coordinates[i - 1];
				
				c2 = r.coordinates[i];

                tempClosestPoint = this.Location.ClosestPoint(c1, c2);
                tempShortestDistance = tempClosestPoint.DistanceTo(this.Location);
				
				if (tempShortestDistance < shortestDistance)
				{
					shortestDistance = tempShortestDistance;
					closestPoint = tempClosestPoint;
					precedingPointId = i == 0 ? r.coordinates.Count - 1 : i;
				}
			}

            this.snappedCoordinate.Add(r.Id, closestPoint);
            this.precedingCoordinate.Add(r.Id, precedingPointId);
            this.precedingCoordinateDistance.Add(r.Id, r.coordinates[precedingPointId].DistanceTo(Location));
		}

        #endregion

        #region Overridden Methods

        public override bool Equals(object obj)
        {
            if (obj == null)
                return false;

            Stop s = obj as Stop;
            if (s == null)
                return false;

            return this.Id == s.Id;
        }

        public override int GetHashCode()
        {
            return this.Id.GetHashCode();
        }

        #endregion
    }
}
