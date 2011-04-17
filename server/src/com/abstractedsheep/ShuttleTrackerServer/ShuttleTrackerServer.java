package com.abstractedsheep.ShuttleTrackerServer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.abstractedsheep.extractor.*;

/**
 * This is the main class that will run all of the server code.
 * This class retrieves the stop and route data upon initialization from
 * {@link JSONExtractor.readRouteData()} and periodically gets the shuttle data
 * from {@linkplain JSONExtractor.readShuttleData()} every five seconds. The shuttle data
 * then undergoes some processing in order to determine the arrival times to each stop on
 * each shuttle's route, after which this arrival time data is written to MySQL database.
 * @author saiumesh
 * 
 */
public class ShuttleTrackerServer {
	private JSONExtractor jsExtractor;
	private HashSet<Shuttle> shuttleList;

	public ShuttleTrackerServer() {
		this.jsExtractor = new JSONExtractor();
		this.shuttleList = new HashSet<Shuttle>();
		getStaticData();
		// read the shuttle data and calculate the ETAs in the background
		startThread();
	}
	
	/**
	 * Starts periodically reading the shuttle data from JSONExtractor's shuttleURL
	 * every five seconds.
	 */
	private void startThread() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				// read the shuttle data
				readDynamicData();
			}
		});

		t.start();
	}
	
	/**
	 * Gets the shuttle data {@link JSONExtractor.shuttleURL}, processes the
	 * information and then writes the data to the database.
	 */
	private void readDynamicData() {
		while (true) {
			try {
				System.out.println("Reading Shuttle data and trying to manipulate it.");
				jsExtractor.readShuttleData();
				this.shuttleList = new HashSet<Shuttle>(jsExtractor.getShuttleList());

				if(shuttleList.size() > 0){
					// do ETA calculations and print to the database
					calculateETA();
					JSONSender.saveToDatabase(shuttleList, "shuttle_eta", false);
					JSONSender.saveToDatabase(shuttleList, "extra_eta", true);
					JSONSender.printToConsole(shuttleList);
				} else {//clear the database and the shuttle list
					JSONSender.connectToDatabase("shuttle_eta");
					JSONSender.connectToDatabase("extra_eta");
					jsExtractor.clearShuttleList();
				}
				// have the thread sleep for 15 seconds (approximate update
				// time)

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					Thread.sleep(1000 * 5);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Calculates the time for each shuttle to arrive at each stop. If a shuttle
	 * does not go to that stop (i.e. that stop is not part of the shuttle's
	 * route), then a -1 is returned. The values are then stored in a list to
	 * later be used to save the data to a file.
	 */
	private void calculateETA() {
		for (Shuttle shuttle : shuttleList) {
				shuttle.getETAToStop();
		}
	}

	/**
	 * the static data is the route and stop data as they will not change over
	 * time.
	 */
	private void getStaticData() {
		try {
			jsExtractor.readRouteData();
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