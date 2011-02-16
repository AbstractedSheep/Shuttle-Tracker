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
	private Vehicle vehicle;
	
	
	public static class Vehicle {
		private int id;
		private String name;
		private Latest_Position latest_position;
		private Icon icon;
		
		public static class Latest_Position {
			private int heading;
			private double latitude;
			private double longitude;
			private int speed;
			private String timestamp;
			private String public_status_msg;
			private String cardinal_point;
			
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
			public String getTimestamp() {
				return timestamp;
			}
			public void setTimestamp(String timestamp) {
				this.timestamp = timestamp;
			}
			public String getPublic_status_msg() {
				return public_status_msg;
			}
			public void setPublic_status_msg(String public_status_msg) {
				this.public_status_msg = public_status_msg;
			}
			public String getCardinal_point() {
				return cardinal_point;
			}
			public void setCardinal_point(String cardinal_point) {
				this.cardinal_point = cardinal_point;
			}
		}
	
		public static class Icon {
			private int id;
		
			public int getId() {
				return id;
			}
			public void setId(int id) {
				this.id = id;
			}
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public Latest_Position getLatest_position() {
			return latest_position;
		}
		public void setLatest_position(Latest_Position latest_position) {
			this.latest_position = latest_position;
		}
		public Icon getIcon() {
			return icon;
		}
		public void setIcon(Icon icon) {
			this.icon = icon;
		}
	}
	
	public DirectionalOverlayItem toOverlayItem() {
		return new DirectionalOverlayItem(new GeoPoint((int)(vehicle.latest_position.latitude * 1e6), (int)(vehicle.latest_position.longitude * 1e6)), vehicle.latest_position.heading, vehicle.name, "");
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}
}
