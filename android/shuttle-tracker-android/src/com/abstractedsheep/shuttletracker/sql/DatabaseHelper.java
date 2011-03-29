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

import com.abstractedsheep.shuttletracker.json.RoutesJson;
import com.abstractedsheep.shuttletracker.json.RoutesJson.Route;
import com.abstractedsheep.shuttletracker.json.RoutesJson.Route.Coord;
import com.abstractedsheep.shuttletracker.json.RoutesJson.Stop;

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
		public static final String colFavorite="favorite";
		public static final String colEta="eta";
	}
	
	public DatabaseHelper(Context context) {
		super(context, dbName, null, 1);
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
			" " + RoutesTable.colWidth + " INTEGER" +
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
				" " + StopsOnRoutesTable.colFavorite + " INTEGER," +
				" PRIMARY KEY(" + StopsOnRoutesTable.colStopId + ", " + StopsOnRoutesTable.colRouteId + ")" +
			");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS "+StopsTable.tableName);
		db.execSQL("DROP TABLE IF EXISTS "+RoutesTable.tableName);
		db.execSQL("DROP TABLE IF EXISTS "+RoutePointsTable.tableName);
		db.execSQL("DROP TABLE IF EXISTS "+StopsOnRoutesTable.tableName);
		
		onCreate(db);
	}
	
	public void putRoutes(RoutesJson routes) {
		clearRoutes();
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv;
		
		for (Route r: routes.getRoutes()) {
			cv = new ContentValues();
			cv.put(RoutesTable.colName, r.getName());
			cv.put(RoutesTable.colId, r.getId());
			cv.put(RoutesTable.colColor, r.getColor());
			cv.put(RoutesTable.colWidth, r.getWidth());
			db.insert(RoutesTable.tableName, RoutesTable.colName, cv);
			
			for (Coord c : r.getCoords()) {
				cv = new ContentValues();
				cv.put(RoutePointsTable.colRouteId, r.getId());
				cv.put(RoutePointsTable.colLat, c.getLatitude());
				cv.put(RoutePointsTable.colLon, c.getLongitude());
				db.insert(RoutePointsTable.tableName, RoutePointsTable.colLon, cv);
			}
		}
		
		for (Stop s : routes.getStops()) {
			cv = new ContentValues();
			cv.put(StopsTable.colName, s.getName());
			cv.put(StopsTable.colId, s.getShort_name());
			cv.put(StopsTable.colLat, s.getLatitude());
			cv.put(StopsTable.colLon, s.getLongitude());
			db.insert(StopsTable.tableName, StopsTable.colName, cv);
			
			for (Stop.Route r : s.getRoutes()) {
				cv = new ContentValues();
				cv.put(StopsOnRoutesTable.colStopId, s.getShort_name());
				cv.put(StopsOnRoutesTable.colRouteId, r.getId());
				cv.put(StopsOnRoutesTable.colFavorite, 0);
				db.insert(StopsOnRoutesTable.tableName, StopsOnRoutesTable.colFavorite, cv);
			}
		}
		
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
		boolean result = cur.getCount() > 0 ? true : false;
		cur.close();
		db.close();
		return result;
	}
	
	public RoutesJson getRoutes() {
		RoutesJson result = new RoutesJson();
		ArrayList<Route> routesArr = new ArrayList<RoutesJson.Route>();
		Cursor routes = getRoutesCursor();
		routes.moveToFirst();
		while (!routes.isAfterLast()) {
			Route r = new Route();
			r.setColor(routes.getString(routes.getColumnIndex(RoutesTable.colColor)));
			r.setName(routes.getString(routes.getColumnIndex(RoutesTable.colName)));
			r.setId(routes.getInt(routes.getColumnIndex(RoutesTable.colId)));
			r.setWidth(routes.getInt(routes.getColumnIndex(RoutesTable.colWidth)));
			
			ArrayList<Coord> pointsArr = new ArrayList<RoutesJson.Route.Coord>();
			Cursor points = getRoutePoints(r.getId());
			points.moveToFirst();
			while (!points.isAfterLast()) {
				Coord p = new Coord();
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
		
		ArrayList<Stop> stopsArr = new ArrayList<RoutesJson.Stop>();
		Cursor stops = getStops();
		stops.moveToFirst();
		while (!stops.isAfterLast()) {
			Stop s = new Stop();
			s.setName(stops.getString(stops.getColumnIndex(StopsTable.colName)));
			s.setShort_name(stops.getString(stops.getColumnIndex(StopsTable.colId)));
			s.setLatitude(stops.getDouble(stops.getColumnIndex(StopsTable.colLat)));
			s.setLongitude(stops.getDouble(stops.getColumnIndex(StopsTable.colLon)));
			
			ArrayList<Stop.Route> routesByStopArr = new ArrayList<RoutesJson.Stop.Route>();
			Cursor routesByStop = getRoutesByStop(s.getShort_name());
			routesByStop.moveToFirst();
			while (!routesByStop.isAfterLast()) {
				Stop.Route r = new Stop.Route();
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
		//Cursor cur = db.rawQuery("SELECT " + RoutesTable.tableName + "." + RoutesTable.colId + ", " + RoutesTable.tableName + "." + RoutesTable.colName +
		//		" FROM (" + StopsOnRoutesTable.tableName + " JOIN " + RoutesTable.tableName + " ON " +
		//		StopsOnRoutesTable.tableName + "." + StopsOnRoutesTable.colRouteId + " = " + RoutesTable.tableName + "." + RoutesTable.colId + ")" +
		//		" WHERE " + StopsOnRoutesTable.tableName + "." + StopsOnRoutesTable.colStopId + " = '" + stopId + "'", null);
		Cursor cur = db.rawQuery("SELECT id, name FROM (StopsOnRoutes JOIN Routes ON routeId = id) WHERE stopId = '" + stopId + "'", null);
		return cur;
	}
	
	private Cursor getRoutesCursor() {
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
	
	private Cursor getStops() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cur = db.rawQuery("SELECT * FROM " + StopsTable.tableName, null);
		return cur;
	}
	
	public void updateFavorites(ArrayList<Stop> favorites) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(StopsOnRoutesTable.colFavorite, 0);
		db.update(StopsOnRoutesTable.tableName, cv, null, null);
		
		for (Stop s : favorites) {
			db.execSQL("UPDATE " + StopsOnRoutesTable.tableName + " SET " + StopsOnRoutesTable.colFavorite + 
					" = 1 WHERE " + StopsOnRoutesTable.colStopId + " = '" + s.getShort_name() + "' AND " +
					StopsOnRoutesTable.colRouteId + " = " + s.getFavoriteRoute());
		}
		
		db.close();
	}
	
	public ArrayList<Stop> getFavorites() {
		ArrayList<Stop> result = new ArrayList<RoutesJson.Stop>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cur = db.rawQuery("SELECT " + StopsTable.colId + ", " + StopsTable.colName + ", " + 
				StopsTable.colLat + ", " + StopsTable.colLon + ", " + StopsOnRoutesTable.colRouteId + " FROM ( " +
				StopsOnRoutesTable.tableName + " JOIN " + StopsTable.tableName + " ON " + 
				StopsOnRoutesTable.colStopId + " = " + StopsTable.colId + ")" +
				" WHERE " + StopsOnRoutesTable.colFavorite + " = 1", null);
		
		cur.moveToFirst();
		while (!cur.isAfterLast()) {
			Stop s = new Stop();
			s.setName(cur.getString(1));
			s.setShort_name(cur.getString(0));
			s.setLatitude(cur.getDouble(2));
			s.setLongitude(cur.getDouble(3));	
			s.setFavoriteRoute(cur.getInt(4));
			result.add(s);
			cur.moveToNext();
		}
		cur.close();
		db.close();
		return result;
	}
}
