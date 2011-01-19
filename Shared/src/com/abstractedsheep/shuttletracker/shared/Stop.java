package com.abstractedsheep.shuttletracker.shared;

import java.util.HashMap;
/**
 * sample output
latitude 42.7302712352
longitude -73.6765441399
name Student Union
short_name union
id 1
name West Route
id 2
name East Campus

 * @author jonnau
 *
 */
public class Stop {
	private double lon, lat;
	private String name, shortName;
	private HashMap<Integer, String> routeMap;
}
