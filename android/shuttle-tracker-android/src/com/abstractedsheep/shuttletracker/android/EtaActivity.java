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
import java.util.Date;

import com.abstractedsheep.shuttletracker.json.EtaJson;
import com.abstractedsheep.shuttletracker.json.RoutesJson;
import com.abstractedsheep.shuttletracker.json.VehicleJson;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class EtaActivity extends Activity implements IShuttleDataUpdateCallback {
	private ArrayList<EtaJson> etas;
	RoutesJson routes;
	private SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
	private ShuttleDataService dataService;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.eta);
		
		dataService = ShuttleDataService.getInstance();
		routesUpdated(dataService.getRoutes());
	}
   
	public void dataUpdated(ArrayList<VehicleJson> vehicles,
			ArrayList<EtaJson> etas) {
		if (etas != null) {
			this.etas = etas;
			
			runOnUiThread(updateList);	
		}
	}
	
	private Runnable updateList = new Runnable() {
		public void run() {
			ListView lv = (ListView) findViewById(R.id.eta_list);
			ArrayAdapter<String> aa = new ArrayAdapter<String>(EtaActivity.this, android.R.layout.simple_list_item_1);
			for (EtaJson e : etas) {
				long now = (new Date()).getTime();
				Date arrival = new Date(now + Long.parseLong(e.getEta()));
				aa.add(formatter.format(arrival));
			}
			lv.setAdapter(aa);
		}
	};

	public void routesUpdated(RoutesJson routes) {
		if (routes != null)
			this.routes = routes;
	}
}
