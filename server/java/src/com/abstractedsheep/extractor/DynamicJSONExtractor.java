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

package com.abstractedsheep.extractor;

import com.abstractedsheep.world.Coordinate;
import com.abstractedsheep.world.Route;
import com.abstractedsheep.world.Shuttle;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonToken;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class DynamicJSONExtractor extends AbstractJSONExtractor {
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
                        Shuttle s = this.parseData(extractedValueList2, routeList.values().toArray());
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
     * @param list      - values to construct desired object
     * @param routeList
     * @return shuttle object
     * @throws ParseException
     */
    public Shuttle parseData(ArrayList<String> list,
                             Object[] routeList) throws ParseException {
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dt.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = new Date(dt.parse(list.get(6)).getTime());
        long time = System.currentTimeMillis(), parsedTime = dt.parse(dt.format(date)).getTime();
        System.out.println((time - parsedTime) / (1000 * 60 * 60));
        Shuttle shuttle = new Shuttle();
        shuttle.setShuttleId(Integer.parseInt(list.get(0)));
        shuttle.setHeading(Integer.parseInt(list.get(2)));
        shuttle.setName(list.get(1));
        shuttle.setCurrentLocation(new Coordinate(Double.parseDouble(list
                .get(3)), Double.parseDouble(list.get(4))), time);
        shuttle.setSpeed(Integer.parseInt(list.get(5)));
        shuttle.setCardinalPoint(list.get(list.size() - 1));
        shuttle.setCurrentRoute((Route) routeList[0]);
        double d = shuttle.getDistanceToClosestPoint();
        Route curr = (Route) routeList[0];
        for (int i = 1; i < routeList.length; i++) {
            shuttle.setCurrentRoute((Route) routeList[i]);

            if (d > shuttle.getDistanceToClosestPoint()) {
                curr = shuttle.getCurrentRoute();
                d = shuttle.getDistanceToClosestPoint();
            } else {
                shuttle.setCurrentRoute(curr);
            }
        }
        return shuttle;
    }

    public HashMap<Integer, Shuttle> getDynamicData() {
        return shuttleList;
    }
}
