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

import java.util.ArrayList;

import com.abstractedsheep.shuttletracker.json.EtaJson;
import com.abstractedsheep.shuttletracker.json.RoutesJson;
import com.abstractedsheep.shuttletracker.json.VehicleJson;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ExpandableListView;

public class EtaActivity extends Activity implements IShuttleServiceCallback {
	private ArrayList<EtaJson> etas;
	RoutesJson routes;
	private ShuttleDataService dataService;
	ExpandableListView etaListView;
	EtaListAdapter etaAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.eta);
		
		etaListView = (ExpandableListView) findViewById(R.id.eta_list);
		etaAdapter = new EtaListAdapter(getLayoutInflater());
		etaListView.setAdapter(etaAdapter);
		dataService = ShuttleDataService.getInstance();
		routesUpdated(dataService.getRoutes());
	}
   
	public void dataUpdated(ArrayList<VehicleJson> vehicles, ArrayList<EtaJson> etas) {
		if (etas != null) {
			this.etas = etas;
			runOnUiThread(updateList);	
		}
	}
	
	private Runnable updateList = new Runnable() {
		public void run() {
			etaAdapter.putEtas(etas);
		}
	};
	
	private Runnable setRoutes = new Runnable() {
		public void run() {
			etaAdapter.setRoutes(routes);
		}
	};

	

	public void routesUpdated(RoutesJson routes) {
		if (routes != null) {
			this.routes = routes;
			runOnUiThread(setRoutes);
		}
	}

	public void dataServiceError(int errorCode) {
	}
}
