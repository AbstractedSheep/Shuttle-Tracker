/*
 * Copyright 2011 Austin Wagner
 *
 * This file is part of Mobile Shuttle Tracker.
 *
 * Mobile Shuttle Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mobile Shuttle Tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mobile Shuttle Tracker.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.abstractedsheep.shuttletracker.mapoverlay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.LauncherActivity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.abstractedsheep.shuttletracker.TrackerPreferences;
import com.abstractedsheep.shuttletrackerworld.Route;
import com.abstractedsheep.shuttletrackerworld.Shuttle;
import com.abstractedsheep.shuttletrackerworld.World;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;
import com.readystatesoftware.mapviewballoons.BalloonOverlayView;

public class ShuttleItemizedOverlay extends BalloonItemizedOverlay<DirectionalOverlayItem> {
	private static final int MAGENTA = Color.rgb(255, 0, 255);

	private final Bitmap markerBitmap;
	private final Bitmap markerBitmapFlipped;
	private final HashMap<Integer, Bitmap> coloredMarkers = new HashMap<Integer, Bitmap>();
	private final HashMap<Integer, Bitmap> coloredMarkersFlipped = new HashMap<Integer, Bitmap>();
	private final BiMap<Integer, Integer> idToIndex = HashBiMap.create();
	private int visibleBalloon = -1;
    private final World world;
    private boolean visible = true;
    private final List<Shuttle> shuttles = new ArrayList<Shuttle>();
	private final SimpleDateFormat formatter12 = new SimpleDateFormat("MM/dd/yy h:mm:ss a");
	private final SimpleDateFormat formatter24 = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
    private final SharedPreferences prefs;
	
	public ShuttleItemizedOverlay(Drawable defaultMarker, MapView map, World world, SharedPreferences prefs) {
		super(boundCenter(defaultMarker), map);
		this.world = world;
        this.prefs = prefs;
		shuttlesUpdated();
		
		Matrix flip = new Matrix();
		flip.reset();
		flip.setScale(-1.0f, 1.0f);

		markerBitmap = ((BitmapDrawable) boundCenter(defaultMarker)).getBitmap();
		markerBitmapFlipped = Bitmap.createBitmap(markerBitmap, 0, 0, markerBitmap.getWidth(), markerBitmap.getHeight(), flip, true);
        generateColoredMarkers();
	}
	
	@Override
	protected boolean onTap(int index) {
		visibleBalloon = idToIndex.inverse().get(index);
		long now = (new Date()).getTime();
		Date lastUpdate = new Date(shuttles.get(index).getLastUpdateTime());

        return (now - lastUpdate.getTime()) >= 45000 || super.onTap(index);
	}
	
	@Override
	public void hideBalloon() {
		visibleBalloon = -1;
		super.hideBalloon();
	}

    public void hide() {
        visible = false;
    }

    public void show() {
        visible = true;
    }
	
	public void shuttlesUpdated() {
        this.shuttles.clear();
        this.shuttles.addAll(world.getShuttleList());
        populate();

		BalloonOverlayView balloonView = getBalloonView();
		Integer vehicleId = idToIndex.get(visibleBalloon);
		if (balloonView != null && balloonView.isVisible() && vehicleId != null) {
			
			OverlayItem oi = getItem(vehicleId);
			balloonView.setData(oi);
			
			MapView.LayoutParams params = new MapView.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, oi.getPoint(),
					MapView.LayoutParams.BOTTOM_CENTER);
			params.mode = MapView.LayoutParams.MODE_MAP;
			
			balloonView.setVisibility(View.VISIBLE);
			balloonView.setLayoutParams(params);
		} else if (balloonView != null && balloonView.isVisible() && vehicleId == null) {
			hideBalloon();
		}
	}

	@Override
	protected synchronized DirectionalOverlayItem createItem(int i) {
		Shuttle s = shuttles.get(i);
		GeoPoint gp = new GeoPoint(s.getSnappedCoordinate().getLatitudeE6(), s.getSnappedCoordinate().getLongitudeE6());
		String updateTime;

        if (prefs.getBoolean(TrackerPreferences.USE_24_HOUR, false)) {
            updateTime = "Last Updated at\n" + formatter24.format(new Date(s.getLastUpdateTime()));
        } else {
            updateTime = "Last Updated at\n" + formatter12.format(new Date(s.getLastUpdateTime()));
        }
		
		return new DirectionalOverlayItem(gp, s.getBearing(), s.getName(), updateTime);
	}

	@Override
	public int size() {
		return shuttles.size();
	}	
	
	@Override
	public synchronized void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if (!visible) return;

		final Projection p = mapView.getProjection();
		final Matrix rotate = new Matrix();
		long now = (new Date()).getTime();
			
		for (Shuttle s : shuttles) {
            Date lastUpdate = new Date(s.getLastUpdateTime());
            long age = now - lastUpdate.getTime();
            if (age > 45000)
                continue;

            GeoPoint gp = new GeoPoint(s.getLocation().getLatitudeE6(), s.getLocation().getLongitudeE6());
            Point pt = p.toPixels(gp, null);

            rotate.reset();

            Bitmap tempBitmap;
            if (s.getBearing() > 180) {
                tempBitmap = coloredMarkersFlipped.get(s.getCurrentRoute() == null ? -1 : s.getCurrentRoute().getId());
                rotate.postRotate(s.getBearing(), tempBitmap.getWidth() / 2, tempBitmap.getHeight() / 2);
                tempBitmap = Bitmap.createBitmap(tempBitmap, 0, 0, tempBitmap.getWidth(), tempBitmap.getHeight(), rotate, true);
            } else {
                tempBitmap = coloredMarkers.get(s.getCurrentRoute() == null ? -1 : s.getCurrentRoute().getId());
                rotate.postRotate(s.getBearing(), tempBitmap.getWidth() / 2, tempBitmap.getHeight() / 2);
                tempBitmap = Bitmap.createBitmap(tempBitmap, 0, 0, tempBitmap.getWidth(), tempBitmap.getHeight(), rotate, true);
            }

            canvas.drawBitmap(tempBitmap, pt.x - (tempBitmap.getWidth() / 2), pt.y - (tempBitmap.getHeight() / 2), null);
		}	
	}

    private void generateColoredMarkers() {
        coloredMarkers.put(-1, recolorBitmap(markerBitmap, Color.WHITE));
        coloredMarkersFlipped.put(-1, recolorBitmap(markerBitmapFlipped, Color.WHITE));
        for (Route r : world.getRouteList()) {
            coloredMarkers.put(r.getId(), recolorBitmap(markerBitmap, r.getColor()));
            coloredMarkersFlipped.put(r.getId(), recolorBitmap(markerBitmapFlipped, r.getColor()));
        }
    }

	private Bitmap recolorBitmap(Bitmap bitmap, int color) {
		Bitmap b = bitmap.copy(Config.ARGB_8888, true);
		for (int i = 0; i < b.getWidth(); i++) {
			for (int j = 0; j < b.getHeight(); j++) {
				if (b.getPixel(i, j) == MAGENTA)
					b.setPixel(i, j, color);
			}
		}
		return b;
	}
}
