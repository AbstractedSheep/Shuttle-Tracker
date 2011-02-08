
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
	private static String getTimeStamp(Integer integer) {
		String str = new Timestamp(System.currentTimeMillis() + integer).toString();
		return str.substring(0, str.indexOf('.'));
	}
}
