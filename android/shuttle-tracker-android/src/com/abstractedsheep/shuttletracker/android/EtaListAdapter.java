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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class EtaListAdapter extends BaseExpandableListAdapter {

	private HashMap<String, EtaJson> etas = new HashMap<String, EtaJson>();
	private ArrayList<Route> parents = new ArrayList<RoutesJson.Route>();
	private ArrayList<ArrayList<Stop>> children = new ArrayList<ArrayList<Stop>>();
	private ArrayList<Stop> favorites = new ArrayList<Stop>();
	private ArrayList<Integer> favoritesRoutes = new ArrayList<Integer>();
	SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
	LayoutInflater inflater;
	Context ctx;
	
	public EtaListAdapter(Context context, LayoutInflater li) {
		this.inflater = li;
		this.ctx = context;
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
	
	private boolean stopOnRoute(Stop stop, int routeId) {
		for (Stop.Route r : stop.getRoutes()) {
			if (r.getId() == routeId)
				return true;
		}
		return false;
	}

	public Route getGroup(int groupPosition) {
		if (favorites.size() > 0 && groupPosition == 0)
			return new Route();
		else if (favorites.size() > 0) 
			return parents.get(groupPosition - 1);
		else
			return parents.get(groupPosition);
	}

	public Stop getChild(int groupPosition, int childPosition) {
		if (favorites.size() > 0 && groupPosition == 0)
			return favorites.get(groupPosition);
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
		View v = inflater.inflate(R.layout.simple_list_item_1, null);
		TextView tv = (TextView) v.findViewById(R.id.text1);
		
		Route route;
		Stop stop;
		EtaJson eta;
		
		if (favorites.size() > 0 && groupPosition == 0) {
			stop = favorites.get(childPosition);
			eta = etas.get(stop.getShort_name() + favoritesRoutes.get(childPosition));
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
			etaString = formatter.format(arrival);
		}
		
		tv.setText(stop.getName() + ": " + etaString);
		
		return v;
	}
	
	public void addFavorite(int groupPosition, int childPosition) {
		if (favoritesVisible())
			groupPosition--;
		
		if (!favorites.contains(children.get(groupPosition).get(childPosition))) {
			favorites.add(children.get(groupPosition).get(childPosition));
			favoritesRoutes.add(parents.get(groupPosition).getId());
		}
		

		saveFavorites();
		notifyDataSetInvalidated();
	}
	
	public void removeFavorite(int childPosition) {
		favorites.remove(childPosition);
		favoritesRoutes.remove(childPosition);
		
		notifyDataSetInvalidated();
	}
	
	public void saveFavorites() {
		ByteArrayOutputStream bos = null;
		try {
		        bos = new ByteArrayOutputStream();
		        ObjectOutputStream obj_out = new ObjectOutputStream(bos);
		        obj_out.writeObject(favorites);
		        
		        FileOutputStream fos = ctx.openFileOutput("favorite_stops", Context.MODE_PRIVATE);
		        fos.write(bos.toByteArray());
		        fos.close();
		} catch (IOException e) {
		        e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void loadFavorites() {
		try {
			FileInputStream fis = ctx.openFileInput("favorite_stops");
			byte fileContent[] = new byte[100000];
			fis.read(fileContent);
			fis.close();
			ByteArrayInputStream bis = new ByteArrayInputStream(fileContent);
	        ObjectInputStream obj_in = new ObjectInputStream(bis);

	        favorites = (ArrayList<Stop>) obj_in.readObject();
	        notifyDataSetInvalidated();
		} catch (IOException e) {
	        e.printStackTrace();
		} catch (ClassNotFoundException e) {
	        e.printStackTrace();
		}

	}
	
	public boolean favoritesVisible() {
		return favorites.size() > 0 ? true : false;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}
