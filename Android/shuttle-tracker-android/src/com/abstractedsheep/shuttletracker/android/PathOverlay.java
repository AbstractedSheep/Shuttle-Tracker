package com.abstractedsheep.shuttletracker.android;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

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
	
	public void addPoint(GeoPoint p) {
		path.add(p);
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
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
	    super.draw(canvas, mapView, shadow);
	}

}
