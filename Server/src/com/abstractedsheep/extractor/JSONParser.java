/**
 * 
 */
package com.abstractedsheep.extractor;

import java.util.ArrayList;
import java.util.HashMap;

import com.abstractedsheep.shuttletracker.shared.Stop;

/**
 * @author jonnau
 * @author
 */
public class JSONParser {
	/**
	 * write a stop object from the given values in the arraylist.
	 * @param list
	 * @return stop object
	 */
	public static Stop listToStop(ArrayList<String> list) {
		Stop stop = new Stop();
		stop.setLat(Double.parseDouble(list.get(0)));
		stop.setLon(Double.parseDouble(list.get(1)));
		stop.setName(list.get(2));
		stop.setShortName(list.get(3));
		
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		
		for(int i = 4; i < list.size(); i+= 2) {
			map.put(Integer.parseInt(list.get(i)), list.get(i + 1));
		}
		
		stop.setRouteMap(map);
		return stop;
	}
}
