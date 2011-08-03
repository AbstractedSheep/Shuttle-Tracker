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

import com.abstractedsheep.ShuttleTrackerService.ETACalculator;
import com.abstractedsheep.ShuttleTrackerService.ETACalculator.Eta;
import com.abstractedsheep.config.DBProperties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;

/**
 * Connects to the server {@link www.abstractedsheep.com/phpMyAdmin/} and
 * writes data to the DB table TODO: This program writes specifically to one
 * table in the database called shutle_eta, for the sake of making the code more
 * robust, it might be better to include this table in the file sts.properties
 * along with the other data to connect to the database.
 *
 * @author saiumesh
 */
public class DatabaseWriter extends AbstractQueryRunner {
    private static Connection conn;

    // private final Logger log = Logger.getLogger(null);

    /**
     * Method connections to the MySQL server using the arguments in the file
     * sts.properties.
     *
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws SQLException
     */
    public static void connectToDatabase(String tableName)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException, IOException, SQLException {
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(DBProperties.TEST_DB_LINK.toString(),
                            DBProperties.USER_NAME.toString(), DBProperties.PASSWORD.toString());

        deleteTable(tableName);
    }

    private Connection createConnection(boolean isServer)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException, IOException, SQLException {
        String link = (isServer) ? DBProperties.ETA_DB_LINK.toString() : DBProperties.TEST_DB_LINK.toString();
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        return DriverManager.getConnection(link,
                            DBProperties.USER_NAME.toString(), DBProperties.PASSWORD.toString());
    }

    //TODO: pass this to AbstractQueryRunner.batch
    public void writeToDatabase(ETACalculator etaList,
                                String tableName) throws SQLException, IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (conn == null) {
            connectToDatabase(tableName);
        }
        String header = "INSERT INTO %s (shuttle_id, stop_id, eta_id, eta, absolute_eta, route)\n";
        String values = "VALUES ( %d,'%s',%d, %d, %d, '%d')";
        String insertQuery = header + values + " ON DUPLICATE KEY UPDATE ";
        String updateQuery = "eta=VALUES(eta), absolute_eta=VALUES(absolute_eta), route=VALUES(route)";
        Statement stmt = conn.createStatement();
        final String query = insertQuery + updateQuery;
        String sql = "";
        for (Eta eta : etaList.getETAs()) {
            sql = String.format(query, new Object[]{tableName, eta.shuttleId,
                    eta.stopId, eta.Id, eta.time, eta.arrivalTime, eta.routeId});

            stmt.addBatch(sql);
        }

        stmt.executeBatch();
    }

    public void runAsBatch(Connection conn, String query, Object[][] values) throws SQLException {
        this.batch(conn, query, values);
    }

    public void writeTestShutleData(String query, Object[][] values)
            throws IOException, ClassNotFoundException, SQLException,
                   IllegalAccessException, InstantiationException {
        Connection connection = this.createConnection(false);
        this.runAsBatch(connection, query, values);

    }
    //XXX now defunct
    public static void saveToDatabase(ETACalculator etaList, String tableName) {
        try {
            connectToDatabase(tableName);
            Statement stmt = conn.createStatement();
            MessageFormat f = null;
            for (Eta eta : etaList.getETAs()) {
                String query = "UPDATE {0} SET eta = '{1}'"
                        + "WHERE shuttle_id = {2} AND stop_id = '{3}'"
                        + " AND route = '{4}' AND eta_id = '{5}'"
                        + " AND absolute_eta = '{6}'";

                f = new MessageFormat(query);
                f.format(new Object[]{tableName, eta.time, eta.shuttleId,
                        eta.stopName, eta.routeId, eta.Id, eta.arrivalTime});
                query = f.toString();

                int updateCount = stmt.executeUpdate(query);

                if (updateCount == 0) {
                    String header = "INSERT INTO {0} (shuttle_id, stop_id, eta_id, eta, absolute_eta, route)\n";
                    String values = "VALUES ( {1},'{2}','{3}', '{4}', '{5}', '{6}')";
                    query = header + values;
                    f = new MessageFormat(query);
                    f.format(new Object[]{tableName, eta.shuttleId,
                            eta.stopName, eta.Id, eta.time, eta.arrivalTime,
                            eta.routeId});
                    query = f.toString();

                    stmt.executeUpdate(query);
                }
            }
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            // after writing the values to the DB, close the connection to the
            // database.
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
            }
        }
    }

    /**
     * Delete the values in the database table
     *
     * @param tableName this value is currently not used
     */
    private static void deleteTable(String tableName) {
        try {
            Statement stm = conn.createStatement();
            String sql = "TRUNCATE TABLE " + tableName;

            stm.executeUpdate(sql);
        } catch (SQLException e) {
        }
    }

    /**
     * @param path path to sts.properties file
     * @return returns the arguments from the sts.properties file
     * @throws IOException
     */
    private static String[] getArgumentsFromPropertiesFile(String path)
            throws IOException {
        String[] values = new String[3];
        File f = new File(path);
        System.out.println(f.getAbsolutePath());
        BufferedReader buf = new BufferedReader(new FileReader(f));
        String line = "";
        line = buf.readLine();
        for (int i = 0; (line != null); i++) {
            values[i] = line;
            line = buf.readLine();
        }
        buf.close();
        return values;
    }
}