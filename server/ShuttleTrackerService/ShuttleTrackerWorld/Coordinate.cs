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
    /// <summary>
    /// Represents an immutable Latitude, Longitude pair on Earth.
    /// </summary>
    public class Coordinate
    {
        #region Global Variables

        private readonly int latitudeE6;
        private readonly int longitudeE6;
        private readonly double latitude;
        private readonly double longitude;

        #endregion

        #region Properties

        public int LatitudeE6
        {
            get { return this.latitudeE6; }
        }

        public int LongitudeE6
        {
            get { return this.longitudeE6; }
        }

        #endregion

        #region Constructors

        public Coordinate(int latitudeE6 = 0, int longitudeE6 = 0)
        {
            this.latitudeE6 = latitudeE6;
            this.latitude = latitudeE6 / 1E6;
            this.longitudeE6 = longitudeE6;
            this.longitude = longitudeE6 / 1E6;
        }

        #endregion

        #region Operators

        public static bool operator==(Coordinate c1, Coordinate c2)
        {
            return c1.latitudeE6 == c2.latitudeE6 && c1.longitudeE6 == c2.longitudeE6;
        }

        public static bool operator !=(Coordinate c1, Coordinate c2)
        {
            return !(c1 == c2);
        }

        #endregion

        #region Public Methods

        /// <summary>
        /// Calculates the distance in miles to another Coordinate
        /// </summary>
        /// <param name="c">The Coordinate to measure the distance to.</param>
        /// <returns>The distance in miles from this Coordinate to the given Coordinate.</returns>
        public double DistanceTo(Coordinate c)
        {
	        double earthRadius = 3956; // Miles
	        double dlong = DegToRad(c.longitude - this.longitude);
	        double dlat = DegToRad(c.latitude - this.latitude);
            double lat1 = DegToRad(this.latitude);
            double lat2 = DegToRad(c.latitude);

	        double a = Math.Pow(Math.Sin(dlat / 2.0), 2) +
                    Math.Pow(Math.Sin(dlong / 2.0), 2) * Math.Cos(lat1) * Math.Cos(lat2);
	        double b = 2 * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1.0 - a));
	        return earthRadius * b;
        }

        /// <summary>
        /// Calculates the bearing towards another Coordinate. 
        /// </summary>
        /// <param name="c">The Coordinate to get the bearing towards.</param>
        /// <returns>The bearing (in degrees from North) from this Coordinate to the given Coordinate.</returns>
        public double BearingTowards(Coordinate c)
        {
	        double dlong = this.longitude - c.longitude;
	        double y = Math.Sin(dlong) * Math.Cos(c.latitude);
	        double x = Math.Cos(this.latitude) * Math.Sin(c.latitude) -
		        Math.Sin(this.latitude) * Math.Cos(c.latitude) * Math.Cos(dlong);
	        return RadToDeg(Math.Atan2(x, y));
        }

        /// <summary>
        /// Calculates the closest Coordinate to a line created by two other Coordinates. The resulting Coordinate is not garunteed
        /// to fall between the two endpoint Coordinates.
        /// </summary>
        /// <param name="endpoint1">A Coordinate on the line.</param>
        /// <param name="endpoint2">Another Coordinate on the line.</param>
        /// <returns>The closest Coordinate on the line to this Coordinate.</returns>
        public Coordinate ClosestPoint(Coordinate endpoint1, Coordinate endpoint2)
        {	
	        double R = 3956;
	        Point3D ep1cart = new Point3D(
		        R * Math.Cos(DegToRad(endpoint1.latitude)) * Math.Cos(DegToRad(endpoint1.longitude)),
		        R * Math.Cos(DegToRad(endpoint1.latitude)) * Math.Sin(DegToRad(endpoint1.longitude)),
		        R * Math.Sin(DegToRad(endpoint1.latitude)));
	        Point3D ep2cart = new Point3D(
		        R * Math.Cos(DegToRad(endpoint2.latitude)) * Math.Cos(DegToRad(endpoint2.longitude)),
		        R * Math.Cos(DegToRad(endpoint2.latitude)) * Math.Sin(DegToRad(endpoint2.longitude)),
		        R * Math.Sin(DegToRad(endpoint2.latitude)));
	        Point3D ptcart = new Point3D(
		        R * Math.Cos(DegToRad(this.latitude)) * Math.Cos(DegToRad(this.longitude)),
		        R * Math.Cos(DegToRad(this.latitude)) * Math.Sin(DegToRad(this.longitude)),
		        R * Math.Sin(DegToRad(this.latitude)));

	        Point3D origin = new Point3D(0, 0, 0);

	        double d = ((ptcart - ep1cart).CrossProduct(ptcart - ep2cart)).Magnitude() / (ep2cart - ep1cart).Magnitude();
	        double hypotenuse = ptcart.DistanceTo(ep1cart);
	        double theta = Math.Asin(d/hypotenuse);
	        double adjacent = d / Math.Tan(theta);

	        Point3D closestcart = ep1cart.MoveTowards(ep2cart, adjacent);
	        Point3D surfacecart = origin.MoveTowards(closestcart, R);

	        return new Coordinate(
		        (int)(RadToDeg(Math.Asin(surfacecart.Z / R)) * 1E6), 
		        (int)(RadToDeg(Math.Atan2(surfacecart.Y, surfacecart.X)) * 1E6));

        }

        #endregion

        #region Private Methods

        private double DegToRad(double degrees) 
        {
	        return degrees / 180.0 * Math.PI;
        }

        private double RadToDeg(double rad)
        {
	        return rad * 180.0 / Math.PI;
        }

        #endregion

        #region Overridden Methods

        public override string ToString()
        {
            return "(" + (this.latitudeE6 / 1E6) + ", " + (this.longitudeE6 / 1E6) + ")";
        }

        public override bool Equals(object obj)
        {
            if (obj == null)
                return false;

            Coordinate c = obj as Coordinate;
            if (c == null)
                return false;

            return this == c;
        }

        public override int GetHashCode()
        {
            return this.latitudeE6 ^ this.longitudeE6;
        }

        #endregion
    }
}
