package com.abstractedsheep.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.abstractedsheep.world.*;

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
	 * @param c
	 *            - desired object type
	 * @return - a collection of objects of type c.
	 * @throws ClassNotFoundException
	 *             - thrown if the class is not of type Shuttle, Route or Stop.
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
		return list;
	}
}
