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

import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Color;

import com.abstractedsheep.shuttletracker.mapoverlay.DirectionalOverlayItem;
import com.google.android.maps.GeoPoint;

public class RoutesJson {
	private ArrayList<Stop> stops;
	private ArrayList<Route> routes;
	private HashMap<Integer, Route> routesMap = new HashMap<Integer, RoutesJson.Route>();
	
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
		private boolean visible = true;
		
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

		public boolean getVisible() {
			return visible;
		}

		public void setVisible(boolean visible) {
			this.visible = visible;
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
		
		routesMap.clear();
		
		if (routes != null) {
			for (Route r : routes) {
				routesMap.put(r.id, r);
			}
		}
	}

	public HashMap<Integer, Route> getRoutesMap() {
		return routesMap;
	}
}
