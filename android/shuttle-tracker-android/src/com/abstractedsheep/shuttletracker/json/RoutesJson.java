package com.abstractedsheep.shuttletracker.json;

import java.util.ArrayList;

import android.graphics.Color;

import com.abstractedsheep.kml.Style;
import com.abstractedsheep.shuttletracker.android.DirectionalOverlayItem;
import com.google.android.maps.GeoPoint;

public class RoutesJson {
	private ArrayList<Stop> stops;
	private ArrayList<Route> routes;
	
	public static class Stop implements Comparable<Stop> {
		private int favoriteRoute = -1;
		private double latitude;
		
		public String getUniqueId() {
			return short_name + favoriteRoute;
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

		public String getName() {
			if (this.short_name.equals("blitman"))
				return "Blitman Commons";
			else
				return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getShort_name() {
			return short_name;
		}

		public void setShort_name(String short_name) {
			this.short_name = short_name;
		}

		public ArrayList<Route> getRoutes() {
			return routes;
		}

		public void setRoutes(ArrayList<Route> routes) {
			this.routes = routes;
		}

		private double longitude;
		private String name;
		private String short_name;
		private ArrayList<Route> routes;
		
		public static class Route {
			private int id;
			public int getId() {
				return id;
			}
			public void setId(int id) {
				this.id = id;
			}
			public String getName() {
				if (name.equals("East Campus"))
					return "East Route";
				else
					return name;
			}
			public void setName(String name) {
				this.name = name;
			}
			private String name;
		}
		
		public DirectionalOverlayItem toOverlayItem() {	
			return new DirectionalOverlayItem(new GeoPoint((int) (this.latitude * 1e6), (int)(this.longitude * 1e6)), this.name, "");
		}

		public int compareTo(Stop another) {
			if (favoriteRoute == -1)
				return name.compareTo(another.name);
			else {
				String s1 = name + favoriteRoute;
				String s2 = name + another.favoriteRoute;
				return s1.compareTo(s2);
			}
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof Stop)
				return this.short_name.equals(((Stop) o).short_name);
			else
				return super.equals(o);
		}

		public int getFavoriteRoute() {
			return favoriteRoute;
		}

		public void setFavoriteRoute(int favoriteRoute) {
			this.favoriteRoute = favoriteRoute;
		}
	}
	
	public static class Route {
		private String color;
		private int id;
		private String name;
		
		public String getColor() {
			return color;
		}
		
		public int getColorInt() {
			int[] colors = Style.hexStringToByteArray(color.substring(1));
			
			if (colors.length == 4)
				return Color.argb(colors[0], (colors[3] - 10 > 0) ? colors[3] - 10 : 0,  (colors[2] - 10 > 0) ? colors[2] - 10 : 0,  (colors[1] - 10 > 0) ? colors[1] - 10 : 0);
			else
				return Color.rgb(colors[0],  (colors[1] - 10 > 0) ? colors[1] - 10 : 0,  (colors[2] - 10 > 0) ? colors[2] - 10 : 0);
		}

		public void setColor(String color) {
			this.color = color;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			if (name.equals("East Campus"))
				return "East Route";
			else
				return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getWidth() {
			return width;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public ArrayList<Coord> getCoords() {
			return coords;
		}

		public void setCoords(ArrayList<Coord> coords) {
			this.coords = coords;
		}

		private int width;
		private ArrayList<Coord> coords;
		
		public static class Coord {
			private double latitude;
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
			private double longitude;
		}
	}

	public ArrayList<Stop> getStops() {
		return stops;
	}

	public void setStops(ArrayList<Stop> stops) {
		this.stops = stops;
	}

	public ArrayList<Route> getRoutes() {
		return routes;
	}

	public void setRoutes(ArrayList<Route> routes) {
		this.routes = routes;
	}
}
