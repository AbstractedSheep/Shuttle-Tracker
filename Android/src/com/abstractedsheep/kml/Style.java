package com.abstractedsheep.kml;

import android.graphics.Color;
import android.util.Log;

public class Style {
	public int color;
	public int width;
	
	
	public void setAttribute(String name, String value) {
		if (name.equalsIgnoreCase("color")) {
			int[] colors = hexStringToByteArray(value);
			this.color = Color.argb(colors[0], colors[1], colors[2], colors[3]);
		} else if (name.equalsIgnoreCase("width")) {
			this.width = Integer.parseInt(value);
		}
	}
	
	public static int[] hexStringToByteArray(String s) {
	    int len = s.length();
	    int[] data = new int[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (int) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}

}
