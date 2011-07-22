/*
 * Copyright 2011
 *
 *   This file is part of Mobile Shuttle Tracker.
 *
 *   Mobile Shuttle Tracker is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Mobile Shuttle Tracker is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Mobile Shuttle Tracker.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.abstractedsheep.shuttletrackerworld;

    public class Point3D
    {
    	private double x;
		private double y;
    	private double z;

        public Point3D (double x, double y, double z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}

		public double getZ() {
			return z;
		}

        public Point3D subtract(Point3D p)
        {
	        return new Point3D(this.x - p.x, this.y - p.y, this.z - p.z);
        }

        public Point3D add(Point3D p)
        {
	        return new Point3D(this.x + p.x, this.y + p.y, this.z + p.z);
        }

        public Point3D divide(double d)
        {
	        return new Point3D(this.x / d, this.y / d, this.z / d);
        }

        public Point3D multiply(double d)
        {
	        return new Point3D(this.x * d, this.y * d, this.z * d);
        }

        public Point3D crossProduct(Point3D p)
        {
            return new Point3D(
                this.y * p.z - this.z * p.y,
                this.z * p.x - this.x * p.z,
                this.x * p.y - this.y * p.x);
        }

        public double distanceTo(Point3D p)
        {
	        return Math.sqrt(Math.pow(p.x - this.x, 2) + Math.pow(p.y - this.y, 2) + Math.pow(p.z - this.z, 2));
        }

        public double magnitude()
        {
	        return Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2) + Math.pow(this.z, 2));
        }

        public Point3D moveTowards(Point3D p, double distance)
        {
	        Point3D directionVector = p.subtract(this);
	        directionVector = directionVector.divide(directionVector.magnitude());

	        return directionVector.multiply(distance).add(this);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
                return false;

            try {
            	Point3D p = (Point3D)obj;
            	return this.x == p.x && this.y == p.y && this.z == p.z;
            } catch (ClassCastException e) {
            	return false;
            }
        }

        @Override
        public int hashCode()
        {
            return ((Double)this.x).hashCode() ^ ((Double)this.y).hashCode() ^
            		((Double)this.z).hashCode();
        }

        @Override
        public String toString()
        {
            return "(" + this.x + ", " + this.y + ", " + this.z + ")";
        }

}

