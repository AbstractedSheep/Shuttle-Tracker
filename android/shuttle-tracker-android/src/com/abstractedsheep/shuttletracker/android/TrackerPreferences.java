package com.abstractedsheep.shuttletracker.android;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class TrackerPreferences extends PreferenceActivity {
	public static final String MY_LOCATION = "MY_LOCATION";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
	}
}
