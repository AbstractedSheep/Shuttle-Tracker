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

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class TrackerTabActivity extends TabActivity {
	private TabHost tabHost;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.tabHost = getTabHost();
		
		TabSpec tab = this.tabHost.newTabSpec("map");
		Intent i = new Intent(this, TrackerMapActivity.class);
		tab.setContent(i);
		tab.setIndicator("Map");
		this.tabHost.addTab(tab);
		
		tab = this.tabHost.newTabSpec("eta");
		i = new Intent(this, EtaActivity.class);
		tab.setContent(i);
		tab.setIndicator("ETA");
		this.tabHost.addTab(tab);
	}
}
