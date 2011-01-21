package com.abstractedsheep.extractor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import com.abstractedsheep.shuttletracker.shared.Route;
import com.abstractedsheep.shuttletracker.shared.Stop;
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
	private ArrayList<String> extractedValueList; //will hold values from the JSON
	//these two variables are not used atm, but would store the data from the json.
	private ArrayList<Stop> stopList;
	private ArrayList<Route> routeList;
	
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
			
			if(parser.getCurrentName() != null) {
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
		while(parser.nextToken() != JsonToken.END_ARRAY){ //keep reading the stops array until you have reached the end of it.
			if(!parser.getCurrentToken().equals(JsonToken.FIELD_NAME) && parser.getCurrentName() != null){
				
				this.extractedValueList.add(parser.getText()); // a VALUE_NAME token's getText() will return a number or name
				System.out.println(parser.getCurrentName() + " " + parser.getText());
				
				//each stop belongs to either the west route or the east route. Since this information is also
				//stored in an array, another loop is needed to extract this info.
				if(parser.getCurrentName().equals("routes")){
					
					extractedValueList.remove(extractedValueList.size() - 1);
					while(parser.nextToken() != JsonToken.END_ARRAY) {
						
						if(!parser.getCurrentToken().equals(JsonToken.FIELD_NAME) && parser.getCurrentName() != null){
							this.extractedValueList.add(parser.getText());
							System.out.println(parser.getCurrentName() + " " + parser.getText());
						}
					}
					//TODO: at this point, you should have all of the necessary data in the list for one stop, so call the parser
					//		and remove all elements from this list.
					this.extractedValueList.removeAll(extractedValueList);
					System.out.println();
					//skip the end array token start array token and so forth.
					//TODO: put this in a loop
					parser.nextToken();
					parser.nextToken();
					parser.nextToken();
				}
				//you have reached the end of the stops array, return and start parsing the route data.
				if(parser.getCurrentName().equals("routes"))
					return;
			}
		}
	}
	/**
	 * processes the JSON line by line for the route data.
	 * @throws JsonParseException
	 * @throws IOException
	 */
	private void readRoutesData() throws JsonParseException, IOException {
		while(parser.getCurrentToken() != null && parser.nextToken() != JsonToken.END_OBJECT){
			if(parser.getCurrentName() != null && !parser.getCurrentToken().equals(JsonToken.FIELD_NAME)) {
				
				this.extractedValueList.add(parser.getText());
				System.out.println(parser.getCurrentName() + " " + parser.getText());
				
				if(parser.getCurrentName().equals("coords")){
					extractedValueList.remove(extractedValueList.size() - 1);
					
					while(parser.nextToken() != JsonToken.END_ARRAY){
						
						if(!parser.getCurrentToken().equals(JsonToken.FIELD_NAME) && parser.getCurrentName() != null){
							
							this.extractedValueList.add(parser.getText());
							System.out.println(parser.getCurrentName() + " " + parser.getText());
						}
					}
					extractedValueList.removeAll(extractedValueList);
					System.out.println();
					//skip the end array token start array token and so forth.
					//TODO: put this in a loop
					parser.nextToken(); //end array token
					parser.nextToken();
					//parser.nextToken();
				}
			}
		}
	}
	//FIXME: since you get the shuttle data from current.js, this method is currently not in use and contains
	//		 garbage code.
	public void readShuttleData() throws IOException {	}
	
	public static void main(String[] args) {
		JSONExtractor ex = new JSONExtractor();
		try{
			ex.readRouteData();
		} catch(IOException e) {}
	}
}