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

import com.abstractedsheep.config.DBProperties;
import com.abstractedsheep.world.Coordinate;
import com.abstractedsheep.world.Route;
import com.abstractedsheep.world.Shuttle;
import com.abstractedsheep.world.Stop;

import java.awt.*;
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

    private Object convertTableToObject(Class<?> c, ResultSet res) throws SQLException {
        if (c.getSimpleName().equals("Shuttle")) {
            return parseToShuttle(res);
        } else if (c.getSimpleName().equals("Stop")) {
            return parseToStop(res);
        } else if (c.getSimpleName().equals("Route")) {
            return parseToRoute(res);
        }

        return null;
    }

    private Route parseToRoute(ResultSet res) throws SQLException {
        Point pt = null;
        ResultSet r = null;
        ArrayList<Coordinate> list = new ArrayList<Coordinate>();

        int idNum = res.getInt("route_id");
        String name = res.getString("route_name");
        String sql = String.format("SELECT * FROM route_coords WHERE route_id= %d ORDER BY seq", new Object[] {
                idNum });
        r = this.executeQuery(conn, sql);
        pt = null;
        while(r.next()) {
            pt = (Point) r.getObject("location");
            list.add(new Coordinate(pt.getX(), pt.getY()));
        }
        Route route = new Route(idNum, name, list);
        return route;
    }

    private Stop parseToStop(ResultSet res) {
        // TODO Auto-generated method stub
        return null;
    }

    private Shuttle parseToShuttle(ResultSet res) throws SQLException {
        Shuttle s = new Shuttle();
        s.setName(res.getString("name"));
        s.setSpeed(res.getInt("speed"));
        String[] obj = res.getString("asText(location)").split(" ");
        Double lat = Double.parseDouble(obj[0].substring(obj[0].indexOf("T") + 2));
        Double lon = Double.parseDouble(obj[1].substring(0, obj[1].length() - 1));
        s.setCurrentLocation(new Coordinate(lat, lon), res.getLong("update_time"));
        s.setShuttleId(res.getInt("shuttle_id"));
        //remember to set the route later
        s.setRouteId(res.getInt("route_id"));
        return s;
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
        String sql = "SELECT * FROM ";
        if (classType.getSimpleName().equals("Shuttle")) {
            tableName = DBProperties.SHUTTLE_TABLE_NAME.toString();
            sql = "SELECT shuttle_id, asText(location), route_id, name, speed, update_time FROM " + tableName;
        } else if (classType.getSimpleName().equals("Stop")) {
            sql += DBProperties.STOP_TABLE_NAME.toString();
        } else if (classType.getSimpleName().equals("Route")) {
            sql += DBProperties.ROUTE_TABLE_NAME.toString();
        } else {
            throw new ClassNotFoundException();
        }

        Collection<Object> list = new ArrayList<Object>();
        ResultSet res = this.readDataFromTable(conn, sql);
        // populate list using convertTableToObject
        while (res.next())
            list.add(this.convertTableToObject(classType, res));
        return list;
    }
}
