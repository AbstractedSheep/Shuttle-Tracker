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

import com.abstractedsheep.shuttletracker.mapoverlay.DirectionalOverlayItem;
import com.abstractedsheep.shuttletracker.json.MapJsonInputToClass;
import com.abstractedsheep.shuttletracker.json.MapJsonInputToClass2;
import com.google.android.maps.GeoPoint;


public class VehicleJson {
	private int id = 0;
	private String name = "";
	private MapJsonInputToClass latest_position = new MapJsonInputToClass();
	
	public int getRoute_id() {
		return 0;
	}
	public void setRoute_id(int route_id) {

	}
	
	public int getHeading() {
		return latest_position.heading;
	}
	public void setHeading(int heading) {
		this.latest_position.heading = heading;
	}
	public double getLatitude() {
		return Double.parseDouble(latest_position.latitude);
	}
	public void setLatitude(double latitude) {
		this.latest_position.latitude = Double.toString(latitude);
	}
	public double getLongitude() {
		return Double.parseDouble(latest_position.longitude);
	}
	public void setLongitude(double longitude) {
		this.latest_position.longitude = Double.toString(longitude);
	}
	public int getSpeed() {
		return latest_position.speed;
	}
	public void setSpeed(int speed) {
		this.latest_position.speed = speed;
	}
	public String getUpdate_time() {
		return latest_position.timestamp;
	}
	public void setUpdate_time(String update_time) {
		this.latest_position.timestamp = update_time;
	}
	public String getCardinal_point() {
		return latest_position.cardinal_point;
	}
	public void setCardinal_point(String cardinal_point) {
		this.latest_position.cardinal_point = cardinal_point;
	}


	public int getShuttle_id() {
		return id;
	}
	public void setShuttle_id(int shuttle_id) {
		this.id = shuttle_id;
	}
	
	public DirectionalOverlayItem toOverlayItem() {
		return new DirectionalOverlayItem(new GeoPoint((int)(Double.parseDouble(this.latest_position.latitude) * 1e6), (int)(Double.parseDouble(this.latest_position.longitude) * 1e6)), this.latest_position.heading, "", "");
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
