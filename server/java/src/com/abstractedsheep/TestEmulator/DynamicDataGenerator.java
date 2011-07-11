package com.abstractedsheep.TestEmulator;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.abstractedsheep.extractor.Netlink.RouteJson;
import com.abstractedsheep.extractor.Netlink.RouteJson.RouteCoordinateJson;
import com.abstractedsheep.extractor.Netlink.StopJson;
import com.abstractedsheep.extractor.Netlink.StopJson.StopRouteJson;
import com.abstractedsheep.extractor.StaticJSONExtractor;
import com.abstractedsheep.world.Coordinate;
import com.abstractedsheep.world.Route;
import com.abstractedsheep.world.Shuttle;
import com.abstractedsheep.world.Stop;

public class DynamicDataGenerator {
	protected HashMap<Integer, Route> routeList;
	protected HashMap<String, Stop> stopList;
	protected HashMap<Integer, Shuttle> shuttleList;
	protected static final int SHUTTLES_TO_GENERATE = 4;
	
	public DynamicDataGenerator(URL url) {
		this.routeList = new HashMap<Integer, Route>();
		this.shuttleList = new HashMap<Integer, Shuttle>();
		this.stopList = new HashMap<String, Stop>();
		initStaticData(url);
		
		try {
			generateData();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void generateData() throws InterruptedException {
		while (true) {
			for (int i = 1; i <= SHUTTLES_TO_GENERATE; i++)
				shuttleList.put(i, createShuttle(i));
			printShuttleData();
			//write shuttle data to DB
			Thread.sleep(5000);
		}
	}

	public void printShuttleData() {
		String str = "ID: {%s}, Current Route: {%s}, Average Speed: {%s}";
		for (Shuttle s : shuttleList.values()) {
			String printMsg = String.format(
					str, new Object[] {s.getShuttleId(), s.getCurrentRoute().getIdNum(), s.getSpeed()});
			System.out.println(printMsg);
		}
	}

	private Shuttle createShuttle(int shuttle_id) {
		Random r = new Random();
		Collection<Route> list = routeList.values();
		int index = 0;
		while(index == 0) {
			index = r.nextInt(list.size() - 0);
		}
		Route currentRoute = routeList.get(index);
		Shuttle s = shuttleList.get(shuttle_id);
		
		if(shuttleList.get(shuttle_id) != null) {
			s.setSpeed(r.nextInt(30));
			double distanceTraveled = (0.00138888889) * s.getSpeed();
			Coordinate endPoint = s.getCurrentRoute().getCoordinateList().get(s.getNextRouteCoordinate());
			s.setCurrentLocation(s.getCurrentLocation().findCoordinateInLine(distanceTraveled, endPoint));
		} else {
			s = new Shuttle(shuttle_id, new ArrayList<Route>( list ));
			s.setSpeed(r.nextInt(30));
			int listSize = currentRoute.getCoordinateList().size();
			s.setCurrentLocation(currentRoute.getCoordinateList().get(r.nextInt(listSize - 1)));
			s.setCurrentRoute(currentRoute);
		}
		return s;
	}

	private void initStaticData(URL url) {
		StaticJSONExtractor staticData = new StaticJSONExtractor(url);
		
		staticData.readDataFromURL();
		
		for(RouteJson r : staticData.getRouteList()) {
			this.addRoute(r);
		}
		
		for(StopJson stop : staticData.getStopList()) {
			this.addStop(stop);
		}
	}
	
	private void addStop(StopJson stop) {
		List<Integer> routes = new ArrayList<Integer>();
        for (StopRouteJson sj : stop.getRoutes()) {
            routes.add(sj.getId());
        }
        
        Stop s = new Stop(new Coordinate(stop.getLatitude(), stop.getLongitude()), stop.getShort_name(), stop.getName());
        HashMap<Integer, Route> tempRouteList = this.routeList;
        for (Integer i : routes)
        {
            Route r = tempRouteList.get(i);
            s.addRoute(r);
            r.addStop(s);
            s.snapToRoute(r);
            routeList.put(i, r);
        }
        stopList.put(s.getShortName(), s);
	}

	private void addRoute(RouteJson r) {
		List<Coordinate> coords = new ArrayList<Coordinate>();
        for (RouteCoordinateJson rc : r.getCoords()) {
            coords.add(new Coordinate( rc.getLatitude(), rc.getLongitude()));
        }
        
        Route route = new Route(r.getId(), r.getName(), (ArrayList<Coordinate>) coords);
        routeList.put(route.getIdNum(), route);
	}
}
