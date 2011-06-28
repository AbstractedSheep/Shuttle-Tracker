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
    /// Represents an immutable point in 3D Cartesian space.
    /// </summary>
    public class Point3D
    {
        #region Properties

        public double X { get; private set; }
        public double Y { get; private set; }
        public double Z { get; private set; }

        #endregion

        #region Contructors

        public Point3D (double x = 0.0, double y = 0.0, double z = 0.0)
        {
            this.X = x;
            this.Y = y;
            this.Z = z;
        }

        #endregion

        #region Operators

        public static bool operator ==(Point3D p1, Point3D p2)
        {
            return p1.X == p2.X && p1.Y == p2.Y && p1.Z == p2.Z;
        }

        public static bool operator !=(Point3D p1, Point3D p2)
        {
            return !(p1 == p2);
        }

        public static Point3D operator -(Point3D p1, Point3D p2)
        {
	        return new Point3D(p1.X - p2.X, p1.Y - p2.Y, p1.Z - p2.Z);
        }

        public static Point3D operator +(Point3D p1, Point3D p2)
        {
	        return new Point3D(p1.X + p2.X, p1.Y + p2.Y, p1.Z + p2.Z);
        }

        public static Point3D operator /(Point3D p, double d)
        {
	        return new Point3D(p.X / d, p.Y / d, p.Z / d);
        }

        public static Point3D operator *(Point3D p, double d)
        {
	        return new Point3D(p.X * d, p.Y * d, p.Z * d);
        }

        #endregion

        #region Public Methods

        /// <summary>
        /// Calculates the cross product of two points as vectors positioned on the origin.
        /// </summary>
        /// <param name="p">The point to cross with.</param>
        /// <returns>The cross product of the points.</returns>
        public Point3D CrossProduct(Point3D p)
        {
            return new Point3D(
                this.Y * p.Z - this.Z * p.Y,
                this.Z * p.X - this.X * p.Z,
                this.X * p.Y - this.Y * p.X);
        }

        /// <summary>
        /// Calculates the Euclidian distance between this point and another point.
        /// </summary>
        /// <param name="p">The point to measure the distance to.</param>
        /// <returns>The distance between this point and point p.</returns>
        public double DistanceTo(Point3D p)
        {
	        return Math.Sqrt(Math.Pow(p.X - this.X, 2) + Math.Pow(p.Y - this.Y, 2) + Math.Pow(p.Z - this.Z, 2));
        }

        /// <summary>
        /// Calculate the magnitude of the vector formed between the origin and this point.
        /// </summary>
        /// <returns>The magnitude of this vector.</returns>
        public double Magnitude()
        {
	        return Math.Sqrt(Math.Pow(this.X, 2) + Math.Pow(this.Y, 2) + Math.Pow(this.Z, 2));
        }

        /// <summary>
        /// Calculate the result of moving this point a given distance towards another point.
        /// </summary>
        /// <param name="p">The point to move towards.</param>
        /// <param name="distance">The distance to move. It is possible to move past either endpoint.</param>
        /// <returns>A point representing the result of moving this point towards the other.</returns>
        public Point3D MoveTowards(Point3D p, double distance)
        {
	        Point3D directionVector = p - this;
	        directionVector = directionVector / directionVector.Magnitude();

	        return (directionVector * distance) + this;
        }

        #endregion

        #region Overridden Methods

        public override bool Equals(object obj)
        {
            if (obj == null)
                return false;

            Point3D p = obj as Point3D;
            if (p == null)
                return false;

            return this == p;
        }

        public override int GetHashCode()
        {
            return this.X.GetHashCode() ^ this.Y.GetHashCode() ^ this.Z.GetHashCode();
        }

        public override string ToString()
        {
            return "(" + this.X + ", " + this.Y + ", " + this.Z + ")";
        }

        #endregion
    }
}
