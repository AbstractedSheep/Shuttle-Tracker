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

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

public class NullItemizedOverlay extends ItemizedOverlay<OverlayItem>{

	private MapView map;
	
	public NullItemizedOverlay(Drawable defaultMarker, MapView map) {
		super(defaultMarker);
		this.map = map;
		populate();
	}
	
	@Override
	protected boolean onTap(int index) {
		for (Overlay overlay : map.getOverlays()) {
			if (overlay instanceof BalloonItemizedOverlay<?>) {
				((BalloonItemizedOverlay<?>) overlay).hideBalloon();
			}
		}
		return true;
	}
	
	@Override
	protected boolean hitTest(OverlayItem item, Drawable marker, int hitX,
			int hitY) {		
		return true;
	}

	@Override
	protected OverlayItem createItem(int i) {
		return new OverlayItem(new GeoPoint(0,0), "", "");
	}

	@Override
	public int size() {
		return 1;
	}

}
