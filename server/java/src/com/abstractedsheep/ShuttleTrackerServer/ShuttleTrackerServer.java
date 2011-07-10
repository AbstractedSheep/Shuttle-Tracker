package com.abstractedsheep.ShuttleTrackerServer;

import java.net.MalformedURLException;
import java.net.URL;
import com.abstractedsheep.ShuttleTrackerService.ETACalculator;
import com.abstractedsheep.db.DatabaseWriter;
import com.abstractedsheep.extractor.*;
import com.abstractedsheep.world.World;

/**
 * This is the main class that will run all of the server code. This class
 * retrieves the stop and route data upon initialization from {@link
 * JSONExtractor.readRouteData()} and periodically gets the shuttle data from
 * {@linkplain JSONExtractor.readShuttleData()} every five seconds. The shuttle
 * data then undergoes some processing in order to determine the arrival times
 * to each stop on each shuttle's route, after which this arrival time data is
 * written to MySQL database.
 * 
 * @author saiumesh
 * 
 */
public class ShuttleTrackerServer {

	private static final int SLEEP_INTERVAL = (1000 * 5);
	private final URL staticDataURL;
	private final URL dynamicDataURL;
	private final World world;
	private ETACalculator calc;

	public ShuttleTrackerServer() throws MalformedURLException {
		this.staticDataURL = new URL(
				"http://shuttles.rpi.edu/displays/netlink.js");
		dynamicDataURL = new URL("http://shuttles.rpi.edu/vehicles/current.js");
		this.world = new World(new StaticJSONExtractor(staticDataURL),
				new DynamicJSONExtractor(dynamicDataURL));
		this.calc = new ETACalculator();
		executeWorld();
	}

	private void executeWorld() {
		// XXX All updates and modifications to the world are accomplished
		// within it.
		this.world.generateWorld();
		while (true) {
			updateWorld();
			DatabaseWriter.saveToDatabase(calc, "extra_eta");
			try {
				Thread.sleep(SLEEP_INTERVAL);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void updateWorld() {
		this.world.updateWorld();
		// update calculator's instance of the world before calculating the
		// etas.
		this.calc.updateWorld(world);
	}

	public static void initServer(String[] args) {
		// creates an instance of this server class and executes it
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String dbPropertiesPath = "";
		String loggingPath = "";
		String applicationPropertiesPath = "";
		try {
			new ShuttleTrackerServer();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}