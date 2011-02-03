package com.abstractedsheep.shuttletracker.android;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class StopsItemizedOverlay extends ItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();
	private Context context;
	
	public StopsItemizedOverlay(Drawable defaultMarker) {
		super(boundCenter(defaultMarker));
	}
	
	public StopsItemizedOverlay(Drawable defaultMarker, Context context) {
		super(boundCenter(defaultMarker));
		this.context = context;
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
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		dialog.show();
		return true;
	}

}
