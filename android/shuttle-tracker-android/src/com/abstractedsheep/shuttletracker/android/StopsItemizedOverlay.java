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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.abstractedsheep.shuttletracker.json.EtaJson;
import com.abstractedsheep.shuttletracker.json.RoutesJson.Stop;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;
import com.readystatesoftware.mapviewballoons.BalloonOverlayView;

public class StopsItemizedOverlay extends BalloonItemizedOverlay<DirectionalOverlayItem> {

	private ArrayList<Stop> stops = new ArrayList<Stop>();
	private HashMap<String, EtaJson> etas = new HashMap<String, EtaJson>();
	private SimpleDateFormat formatter12 = new SimpleDateFormat("hh:mm a");
	private SimpleDateFormat formatter24 = new SimpleDateFormat("HH:mm");
	private SharedPreferences prefs;
	
	
	public StopsItemizedOverlay(Drawable defaultMarker, MapView mapView, SharedPreferences prefs) {
		super(boundCenter(defaultMarker), mapView);
		this.prefs = prefs;
		populate();
	}

	public synchronized void addAllStops(Collection<? extends Stop> stops) {
		this.stops.addAll(stops);
		populate();
	}
	
	public synchronized void addStop(Stop stop) { 
	    stops.add(stop);
	    populate();
	}
	
	public synchronized void removeAllStops() {
		stops.clear();
		populate();
	}
	
	public void putEtas(List<EtaJson> etaList) {
		EtaJson eta;
		EtaJson tempEta;
		for (int i = 0; i < etaList.size(); i++) {
			eta = etaList.get(i);
			tempEta = etas.get(eta.getStop_id() + eta.getRoute());
			if (tempEta == null || (tempEta != null && eta.getEta() < tempEta.getEta())) {
				etas.put(eta.getStop_id() + eta.getRoute(), eta);
			}	
		}
	}

	
	public void refreshBalloon() {
		BalloonOverlayView bov = getBalloonView();
		int index = getCurrentIndex();
		if (bov != null && index >= 0 && bov.isVisible()) {
			bov.setData(createItem(index));
		}
	}

	@Override
	protected synchronized OverlayItem createItem(int i) {
		Stop s = stops.get(i);
		EtaJson eta;
		String snippet = "";
		Date arrival;
		long now = (new Date()).getTime();
		
		for (Stop.Route r : s.getRoutes()) {
			eta = etas.get(s.getShort_name() + r.getId());
			if (eta != null) {
				arrival = new Date(now + eta.getEta());
				snippet += (!snippet.equals("") ? "\n" : "") + r.getName() + ": " + (prefs.getBoolean(TrackerPreferences.USE_24_HOUR, false) ? 
						formatter24.format(arrival) : formatter12.format(arrival));
			}
		}
		
		return new OverlayItem(new GeoPoint((int)(s.getLatitude() * 1e6), (int)(s.getLongitude() * 1e6)), s.getName(), snippet);
	}

	@Override
	public int size() {
		return stops.size();
	}

	@Override
	protected boolean onBalloonTap(int index) {
		return false;
	}	
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		//long start = System.currentTimeMillis();
		super.draw(canvas, mapView, shadow);
		//Log.d("Tracker", "Stops drawing complete in " + String.valueOf(System.currentTimeMillis() - start) + "ms");
	}
	
	/*
	@Override
	protected boolean hitTest(OverlayItem item, Drawable marker, int hitX,
			int hitY) {
		if (hitX > -20 && hitX < 20 && hitY > -20 && hitY < 20)
			return true;
		else
			return false;
	}
	*/
}
