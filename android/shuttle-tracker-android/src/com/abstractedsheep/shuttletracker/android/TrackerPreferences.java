package com.abstractedsheep.shuttletracker.android;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class TrackerPreferences extends PreferenceActivity {
	public static final String MY_LOCATION = "MY_LOCATION";
	public static final String USE_24_HOUR = "USE_24_HOUR";
	public static final String UPDATE_RATE = "UPDATE_RATE";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
	}
}
