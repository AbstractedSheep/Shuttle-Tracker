package com.abstractedsheep.shuttletracker.mapoverlay;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

public class NullItemizedOverlay extends ItemizedOverlay<OverlayItem>{

	private MapView map;
	
	public NullItemizedOverlay(Drawable defaultMarker, MapView map) {
		super(defaultMarker);
		this.map = map;
		populate();
	}
	
	@Override
	protected boolean onTap(int index) {
		for (Overlay overlay : map.getOverlays()) {
			if (overlay instanceof BalloonItemizedOverlay<?>) {
				((BalloonItemizedOverlay<?>) overlay).hideBalloon();
			}
		}
		return true;
	}
	
	@Override
	protected boolean hitTest(OverlayItem item, Drawable marker, int hitX,
			int hitY) {		
		return true;
	}

	@Override
	protected OverlayItem createItem(int i) {
		return new OverlayItem(new GeoPoint(0,0), "", "");
	}

	@Override
	public int size() {
		return 1;
	}

}
