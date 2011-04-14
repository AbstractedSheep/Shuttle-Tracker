package com.abstractedsheep.shuttletracker;

import com.abstractedsheep.shuttletracker.R;
import com.abstractedsheep.shuttletracker.sql.DatabaseHelper;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;


public class EtaDetailsActivity extends Activity {
	private String stopId;
	private int routeId;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.eta_details);
		
		TextView tv = (TextView) findViewById(R.id.eta_list);
		tv.setText("00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00\n00:00");
	
		stopId = getIntent().getExtras().getString("stop_id");
		routeId = getIntent().getExtras().getInt("route_id");

		tv = (TextView) findViewById(R.id.title);
		tv.setText(getIntent().getExtras().getString("stop_name"));
		
		tv = (TextView) findViewById(R.id.subtitle);
		tv.setText(getIntent().getExtras().getString("route_name"));
	}
}
