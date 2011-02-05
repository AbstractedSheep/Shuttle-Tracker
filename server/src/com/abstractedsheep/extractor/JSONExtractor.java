package com.abstractedsheep.extractor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 * The purpose of this class is to extract the jsons from the rpi shuttle server and process the data.
 * @author jonnau
 *
 */
public class JSONExtractor{
	private URL routeURL;
	private URL shuttleURL;
	private JsonParser parser;
	private JsonFactory f; //not required globally
	private ArrayList<String> extractedValueList, extractedValueList2; //will hold values from the JSON
	//these two variables are not used atm, but would store the data from the json.
	private ArrayList<Stop> stopList;
	private ArrayList<Route> routeList;
	private HashSet<Shuttle> shuttleList;
	
	public JSONExtractor() {
		f = new JsonFactory();
		try {
			//get link to stops and shuttles
			routeURL = new URL("http://shuttles.rpi.edu/displays/netlink.js");
			shuttleURL = new URL("http://shuttles.rpi.edu/vehicles/current.js");
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		extractedValueList = new ArrayList<String>();
		extractedValueList2 = new ArrayList<String>();
		stopList = new ArrayList<Stop>();
		routeList = new ArrayList<Route>();
		shuttleList = new HashSet<Shuttle>();
	}
	
	//TODO: data shows up in one line, need to make a new method/class to parse json data.
	/**
	 * the purpose of this method is to extract the data from the routes json and store the values in
	 * StopList and RouteList.
	 */
	public void readRouteData() throws IOException {
		parser = f.createJsonParser(routeURL);
		parser.nextToken();
		while(parser.nextToken() != JsonToken.END_OBJECT){
			//parser.nextToken();
			if(parser.getCurrentToken() == null)
				break;
			
			if(parser.getCurrentName() != null && parser.getCurrentToken() != JsonToken.FIELD_NAME) {
				//this JSON is split into two arrays, one for stops and one for the routes (east vs west).
				if(parser.getCurrentName().equals("stops"))
					readStopData();
				else
					readRoutesData();
			}
		}
	}
	/**
	 * processes the JSON line by line for the station stop data. It should be noted that each line (token)
	 * represents something in the JSON. For instance, an END_OBJECT token represents the end of an array.
	 * The data that we care about for getting the stop information is located under a VALUE_NAME token, which usually comes after
	 * a FIELD_NAME token.
	 * @throws JsonParseException
	 * @throws IOException
	 */
	private void readStopData() throws JsonParseException, IOException {
		// XXX This code is repetitive, but it works.
		//parser.nextToken();
		while(true){ //keep reading the stops array until you have reached the end of it.
			parser.nextToken();
			if(!parser.getCurrentToken().equals(JsonToken.FIELD_NAME) && parser.getCurrentName() != null){
				//exit out of stop array
				if(parser.getCurrentName().equals("stops") && parser.getText().equals("]"))
					break;
				this.extractedValueList.add(parser.getText()); // a VALUE_NAME token's getText() will return a number or name
				
				//each stop belongs to either the west route or the east route. Since this information is also
				//stored in an array, another loop is needed to extract this info.
				if(parser.getCurrentName().equals("routes")){
					this.extractedValueList.remove(extractedValueList.size() - 1);
					if(parser.getText().equals("]")){
						stopList.add(JSONParser.listToStop(extractedValueList));
						//TODO: at this point, you should have all of the necessary data in the list for one stop, so call the parser
						//		and remove all elements from this list.
						this.extractedValueList.removeAll(extractedValueList);
					}
				}
			}
		}
	}
	/**
	 * processes the JSON line by line for the route data.
	 * @throws JsonParseException
	 * @throws IOException
	 */
	private void readRoutesData() throws JsonParseException, IOException {
		while(true){
			parser.nextToken();
			if(parser.getCurrentName() != null && !parser.getCurrentToken().equals(JsonToken.FIELD_NAME)) {
				if(parser.getCurrentName().equals("routes") && parser.getText().equals("]"))
					break;
				this.extractedValueList.add(parser.getText());
				
				if(parser.getCurrentName().equals("coords")){
					extractedValueList.remove(extractedValueList.size() - 1);
					if(parser.getText().equals("]"))  {
					
						this.routeList.add(JSONParser.listToRoute(extractedValueList));
						extractedValueList.removeAll(extractedValueList);
						//System.out.println();
					}
				}
				//System.out.println(parser.getCurrentName() + " " + parser.getText());
			}
		}
	}
	
	/**
	 * extracts the shuttle data from the corresponding json file.
	 * @throws IOException
	 */
	public void readShuttleData() throws IOException {
		parser = f.createJsonParser(shuttleURL);
		parser.nextToken();
		while(parser.nextToken() != JsonToken.END_ARRAY){ //keep reading the stops array until you have reached the end of it.
			if(parser.getCurrentName() != null && !parser.getCurrentToken().equals(JsonToken.FIELD_NAME)){
				if(!parser.getCurrentName().equals("vehicle") && !parser.getCurrentName().equals("latest_position") &&
						!parser.getCurrentName().equals("icon")) {
					//System.out.println(parser.getCurrentName() + " " + parser.getText());
					this.extractedValueList2.add(parser.getText());
				}
				
				if(parser.getCurrentName().equals("vehicle") && parser.getText().equals("}")) {
					//System.out.println();
					Shuttle s = JSONParser.listToShuttle(extractedValueList2, stopList, routeList);
					addDataToExistingShuttle(s);
					this.extractedValueList2.removeAll(extractedValueList2);
				}
			}
		}
	}
	
	//pretty sure this is very inefficient
	private void addDataToExistingShuttle(Shuttle s) {
		Shuttle s2 = null;
		
		for(Shuttle shuttle : shuttleList) {
			if(shuttle.equals(s)) {
				s2 = shuttle;
				s2.setCurrentLocation(s.getCurrentLocation());
				shuttleList.remove(s);
				break;
			}
		}
		
		shuttleList.add(
				(s2 == null) ? s :	s2);
	}

	//getters for the three lists
	public ArrayList<Stop> getStopList() {
		return this.stopList;
	}

	public ArrayList<Route> getRouteList() {
		return this.routeList;
	}

	public HashSet<Shuttle> getShuttleList() {
		return this.shuttleList;
	}
	
	public static void main(String[] args) {
		JSONExtractor ex = new JSONExtractor();
		try{
			//ex.readRouteData();
			ex.readShuttleData();
		} catch(IOException e) {}
	}
}