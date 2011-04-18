package com.abstractedsheep.shuttletracker;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.abstractedsheep.shuttletracker.R;
import com.abstractedsheep.shuttletracker.json.ExtraEtaJson;
import com.abstractedsheep.shuttletracker.sql.DatabaseHelper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class EtaDetailsActivity extends Activity {
	private String stopId;
	private int routeId;
	private ShuttleDataService dataService;
	private DatabaseHelper db;
	private Button favoriteButton;
	private SharedPreferences prefs;
	private SimpleDateFormat formatter12 = new SimpleDateFormat("hh:mm a");
	private SimpleDateFormat formatter24 = new SimpleDateFormat("HH:mm");
	private Handler handler = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.eta_details);
		
		dataService = ShuttleDataService.getInstance();
			
		TextView tv = (TextView) findViewById(R.id.eta_list);
		tv.setText("Loading...");
		
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		stopId = getIntent().getExtras().getString("stop_id");
		routeId = getIntent().getExtras().getInt("route_id");

		tv = (TextView) findViewById(R.id.title);
		tv.setText(getIntent().getExtras().getString("stop_name"));
		
		tv = (TextView) findViewById(R.id.subtitle);
		tv.setText(getIntent().getExtras().getString("route_name"));
		
		db = new DatabaseHelper(this);
		favoriteButton = (Button) findViewById(R.id.favorites_button);
		if (db.isStopFavorite(stopId, routeId))
			favoriteButton.setText(R.string.remove_favorite);
		else
			favoriteButton.setText(R.string.add_favorite);
		
		favoriteButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (db.isStopFavorite(stopId, routeId)) {
					db.setStopFavorite(stopId, routeId, false);
					favoriteButton.setText(R.string.add_favorite);
				} else {
					db.setStopFavorite(stopId, routeId, true);
					favoriteButton.setText(R.string.remove_favorite);
				}
			}
		});
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		handler.removeCallbacks(GetEtaTask);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		handler.post(GetEtaTask);
	}
	
	private Runnable GetEtaTask = new Runnable() {
		public void run() {
			ExtraEtaJson result = dataService.parseJson("http://shuttles.abstractedsheep.com/data_service.php?action=get_all_extra_eta&rt=" + routeId + "&st=" + stopId, ExtraEtaJson.class);
			TextView tv = (TextView) findViewById(R.id.eta_list);
			String text = "Could not get arrival times.";
			Date now = new Date();
			String time;
			
			if (result != null) {
				text = "";
				for (Integer i : result.getEta()) {
					if (prefs.getBoolean(TrackerPreferences.USE_24_HOUR, false)) {
						time = formatter24.format(new Date(now.getTime() + i));
					} else {
						time = formatter12.format(new Date(now.getTime() + i));
					}
					text += time + ", ";
				}
				if (text.length() > 2) text = text.substring(0, text.length() - 2);
			}
			tv.setText(text);
			
			handler.postDelayed(this, 5000);
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.options:
			startActivity(new Intent(this, TrackerPreferences.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
