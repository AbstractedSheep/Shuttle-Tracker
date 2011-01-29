package com.abstractedsheep.ShuttleTrackerServer;

import java.io.IOException;
import java.util.ArrayList;
import com.abstractedsheep.extractor.*;

/**
 * @author jonnau
 * 
 */
public class ShuttleTrackerServer implements Runnable{
	private JSONExtractor jsExtractor;
	private ArrayList<Stop> stopList;
	private ArrayList<Shuttle> shuttleList;
	private ArrayList<Route> routeList;
	
	public ShuttleTrackerServer() {
		this.jsExtractor = new JSONExtractor();
		this.stopList = new ArrayList<Stop>();
		this.shuttleList = new ArrayList<Shuttle>();
		this.routeList = new ArrayList<Route>();
		//put values in the stop and route lists 
		getStaticData();
		//read the shuttle data and calculate the ETAs in the background
		startThread();
	}
	
	/*TODO: see below todo comment; startThread will get the shuttle data
	 * 		on a separate thread.
	 */
	private void startThread() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				//read the shuttle data
				readDynamicData();
				//calculate the ETA and then print it to a file
			}
		});
		
		t.start();
	}
	private void readDynamicData() {
		while(true) {
			try {
				jsExtractor.readShuttleData();
				this.shuttleList = jsExtractor.getShuttleList();
				//do ETA calculations
				calculateETA();
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
	
	//TODO: now that I think about it, it seems stupid to have a server object to run on a separate thread.
	@Override
	public void run() {
		while(true) {
			try {
				jsExtractor.readShuttleData();
				this.shuttleList = jsExtractor.getShuttleList();
				//do ETA calculations
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
	
	private void calculateETA() {
		for(Stop stop : stopList) {
			for(Shuttle shuttle : shuttleList) {
				shuttle.getETAToStop(stop.getName(), routeList);
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
		// TODO Auto-generated method stub

	}

}
