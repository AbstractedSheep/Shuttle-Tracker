package com.abstractedsheep.ShuttleTrackerServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.abstractedsheep.extractor.*;

/**
 * @author jonnau
 * 
 */
public class ShuttleTrackerServer{
	private JSONExtractor jsExtractor;
	private ArrayList<Stop> stopList;
	private HashSet<Shuttle> shuttleList;
	private ArrayList<Route> routeList;
	
	public ShuttleTrackerServer() {
		this.jsExtractor = new JSONExtractor();
		this.stopList = new ArrayList<Stop>();
		this.shuttleList = new HashSet<Shuttle>();
		this.routeList = new ArrayList<Route>();
		//put values in the stop and route lists 
		getStaticData();
		//read the shuttle data and calculate the ETAs in the background
		startThread();
	}
	
	private void startThread() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				//read the shuttle data
				readDynamicData();
			}
		});
		
		t.start();
	}
	private void readDynamicData() {
		while(true) {
			try {
				System.out.println("Reading Shuttle data and trying to manipulate it.");
				jsExtractor.readShuttleData();
				this.shuttleList = jsExtractor.getShuttleList();
				//do ETA calculations and print to file
				calculateETA();
				JSONSender.saveToDatabase(shuttleList);
				//have the thread sleep for 15 seconds (approximate update time)
				Thread.sleep(15 * 1000);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Calculates the time for each shuttle to arrive at each stop. If a shuttle does not go to that stop
	 * (i.e. that stop is not part of the shuttle's route), then a -1 is returned. The values are then stored in a list
	 * to later be used to save the data to a file.
	 */
	private void calculateETA() {		
		for(Shuttle shuttle : shuttleList) {
			for(Stop stop : stopList) {
				shuttle.getETAToStop();
			}
		}
	}

	/**
	 * the static data is the route and stop data as they will not change over time.
	 */
	private void getStaticData() {
		try {
			jsExtractor.readRouteData();
			this.stopList = jsExtractor.getStopList();
			this.routeList = jsExtractor.getRouteList();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ShuttleTrackerServer();
	}

}
