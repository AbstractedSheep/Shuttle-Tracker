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
import com.abstractedsheep.config.STSProperties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
    @Deprecated
    public static void connectToDatabase(String tableName)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException, IOException, SQLException {
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(DBProperties.ETA_DB_LINK.toString(),
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
            conn = createConnection(!STSProperties.ENABLE_TESTING.asBoolean());
        }

        deleteTable(tableName);

        String header = "INSERT INTO %s (shuttle_id, stop_id, eta_id, eta, absolute_eta, route)\n";
        String values = "VALUES ( %d,\"%s\",%d, %d, %d, '%d')";
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

    /**
     * Delete the values in the database table
     *
     * @param tableName this value is currently not used
     */
    private static void deleteTable(String tableName) throws SQLException {
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