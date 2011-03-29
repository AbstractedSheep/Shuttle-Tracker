package com.abstractedsheep.shuttletracker.android;

import android.content.Context;
import android.database.*;
import android.database.sqlite.*;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;
public class ShuttleDBHelper extends SQLiteOpenHelper {
	
	
	private static final String DATABASE_NAME = "shuttles.db";
	private static final String STOP_TABLE = "Stop";
	private static final String STOP_ID = "id";
	private static final String STOP_LAT = "lat";
	private static final String STOP_LON = "lon";
	private static final String STOP_NAME = "name";
	private static final String ROUTE_TABLE = "Route";
	private static final String ROUTE_ID = "id";
	private static final String ROUTE_NAME = "name";
	private static final String ROUTE_COLOR = "color";
	private static final String STOP_ON_ROUTE_TABLE = "StopOnRoute";
	private static final String STOP_ON_ROUTE_STOP_ID = "stopId";
	private static final String STOP_ON_ROUTE_ROUTE_ID = "routeId";
	private static final String SHUTTLE_TABLE = "Shuttle";
	private static final String SHUTTLE_ID = "id";
	private static final String SHUTTLE_NAME = "name";
	private static final String SHUTTLE_LAT = "lat";
	private static final String SHUTTLE_LON = "lon";
	private static final String SHUTTLE_HEADING = "heading";
	private static final String ETA_TO_STOP_TABLE = "EtaToStop";
	private static final String ETA_TO_STOP_STOP_ID = "stopId";
	private static final String ETA_TO_STOP_ROUTE_ID = "routeId";
	private static final String ETA_TO_STOP_ETA = "eta";
	private static final int DATABASE_VERSION = 1;
	
	public ShuttleDBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + STOP_TABLE + " ");
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
	