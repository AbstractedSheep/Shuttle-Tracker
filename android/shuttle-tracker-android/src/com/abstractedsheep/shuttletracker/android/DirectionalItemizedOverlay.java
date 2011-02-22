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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

public class DirectionalItemizedOverlay extends ItemizedOverlay<DirectionalOverlayItem> {

	private ArrayList<DirectionalOverlayItem> overlays = new ArrayList<DirectionalOverlayItem>();
	private Drawable marker;
	
	public DirectionalItemizedOverlay(Drawable defaultMarker, MapView mapView) {
		super(boundCenter(defaultMarker));
		this.marker = boundCenter(defaultMarker);
	}

	public void addOverlay(DirectionalOverlayItem overlay) {
		synchronized (overlays) {
			overlays.add(overlay);
		    populate();
		}
	}
	
	public void removeAllOverlays() {
		synchronized (overlays) {
			overlays.clear();
			populate();
		}
	}

	@Override
	protected DirectionalOverlayItem createItem(int i) {
		return overlays.get(i);
	}

	@Override
	public int size() {
		return overlays.size();
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

		synchronized (overlays) {
			for (DirectionalOverlayItem doi : overlays) {	
				pt = p.toPixels(doi.getPoint(), null);

				rotate.reset();
				rotate.postRotate(doi.getOrientation(), bitmap.getWidth(), bitmap.getHeight() / 2);
				
				if (doi.getOrientation() > 180) {
					canvas.drawBitmap(Bitmap.createBitmap(flippedBitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotate, true), pt.x - (bitmap.getWidth() / 2), pt.y - (bitmap.getHeight() / 2), null);
				} else {
					canvas.drawBitmap(Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotate, true), pt.x - (bitmap.getWidth() / 2), pt.y - (bitmap.getHeight() / 2), null);
				}
			}
		}
		
	}
}
