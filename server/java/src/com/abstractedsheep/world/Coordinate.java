package com.abstractedsheep.world;

public class Coordinate {
	private double latitude;
	private double longitude;
	private static final double RADIUS_OF_EARTH = 3956;

	public Coordinate() {
		this.latitude = 0.0;
		this.longitude = 0.0;
	}

	/**
	 * @param latitude
	 *            - latitude in degrees (double value)
	 * @param longitude
	 *            - longitude in degrees (double value)
	 */
	public Coordinate(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude
	 *            the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude
	 *            the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public static boolean sameCoordinates(Coordinate c1, Coordinate c2) {
		return false;
	}

	public double distanceFromCoordiante(Coordinate c) {

		double dLong = Math.toRadians((c.getLongitude() - this.getLongitude()));
		double dLat = Math.toRadians((c.getLatitude() - this.getLatitude()));
		double lat1 = Math.toRadians(this.getLatitude());
		double lat2 = Math.toRadians(c.getLatitude());

		double a = Math.pow(Math.sin(dLat / 2.0), 2)
				+ Math.pow(Math.sin(dLong / 2.0), 2) * Math.cos(lat1)
				* Math.cos(lat2);
		double b = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

		return RADIUS_OF_EARTH * b;
	}
	
	/**
	 * This class really has no use except for in the DynamicDataGenerator
	 * class
	 * @param distance - distance from current point
	 * @param endPoint - end point of line
	 * @return the coordinate point some distance away from the current coordinate
	 */
	public Coordinate findCoordinateInLine(double distance, Coordinate endPoint) {
		distance = distance / RADIUS_OF_EARTH;
		double bearing = this.getBearing(endPoint);
		double lat2 = Math.asin( Math.sin(this.getLatitude()) * Math.cos(distance)
					  + Math.cos(this.getLatitude()) * Math.sin(distance) * Math.cos(bearing));
		double lon2 = this.getLongitude() +
					Math.atan2( Math.sin(bearing) * Math.sin(distance) * Math.cos(this.getLatitude()),
								Math.cos(distance) - Math.sin(this.getLatitude()) * Math.sin(lat2));
		
		return new Coordinate(Math.toDegrees(lat2), Math.toDegrees(lon2));
	}

	public double getBearing(Coordinate c) {
		double delta = Math.toRadians((this.getLongitude() - c.getLongitude()));
		double lat1 = Math.toRadians(c.getLatitude());
		double lat2 = Math.toRadians(this.getLatitude());

		double y = Math.sin(delta) * Math.cos(lat1);
		double x = Math.cos(lat2) * Math.sin(lat1) - Math.sin(lat2)
				* Math.cos(lat1) * Math.cos(delta);

		return Math.toDegrees(Math.atan2(y, x));
	}

	/**
	 * Calculates the closet coordinate point in the line constructed by the
	 * below paramaters
	 * 
	 * @param endPoint1
	 *            - one end point defining the line
	 * @param endPoint2
	 *            - another end point defining the line
	 * @return the closest coordinate point between the two given points.
	 */
	public Coordinate closestPoint(Coordinate endPoint1, Coordinate endPoint2) {
		final int R = 3956;

		Point3D pt1 = new Point3D(R
				* Math.cos(Math.toRadians(endPoint1.getLatitude()))
				* Math.cos(Math.toRadians(endPoint1.getLongitude())), R
				* Math.cos(Math.toRadians(endPoint1.getLatitude()))
				* Math.sin(Math.toRadians(endPoint1.getLongitude())), R
				* Math.sin(Math.toRadians(endPoint1.getLatitude())));
		Point3D pt2 = new Point3D(R
				* Math.cos(Math.toRadians(endPoint2.getLatitude()))
				* Math.cos(Math.toRadians(endPoint2.getLongitude())), R
				* Math.cos(Math.toRadians(endPoint2.getLatitude()))
				* Math.sin(Math.toRadians(endPoint2.getLongitude())), R
				* Math.sin(Math.toRadians(endPoint2.getLatitude())));
		Point3D pt3 = new Point3D(R
				* Math.cos(Math.toRadians(this.getLatitude()))
				* Math.cos(Math.toRadians(this.getLongitude())), R
				* Math.cos(Math.toRadians(this.getLatitude()))
				* Math.sin(Math.toRadians(this.getLongitude())), R
				* Math.sin(Math.toRadians(this.getLatitude())));

		Point3D origin = new Point3D();

		Point3D delta1 = Point3D.subtract(pt3, pt2);
		Point3D delta2 = Point3D.subtract(pt3, pt1);
		Point3D delta3 = Point3D.subtract(pt2, pt1);

		double d = (delta1.crossProduct(delta2)).getMagnitude()
				/ delta3.getMagnitude();
		double hypothenuse = pt3.DistanceTo(pt1);
		double theta = Math.asin(d / hypothenuse);
		double adj = (d / Math.tan(theta));

		Point3D closestPt = pt1.moveTowards(pt2, adj);
		Point3D surfacePt = origin.moveTowards(closestPt, R);

		return new Coordinate(
				(Math.toDegrees((Math.asin(surfacePt.getZ() / R)))),
				(Math.toDegrees((Math.atan2(surfacePt.getY(), surfacePt.getX())))));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof Coordinate))
			return false;
		Coordinate c = (Coordinate) obj;
		return (c.getLatitude() == this.getLatitude())
				&& (c.getLongitude() == this.getLongitude());
	}

}
