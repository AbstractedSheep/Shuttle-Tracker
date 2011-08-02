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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;

import com.abstractedsheep.shuttletracker.TrackerPreferences;
import com.abstractedsheep.shuttletracker.json.EtaJson;
import com.abstractedsheep.shuttletracker.sql.DatabaseHelper;
import com.abstractedsheep.shuttletrackerworld.Netlink.StopJson;
import com.abstractedsheep.shuttletrackerworld.Route;
import com.abstractedsheep.shuttletrackerworld.Stop;
import com.abstractedsheep.shuttletrackerworld.World;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;
import com.readystatesoftware.mapviewballoons.BalloonOverlayView;

public class StopsItemizedOverlay extends BalloonItemizedOverlay<DirectionalOverlayItem> {
	private final List<Stop> stops = new ArrayList<Stop>();
	private final HashMap<String, EtaJson> etas = new HashMap<String, EtaJson>();
	private final SimpleDateFormat formatter12 = new SimpleDateFormat("hh:mm a");
	private final SimpleDateFormat formatter24 = new SimpleDateFormat("HH:mm");
	private final SharedPreferences prefs;
	private final DatabaseHelper db;
    private final World world;
	
	
	public StopsItemizedOverlay(Context context, Drawable defaultMarker, MapView mapView, World world, SharedPreferences prefs) {
		super(boundCenter(defaultMarker), mapView);
		this.prefs = prefs;
        this.world = world;
        this.stops.addAll(world.getStopList());
		populate();
		db = new DatabaseHelper(context);
	}
	
	public void putEtas(List<EtaJson> etaList) {
		EtaJson tempEta;
		for (EtaJson eta : etaList) {
			tempEta = etas.get(eta.getStop_id() + eta.getRoute());
			if (tempEta == null || eta.getEta() < tempEta.getEta()) {
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
		
		for (Route r : s.getRouteList()) {
			if (db.isRouteVisible(r.getId())) {
				eta = etas.get(s.getId() + r.getId());
				if (eta != null) {
					arrival = new Date(now + eta.getEta());
					snippet += (!snippet.equals("") ? "\n" : "") + r.getName() + ": " + (prefs.getBoolean(TrackerPreferences.USE_24_HOUR, false) ? 
							formatter24.format(arrival) : formatter12.format(arrival));
				}
			}
		}
		
		return new OverlayItem(new GeoPoint(s.getLocation().getLatitudeE6(), s.getLocation().getLongitudeE6()), s.getName(), snippet);
	}

	@Override
	public int size() {
		return stops.size();
	}

	@Override
	protected boolean onBalloonTap(int index) {
		return false;
	}	
	
	public void displayStop(String stopId) {
		for (int i = 0; i < stops.size(); i++) {
			if (stops.get(i).getId().equals(stopId)) {
				super.onTap(i);
			}
		}
	}
	
}
