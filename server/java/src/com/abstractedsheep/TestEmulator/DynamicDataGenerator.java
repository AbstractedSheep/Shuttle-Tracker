/*
 * Copyright 2011
 *
 *   This file is part of Mobile Shuttle Tracker.
 *
 *   Mobile Shuttle Tracker is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Mobile Shuttle Tracker is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Mobile Shuttle Tracker.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.abstractedsheep.TestEmulator;

import com.abstractedsheep.db.DatabaseWriter;
import com.abstractedsheep.extractor.Netlink.RouteJson;
import com.abstractedsheep.extractor.Netlink.RouteJson.RouteCoordinateJson;
import com.abstractedsheep.extractor.Netlink.StopJson;
import com.abstractedsheep.extractor.Netlink.StopJson.StopRouteJson;
import com.abstractedsheep.extractor.StaticJSONExtractor;
import com.abstractedsheep.world.Coordinate;
import com.abstractedsheep.world.Route;
import com.abstractedsheep.world.Shuttle;
import com.abstractedsheep.world.Stop;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

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
        } catch (JsonGenerationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void generateData() throws InterruptedException, JsonGenerationException, IOException {
        while (true) {
            for (int i = 1; i <= SHUTTLES_TO_GENERATE; i++)
                shuttleList.put(i, createShuttle(i));
            printShuttleData();
            writeShuttleDataToFile();
            //write shuttle data to DB
            Thread.sleep(5000);
        }
    }

    public void printShuttleData() {
        String str = "ID: {%s}, Current Route: {%s}, Average Speed: {%s}";
        for (Shuttle s : shuttleList.values()) {
            String printMsg = String.format(
                    str, new Object[]{s.getShuttleId(), s.getCurrentRoute().getIdNum(), s.getSpeed()});
            System.out.println(printMsg);
        }
    }

    protected void writeShuttleDataToTable() {
        DatabaseWriter dbWriter = new DatabaseWriter();

    }

    protected void writeShuttleDataToFile() throws JsonGenerationException, IOException {
        JsonFactory f = new JsonFactory();
        JsonGenerator g = f.createJsonGenerator(new File("current.js"), JsonEncoding.UTF32_LE);
        g.writeStartObject();
        g.writeArrayFieldStart("Shuttles");
        for (Shuttle s : this.shuttleList.values()) {
            g.writeStartObject();
            g.writeStringField("shuttle_name", s.getName());
            g.writeNumberField("shuttle_id", s.getShuttleId());
            g.writeObjectFieldStart("Location");
            g.writeNumberField("latitude", s.getCurrentLocation().getLatitude());
            g.writeNumberField("longitude", s.getCurrentLocation().getLongitude());
            g.writeEndObject();
            g.writeNumberField("Speed", s.getSpeed());
            g.writeEndObject();
        }
        g.writeEndArray();
        g.writeEndObject();
        g.close();
    }

    private Shuttle createShuttle(int shuttle_id) {
        Random r = new Random();
        Collection<Route> list = routeList.values();
        int index = 0;
        while (index == 0) {
            index = r.nextInt(list.size() + 1);
        }
        Route currentRoute = routeList.get(index);
        Shuttle s = shuttleList.get(shuttle_id);

        if (shuttleList.get(shuttle_id) != null) {
            s.setSpeed(r.nextInt(30));
            double distanceTraveled = (0.00138888889) * s.getSpeed();
            Coordinate endPoint = s.getCurrentRoute().getCoordinateList().get(s.getNextRouteCoordinate());
            s.setCurrentLocation(s.getCurrentLocation().findCoordinateInLine(distanceTraveled, endPoint));
        } else {
            s = new Shuttle(shuttle_id, new ArrayList<Route>(list));
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

        for (RouteJson r : staticData.getRouteList()) {
            this.addRoute(r);
        }

        for (StopJson stop : staticData.getStopList()) {
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
        for (Integer i : routes) {
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
            coords.add(new Coordinate(rc.getLatitude(), rc.getLongitude()));
        }

        Route route = new Route(r.getId(), r.getName(), (ArrayList<Coordinate>) coords);
        routeList.put(route.getIdNum(), route);
    }
}
