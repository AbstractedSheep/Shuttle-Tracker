
package com.abstractedsheep.ShuttleTrackerServer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.*;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.*;

import com.abstractedsheep.extractor.Shuttle;

public class JSONSender {
	
	/**
	 * prints shuttle arrival time data to a text file in json format
	 * @param shuttleList - shuttle data
	 */
	public static void saveToFileAsJSON(ArrayList<Shuttle> shuttleList) {
		try {
			JsonFactory f = new JsonFactory();
			JsonGenerator gen = f.createJsonGenerator(new FileWriter(new File("shuttleOutputData.txt")));
			HashMap<String, Integer> map = null;
			
			//gen.writeArrayFieldStart("ShuttleETA");
			gen.writeStartObject();
			for(Shuttle shuttle : shuttleList) {
				gen.writeObjectFieldStart(shuttle.getName());
				gen.writeNumberField("Longitude", shuttle.getCurrentLocation().getLon());
				gen.writeNumberField("Latitude", shuttle.getCurrentLocation().getLat());
				gen.writeArrayFieldStart("ETA");
				map = shuttle.getStopETA();
				for(String stop : map.keySet()) {
					gen.writeString(stop + " " + map.get((stop)) + " " + shuttle.getStops().get(stop).toString());
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
}
