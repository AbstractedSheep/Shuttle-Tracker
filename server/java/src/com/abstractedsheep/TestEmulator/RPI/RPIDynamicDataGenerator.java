/*
 * Copyright 2011
 *
 *   This file is part of Mobile Shuttle Tracker.
 *
 *   Mobile Shuttle Tracker is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Mobile Shuttle Tracker is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Mobile Shuttle Tracker.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.abstractedsheep.TestEmulator.RPI;

import com.abstractedsheep.TestEmulator.DynamicDataGenerator;

import java.net.MalformedURLException;
import java.net.URL;

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
