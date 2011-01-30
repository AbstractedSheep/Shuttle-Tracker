/*
 * This class is currently set up for demonstration.
 * It is heavily commented for educational purposes.
 */

package com.abstractedsheep.shuttletracker.android;

import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.*;

import com.abstractedsheep.extractor.Shuttle;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class Tracker extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Show the layout specified in /res/layout/main.xml
        setContentView(R.layout.main);
        
        // ObjectMapper's functions can throw one of three exceptions, all of which are required to be caught
        // Although I have not implemented proper exception handling (an exception will crash the application),
        // this will satisfy the Java compiler
        try {
	        // ObjectMapper can be reused for all serializing and deserializing without reinstantiation
	        ObjectMapper mapper = new ObjectMapper();
	        // findViewById is used to get a reference to the text view with the id 'hello' in /res/layout/main.xml
	        TextView tv = (TextView)findViewById(R.id.hello);
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        
	        // Test data for shuttle class
	        Shuttle s = new Shuttle(2, 0);
//	        s.addStop(20.22930, -67.29415);
//	        s.addStop(20.22927, -67.29424);
//	        s.addStop(16, 5);
	        
	        // Convert the shuttle to JSON and place the result in the output stream
			mapper.writeValue(baos, s);
	
			// Put the output data into an input stream for reading back
	        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	        
	        // Clear s so we know that the ObjectMapper put the new data in, and that it is not just the old data
	        s = null;
	        
	        // Read the JSON into a variable, the second parameter here must be the same class as the JSON was created from
	        // For JSON that we didn't create ourselves i.e. from the shuttle tracker site, we're better off using the manual
	        // deserialization which is documented on the Jackson wiki
	        // Also note that the ObjectMapper will have trouble if the class is not structured properly, so check the
	        // Shuttle.java file in Shuttle-Tracker-Shared
			s = mapper.readValue(bais, Shuttle.class);
			
			// Display the data in s so we can confirm that it is correct
	        tv.setText(s.getRouteId() + " " + s.getShuttleId() + " " + s.getStops().toString());
		} catch (JsonGenerationException e) {
			// Exception.printStackTrace() is a good debugging tool that makes the error message and stack trace
			// show up in the DDMS Log. Other ways to print to the log for debugging are available through the
			// android.util.Log class. The function letters indicate the log level (w = Warning, d = Debug,
			// v = Verbose, e = Error, i = Information). When using the Log class, keep the tag consistent throughout
			// the entire application
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}