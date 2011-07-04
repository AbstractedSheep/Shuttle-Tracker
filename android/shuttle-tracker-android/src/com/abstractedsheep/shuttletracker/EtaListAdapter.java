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

package com.abstractedsheep.shuttletracker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.abstractedsheep.shuttletracker.R;
import com.abstractedsheep.shuttletracker.json.EtaJson;
import com.abstractedsheep.shuttletracker.json.ExtraEtaJson;
import com.abstractedsheep.shuttletracker.sql.DatabaseHelper;
import com.abstractedsheep.shuttletrackerworld.Netlink;
import com.abstractedsheep.shuttletrackerworld.Netlink.RouteJson;
import com.abstractedsheep.shuttletrackerworld.Netlink.StopJson;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class EtaListAdapter extends BaseExpandableListAdapter {
	private HashMap<String, EtaJson> etas = new HashMap<String, EtaJson>();
	private ArrayList<RouteJson> parents = new ArrayList<Netlink.RouteJson>();
	private HashMap<Integer, String> routeNames = new HashMap<Integer, String>();
	private ArrayList<ArrayList<StopJson>> children = new ArrayList<ArrayList<StopJson>>();
	private ArrayList<StopJson> favorites = new ArrayList<StopJson>();
	private SimpleDateFormat formatter12 = new SimpleDateFormat("h:mm a");
	private SimpleDateFormat formatter24 = new SimpleDateFormat("HH:mm");
	private LayoutInflater inflater;
	private Context ctx;
	private DatabaseHelper db;
	private SharedPreferences prefs;
	private int expandedChild = -1;
	private int expandedGroup = -1;
	private String extraEtas;
	private ExpandableListView parent;
	private TrackerTabActivity tabActivity;
	
	public EtaListAdapter(TrackerTabActivity tabActivity, ExpandableListView parent, Context context, LayoutInflater li, SharedPreferences prefs) {
		this.inflater = li;
		this.ctx = context;
		this.prefs = prefs;
		db = new DatabaseHelper(context);
		this.parent = parent;
		this.tabActivity = tabActivity;
	}
	
	public void setRoutes(Netlink routes) {
		parents.clear();
		children.clear();
		
		for (RouteJson route : routes.getRoutes()) {
			parents.add(route);
			routeNames.put(route.getId(), route.getName());
			children.add(new ArrayList<StopJson>());
		}
		
		RouteJson r;
		
		for (StopJson s : routes.getStops()) {
			for (int i = 0; i < parents.size(); i++) {
				r = parents.get(i);
				
				if (stopOnRoute(s, r.getId())) {
					children.get(i).add(s);
				}
			}
		}
		
		for (ArrayList<StopJson> stops : children) {
			Collections.sort(stops);
		}
		
		loadFavorites();
		
		notifyDataSetInvalidated();
	}
	
	public void putEtas(List<EtaJson> etaList) {
		EtaJson eta;
		EtaJson tempEta;
		for (int i = 0; i < etaList.size(); i++) {
			eta = etaList.get(i);
			tempEta = etas.get(eta.getStop_id() + eta.getRoute());
			if (tempEta == null || (tempEta != null && eta.getEta() < tempEta.getEta())) {
				etas.put(eta.getStop_id() + eta.getRoute(), eta);
			}
			
		}
		
		notifyDataSetChanged();
	}
	
	public int getGroupCount() {
		return favorites.size() > 0 ? parents.size() + 1 : parents.size();
	}

	public int getChildrenCount(int groupPosition) {
		if (favorites.size() > 0 && groupPosition == 0)
			return favorites.size();
		else if (favorites.size() > 0) 
			return children.get(groupPosition - 1).size();
		else
			return children.get(groupPosition).size();
	}
	
	private boolean stopOnRoute(StopJson stop, int routeId) {
		for (StopJson.StopRouteJson r : stop.getRoutes()) {
			if (r.getId() == routeId)
				return true;
		}
		return false;
	}

	public RouteJson getGroup(int groupPosition) {
		if (favorites.size() > 0 && groupPosition == 0)
			return new RouteJson();
		else if (favorites.size() > 0) 
			return parents.get(groupPosition - 1);
		else
			return parents.get(groupPosition);
	}

	public StopJson getChild(int groupPosition, int childPosition) {
		if (favorites.size() > 0 && groupPosition == 0)
			return favorites.get(childPosition);
		else if (favorites.size() > 0) 
			return children.get(groupPosition - 1).get(childPosition);
		else
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
		String text;
		
		if (favorites.size() > 0 && groupPosition == 0)
			text = ctx.getString(R.string.favorites);
		else if (favorites.size() > 0) 
			text = parents.get(groupPosition - 1).getName();
		else
			text = parents.get(groupPosition).getName();
		
		View v = inflater.inflate(R.layout.simple_expandable_list_item_1, null);	
		TextView tv = (TextView) v.findViewById(R.id.text1);
		
		tv.setText(text);
		
		return v;
	}

	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		View v;
		
		if (groupPosition == expandedGroup && childPosition == expandedChild) {
			v = inflater.inflate(R.layout.expanded_eta_list_item, null);
			Button b1 = (Button)v.findViewById(R.id.button1);
			b1.setOnClickListener(buttonClickListener);
			b1.setText(ctx.getResources().getString(R.string.map_this));
			Button b2 = (Button)v.findViewById(R.id.button2);
			b2.setOnClickListener(buttonClickListener);
			b2.setText((groupPosition == 0 && favoritesVisible()) ? ctx.getResources().getString(R.string.remove_favorite) : ctx.getResources().getString(R.string.add_favorite));
				
		} else {
			v = inflater.inflate(R.layout.eta_list_item, null);
		}
		
		TextView text = (TextView) v.findViewById(R.id.text1);
		TextView subText = (TextView) v.findViewById(R.id.text2);
		TextView timeText = (TextView) v.findViewById(R.id.text3);
		
		RouteJson route;
		StopJson stop;
		EtaJson eta;
		
		if (favorites.size() > 0 && groupPosition == 0) {
			stop = favorites.get(childPosition);
			eta = etas.get(stop.getUniqueId());
		} else if (favorites.size() > 0) {
			route = parents.get(groupPosition - 1);
			stop = children.get(groupPosition - 1).get(childPosition);
			eta = etas.get(stop.getShort_name() + route.getId());
		} else {
			route = parents.get(groupPosition);
			stop = children.get(groupPosition).get(childPosition);
			eta = etas.get(stop.getShort_name() + route.getId());
		}
		
		String etaString = "";
		
		if (eta != null) {
			long now = (new Date()).getTime();
			Date arrival = new Date(now + eta.getEta());
			
			if (prefs.getBoolean(TrackerPreferences.USE_24_HOUR, false))
				etaString = formatter24.format(arrival);
			else
				etaString = formatter12.format(arrival);
		}
		 
		if (groupPosition == expandedGroup && childPosition == expandedChild) {
			etaString = extraEtas != null ? extraEtas : etaString + "\nLoading...";
		}
		
		text.setText(stop.getName());
		timeText.setText(etaString);
		
		if (favorites.size() > 0 && groupPosition == 0) {
			subText.setVisibility(View.VISIBLE);
			subText.setText(routeNames.get(stop.getFavoriteRoute()));
		} else {
			subText.setVisibility(View.GONE);
		}
		
		return v;
	}
	
	public void addFavorite(int groupPosition, int childPosition) {
		if (favoritesVisible())
			groupPosition--;
		
		if (!favorites.contains(children.get(groupPosition).get(childPosition))) {
			StopJson fav = children.get(groupPosition).get(childPosition);
			fav.setFavoriteRoute(parents.get(groupPosition).getId());
			favorites.add(fav);
		}
		
		saveFavorites();
		notifyDataSetInvalidated();
		parent.setSelectedChild(0, 0, true);
	}
	
	public void removeFavorite(int childPosition) {
		favorites.remove(childPosition);
		
		notifyDataSetInvalidated();
	}
	
	public void saveFavorites() {
		db.updateFavorites(favorites);
	}
	
	public void loadFavorites() {
	    favorites = db.getFavorites();

	    notifyDataSetInvalidated();
	}
	
	public boolean favoritesVisible() {
		return favorites.size() > 0 ? true : false;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	public String getRouteName(int routeId) {
		return routeNames.get(routeId);
	}
	
	public boolean expandChild(int groupPosition, int childPosition) {
		boolean result;
		if (expandedGroup == groupPosition && expandedChild == childPosition) {
			expandedChild = -1;
			expandedGroup = -1;
			result = false;
		} else {
			expandedGroup = groupPosition;
			expandedChild = childPosition;
			result = true;
		}
		
		extraEtas = null;
		notifyDataSetChanged();
		return result;
	}
	
	public void setExtraEtas(ExtraEtaJson etas) {
		String text = "Could not get\narrival times.";
		Date now = new Date();
		String time;
		
		if (etas != null) {
			Collections.sort(etas.getEta());
			text = "";
			Integer t;
			for (int i = 0; i < 12; i++) {
				if (i < etas.getEta().size()) {
					t = etas.getEta().get(i);
					if (prefs.getBoolean(TrackerPreferences.USE_24_HOUR, false)) {
						time = formatter24.format(new Date(now.getTime() + t));
					} else {
						time = formatter12.format(new Date(now.getTime() + t));
					}
					
				} else {
					time = "--:--";
				}
				text += time + "\n";
			}
			if (text.length() > 1) text = text.substring(0, text.length() - 1);
		}
		
		extraEtas = text;
	}
	
	OnClickListener buttonClickListener = new OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.button1:
				tabActivity.showMap(getChild(expandedGroup, expandedChild).getShort_name());
				break;
			case R.id.button2:
				if (expandedGroup == 0 && favoritesVisible()) {
					removeFavorite(expandedChild);
				} else {
					addFavorite(expandedGroup, expandedChild);
				}
				expandChild(expandedGroup, expandedChild);
				break;
			}
		}
	};
}
