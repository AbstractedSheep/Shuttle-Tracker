package com.abstractedsheep.ShuttleTrackerServer;

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
		try {
			Class.forName(driver).newInstance();
			String serverName = "128.113.17.3:3306";
			String dbName = "shuttle_tracker";
			String url = "jdbc:mysql://" + serverName +  "/" + dbName;
			String usr = "";
			String pass = "";
			connection = DriverManager.getConnection(url, usr, pass);
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
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try{
				if(connection != null)
					connection.close();
			} catch(SQLException e) {}
		}
	}
	private static String getTimeStamp(Integer integer) {
		String str = new Timestamp(System.currentTimeMillis() + integer).toString();
		return str.substring(0, str.indexOf('.'));
	}
}
