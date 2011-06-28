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
using System.Collections.ObjectModel;

namespace AbstractedSheep.ShuttleTrackerWorld
{
    public class Route
    {
        #region Global Variables

        internal List<double> distanceToNextCoord;
        internal List<Coordinate> coordinates;
        internal Dictionary<int, Shuttle> shuttles;
        internal Dictionary<string, Stop> stops;

        #endregion

        #region Properties

        public ReadOnlyCollection<double> DistanceToNextCoord { get; private set; }
        public ReadOnlyCollection<Coordinate> Coordinates { get; private set; }
        public ReadOnlyDictionary<int, Shuttle> Shuttles { get; private set; }
        public ReadOnlyDictionary<string, Stop> Stops { get; private set; }
        public string Name { get; private set; }
        public double Length { get; private set; }
        public int Id { get; private set; }

        #endregion

        #region Constructors

        internal Route(int id, string name, List<Coordinate> coords)
        {
            this.Id = id;
			this.Name = name;
            this.coordinates = new List<Coordinate>(coords);
            this.stops = new Dictionary<string, Stop>();
			this.distanceToNextCoord = new List<double>();
            this.shuttles = new Dictionary<int, Shuttle>();
            this.DistanceToNextCoord = this.distanceToNextCoord.AsReadOnly();
            this.Coordinates = this.coordinates.AsReadOnly();
            this.Shuttles = this.shuttles.AsReadOnly<int, Shuttle>();
            this.Stops = this.stops.AsReadOnly<string, Stop>();
			
			for(int i = 0; i < this.coordinates.Count; i++)
			{
				Coordinate c1, c2;
				if (i == 0)
					c1 = this.coordinates.Last();
				else
					c1 = this.coordinates[i - 1];
				
				c2 = this.coordinates[i];
				
				this.distanceToNextCoord.Add(c1.DistanceTo(c2));
			}
        }

        #endregion

        #region Overridden Methods

        public override bool Equals(object obj)
        {
            if (obj == null)
                return false;

            Route r = obj as Route;
            if (r == null)
                return false;

            return this.Id == r.Id;
        }

        public override int GetHashCode()
        {
            return this.Id;
        }

        #endregion
    }
}
