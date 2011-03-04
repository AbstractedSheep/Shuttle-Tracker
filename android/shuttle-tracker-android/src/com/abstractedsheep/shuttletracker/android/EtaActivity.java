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
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class EtaActivity extends Activity implements IShuttleDataUpdateCallback {
	private ArrayList<EtaJson> etas;
	RoutesJson routes;
	private SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
	private IShuttleDataMonitor service = null;
	private ServiceConnection svcConn = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			service = (IShuttleDataMonitor)binder;
	    	
	    	service.registerCallback(EtaActivity.this);
		}

		public void onServiceDisconnected(ComponentName className) {
			service = null;
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.eta);
		
		getApplicationContext().bindService(new Intent(this, ShuttleDataService.class), svcConn, BIND_AUTO_CREATE);
	}
	
	@Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	if (service != null)
    		getApplicationContext().unbindService(svcConn);
    }
	 
	@Override
    protected void onResume() {
    	super.onResume();
    	
    	if (service != null)
    		service.registerCallback(this);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	
    	if (service != null)
    		service.unregisterCallback(this);
    }
    
	
   
	public void dataUpdated(ArrayList<VehicleJson> vehicles,
			ArrayList<EtaJson> etas) {
		this.etas = etas;
		
		runOnUiThread(updateList);	
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
