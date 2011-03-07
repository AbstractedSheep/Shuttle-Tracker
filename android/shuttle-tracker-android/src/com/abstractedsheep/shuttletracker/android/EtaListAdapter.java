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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.abstractedsheep.shuttletracker.json.EtaJson;
import com.abstractedsheep.shuttletracker.json.RoutesJson;
import com.abstractedsheep.shuttletracker.json.RoutesJson.Route;
import com.abstractedsheep.shuttletracker.json.RoutesJson.Stop;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class EtaListAdapter extends BaseExpandableListAdapter {

	private HashMap<String, EtaJson> etas = new HashMap<String, EtaJson>();
	private LayoutInflater inflater;
	private ArrayList<Route> parents = new ArrayList<RoutesJson.Route>();
	private ArrayList<ArrayList<Stop>> children = new ArrayList<ArrayList<Stop>>();
	SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
	
	public EtaListAdapter(LayoutInflater li) {
		inflater = li;
	}
	
	public void setRoutes(RoutesJson routes) {
		parents.clear();
		children.clear();
		
		for (Route route : routes.getRoutes()) {
			parents.add(route);
			children.add(new ArrayList<Stop>());
		}
		
		Route r;
		
		for (Stop s : routes.getStops()) {
			for (int i = 0; i < parents.size(); i++) {
				r = parents.get(i);
				
				if (stopOnRoute(s, r.getId())) {
					children.get(i).add(s);
				}
			}
		}
		
		for (ArrayList<Stop> stops : children) {
			Collections.sort(stops);
		}
		
		notifyDataSetInvalidated();
	}
	
	public void putEtas(List<EtaJson> etaList) {
		EtaJson eta;
		EtaJson tempEta;
		for (int i = 0; i < etaList.size(); i++) {
			eta = etaList.get(i);
			tempEta = etas.get(eta.getStop_id() + eta.getRoute());
			if (tempEta == null || (tempEta != null && eta.getEta() < tempEta.getEta())) {
				Log.d("Tracker", "Putting " + eta.getEta() + " at " + eta.getStop_id() + eta.getRoute());
				etas.put(eta.getStop_id() + eta.getRoute(), eta);
			}
			
		}
		
		notifyDataSetChanged();
	}
	
	public int getGroupCount() {
		return parents.size();
	}

	public int getChildrenCount(int groupPosition) {
		return children.get(groupPosition).size();
	}
	
	private boolean stopOnRoute(Stop stop, int routeId) {
		for (Stop.Route r : stop.getRoutes()) {
			if (r.getId() == routeId)
				return true;
		}
		return false;
	}

	public Route getGroup(int groupPosition) {
		return parents.get(groupPosition);
	}

	public Stop getChild(int groupPosition, int childPosition) {
		return children.get(groupPosition).get(childPosition);
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	public boolean hasStableIds() {
		return false;
	}

	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		View v = inflater.inflate(R.layout.simple_expandable_list_item_1, null);
		
		TextView tv = (TextView) v.findViewById(R.id.text1);
		
		tv.setText(parents.get(groupPosition).getName());
		
		return v;
	}

	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		View v = inflater.inflate(R.layout.simple_list_item_1, null);
		
		TextView tv = (TextView) v.findViewById(R.id.text1);
		
		Route route = parents.get(groupPosition);
		Stop stop = children.get(groupPosition).get(childPosition);
		EtaJson eta = etas.get(stop.getShort_name() + route.getId());
		Log.d("Tracker", "Got " + ((eta == null) ? null : eta.getEta()) + " from " + stop.getShort_name() + route.getId());
		String etaString = "";
		
		if (eta != null) {
			long now = (new Date()).getTime();
			Date arrival = new Date(now + eta.getEta());
			etaString = formatter.format(arrival);
		}
		
		tv.setText(stop.getName() + ": " + etaString);
		
		return v;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

}
