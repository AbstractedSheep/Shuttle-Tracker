package com.abstractedsheep.shuttletracker.android;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.*;

import com.abstractedsheep.shuttletracker.shared.Shuttle;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class Tracker extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ObjectMapper mapper = new ObjectMapper();
        Shuttle s = new Shuttle(2, 0);
        
        java.io.ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
			mapper.writeValue(baos, s);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		TextView tv = (TextView)findViewById(R.id.hello);
        String mapperOutput = new String(baos.toByteArray());
        tv.setText(mapperOutput);
        
    }
}