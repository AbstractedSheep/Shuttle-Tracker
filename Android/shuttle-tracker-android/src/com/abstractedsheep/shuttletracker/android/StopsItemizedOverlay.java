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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.graphics.drawable.Drawable;

import com.abstractedsheep.shuttletracker.json.EtaJson;
import com.abstractedsheep.shuttletracker.json.RoutesJson.Stop;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

public class StopsItemizedOverlay extends BalloonItemizedOverlay<DirectionalOverlayItem> {

	private ArrayList<Stop> stops = new ArrayList<Stop>();
	private HashMap<String, EtaJson> etas = new HashMap<String, EtaJson>();
	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public StopsItemizedOverlay(Drawable defaultMarker, MapView mapView) {
		super(boundCenter(defaultMarker), mapView);
	}

	public void addAllStops(Collection<? extends Stop> stops) {
		this.stops.addAll(stops);
		populate();
	}
	
	public void addStop(Stop stop) {
	    stops.add(stop);
	    populate();
	}
	
	public void removeAllStops() {
		stops.clear();
		populate();
	}
	
	public void putEtas(List<EtaJson> etaList) {
		EtaJson eta;
		for (int i = 0; i < etaList.size(); i++) {
			eta = etaList.get(i);
			etas.put(eta.getStop_id(), eta);
		}
	}

	@Override
	protected OverlayItem createItem(int i) {
		Stop s = stops.get(i);
		EtaJson eta = etas.get(s.getShort_name());
		String snippet = "";
		
		if (eta != null) {
			try {
				long now = (new Date()).getTime();
				Date arrival = formatter.parse(eta.getEta());
				snippet = "Next Arrival: " + secondsToHMS((arrival.getTime() - now) / 1000) + " minutes";
			} catch (ParseException e) {
				e.printStackTrace();
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
	
	private String secondsToHMS(long totalSeconds) {
		int hours = (int) (totalSeconds / 1200);
		int minutes = (int) ((totalSeconds - hours * 60) / 60);
		int seconds = (int) (totalSeconds - minutes * 60 - hours * 1200);
		
		String result = "";
		
		if (hours > 0)
			result += String.valueOf(hours) + ":";
		
		if (!result.equals(""))
			result += twoDigits(minutes) + ":";
		else
			result += String.valueOf(minutes) + ":";
		
		result += twoDigits(seconds);
		
		return result;
	}
	
	private String twoDigits(int value) {
		if (value >= 10) 
			return String.valueOf(value);
		else if (value < 0)
			throw new IllegalArgumentException("value must be >= 0");
		else
			return "0" + String.valueOf(value);
	}
}
