
package com.abstractedsheep.ShuttleTrackerServer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

import com.abstractedsheep.extractor.Shuttle;

/**
 * Connects to http://www.abstractedsheep.com/phpMyAdmin/ and writes data to the DB
 * @author jonnau
 *
 */
public class JSONSender {
	private final String url = ""; //URL to the DB
	private Connection conn;
	/**
	 * prints shuttle arrival time data to a text file in json format
	 * @param shuttleList - shuttle data
	 */
	public static void saveToFileAsJSON(HashSet<Shuttle> shuttleList) {
		try {
			JsonFactory f = new JsonFactory();
			JsonGenerator gen = f.createJsonGenerator(new FileWriter(new File("shuttleOutputData" + System.currentTimeMillis() + ".txt")));
			HashMap<String, Integer> map = null;
			
			//gen.writeArrayFieldStart("ShuttleETA");
			gen.writeStartObject();
			for(Shuttle shuttle : shuttleList) {
				gen.writeObjectFieldStart(shuttle.getName());
				gen.writeNumberField("Longitude", Shuttle.getCurrentLocation().getLon());
				gen.writeNumberField("Latitude", Shuttle.getCurrentLocation().getLat());
				gen.writeArrayFieldStart("ETA");
				map = shuttle.getStopETA();
				for(String stop : map.keySet()) {
					gen.writeString(stop + " " + getTimeStamp(map.get(stop)));
				}
				
				gen.writeEndArray();
				gen.writeEndObject();
			}
			//gen.writeEndArray();
			gen.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * save this data to the database
	 * @param shuttleList
	 */
	public static void saveToDatabase(HashSet<Shuttle> shuttleList) {
		String driver = "com.mysql.jdbc.Driver";
		Connection connection = null;
		try {
			Class.forName(driver);
			String serverName = "128.113.17.3";
			String dbName = "shuttle_tracker";
			
			String url = "jdbc:mysql://" + serverName +  "/" + dbName;
			String usr = "root";
			String pass = "salamander_s4";
			connection = DriverManager.getConnection(url, usr, pass);
			System.out.println("Connected to server");
			Statement stmt = connection.createStatement();
			for(Shuttle shuttle : shuttleList) {
				for(String stop : shuttle.getStopETA().keySet()){
					String sql = "UPDATE shuttle_eta SET eta = " + getTimeStamp(shuttle.getStopETA().get(stop)) +
								 "WHERE shuttle_id = " + shuttle.getShuttleId() + "AND stop_id = " +
								 shuttle.getStops().get(stop).getShortName();
					int updateCount = stmt.executeUpdate(sql);
					System.out.println(updateCount);
				}
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static String getTimeStamp(Integer integer) {
		String str = new Timestamp(System.currentTimeMillis() + integer).toString();
		return str.substring(0, str.indexOf('.'));
	}
}
