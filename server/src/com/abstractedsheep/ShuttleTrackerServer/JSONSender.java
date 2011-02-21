package com.abstractedsheep.ShuttleTrackerServer;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashSet;

import com.abstractedsheep.extractor.Shuttle;

/**
 * Connects to http://www.abstractedsheep.com/phpMyAdmin/ and writes data to the
 * DB
 * 
 * @author jonnau
 * 
 */
public class JSONSender {
	private static Connection conn;
	
	private static void connectToDatabase() throws InstantiationException, IllegalAccessException,
											ClassNotFoundException, IOException, SQLException {
		String driver = "com.mysql.jdbc.Driver";
		String[] args = null;
		
		Class.forName(driver).newInstance();
		args = getArgumentsFromPropertiesFile("sts.properties");
		conn = DriverManager.getConnection(args[0], args[1], args[2]);
		
		deleteTable(args[2]);
	}
	
	/**
	 * save this data to the database
	 * 
	 * @param shuttleList
	 */
	public static void saveToDatabase(HashSet<Shuttle> shuttleList) {
//		String driver = "com.mysql.jdbc.Driver";
//		Connection connection = null;
//		String[] args = null;
		try {
			connectToDatabase();
//			Class.forName(driver).newInstance();
//			args = getArgumentsFromPropertiesFile("sts.properties");
//			connection = DriverManager.getConnection(args[0], args[1], args[2]);
			System.out.println("Connected to database");
			Statement stmt = conn.createStatement();
			for (Shuttle shuttle : shuttleList) {
				for (String stop : shuttle.getStopETA().keySet()) {
					//update table in DB
					String sql = "UPDATE shuttle_eta SET eta = '"
							+ shuttle.getStopETA().get(stop)
							+ "' WHERE shuttle_id = " + shuttle.getShuttleId()
							+ " AND stop_id = '"
							+ shuttle.getStops().get(stop).getShortName() + "' AND route = '" +
							shuttle.getRouteId() + "'";
					int updateCount = stmt.executeUpdate(sql);

					if (updateCount == 0) {
						String insertHeader = "INSERT INTO shuttle_eta (shuttle_id, stop_id, eta, route)\n";
						String interValues = "VALUES ("
								+ shuttle.getShuttleId() + ",'"
								+ shuttle.getStops().get(stop).getShortName()
								+ "','"
								+ shuttle.getStopETA().get(stop)
								+ "', '"
								+ shuttle.getRouteId() + "')";
						stmt.executeUpdate(insertHeader + interValues);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
			}
		}
		
		printToConsole(shuttleList);
	}
	
	private static void deleteTable(String tableName) {
		try {
			Statement stm = conn.createStatement();
			String sql = "TRUNCATE TABLE shuttle_eta";
			
			stm.executeUpdate(sql);
		} catch(SQLException e) {}
	}

	public static void printToConsole(HashSet<Shuttle> shuttleList) {
		for(Shuttle shuttle : shuttleList) {
			System.out.println(shuttle.getName() + " " + shuttle.getShuttleId() + " " + shuttle.getRouteName() + " " + shuttle.getRouteId());
			for(String name : shuttle.getStopETA().keySet()) {
				System.out.println("\t" + name + " " + getTimeStamp(shuttle.getStopETA().get(name)) + " " + (shuttle.getStopETA().get(name)) / (1000 * 60));
			}
		}
	}

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

	private static String getTimeStamp(Integer integer) {
		String str = new Timestamp(System.currentTimeMillis() + integer)
				.toString();
		return str.substring(0, str.indexOf('.'));
	}
}