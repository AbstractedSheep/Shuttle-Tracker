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

package com.abstractedsheep.config;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: ujonnalagadda
 * Date: Jul 21, 2011
 * Time: 5:55:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBProperties extends AbstractProperties {
    public static final Property USER_NAME = new Property("user", "r00t");
    public static final Property PASSWORD = new Property("password", null);
    public static final Property STOP_TABLE_NAME = new Property("stopTable", "stops");
    public static final Property SHUTTLE_TABLE_NAME = new Property("shuttleTable", "shuttles");
    public static final Property ROUTE_TABLE_NAME = new Property("routeTable", "routes");
    public static final Property TEST_DB_LINK = new Property("testDB", "");
    public static final Property ETA_DB_LINK = new Property("etaDB", "");
    public static final Property SHUTTLE_TIMEOUT = new Property("timeout", "45000");

    public static void main(String[] args) {

        try {
            DBProperties.loadProperties("/Users/ujonnalagadda/Shuttle-Tracker/server/java/conf/db.properties");
            System.out.println(DBProperties.TEST_DB_LINK);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
}
