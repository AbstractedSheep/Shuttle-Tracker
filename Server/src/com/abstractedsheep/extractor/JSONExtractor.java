package com.abstractedsheep.extractor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class JSONExtractor{
	
	private URLConnection routeConnector, shuttleConnector;
	
	public JSONExtractor() {
		URL routeURL = null;
		URL shuttleURL = null;
		try {
			//gets routes and stops
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
		BufferedReader in = new BufferedReader(
                new InputStreamReader(
                routeConnector.getInputStream()));
		String inputLine;
		
		while ((inputLine = in.readLine()) != null) 
		System.out.println(inputLine);
		in.close();
	}
	//XXX: data shows up in one line, need to make a new method/class to parse json data.
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
