package com.abstractedsheep.shuttletracker.android;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class TimestampOverlay extends Overlay {
	private Date lastUpdateTime = new Date();
	private SimpleDateFormat formatter12 = new SimpleDateFormat("h:mm:ss a");
	private SimpleDateFormat formatter24 = new SimpleDateFormat("HH:mm:ss");
	private boolean time24hour = false;
	private Paint paint = new Paint();
	private String statusText = "";
	
	public TimestampOverlay() {
		paint.setColor(Color.BLACK);
		paint.setAntiAlias(true);
		paint.setTextSize(20);
	}
	
	public TimestampOverlay(boolean time24hour) {
		this();
		this.time24hour = time24hour;
	}
	
	public void set24Hour(boolean time24hour) {
		this.time24hour = time24hour;
	}
	
	public boolean get24Hour() {
		return this.time24hour;
	}
	
	public void setStatusText(String statusText) {
		this.statusText = statusText;
	}
	
	public String getStatusText() {
		return this.statusText;
	}
	
	public void setLastUpdateTime(Date lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		String text = time24hour ? formatter24.format(lastUpdateTime) : formatter12.format(lastUpdateTime);
		if (statusText != null && !statusText.equals(""))
			text += " - " + statusText;
		canvas.drawText(text, 10, 20, paint);
	}
}
