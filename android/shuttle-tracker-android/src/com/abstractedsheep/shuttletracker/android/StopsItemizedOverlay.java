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

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

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
	SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
	
	public StopsItemizedOverlay(Drawable defaultMarker, MapView mapView) {
		super(boundCenter(defaultMarker), mapView);
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
	
	public synchronized void putEtas(List<EtaJson> etaList) {
		EtaJson eta;
		for (int i = 0; i < etaList.size(); i++) {
			eta = etaList.get(i);
			etas.put(eta.getStop_id(), eta);
		}
		
		refreshBalloon();
	}
	
	public void refreshBalloon() {
		BalloonOverlayView bov = getBalloonView();
		int index = getCurrentIndex();
		if (bov != null && index >= 0 && bov.isVisible()) {
			Log.d("Tracker", "Refreshing balloon");
			bov.setData(createItem(index));
		}
	}

	@Override
	protected synchronized OverlayItem createItem(int i) {
		Stop s = stops.get(i);
		EtaJson eta = etas.get(s.getShort_name());
		String snippet = "";
		long now = (new Date()).getTime();
		
		if (eta != null) {
			Date arrival = new Date(now + Long.parseLong(eta.getEta()));
			snippet = "Next Arrival: " + formatter.format(arrival);
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
}
