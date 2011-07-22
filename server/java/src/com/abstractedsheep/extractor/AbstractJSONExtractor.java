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

package com.abstractedsheep.extractor;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * The purpose of this class is to extract the jsons from the rpi shuttle server
 * and process the data.
 * 
 * @author saiumesh
 * 
 */
public abstract class AbstractJSONExtractor {
	protected URL url;
	protected JsonParser parser;
	protected JsonFactory f;
	protected ArrayList<String> extractedValueList1, extractedValueList2;

	public AbstractJSONExtractor() {
		f = new JsonFactory();
		this.extractedValueList1 = new ArrayList<String>();
		this.extractedValueList2 = new ArrayList<String>();
		try {
			// get link to stops and shuttles
			url = new URL("");

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public AbstractJSONExtractor(URL u) {
		this.f = new JsonFactory();
		// get link to stops and shuttles
		this.url = u;
		this.extractedValueList1 = new ArrayList<String>();
		this.extractedValueList2 = new ArrayList<String>();
	}

	// TODO: data shows up in one line, need to make a new method/class to parse
	// json data.
	/**
	 * the purpose of this method is to extract the data from the routes json
	 * and store the values in StopList and RouteList.
	 */
	public abstract void readDataFromURL();
}