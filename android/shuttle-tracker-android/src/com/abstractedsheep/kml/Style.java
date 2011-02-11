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

package com.abstractedsheep.kml;

import android.graphics.Color;

public class Style {
	public int color;
	public int width;
	
	
	public void setAttribute(String name, String value) {
		if (name.equalsIgnoreCase("color")) {
			int[] colors = hexStringToByteArray(value);
			this.color = Color.argb(colors[0], colors[3], colors[2], colors[1]);
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
