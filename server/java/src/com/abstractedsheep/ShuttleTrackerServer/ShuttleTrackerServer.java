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

package com.abstractedsheep.ShuttleTrackerServer;

import com.abstractedsheep.ShuttleTrackerService.ETACalculator;
import com.abstractedsheep.config.DBProperties;
import com.abstractedsheep.config.STSProperties;
import com.abstractedsheep.db.DatabaseWriter;
import com.abstractedsheep.extractor.DynamicJSONExtractor;
import com.abstractedsheep.extractor.StaticJSONExtractor;
import com.abstractedsheep.world.World;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;

/**
 * This is the main class that will run all of the server code. This class
 * retrieves the stop and route data upon initialization from {@linkplain
 * JSONExtractor.readRouteData()} and periodically gets the shuttle data from
 * {@linkplain com.abstractedsheep.extractor.JSONExtractor.readShuttleData()} every five seconds. The shuttle
 * data then undergoes some processing in order to determine the arrival times
 * to each stop on each shuttle's route, after which this arrival time data is
 * written to MySQL database.
 *
 * @author saiumesh
 */
public class ShuttleTrackerServer {

    private static final int SLEEP_INTERVAL = (1000 * 5);
    private final URL staticDataURL;
    private final URL dynamicDataURL;
    private final World world;
    private ETACalculator calc;

    public ShuttleTrackerServer() throws MalformedURLException {
        this.staticDataURL = new URL(
                "http://shuttles.rpi.edu/displays/netlink.js");
        dynamicDataURL = new URL("http://shuttles.rpi.edu/vehicles/current.js");
        this.world = new World(new StaticJSONExtractor(staticDataURL),
                new DynamicJSONExtractor(dynamicDataURL));
        this.calc = new ETACalculator();
        executeWorld();
    }

    private void executeWorld() {
        // XXX All updates and modifications to the world are accomplished
        // within it.
        this.world.generateWorld();
        while (true) {
            updateWorld();
            try {
                (new DatabaseWriter()).writeToDatabase(calc, "extra_eta");
            } catch (SQLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ClassNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InstantiationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            try {
                Thread.sleep(SLEEP_INTERVAL);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void updateWorld() {
        this.world.update();
        // update calculator's instance of the world before calculating the
        // etas.
        this.calc.updateWorld(world);
    }

    public static void initServer(String[] args) {
        // creates an instance of this server class and executes it
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String dbPropertiesPath = "";
        String loggingPath = "";
        String applicationPropertiesPath = "";
        try {
            DBProperties.loadDBProperties(STSProperties.DB_PATH.toString());
            new ShuttleTrackerServer();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}