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

package com.abstractedsheep.shuttletracker.sql;

import java.util.ArrayList;
import java.util.List;

import com.abstractedsheep.shuttletracker.FavoriteStop;
import com.abstractedsheep.shuttletrackerworld.Netlink;
import com.abstractedsheep.shuttletrackerworld.Netlink.RouteJson;
import com.abstractedsheep.shuttletrackerworld.Netlink.StopJson;
import com.abstractedsheep.shuttletrackerworld.Netlink.RouteJson.RouteCoordinateJson;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	public static final String dbName="trackerDB";
	
	public static class StopsTable {
		public static final String tableName="Stops";
		public static final String colId="id";
		public static final String colName="name";
		public static final String colLat="latitude";
		public static final String colLon="longitude";
	}
	
	public static class RoutesTable {
		public static final String tableName="Routes";
		public static final String colId="id";
		public static final String colName="name";
		public static final String colColor="color";
		public static final String colWidth="width";
		public static final String colVisible="visible";
	}
	
	public static class RoutePointsTable {
		public static final String tableName="RoutePoints";
		public static final String colId="_id";
		public static final String colRouteId="routeId";
		public static final String colLat="latitude";
		public static final String colLon="longitude";
	}
	
	public static class StopsOnRoutesTable {
		public static final String tableName="StopsOnRoutes";
		public static final String colStopId="stopId";
		public static final String colRouteId="routeId";
		@Deprecated
		public static final String colFavorite="favorite";
	}
	
	public static class FavoritesTable {
		public static final String tableName="Favorites";
		public static final String colStopId="stopId";
		public static final String colRouteId="routeId";
	}
	
	public DatabaseHelper(Context context) {
		super(context, dbName, null, 3);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + StopsTable.tableName + "(" +
			" " + StopsTable.colId + " TEXT PRIMARY KEY," +
			" " + StopsTable.colName + " TEXT," +
			" " + StopsTable.colLat + " REAL," +
			" " + StopsTable.colLon + " REAL" +
		");");

		db.execSQL("CREATE TABLE " + RoutesTable.tableName + "(" +
			" " + RoutesTable.colId + " INTEGER PRIMARY KEY," +
			" " + RoutesTable.colName + " TEXT," +
			" " + RoutesTable.colColor + " TEXT," +
			" " + RoutesTable.colWidth + " INTEGER," +
			" " + RoutesTable.colVisible + " INTEGER" +
		");");

		db.execSQL("CREATE TABLE " + RoutePointsTable.tableName + "(" +
			" " + RoutePointsTable.colId + " INTEGER PRIMARY KEY," +
			" " + RoutePointsTable.colRouteId + " INTEGER," +
			" " + RoutePointsTable.colLat + " REAL," +
			" " + RoutePointsTable.colLon + " REAL" +
		");");
		
		db.execSQL("CREATE TABLE " + StopsOnRoutesTable.tableName + "(" +
				" " + StopsOnRoutesTable.colStopId + " TEXT," +
				" " + StopsOnRoutesTable.colRouteId + " INTEGER," +
				" PRIMARY KEY(" + StopsOnRoutesTable.colStopId + ", " + StopsOnRoutesTable.colRouteId + ")" +
			");");
		
		db.execSQL("CREATE TABLE " + FavoritesTable.tableName + "(" +
				" " + FavoritesTable.colStopId + " TEXT," +
				" " + FavoritesTable.colRouteId + " INTEGER," +
				" PRIMARY KEY(" + FavoritesTable.colStopId + ", " + FavoritesTable.colRouteId + ")" +
			");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion <= 1) {
			db.execSQL("ALTER TABLE "+RoutesTable.tableName + " ADD " + RoutesTable.colVisible + " INTEGER");
			ContentValues cv = new ContentValues();
			cv.put(RoutesTable.colVisible, 1);
			db.update(RoutesTable.tableName, cv, null, null);
			
			cv = new ContentValues();
			cv.put(RoutesTable.colName, "East Campus");
			db.update(RoutesTable.tableName, cv, RoutesTable.colName + "='East Route'", null);
			
			cv = new ContentValues();
			cv.put(RoutesTable.colName, "West Campus");
			db.update(RoutesTable.tableName, cv, RoutesTable.colName + "='West Route'", null);
		}
		
		if (oldVersion <= 2) {
			// Copy favorites into separate table
			db.execSQL("CREATE TABLE " + FavoritesTable.tableName + "(" +
					" " + FavoritesTable.colStopId + " TEXT," +
					" " + FavoritesTable.colRouteId + " INTEGER," +
					" PRIMARY KEY(" + FavoritesTable.colStopId + ", " + FavoritesTable.colRouteId + ")" +
				");"); 
			db.execSQL("INSERT INTO " + FavoritesTable.tableName + " (" + FavoritesTable.colStopId + ", " + FavoritesTable.colRouteId + ")" + 
					" SELECT " + StopsOnRoutesTable.colStopId + ", " + StopsOnRoutesTable.colRouteId + " FROM " + StopsOnRoutesTable.tableName + 
					" WHERE " + StopsOnRoutesTable.colFavorite + "=1");
			
			// Remove the favorites column
			db.execSQL("ALTER TABLE " + StopsOnRoutesTable.tableName + " RENAME TO OldStopsOnRoutes");
			db.execSQL("CREATE TABLE " + StopsOnRoutesTable.tableName + "(" +
					" " + StopsOnRoutesTable.colStopId + " TEXT," +
					" " + StopsOnRoutesTable.colRouteId + " INTEGER," +
					" PRIMARY KEY(" + StopsOnRoutesTable.colStopId + ", " + StopsOnRoutesTable.colRouteId + ")" +
				");");
			db.execSQL("INSERT INTO " + StopsOnRoutesTable.tableName + " SELECT " + StopsOnRoutesTable.colStopId + ", " + StopsOnRoutesTable.colRouteId + " FROM OldStopsOnRoutes");
		}
	}
	
	public void putRoutes(Netlink routes) {
		clearRoutes();
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv;
		
		for (RouteJson r: routes.getRoutes()) {
			cv = new ContentValues();
			cv.put(RoutesTable.colName, r.getName());
			cv.put(RoutesTable.colId, r.getId());
			cv.put(RoutesTable.colColor, r.getColor());
			cv.put(RoutesTable.colWidth, r.getWidth());
			cv.put(RoutesTable.colVisible, r.getVisible() ? 1 : 0);
			db.insert(RoutesTable.tableName, RoutesTable.colName, cv);
			
			for (RouteCoordinateJson c : r.getCoords()) {
				cv = new ContentValues();
				cv.put(RoutePointsTable.colRouteId, r.getId());
				cv.put(RoutePointsTable.colLat, c.getLatitude());
				cv.put(RoutePointsTable.colLon, c.getLongitude());
				db.insert(RoutePointsTable.tableName, RoutePointsTable.colLon, cv);
			}
		}
		
		for (StopJson s : routes.getStops()) {
			cv = new ContentValues();
			cv.put(StopsTable.colName, s.getName());
			cv.put(StopsTable.colId, s.getShort_name());
			cv.put(StopsTable.colLat, s.getLatitude());
			cv.put(StopsTable.colLon, s.getLongitude());
			db.insert(StopsTable.tableName, StopsTable.colName, cv);
			
			for (StopJson.StopRouteJson r : s.getRoutes()) {
				cv = new ContentValues();
				cv.put(StopsOnRoutesTable.colStopId, s.getShort_name());
				cv.put(StopsOnRoutesTable.colRouteId, r.getId());
				db.insert(StopsOnRoutesTable.tableName, null, cv);
			}
		}
		
		db.close();
	}
	
	public void setRouteVisibility(int routeId, boolean visible) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(RoutesTable.colVisible, visible ? 1 : 0);
		db.update(RoutesTable.tableName, cv, RoutesTable.colId + "=" + routeId, null);
		db.close();
	}
	
	public void clearRoutes() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(RoutesTable.tableName, null, null);
		db.delete(RoutePointsTable.tableName, null, null);
		db.delete(StopsTable.tableName, null, null);
		db.delete(StopsOnRoutesTable.tableName, null, null);
		db.close();
	}
	
	
	public boolean hasRoutes() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cur = db.rawQuery("SELECT * FROM " + RoutesTable.tableName, null);
		boolean result = cur.getCount() > 0;
		cur.close();
		db.close();
		return result;
	}
	
	public Netlink getRoutes() {
		Netlink result = new Netlink();
		ArrayList<RouteJson> routesArr = new ArrayList<Netlink.RouteJson>();
		Cursor routes = getRoutesCursor();
		routes.moveToFirst();
		while (!routes.isAfterLast()) {
			RouteJson r = new RouteJson();
			r.setColor(routes.getString(routes.getColumnIndex(RoutesTable.colColor)));
			r.setName(routes.getString(routes.getColumnIndex(RoutesTable.colName)));
			r.setId(routes.getInt(routes.getColumnIndex(RoutesTable.colId)));
			r.setWidth(routes.getInt(routes.getColumnIndex(RoutesTable.colWidth)));
			r.setVisible(routes.getInt(routes.getColumnIndex(RoutesTable.colVisible)) != 0);
			
			ArrayList<RouteCoordinateJson> pointsArr = new ArrayList<Netlink.RouteJson.RouteCoordinateJson>();
			Cursor points = getRoutePoints(r.getId());
			points.moveToFirst();
			while (!points.isAfterLast()) {
				RouteCoordinateJson p = new RouteCoordinateJson();
				p.setLatitude(points.getDouble(points.getColumnIndex(RoutePointsTable.colLat)));
				p.setLongitude(points.getDouble(points.getColumnIndex(RoutePointsTable.colLon)));
				pointsArr.add(p);
				points.moveToNext();
			}
			points.close();
			
			r.setCoords(pointsArr);
			
			routesArr.add(r);
			routes.moveToNext();
		}
		routes.close();
		
		ArrayList<StopJson> stopsArr = new ArrayList<Netlink.StopJson>();
		Cursor stops = getStops();
		stops.moveToFirst();
		while (!stops.isAfterLast()) {
			StopJson s = new StopJson();
			s.setName(stops.getString(stops.getColumnIndex(StopsTable.colName)));
			s.setShort_name(stops.getString(stops.getColumnIndex(StopsTable.colId)));
			s.setLatitude(stops.getDouble(stops.getColumnIndex(StopsTable.colLat)));
			s.setLongitude(stops.getDouble(stops.getColumnIndex(StopsTable.colLon)));
			
			ArrayList<StopJson.StopRouteJson> routesByStopArr = new ArrayList<Netlink.StopJson.StopRouteJson>();
			Cursor routesByStop = getRoutesByStop(s.getShort_name());
			routesByStop.moveToFirst();
			while (!routesByStop.isAfterLast()) {
				StopJson.StopRouteJson r = new StopJson.StopRouteJson();
				r.setId(routesByStop.getInt(0));
				r.setName(routesByStop.getString(1));
				routesByStopArr.add(r);
				routesByStop.moveToNext();
			}
			routesByStop.close();
			
			s.setRoutes(routesByStopArr);
			
			stopsArr.add(s);
			stops.moveToNext();
		}
		stops.close();
		
		result.setRoutes(routesArr);
		result.setStops(stopsArr);
		
		return result;
	}
	
	private Cursor getRoutesByStop(String stopId) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cur = db.rawQuery("SELECT " + RoutesTable.colId + ", " + RoutesTable.colName +
				" FROM (" + StopsOnRoutesTable.tableName + " JOIN " + RoutesTable.tableName + " ON " +
				StopsOnRoutesTable.colRouteId + " = " + RoutesTable.colId + ") WHERE " + StopsOnRoutesTable.colStopId + "='" + stopId + "'", null);
		return cur;
	}
	
	public Cursor getRoutesCursor() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cur = db.rawQuery("SELECT * FROM " + RoutesTable.tableName, null);
		return cur;
	}
	
	private Cursor getRoutePoints(int routeId) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cur = db.rawQuery("SELECT * FROM " + RoutePointsTable.tableName +
				" WHERE " + RoutePointsTable.colRouteId + " = " + routeId, null);
		return cur;
	}
	
	public Cursor getStops() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cur = db.rawQuery("SELECT * FROM " + StopsTable.tableName, null);
		return cur;
	}
	
	public void updateFavorites(List<FavoriteStop> favorites) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		db.execSQL("DELETE FROM " + FavoritesTable.tableName);
		
		for (Stop s : favorites) {
			ContentValues cv = new ContentValues();
			cv.put(FavoritesTable.colRouteId, s.routeId);
			cv.put(FavoritesTable.colStopId, s.stopId);
			db.insert(FavoritesTable.tableName, null, cv);
		}
		
		db.close();
	}
	
	public List<FavoriteStop> getFavorites() {
		List<FavoriteStop> result = new ArrayList<FavoriteStop>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cur = db.rawQuery("SELECT " + StopsTable.colId + ", " + StopsTable.colName + ", " + 
				StopsTable.colLat + ", " + StopsTable.colLon + ", " + FavoritesTable.colRouteId + " FROM ( " +
				FavoritesTable.tableName + " JOIN " + StopsTable.tableName + " ON " + 
				FavoritesTable.colStopId + " = " + StopsTable.colId + ")", null);
		
		cur.moveToFirst();
		while (!cur.isAfterLast()) {
			result.add(new FavoriteStop(cur.getInt(4), cur.getString(0)));
			cur.moveToNext();
		}
		cur.close();
		db.close();
		return result;
	}
	
	public boolean isRouteVisible(int routeId) {
		boolean result = false;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cur = db.rawQuery("SELECT * FROM " + RoutesTable.tableName +
				" WHERE " + RoutesTable.colId + "=" + routeId +
				" AND " + RoutesTable.colVisible + "=1", null);
		if (cur.getCount() > 0) {
			result = true;
		}
		
		cur.close();
		db.close();
		return result;
	}
	
	public boolean isStopFavorite(String stopId, int routeId) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cur = db.rawQuery("SELECT * " +
				" FROM " + FavoritesTable.tableName +
				" WHERE stopId='" + stopId + "' AND routeId=" + routeId, null);
		boolean result = cur.getCount() > 0;
		cur.close();
		db.close();
		return result;
	}
	
	public void setStopFavorite(String stopId, int routeId, boolean favorite) { 
		SQLiteDatabase db = this.getWritableDatabase();
		if (favorite) {
			ContentValues cv = new ContentValues();
			cv.put(FavoritesTable.colStopId, stopId);
			cv.put(FavoritesTable.colRouteId, routeId);
			db.insert(FavoritesTable.tableName, null, cv);
		} else {
			db.delete(FavoritesTable.tableName, FavoritesTable.colRouteId + "=" + routeId + " AND " + FavoritesTable.colStopId + "=" + stopId, null);
		}
		db.close();
	}
}
