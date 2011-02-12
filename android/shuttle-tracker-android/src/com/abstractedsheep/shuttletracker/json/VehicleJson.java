package com.abstractedsheep.shuttletracker.json;

public class VehicleJson {
	public static class Vehicle {
		private String name;
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
	}
}
