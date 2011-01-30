
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

import com.abstractedsheep.extractor.Shuttle;

public class JSONSender {

	public static void printToFile(ArrayList<Shuttle> shuttleList) {
		//the output file will show up in ~/ShuttleTracker/Server
		File f = new File("shuttleOutputData.txt");
		String shuttleHeader = "", stopInfo = "";
		BufferedWriter fOut = null;
		try {
			
			fOut = new BufferedWriter(new FileWriter(f));
			HashMap<String, Integer> map = null;
			
			for(Shuttle shuttle : shuttleList) {
				map = shuttle.getStopETA();
				shuttleHeader = "Shuttle Name:" + shuttle.getName() + " " + shuttle.getCurrentLocation() + "\n";
				System.out.print(shuttleHeader);
				fOut.write(shuttleHeader);
				//write travel time data to file
				for(String stop : map.keySet()) {
					stopInfo = "\t" + stop + ": " + map.get(stop) + "\n";
					System.out.print(stopInfo);
					fOut.write(stopInfo);
				}
				System.out.println();
				fOut.write("\n");
			}
		} catch(FileNotFoundException e) {
			System.err.printf("File %s not found\n", f.getAbsoluteFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally { //close file
			try {
			if(fOut != null)
				fOut.close();
			} catch(IOException ex) {}
		}
		
	}
}
