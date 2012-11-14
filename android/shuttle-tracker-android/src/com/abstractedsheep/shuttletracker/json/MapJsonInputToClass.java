/*The original server this code ran off of is down 
 * and a new Json endpoint with a different format
 * needed to be used instead, this structure is used
 * to help match the old class to the new format*/

package com.abstractedsheep.shuttletracker.json;

public class MapJsonInputToClass {
	public int heading = 0;
	public String latitude = "";
	public String longitude = "";
	public int speed = 0;
	public String timestamp = "";
	public String public_status_msg = null;
	public String cardinal_point = "";

}
