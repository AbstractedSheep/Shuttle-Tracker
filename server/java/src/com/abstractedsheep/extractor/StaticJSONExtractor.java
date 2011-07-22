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

import com.abstractedsheep.extractor.Netlink.RouteJson;
import com.abstractedsheep.extractor.Netlink.StopJson;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

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
            Netlink link = mapper.readValue(new InputStreamReader(url.openStream()), Netlink.class);
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
