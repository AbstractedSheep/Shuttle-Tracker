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
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
//import android.util.Log;

import com.abstractedsheep.kml.Style;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class PathOverlay extends Overlay {

	List<GeoPoint> path;
	Style style;
	
	public PathOverlay(List<GeoPoint> path, Style style) {
		this.path = path;
		this.style = style;
	}
	
	public PathOverlay(Style style) {
		this.path = new ArrayList<GeoPoint>();
		this.style = style;
	}
	
	public void setPoints(ArrayList<GeoPoint> p) {
		path = p;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		//long start = System.currentTimeMillis();
		
		Projection projection = mapView.getProjection();
	    Path p = new Path();
	    Paint polygonPaint = new Paint();
	    polygonPaint.setStrokeWidth(this.style.width); 
	    polygonPaint.setColor(this.style.color);
	    polygonPaint.setStyle(Paint.Style.STROKE);
	    polygonPaint.setAntiAlias(true); 
    
	    for (int i = 0; i < path.size(); i++) {
		    if (i == path.size() - 1) {
		        break;
		    } 
		    Point from = new Point();
		    Point to = new Point();
		    projection.toPixels(path.get(i), from);
		    projection.toPixels(path.get(i + 1), to);
		    if (i == 0) { 
		    	p.moveTo(from.x, from.y); 
		    }
		    p.lineTo(to.x, to.y);
	    }
	    
	    canvas.drawPath(p, polygonPaint);
	    
	    //Log.d("Tracker", "Path drawing complete in " + String.valueOf(System.currentTimeMillis() - start) + "ms");
	}

}
