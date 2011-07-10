package com.abstractedsheep.extractor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.abstractedsheep.extractor.Netlink.RouteJson;
import com.abstractedsheep.extractor.Netlink.StopJson;

public class StaticJSONExtractor extends AbstractJSONExtractor {
	public ArrayList<RouteJson> routeList;
	public ArrayList<StopJson> stopList;
	private ObjectMapper mapper;

	public StaticJSONExtractor(URL u) {
		super(u);
		routeList = new ArrayList<RouteJson>();
		stopList = new ArrayList<StopJson>();
		this.mapper = new ObjectMapper();
	}

	@Override
	public void readDataFromURL() {
		try {
			Netlink link = mapper.readValue(System.in, Netlink.class);
			routeList = link.getRoutes();
			stopList = link.getStops();
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @return the routeList
	 */
	public ArrayList<RouteJson> getRouteList() {
		return routeList;
	}

	/**
	 * @return the stopList
	 */
	public ArrayList<StopJson> getStopList() {
		return stopList;
	}

	public static void main(String[] args) {
		try {
			new URL("http://shuttles.rpi.edu/displays/netlink.js");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
