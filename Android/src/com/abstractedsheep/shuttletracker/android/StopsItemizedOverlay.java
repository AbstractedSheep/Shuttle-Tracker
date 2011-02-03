package com.abstractedsheep.shuttletracker.android;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.LayoutParams;
import com.google.android.maps.OverlayItem;

public class StopsItemizedOverlay extends ItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();
	private Context context;
	private MapView map;
	private LayoutInflater inflater;
	private int index = -1;
	
	public StopsItemizedOverlay(Drawable defaultMarker) {
		super(boundCenter(defaultMarker));
	}
	
	public StopsItemizedOverlay(Drawable defaultMarker, Context context, MapView map, LayoutInflater li) {
		super(boundCenter(defaultMarker));
		this.context = context;
		this.map = map;
		this.inflater = li;
	}

	public void addOverlay(OverlayItem overlay) {
		this.overlays.add(overlay);
		populate();
	}
	
	
	@Override
	protected OverlayItem createItem(int i) {
		return this.overlays.get(i);
	}

	@Override
	public int size() {
		return this.overlays.size();
	}
	
	@Override
	protected boolean onTap(int index) {
		OverlayItem item = this.overlays.get(index);
		View balloon = inflater.inflate(R.layout.balloon, null);
		MapView.LayoutParams lp = new MapView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, item.getPoint(), LayoutParams.BOTTOM_CENTER);
		balloon.setLayoutParams(lp);
		
		TextView title = (TextView) balloon.findViewById(R.id.balloon_title);
		title.setText(item.getTitle());
		TextView desc = (TextView) balloon.findViewById(R.id.balloon_text);
		if (item.getSnippet() == null || item.getSnippet().equals(""))
			desc.setVisibility(View.GONE);
		else
			desc.setText(item.getSnippet());
		
		if (this.index >= 0)
			map.removeViewAt(this.index);
		
		this.index = map.getChildCount();
		
		map.addView(balloon);

		return true;
	}

}
