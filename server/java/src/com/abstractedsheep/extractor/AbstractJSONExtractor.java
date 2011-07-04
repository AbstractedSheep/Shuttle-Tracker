package com.abstractedsheep.extractor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;

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