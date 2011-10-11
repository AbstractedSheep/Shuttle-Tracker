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

import android.text.format.DateFormat;
import android.util.Log;
import com.abstractedsheep.shuttletracker.R;
import com.abstractedsheep.shuttletracker.json.EtaJson;
import com.abstractedsheep.shuttletracker.json.ExtraEtaJson;
import com.abstractedsheep.shuttletracker.sql.DatabaseHelper;
import com.abstractedsheep.shuttletrackerworld.Route;
import com.abstractedsheep.shuttletrackerworld.Stop;
import com.abstractedsheep.shuttletrackerworld.World;

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
	private final HashMap<String, EtaJson> etas = new HashMap<String, EtaJson>();
	private List<Route> parents = new ArrayList<Route>();
	private List<FavoriteStop> favorites = new ArrayList<FavoriteStop>();
	private World world;
	private final SimpleDateFormat formatter12 = new SimpleDateFormat("h:mm a");
	private final SimpleDateFormat formatter24 = new SimpleDateFormat("HH:mm");
	private final LayoutInflater inflater;
	private final Context ctx;
	private final DatabaseHelper db;
	private final SharedPreferences prefs;
	private int expandedChild = -1;
	private int expandedGroup = -1;
	private String extraEtas;
	private final ExpandableListView parent;
	private final TrackerTabActivity tabActivity;
	
	public EtaListAdapter(TrackerTabActivity tabActivity, ExpandableListView parent, Context context, LayoutInflater li, SharedPreferences prefs) {
		this.inflater = li;
		this.ctx = context;
		this.prefs = prefs;
		db = new DatabaseHelper(context);
		this.parent = parent;
		this.tabActivity = tabActivity;
	}
	
	public void setRoutes(World world) {
		this.world = world;
		this.parents = this.world.getRouteList();
		loadFavorites();
		
		notifyDataSetInvalidated();
	}
	
	public void putEtas(List<EtaJson> etaList) {
        for (EtaJson eta : etaList) {
            if (eta.getStop_id().equals("brinsmade"))
                Log.d("Tracker", eta.getRetrievalTime() + " " + eta.getEta());
            EtaJson tempEta = etas.get(eta.getStop_id() + eta.getRoute());
            if (tempEta == null) {
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
			return parents.get(groupPosition - 1).getStopList().size();
		else
			return parents.get(groupPosition).getStopList().size();
	}

	public Route getGroup(int groupPosition) {
		if (favorites.size() > 0 && groupPosition == 0)
			return null;
		else if (favorites.size() > 0) 
			return parents.get(groupPosition - 1);
		else
			return parents.get(groupPosition);
	}

	public Stop getChild(int groupPosition, int childPosition) {
		if (favorites.size() > 0 && groupPosition == 0)
			return world.getStops().get(favorites.get(childPosition).stopId);
		else if (favorites.size() > 0)
			return parents.get(groupPosition - 1).getStopList().get(childPosition);
		else
			return parents.get(groupPosition).getStopList().get(childPosition);
	}
	
	public String getStopId(int groupPosition, int childPosition) {
		if (favorites.size() > 0 && groupPosition == 0)
			return favorites.get(childPosition).stopId;
		else if (favorites.size() > 0)
			return parents.get(groupPosition - 1).getStopList().get(childPosition).getId();
		else
			return parents.get(groupPosition).getStopList().get(childPosition).getId();
	}
	
	public int getRouteId(int groupPosition, int childPosition) {
		if (favorites.size() > 0 && groupPosition == 0)
			return favorites.get(childPosition).routeId;
		else if (favorites.size() > 0) 
			return parents.get(groupPosition - 1).getId();
		else
			return parents.get(groupPosition).getId();
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
		
		Route route;
		Stop stop;
		EtaJson eta;
		
		if (favorites.size() > 0 && groupPosition == 0) {
			FavoriteStop fav = favorites.get(childPosition);
			stop = world.getStops().get(fav.stopId);
			eta = etas.get(fav.getUniqueId());
			subText.setVisibility(View.VISIBLE);
			subText.setText(world.getRoutes().get(fav.routeId).getName());
		} else if (favorites.size() > 0) {
			route = parents.get(groupPosition - 1);
			stop = route.getStopList().get(childPosition);
			eta = etas.get(stop.getId() + route.getId());
			subText.setVisibility(View.GONE);
		} else {
			route = parents.get(groupPosition);
			stop = route.getStopList().get(childPosition);
			eta = etas.get(stop.getId() + route.getId());
			subText.setVisibility(View.GONE);
		}
		
		String etaString = "";
		
		if (eta != null) {
			long now = (new Date()).getTime();
			long arrival = eta.getEta() - (now - eta.getRetrievalTime());
			
			etaString = (arrival / 1000 / 60) + " minutes";
		}
		 
		if (groupPosition == expandedGroup && childPosition == expandedChild) {
			etaString = extraEtas != null ? extraEtas : etaString + "\nLoading...";
		}
		
		text.setText(stop.getName());
		timeText.setText(etaString);
		
		return v;
	}
	
	public void addFavorite(int groupPosition, int childPosition) {
		if (favoritesVisible())
			groupPosition--;
		
		Route r = world.getRouteList().get(groupPosition);
		Stop s = r.getStopList().get(childPosition);
		FavoriteStop fav = new FavoriteStop(r.getId(), s.getId());
		
		if (!favorites.contains(fav)) {
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
		return favorites.size() > 0;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	public String getRouteName(int routeId) {
		return world.getRoutes().get(routeId).getName();
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
	
	public FavoriteStop getFavoriteStop(int childPosition) {
		return favorites.get(childPosition);
	}
	
	public void setExtraEtas(ExtraEtaJson etas) {
		String text = "Could not get\narrival times.";
		Date now = new Date();
		String time;
		
		if (etas != null) {
			Collections.sort(etas.getEta());
			text = "";
			Integer t;
			for (int i = 0; i < 8; i++) {
				if (i < etas.getEta().size()) {
					t = etas.getEta().get(i);
					if (DateFormat.is24HourFormat(ctx)) {
						time = formatter24.format(new Date(now.getTime() + t - (now.getTime() - etas.getRetrievalTime())));
					} else {
						time = formatter12.format(new Date(now.getTime() + t - (now.getTime() - etas.getRetrievalTime())));
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
	
	final OnClickListener buttonClickListener = new OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.button1:
				tabActivity.showMap(getChild(expandedGroup, expandedChild).getId());
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
