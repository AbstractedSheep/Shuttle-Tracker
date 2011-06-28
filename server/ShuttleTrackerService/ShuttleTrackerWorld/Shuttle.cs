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

namespace AbstractedSheep.ShuttleTrackerWorld
{
    public class Shuttle
    {
        #region Global Variables

        private int speed;
        private List<int> pastSpeeds;

        #endregion

        #region Properties

        public int NextRouteCoordinate { get; private set; }
        public int Bearing { get; internal set; }
        public string CardinalPoint { get; internal set; }
        public int Id { get; internal set; }
        public Coordinate Location { get; internal set; }
        public long LastUpdateTime { get; internal set; }
        public string Name { get; internal set; }
        public int AverageSpeed { get; private set; }
        public Route CurrentRoute { get; internal set; }
        public Coordinate SnappedCoordinate { get; private set; }
        public int Speed
        {
            get { return this.speed; }
            internal set
            {
                this.speed = value;
                if (this.pastSpeeds.Count == 10)
                    this.pastSpeeds.RemoveAt(0);

                this.pastSpeeds.Add(value);

                this.AverageSpeed = this.pastSpeeds.Sum() / this.pastSpeeds.Count;
            }
        }

        #endregion

        #region Constructors

        internal Shuttle()
        {
            this.Bearing = 0;
            this.CardinalPoint = "";
            this.Id = -1;
            this.Location = null;
            this.LastUpdateTime = -1;
            this.Name = "";
            this.speed = -1;
            this.pastSpeeds = new List<int>(10);
            this.AverageSpeed = -1;
        }

        #endregion

        #region Internal Methods

        internal void SnapToRoute()
        {
            if (this.CurrentRoute != null && this.Location != null)
            {
                Coordinate c1, c2;
                Coordinate closestPoint = null, tempClosestPoint;
                int nextPointId = -1;
                double shortestDistance = 10000, tempShortestDistance = 10000;

                for (int i = 0; i < this.CurrentRoute.coordinates.Count; i++)
                {
                    if (i == 0)
                        c1 = this.CurrentRoute.coordinates.Last();
                    else
                        c1 = this.CurrentRoute.coordinates[i - 1];

                    c2 = this.CurrentRoute.coordinates[i];

                    tempClosestPoint = this.Location.ClosestPoint(c1, c2);
                    tempShortestDistance = tempClosestPoint.DistanceTo(this.Location);

                    if (tempShortestDistance < shortestDistance)
                    {
                        shortestDistance = tempShortestDistance;
                        closestPoint = tempClosestPoint;
                        nextPointId = i + 1 == this.CurrentRoute.coordinates.Count ? 0 : i + 1;
                    }
                }

                this.SnappedCoordinate = closestPoint;
                this.NextRouteCoordinate = nextPointId;
            }
        }

        #endregion

        #region Overridden Methods

        public override bool Equals(object obj)
        {
            if (obj == null)
                return false;

            Shuttle s = obj as Shuttle;
            if (s == null)
                return false;

            return this.Id == s.Id;
        }

        public override int GetHashCode()
        {
            return this.Id;
        }

        #endregion
    }
}
