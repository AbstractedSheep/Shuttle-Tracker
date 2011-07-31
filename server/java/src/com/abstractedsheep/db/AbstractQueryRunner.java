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
/**
 *
 */
package com.abstractedsheep.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The purpose of this class is to contain all of the generic sql read and write
 * actions. This class is mean to act as a parent class to other classes who
 * would write objects to mysql tables and vice versa.
 *
 * @author saiumesh
 */
public abstract class AbstractQueryRunner {

    protected ResultSet readDataFromTable(Connection conn, String tableName)
            throws SQLException {
        String query = String.format("select * from ?",
                new Object[]{tableName});
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }

    protected ResultSet executeQuery(Connection conn, String query) throws SQLException {
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }

    protected int[] batch(Connection conn, String query, Object[][] values)
            throws SQLException {
        Statement stmt = conn.createStatement();

        for (Object[] val : values) {
            stmt.addBatch(fillQuery(query, val));
        }

        return stmt.executeBatch();
    }

    private String fillQuery(String query, Object[] values) {
        return String.format(query, values);
    }
}
