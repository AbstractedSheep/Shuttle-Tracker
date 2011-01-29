package com.abstractedsheep.ShuttleTrackerServer;

import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.*;

import com.abstractedsheep.extractor.Shuttle;

public class JSONSender {

	public static void printToFile(ArrayList<Shuttle> shuttleList) {
		HashMap<String, Integer> map = null;
		for(Shuttle shuttle : shuttleList) {
			map = shuttle.getStopETA();
			System.out.println("Shuttle ID:" + shuttle.getShuttleId() + " " + shuttle.getCurrentLocation());
			for(String stop : map.keySet()) {
				System.out.println("\t" + stop + ": " + map.get(stop) + " " + shuttle.getStops().get(stop));
			}
			System.out.println();
		}
		
	}
}
