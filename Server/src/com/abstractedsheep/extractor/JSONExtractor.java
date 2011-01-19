package com.abstractedsheep.extractor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.codehaus.jackson.*;
/**
 * The purpose of this class is to extract the jsons from the rpi shuttle server and process the data.
 * @author jonnau
 *
 */
public class JSONExtractor{
	private URL routeURL;
	private URL shuttleURL;
	private URLConnection routeConnector, shuttleConnector;
	private JsonParser parser;
	private JsonFactory f;
	
	public JSONExtractor() {
		f = new JsonFactory();
		try {
			//get link to stops and shuttles
			routeURL = new URL("http://shuttles.rpi.edu/displays/netlink.js");
			shuttleURL = new URL("http://shuttles.rpi.edu/vehicles/current.js");
			//open connection to site in order to get the data.
			this.routeConnector = routeURL.openConnection();
			this.shuttleConnector = shuttleURL.openConnection();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//XXX: data shows up in one line, need to make a new method/class to parse json data.
	public void readRouteData() throws IOException {
		String inputLine;
		parser = f.createJsonParser(routeURL);
		parser.nextToken();
		while(parser.nextToken() != JsonToken.END_OBJECT){
			parser.nextToken();
			inputLine = parser.getCurrentName() + " " + parser.getCurrentToken().toString();
			if(parser.getCurrentName() != null) {
				if(parser.getCurrentName().equals("stops"))
					readStopData();
				else
					readRoutesData();
			}
		}
	}
	/**
	 * processes the JSON line by line for the station stop data.
	 * @throws JsonParseException
	 * @throws IOException
	 */
	private void readStopData() throws JsonParseException, IOException {
		// TODO This code is repetitive, but it works.
		while(parser.nextToken() != JsonToken.END_ARRAY){
			if(!parser.getCurrentToken().equals(JsonToken.FIELD_NAME) && parser.getCurrentName() != null){
				if(parser.getCurrentName().equals("routes")){
					while(parser.nextToken() != JsonToken.END_ARRAY) {
						if(!parser.getCurrentToken().equals(JsonToken.FIELD_NAME) && parser.getCurrentName() != null)
							System.out.println(parser.getCurrentName() + " " + parser.getText());
					}
					System.out.println();
					//skip the end array token start array token and so forth.
					//TODO: put this in a loop
					parser.nextToken(); //end array token
					parser.nextToken();
					parser.nextToken();
				}
				if(parser.getCurrentName().equals("routes"))
					return;
				System.out.println(parser.getCurrentName() + " " + parser.getText());
			}
		}
	}
	/**
	 * processes the JSON line by line for the route data.
	 * @throws JsonParseException
	 * @throws IOException
	 */
	private void readRoutesData() throws JsonParseException, IOException {
		while(parser.nextToken() != JsonToken.END_ARRAY){
			if(!parser.getCurrentToken().equals(JsonToken.FIELD_NAME) && parser.getCurrentName() != null) {
				if(parser.getCurrentName().equals("coords")){
					while(parser.nextToken() != JsonToken.END_ARRAY){
						if(!parser.getCurrentToken().equals(JsonToken.FIELD_NAME) && parser.getCurrentName() != null)
							System.out.println(parser.getCurrentName() + " " + parser.getText());
					}
				}
			}
			System.out.println(parser.getCurrentName() + " " + parser.getText());
		}
	}
	public void readShuttleData() throws IOException {
		BufferedReader in = new BufferedReader(
                new InputStreamReader(
                shuttleConnector.getInputStream()));
		String inputLine;
		
		while ((inputLine = in.readLine()) != null) 
			System.out.println(inputLine);
		in.close();
	}
	
	public static void main(String[] args) {
		JSONExtractor ex = new JSONExtractor();
		try{
			ex.readRouteData();
		} catch(IOException e) {}
	}
}
