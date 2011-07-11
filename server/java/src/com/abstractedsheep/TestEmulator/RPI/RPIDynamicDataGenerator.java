package com.abstractedsheep.TestEmulator.RPI;

import java.net.MalformedURLException;
import java.net.URL;

import com.abstractedsheep.TestEmulator.DynamicDataGenerator;

public class RPIDynamicDataGenerator extends DynamicDataGenerator {

	public RPIDynamicDataGenerator(URL url) {
		super(url);
	}
	
	public static void main (String[] args) {
		try {
			new RPIDynamicDataGenerator(new URL("http://shuttles.rpi.edu/displays/netlink.js"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
