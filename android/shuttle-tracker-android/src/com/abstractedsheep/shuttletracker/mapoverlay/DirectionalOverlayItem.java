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

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class DirectionalOverlayItem extends OverlayItem {

	private int orientation;
	private String snippet;
	
	public DirectionalOverlayItem(GeoPoint point, int orientation, String title, String snippet) {
		super(point, title, snippet);
		
		this.snippet = snippet;
		setOrientation(orientation);
	}
	
	public DirectionalOverlayItem(GeoPoint point, String title, String snippet) {
		super(point, title, snippet);
		
		this.orientation = 0;
	}

	public int getOrientation() {
		return orientation;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation % 360;
	}
	
	@Override
	public String getSnippet() {
		return this.snippet;
	}
	
	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}
}
