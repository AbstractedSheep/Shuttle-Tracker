package com.abstractedsheep.extractor;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonToken;

import com.abstractedsheep.world.*;

public class DynamicJSONExtractor extends AbstractJSONExtractor{
	private HashMap<Integer, Shuttle> shuttleList;
	private HashMap<Integer, Route> routeList;
	public DynamicJSONExtractor(URL u, HashMap<Integer, Route> rtList) {
		super(u);
		shuttleList = new HashMap<Integer, Shuttle>();
		this.routeList = rtList;
	}
	
	public DynamicJSONExtractor(URL u) {
		super(u);
		shuttleList = new HashMap<Integer, Shuttle>();
	}
	
	public void setRouteList(HashMap<Integer, Route> list) {
		this.routeList = list;
	}

	@Override
	public void readDataFromURL() {
		try {
			parser = f.createJsonParser(url);
			parser.nextToken();
			while (parser.nextToken() != JsonToken.END_ARRAY) { // keep reading the
				// stops array until
				// you have reached
				// the end of it.
				if (parser.getCurrentName() != null 
						&& !parser.getCurrentToken().equals(JsonToken.FIELD_NAME)) {
					if (!parser.getCurrentName().equals("vehicle")
							&& !parser.getCurrentName().equals("latest_position")
							&& !parser.getCurrentName().equals("icon")) {
						this.extractedValueList2.add(parser.getText());
					}
			
					if (parser.getCurrentName().equals("vehicle")
							&& parser.getText().equals("}")) {
						Shuttle s = this.parseData(extractedValueList2, (ArrayList<Route>) routeList.values());
						shuttleList.put(s.getShuttleId(), s);
						this.extractedValueList2.clear();
					}
				}
			}
		} catch (JsonParseException e) {
			System.err.println("Error: ");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				parser.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
	 * @throws ParseException 
	 */
	public Shuttle parseData(ArrayList<String> list,
			ArrayList<Route> routeList) throws ParseException {
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		dt.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date date = new Date(dt.parse(list.get(6)).getTime());
		long time = System.currentTimeMillis(), parsedTime = dt.parse(dt.format(date)).getTime();
		System.out.println((time - parsedTime) /(1000 * 60 * 60));
		Shuttle shuttle = new Shuttle(routeList);
		shuttle.setShuttleId(Integer.parseInt(list.get(0)));
		shuttle.setName(list.get(1));
		shuttle.setCurrentLocation(new Coordinate(Double.parseDouble(list
				.get(3)), Double.parseDouble(list.get(4))), time);
		shuttle.setSpeed(Integer.parseInt(list.get(5)));
		shuttle.setCardinalPoint(list.get(list.size() - 1));
		shuttle.setCurrentRoute(routeList.get(0));
		double d = shuttle.getDistanceToClosestPoint();
		
		for (int i = 1; i < routeList.size(); i++) {
			shuttle.snapToRoute(routeList.get(i));
			
			if(d > shuttle.getDistanceToClosestPoint()) {
				shuttle.setCurrentRoute(routeList.get(i));
				d = shuttle.getDistanceToClosestPoint();
			}
		}
		return shuttle;
	}
	
	public HashMap<Integer, Shuttle> getDynamicData() {
		return shuttleList;
	}
}
