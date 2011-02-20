/* 
 * Copyright 2011 Austin Wagner
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
 *  
 */

package com.abstractedsheep.shuttletracker.json;

import com.abstractedsheep.shuttletracker.android.DirectionalOverlayItem;
import com.google.android.maps.GeoPoint;

public class VehicleJson {
	private int shuttle_id;
	private int route_id;
	
	
	private int heading;
	private double latitude;
	private double longitude;
	private int speed;
	private String update_time;
	private String cardinal_point;
		
	public int getRoute_id() {
		return route_id + 1;
	}
	public void setRoute_id(int route_id) {
		this.route_id = route_id;
	}
	
	public int getHeading() {
		return heading;
	}
	public void setHeading(int heading) {
		this.heading = heading;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public int getSpeed() {
		return speed;
	}
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	public String getUpdate_time() {
		return update_time;
	}
	public void setUpdate_time(String update_time) {
		this.update_time = update_time;
	}
	public String getCardinal_point() {
		return cardinal_point;
	}
	public void setCardinal_point(String cardinal_point) {
		this.cardinal_point = cardinal_point;
	}


	public int getShuttle_id() {
		return shuttle_id;
	}
	public void setShuttle_id(int shuttle_id) {
		this.shuttle_id = shuttle_id;
	}
	
	public DirectionalOverlayItem toOverlayItem() {
		return new DirectionalOverlayItem(new GeoPoint((int)(this.latitude * 1e6), (int)(this.longitude * 1e6)), this.heading, "", "");
	}

}
