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

package com.abstractedsheep.db;

import com.abstractedsheep.world.Route;
import com.abstractedsheep.world.Shuttle;
import com.abstractedsheep.world.Stop;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class DatabaseReader extends AbstractQueryRunner {
    private Connection conn;

    public DatabaseReader(Connection conn) {
        this.conn = conn;
    }

    private Object convertTableToObject(Class<?> c, ResultSet res) {
        if (c.getSimpleName().equals("Shuttle")) {
            return parseToShuttle(res);
        } else if (c.getSimpleName().equals("Stop")) {
            return parseToStop(res);
        } else if (c.getSimpleName().equals("Route")) {
            return parseToRoute(res);
        }

        return null;
    }

    private Route parseToRoute(ResultSet res) {
        // TODO Auto-generated method stub
        return null;
    }

    private Stop parseToStop(ResultSet res) {
        // TODO Auto-generated method stub
        return null;
    }

    private Shuttle parseToShuttle(ResultSet res) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Contacts the mysql database and sends back a list of object with the
     * desired type.
     *
     * @param c - desired object type
     * @return - a collection of objects of type c.
     * @throws ClassNotFoundException - thrown if the class is not of type Shuttle, Route or Stop.
     * @throws SQLException
     */
    public Collection<?> readData(Class<?> classType)
            throws ClassNotFoundException, SQLException {
        String tableName = "";
        if (classType.getSimpleName().equals("Shuttle")) {
            tableName = "shuttle";
        } else if (classType.getSimpleName().equals("Stop")) {
            tableName = "stops";
        } else if (classType.getSimpleName().equals("Route")) {
            tableName = "route";
        } else {
            throw new ClassNotFoundException();
        }

        Collection<Object> list = new ArrayList<Object>();
        ResultSet res = this.readDataFromTable(conn, tableName);
        // populate list using convertTableToObject
        while (res.next())
            list.add(this.convertTableToObject(classType, res));
        return list;
    }
}
