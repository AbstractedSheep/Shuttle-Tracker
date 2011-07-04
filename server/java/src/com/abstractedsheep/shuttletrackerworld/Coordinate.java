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


package com.abstractedsheep.shuttletrackerworld;

public class Coordinate
{

    private final int latitudeE6;
    private final int longitudeE6;
    private final double latitude;
    private final double longitude;

    public int getLatitudeE6()
    {
    	return this.latitudeE6;
    }

    public int getLongitudeE6()
    {
        return this.longitudeE6;
    }

    public Coordinate(int latitudeE6, int longitudeE6)
    {
        this.latitudeE6 = latitudeE6;
        this.latitude = latitudeE6 / 1E6;
        this.longitudeE6 = longitudeE6;
        this.longitude = longitudeE6 / 1E6;
    }
    
    public double distanceTo(Coordinate c)
    {
        double earthRadius = 3956; // Miles
        double dlong = degToRad(c.longitude - this.longitude);
        double dlat = degToRad(c.latitude - this.latitude);
        double lat1 = degToRad(this.latitude);
        double lat2 = degToRad(c.latitude);

        double a = Math.pow(Math.sin(dlat / 2.0), 2) +
                Math.pow(Math.sin(dlong / 2.0), 2) * Math.cos(lat1) * Math.cos(lat2);
        double b = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return earthRadius * b;
    }

   
    public double bearingTowards(Coordinate c)
    {
        double dlong = this.longitude - c.longitude;
        double y = Math.sin(dlong) * Math.cos(c.latitude);
        double x = Math.cos(this.latitude) * Math.sin(c.latitude) -
	        Math.sin(this.latitude) * Math.cos(c.latitude) * Math.cos(dlong);
        return radToDeg(Math.atan2(x, y));
    }

    public Coordinate closestPoint(Coordinate endpoint1, Coordinate endpoint2)
    {	
        double R = 3956;
        Point3D ep1cart = new Point3D(
	        R * Math.cos(degToRad(endpoint1.latitude)) * Math.cos(degToRad(endpoint1.longitude)),
	        R * Math.cos(degToRad(endpoint1.latitude)) * Math.sin(degToRad(endpoint1.longitude)),
	        R * Math.sin(degToRad(endpoint1.latitude)));
        Point3D ep2cart = new Point3D(
	        R * Math.cos(degToRad(endpoint2.latitude)) * Math.cos(degToRad(endpoint2.longitude)),
	        R * Math.cos(degToRad(endpoint2.latitude)) * Math.sin(degToRad(endpoint2.longitude)),
	        R * Math.sin(degToRad(endpoint2.latitude)));
        Point3D ptcart = new Point3D(
	        R * Math.cos(degToRad(this.latitude)) * Math.cos(degToRad(this.longitude)),
	        R * Math.cos(degToRad(this.latitude)) * Math.sin(degToRad(this.longitude)),
	        R * Math.sin(degToRad(this.latitude)));

        Point3D origin = new Point3D(0, 0, 0);

        double d = ptcart.subtract(ep1cart).crossProduct(ptcart.subtract(ep2cart)).magnitude() / ep2cart.subtract(ep1cart).magnitude();
        double hypotenuse = ptcart.distanceTo(ep1cart);
        double theta = Math.asin(d / hypotenuse);
        double adjacent = d / Math.tan(theta);

        Point3D closestcart = ep1cart.moveTowards(ep2cart, adjacent);
        Point3D surfacecart = origin.moveTowards(closestcart, R);

        return new Coordinate(
	        (int)(radToDeg(Math.asin(surfacecart.getZ() / R)) * 1E6), 
	        (int)(radToDeg(Math.atan2(surfacecart.getY(), surfacecart.getX())) * 1E6));

    }

    private double degToRad(double degrees) 
    {
        return degrees / 180.0 * Math.PI;
    }

    private double radToDeg(double rad)
    {
        return rad * 180.0 / Math.PI;
    }

    @Override
    public String toString()
    {
        return "(" + (this.latitudeE6 / 1E6) + ", " + (this.longitudeE6 / 1E6) + ")";
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;

        try { 
        	Coordinate c = (Coordinate) obj; 
        	return this.latitudeE6 == c.latitudeE6 && this.longitudeE6 == c.longitudeE6;
        } catch (ClassCastException e) {
        	return false;
        }  
    }

    @Override
    public int hashCode()
    {
        return this.latitudeE6 ^ this.longitudeE6;
    }
}
