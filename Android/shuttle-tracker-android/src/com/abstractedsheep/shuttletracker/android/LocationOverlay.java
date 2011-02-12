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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class LocationOverlay extends MyLocationOverlay {

	private Context context;
	private int markerResource;
	
	public LocationOverlay(Context context, MapView mapView, int markerResource) {
		super(context, mapView);
		this.markerResource = markerResource;
		this.context = context;
	}
	
	@Override
	protected void drawMyLocation(Canvas canvas, MapView mapView,
			Location lastFix, GeoPoint myLocation, long when) {		
		Point currLoc = mapView.getProjection().toPixels(myLocation, null);
		
		Drawable d = context.getResources().getDrawable(this.markerResource);
		Bitmap marker = ((BitmapDrawable) d).getBitmap();
	
		//Matrix m = new Matrix();
		//m.postRotate(getOrientation());
		
		//marker = Bitmap.createBitmap(marker, 0, 0, marker.getWidth(), marker.getHeight(), m, true);
		
		canvas.drawBitmap(marker, currLoc.x - (marker.getWidth() / 2), currLoc.y - (marker.getHeight() / 2), null);
	}

}
