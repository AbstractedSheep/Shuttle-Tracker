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

package com.abstractedsheep.world;

/**
 * This class constructs a three dimensional Cartesian point.
 * @author saiumesh
 *
 */
public class Point3D {
	private double x;
	private double y;
	private double z;
	
	public Point3D() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}
	
	/**
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param z - z coordinate
	 */
	public Point3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}
	/**
	 * @param x the x to set
	 */
	public void setX(double x) {
		this.x = x;
	}
	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}
	/**
	 * @param y the y to set
	 */
	public void setY(double y) {
		this.y = y;
	}
	/**
	 * @return the z
	 */
	public double getZ() {
		return z;
	}
	/**
	 * @param z the z to set
	 */
	public void setZ(double z) {
		this.z = z;
	}
	/*
	 * The following four methods modify the given 3d point with another 3d point
	 */
	public static Point3D add(Point3D pt, Point3D d) {
		return new Point3D ( pt.getX() + d.getX(), pt.getY() + d.getY(), pt.getZ() + d.getZ());
	}
	
	public static Point3D subtract(Point3D pt, Point3D d) {
		return new Point3D ( pt.getX() - d.getX(), pt.getY() - d.getY(), pt.getZ() - d.getZ());
	}
	
	public static Point3D multiply(Point3D pt, Point3D d) {
		return new Point3D ( pt.getX() * d.getX(), pt.getY() * d.getY(), pt.getZ() * d.getZ());
	}
	
	public static Point3D divide(Point3D pt, Point3D d) {
		return new Point3D ( pt.getX() / d.getX(), pt.getY() / d.getY(), pt.getZ() / d.getZ());
	}
	
	/*
	 * The following four methods modify the given 3d with a scalar value
	 */
	public static Point3D add(Point3D pt, double d) {
		return new Point3D ( pt.getX() + d, pt.getY() + d, pt.getZ() + d);
	}
	
	public static Point3D subtract(Point3D pt, double d) {
		return new Point3D ( pt.getX() - d, pt.getY() - d, pt.getZ() - d);
	}
	
	public static Point3D multiply(Point3D pt, double d) {
		return new Point3D ( pt.getX() * d, pt.getY() * d, pt.getZ() * d);
	}
	
	public static Point3D divide(Point3D pt, double d) {
		return new Point3D ( pt.getX() / d, pt.getY() / d, pt.getZ() / d);
	}
	
	public Point3D crossProduct(Point3D pt) {
		return new Point3D (
				getY() * pt.getZ() - getZ() * pt.getY(),
				getX() * pt.getZ() - pt.getX() * getZ(),
				getX() * pt.getY() - pt.getX() * getY());
	}
	
	public double DistanceTo (Point3D pt) {
		double dX = pt.getX() - getX();
		double dY = pt.getY() - getY();
		double dZ = pt.getZ() - getZ();
		
		double determinant = Math.pow(dX, 2) + Math.pow(dY, 2) + Math.pow(dZ, 2);
		
		return Math.sqrt(determinant);
	}
	
	public double getMagnitude() {
		double determinant = Math.pow(getX(), 2) + Math.pow(getY(), 2) + Math.pow(getZ(), 2);
		
		return Math.sqrt(determinant);
	}
	
	public Point3D moveTowards (Point3D pt, double d) {
		Point3D vector = Point3D.subtract(pt, this);
		vector = Point3D.divide(vector, vector.getMagnitude());
		
		return Point3D.add(Point3D.multiply(vector, d), this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return ((Double)this.x).hashCode() ^ ((Double)this.y).hashCode() ^
		((Double)this.z).hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
            return false;

        try {
        	Point3D p = (Point3D)obj;
        	return this.x == p.x && this.y == p.y && this.z == p.z;
        } catch (ClassCastException e) {
        	return false;
        }
	}
}
