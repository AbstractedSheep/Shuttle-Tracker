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
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(z);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point3D other = (Point3D) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
			return false;
		return true;
	}
}
