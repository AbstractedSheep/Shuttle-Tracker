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
import com.abstractedsheep.config.STSProperties;
import com.abstractedsheep.world.Coordinate;
import com.abstractedsheep.world.Route;
import com.abstractedsheep.world.Shuttle;
import com.abstractedsheep.world.Stop;

import java.awt.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
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
        int i = 0;
        String name = res.getString("name");
        String sql = String.format("select asText(route_coords.location) from route_coords where route_id = %d" +
                " order by seq", new Object[]{idNum});

        r = this.executeQuery(conn, sql);
        pt = null;
        while (r.next()) {
            String[] obj = r.getString("asText(route_coords.location)").split(" ");
            Double lat = Double.parseDouble(obj[0].substring(obj[0].indexOf("T") + 2));
            Double lon = Double.parseDouble(obj[1].substring(0, obj[1].length() - 1));
            list.add(new Coordinate(lat, lon));
        }
        Route route = new Route(idNum, name, list);
        return route;
    }

    private Stop parseToStop(ResultSet res) throws SQLException {
        ResultSet r = null;
        ArrayList<Integer> list = new ArrayList<Integer>();

        String shortName = res.getString("stop_id");
        String name = res.getString("name");
        String[] obj = res.getString("asText(stops.location)").split(" ");
        Double lat = Double.parseDouble(obj[0].substring(obj[0].indexOf("T") + 2));
        Double lon = Double.parseDouble(obj[1].substring(0, obj[1].length() - 1));
        Coordinate pt = new Coordinate(lat, lon);
        String sql = String.format("select route_id from stop_routes where stop_id = '%s'", new Object[]{
                shortName});
        r = this.executeQuery(conn, sql);

        while (r.next()) {
            list.add(r.getInt("route_id"));
        }
        Stop st = new Stop(pt, shortName, name);
        st.setRoutesToAdd(list);
        return st;
    }

    private Shuttle parseToShuttle(ResultSet res) throws SQLException {
        Shuttle s = new Shuttle();
        s.setName(res.getString("name"));
        s.setSpeed(res.getInt("speed"));
        String[] obj = res.getString("asText(shuttle_coords.location)").split(" ");
        Double lat = Double.parseDouble(obj[0].substring(obj[0].indexOf("T") + 2));
        Double lon = Double.parseDouble(obj[1].substring(0, obj[1].length() - 1));
        s.setCurrentLocation(new Coordinate(lat, lon), res.getTimestamp("update_time").getTime());
        s.setShuttleId(res.getInt("shuttle_id"));
        s.setHeading(res.getInt("heading"));
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
            sql = "SELECT shuttles.shuttle_id, asText(shuttle_coords.location), shuttle_coords.route_id," +
                    " shuttles.name, shuttle_coords.speed, shuttle_coords.update_time, shuttle_coords.heading" +
                    " FROM shuttles, shuttle_coords WHERE " +
                    "shuttles.shuttle_id=shuttle_coords.shuttle_id " +
                    "AND (now() - shuttle_coords.update_time) < (500 * 1000)";
        } else if (classType.getSimpleName().equals("Stop")) {
            sql = "SELECT stops.stop_id, asText(stops.location), stops.name" +
                    " FROM stop_routes, stops WHERE stop_routes.stop_id = stops.stop_id";
        } else if (classType.getSimpleName().equals("Route")) {
            sql = "SELECT routes.route_id, routes.name FROM routes";
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

    public static void main(String[] args) {
        String driver = "com.mysql.jdbc.Driver";
        try {
            String applicationPropertiesPath = "C:/Users/jonnau/Documents/Android projects/Shuttle-Tracker/server/java/conf/sts.properties";
            STSProperties.loadProperties(applicationPropertiesPath);
            DBProperties.loadProperties(STSProperties.DB_PATH.toString());
            Class.forName(driver).newInstance();
            Connection conn = DriverManager.getConnection(DBProperties.TEST_DB_LINK.toString(),
                    DBProperties.USER_NAME.toString(), DBProperties.PASSWORD.toString());
            DatabaseReader reader = new DatabaseReader(conn);
            ArrayList<Route> list = new ArrayList<Route>();
            list.addAll((Collection<? extends Route>) reader.readData(Stop.class));
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
