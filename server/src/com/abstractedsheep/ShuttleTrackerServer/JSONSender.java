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
 * Connects to http://www.abstractedsheep.com/phpMyAdmin/ and writes data to the DB
 * @author jonnau
 *
 */
public class JSONSender {
	
	/**
	 * save this data to the database
	 * @param shuttleList
	 */
	public static void saveToDatabase(HashSet<Shuttle> shuttleList) {
		String driver = "com.mysql.jdbc.Driver";
		Connection connection = null;
		String[] args = null;
		//System.out.println((new File("sts.properties").getAbsolutePath()));
		try {
			Class.forName(driver).newInstance();
			args = getArgumentsFromPropertiesFile("sts.properties");
			connection = DriverManager.getConnection(args[0], args[1], args[2]);
			System.out.println("Connected to database");
			Statement stmt = connection.createStatement();
			for(Shuttle shuttle : shuttleList) {
				for(String stop : shuttle.getStopETA().keySet()){
					String sql = "UPDATE shuttle_eta SET eta = " + getTimeStamp(shuttle.getStopETA().get(stop)) +
								 "WHERE shuttle_id = " + shuttle.getShuttleId() + "AND stop_id = " +
								 shuttle.getStops().get(stop).getShortName();
					int updateCount = stmt.executeUpdate(sql);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try{
				if(connection != null)
					connection.close();
			} catch(SQLException e) {}
		}
	}
	private static String[] getArgumentsFromPropertiesFile(String path) throws IOException {
		String[] values = new String[3];
		BufferedReader buf = new BufferedReader(new FileReader(new File(path)));
		String line = "";
		line = buf.readLine();
		for(int i = 0; (line != null); i++) {
			values[i] = line;
			line = buf.readLine();
		}
		buf.close();
		return values;
	}
	private static String getTimeStamp(Integer integer) {
		String str = new Timestamp(System.currentTimeMillis() + integer).toString();
		return str.substring(0, str.indexOf('.'));
	}
}