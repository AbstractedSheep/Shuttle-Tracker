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

package com.abstractedsheep.shuttletracker.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.abstractedsheep.shuttletracker.json.EtaJson;
import com.abstractedsheep.shuttletracker.json.RoutesJson;
import com.abstractedsheep.shuttletracker.json.VehicleJson;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

public class VehicleItemizedOverlay extends BalloonItemizedOverlay<DirectionalOverlayItem> {

	private static final int MAGENTA = Color.rgb(255, 0, 255);
	
	private HashMap<Integer, RoutesJson.Route> routes;
	private ArrayList<VehicleJson.Vehicle> vehicles = new ArrayList<VehicleJson.Vehicle>();
	private Drawable marker;
	
	public VehicleItemizedOverlay(Drawable defaultMarker, MapView mapView) {
		super(boundCenter(defaultMarker), mapView);
		this.marker = boundCenter(defaultMarker);
	}

	public void addVehicle(VehicleJson.Vehicle vehicle) {
		synchronized (vehicles) {
			vehicles.add(vehicle);
		    populate();
		}
	}
	
	public void removeAllVehicles() {
		synchronized (vehicles) {
			vehicles.clear();
			populate();
		}
	}
	
	public void putRoutes(List<RoutesJson.Route> routeList) {
		RoutesJson.Route route;
		for (int i = 0; i < routeList.size(); i++) {
			route = routeList.get(i);
			routes.put(route.getId(), route);
		}
	}

	@Override
	protected DirectionalOverlayItem createItem(int i) {
		VehicleJson.Vehicle v = vehicles.get(i);
		GeoPoint gp = new GeoPoint((int)(v.getLatest_position().getLatitude() * 1e6), (int)(v.getLatest_position().getLongitude() * 1e6));
		return new DirectionalOverlayItem(gp, v.getLatest_position().getHeading(), v.getName(), "");
	}

	@Override
	public int size() {
		return vehicles.size();
	}

	@Override
	protected boolean onBalloonTap(int index) {
		return false;
	}	
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection p = mapView.getProjection();
		Point pt;
		Bitmap bitmap = ((BitmapDrawable) marker).getBitmap();
		Matrix rotate = new Matrix();
		Matrix flip = new Matrix();
		flip.reset();
		flip.setScale(-1.0f, 1.0f);
		Bitmap flippedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), flip, true);
		Bitmap tempBitmap;

		synchronized (vehicles) {
			for (VehicleJson.Vehicle v : vehicles) {	
				GeoPoint gp = new GeoPoint((int)(v.getLatest_position().getLatitude() * 1e6), (int)(v.getLatest_position().getLongitude() * 1e6));
				pt = p.toPixels(gp, null);

				rotate.reset();
				rotate.postRotate(v.getLatest_position().getHeading(), bitmap.getWidth(), bitmap.getHeight() / 2);
				
				if (v.getLatest_position().getHeading() > 180)
					tempBitmap = Bitmap.createBitmap(flippedBitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotate, true);
				else
					tempBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotate, true);
				
				tempBitmap = recolorBitmap(tempBitmap, routes.get(v.getRoute_id()).getColorInt());
				canvas.drawBitmap(tempBitmap, pt.x - (bitmap.getWidth() / 2), pt.y - (bitmap.getHeight() / 2), null);
			}
		}		
	}
	
	private Bitmap recolorBitmap(Bitmap b, int color) {
		for (int i = 0; i < b.getWidth(); i++) {
			for (int j = 0; j < b.getHeight(); j++) {
				if (b.getPixel(i, j) == MAGENTA)
					b.setPixel(i, j, color);
			}
		}
		
		return b;
	}
}
