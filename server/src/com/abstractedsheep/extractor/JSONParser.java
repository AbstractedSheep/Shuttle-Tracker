/**
 * 
 */
package com.abstractedsheep.extractor;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author jonnau
 * @author
 */
public class JSONParser {
	/**
	 * write a stop object from the given values in the arraylist.
	 * 
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

		for (int i = 4; i < list.size(); i += 2) {
			map.put(Integer.parseInt(list.get(i)), list.get(i + 1));
		}

		stop.setRouteMap(map);
		return stop;
	}

	/**
	 * Constructs route object from the given list of values.
	 * 
	 * @param list
	 *            - values to construct desired object
	 * @return route object
	 */
	public static Route listToRoute(ArrayList<String> list) {
		Route route = new Route(Integer.parseInt(list.get(1)), list.get(2));
		for (int i = 4; i < list.size() - 1; i += 2) {
			route.putCoordinate(Double.parseDouble(list.get(i + 1)),
					Double.parseDouble(list.get(i)));
		}
		route.calculateBearings();
		return route;
	}

	/**
	 * Constructs shuttle object fromt he given list of values.
	 * 
	 * @param list
	 *            - values to construct desired object
	 * @param stopList
	 *            - list of stops
	 * @param routeList
	 * @return shuttle object
	 */
	public static Shuttle listToShuttle(ArrayList<String> list,
			ArrayList<Stop> stopList, ArrayList<Route> routeList) {
		Shuttle shuttle = new Shuttle(routeList);
		shuttle.setShuttleId(Integer.parseInt(list.get(0)));
		shuttle.setName(list.get(1));
		shuttle.setBearing(Integer.parseInt(list.get(2)));
		shuttle.setCurrentLocation(new Shuttle.Point(Double.parseDouble(list
				.get(3)), Double.parseDouble(list.get(4))));
		shuttle.setSpeed(Integer.parseInt(list.get(5)));
		shuttle.setCardinalPoint(list.get(list.size() - 1));
		// TODO: determine whether this shuttle goes on the west route or east
		// route since the shuttle
		// might not go to all of the listed stops.
		for (Stop stop : stopList) {
			shuttle.addStop(stop.getName(), stop);
		}
		return shuttle;
	}
}
