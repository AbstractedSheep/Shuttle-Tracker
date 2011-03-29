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

package com.abstractedsheep.shuttletracker.mapoverlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Projection;

public class LocationOverlay extends MyLocationOverlay {

	private Bitmap marker;
	private Paint accuracyPaint = new Paint();
	private Paint strokePaint = new Paint();
	Matrix rotate = new Matrix();
	
	public LocationOverlay(Context context, MapView mapView, int markerResource) {
		super(context, mapView);
		Drawable d = context.getResources().getDrawable(markerResource);
		marker = ((BitmapDrawable) d).getBitmap();
		accuracyPaint.setAntiAlias(true);
		accuracyPaint.setColor(Color.BLUE);
		accuracyPaint.setAlpha(20);
		strokePaint.setAntiAlias(true);
		strokePaint.setStyle(Style.STROKE);
		strokePaint.setColor(Color.BLUE);
		strokePaint.setAlpha(80);
		strokePaint.setStrokeWidth(2);
	}
	
	@Override
	protected void drawMyLocation(Canvas canvas, MapView mapView,
			Location lastFix, GeoPoint myLocation, long when) {
		if (isMyLocationEnabled()) {
			Projection p = mapView.getProjection();
			float accuracy = p.metersToEquatorPixels(lastFix.getAccuracy());
			Point currLoc = p.toPixels(myLocation, null);
			float orientation = lastFix.getBearing() - 45;
					
			if (accuracy > 10.0f) {
				canvas.drawCircle(currLoc.x, currLoc.y, accuracy, strokePaint);
				canvas.drawCircle(currLoc.x, currLoc.y, accuracy, accuracyPaint);
			}
			
			rotate.reset();
			rotate.postRotate(orientation, marker.getWidth() / 2, marker.getHeight() / 2);
			Bitmap rotatedMarker = Bitmap.createBitmap(marker, 0, 0, marker.getWidth(), marker.getHeight(), rotate, true);
			canvas.drawBitmap(rotatedMarker, currLoc.x - (marker.getWidth() / 2), currLoc.y - (marker.getHeight() / 2), null);
			canvas.restore();
		}
	}

}
