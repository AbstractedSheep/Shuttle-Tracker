<?php
include("../public_html/application.php");


	mysql_query("TRUNCATE stops"); mysql_query("TRUNCATE stop_routes"); mysql_query("TRUNCATE routes"); mysql_query("TRUNCATE route_coords"); mysql_query("TRUNCATE shuttle_coords"); mysql_query("TRUNCATE shuttles");

	importRouteStops();
	
    
	function importRouteStops() {
        include_once("../public_html/apps/routecoorddistances.php");
		$url = "http://shuttles.rpi.edu/displays/netlink.js";
		
		$data = json_decode(getUrl($url));
		//print_ar($data);exit;
		foreach ($data as $type=>$datum) {
			switch ($type) {
				case 'stops' :
					foreach ($datum as $stop) {
						/* update 'stops' table */
						if ($stop->short_name) {
                            					 				
							if (Stop::get($stop->short_name)) {
								Stop::update($stop->short_name,$stop);
							} else {
								Stop::insert($stop);
							}
						}
						foreach ($stop->routes as $stop_route) {
							/* update 'stop_routes' table */
							if (!StopRoute::get($stop->short_name, $stop_route->id)) {
								StopRoute::insert($stop->short_name,$stop_route->id);
							}
						}
					}
					break;
				case 'routes' :
					foreach ($datum as $route) {
						/* update 'route' table */
						if (Route::get($route->id)) {
							Route::update($route->id,$route);
						} else {
							Route::insert($route->id,$route);
						}
						if ($route->coords) {
							RouteCoords::delete($route->id);	/* delete all coords for route */
							$seq = 0;
							foreach ($route->coords as $route_coord) {
								/* update 'route_coords' table */
								RouteCoords::insert($route->id,$seq,$route_coord);
								$seq++;
							}
						}
					}
					break;
			}
		}
        RouteCoordDistances::loadDistanceTable();
	}
	
	function importShuttlePositions() {
		$url = "http://shuttles.rpi.edu/vehicles/current.js";
		//mysql_query("TRUNCATE stops"); mysql_query("TRUNCATE stop_routes"); mysql_query("TRUNCATE routes"); mysql_query("TRUNCATE route_coords");
		
		$data = json_decode(getUrl($url));
		/*$data = json_decode('[
    {
        "vehicle": {
            "id": 7,
            "name": "Bus 90",
            "latest_position": {
                "heading": 10,
                "latitude": "42.73548",
                "longitude": "-73.67086",
                "speed": 30,
                "public_status_msg": null,
                "cardinal_point": "North"
            },
            "icon": {
                "id": 1
            }
        }
    }
]');   */
		//print_ar($data);exit;
		foreach ($data as $current_stat_item) {
			if ($current_stat_item->vehicle) {
				$shuttle_status = $current_stat_item->vehicle;
				/* update 'shuttle' table */
				if (Shuttle::get($shuttle_status->id)) {
					Shuttle::update($shuttle_status->id,$shuttle_status);
				} else {
					Shuttle::insert($shuttle_status);
				}
				if ($shuttle_status->latest_position) {
					/* update 'shuttle_coords' table */
					ShuttleCoords::insert($shuttle_status->id,$shuttle_status->latest_position);
				}
			}
		}
		ShuttleCoords::deleteOlderThan(2); 	/* delete history over 2 days old */
	}
	
	
	exit;
	
class Stop {
	function get($stop_id) {
		$sql = "SELECT * FROM stops WHERE stop_id = '".$stop_id."'";
		return db_query_array($sql);
	}
	function insert($info) {
		$ret = mysql_query("INSERT INTO stops (stop_id , location , name	)
					VALUES ('".$info->short_name."',
						GeomFromText( 'POINT(" . $info->latitude . " " . $info->longitude . ")' ), 
						'".addslashes($info->name)."') ");
		return mysql_insert_id();
	}
	function update($stop_id,$info) {
		
		$ret = mysql_query("UPDATE stops
					 SET location = GeomFromText( 'POINT(" . $info->latitude . " " . $info->longitude . ")' ),
						 name = '".addslashes($info->name)."'
					 WHERE stop_id = '" . addslashes($info->short_name)."'");
				
		return mysql_affected_rows( );
	}
	function delete($stop_id) {
		return db_delete('stops',$stop_id);
	}
}

class StopRoute {
	function get($stop_id='',$route_id='') {
		$sql = "SELECT * FROM stop_routes 
					WHERE 1 ";
		if ($stop_id) 
			$sql .= " AND stop_id = '".$stop_id."'";
		if ($route_id) 
			$sql .= " AND route_id = '".$route_id."'";
		return db_query_array($sql);
	}
	function insert($stop_id,$route_id) {
		$insert = array('stop_id'=>$stop_id,'route_id'=>$route_id);
		return db_insert('stop_routes',$insert);
	}
	function delete($stop_id,$route_id) {
		return db_delete('stop_routes',array($stop_id,$route_id));
	}
}
  
class Shuttle {
	function get($shuttle_id='') {
		$sql = "SELECT * FROM shuttles
					WHERE 1 ";
		if ($shuttle_id) 
			$sql .= " AND shuttle_id = '".$shuttle_id."'";
		return db_query_array($sql);
	}
	function insert($info) {
		$insert = array('shuttle_id'=>$info->id,'name'=>$info->name);
		return db_insert('shuttles',$insert);
	}
	function update($shuttle_id,$info) {
		$update = array('name'=>$info->name);
		return db_update('shuttles',$shuttle_id,$update,'shuttle_id');
	}
	function delete($shuttle_id) {
		return db_delete('shuttles',$shuttle_id);
	}
}
  
class ShuttleCoords {
	function get($shuttle_id='') {
		$sql = "SELECT * FROM shuttle_coords
					WHERE 1 ";
		if ($route_id) 
			$sql .= " AND shuttle_id = '".$shuttle_id."'";
		if ($seq) 
			$sql .= " AND seq = '".$seq."'";
		return db_query_array($sql);
	}
	function insert($shuttle_id,$info) {
		$ret = mysql_query("INSERT INTO shuttle_coords (shuttle_id, heading, location, speed, public_status_msg, cardinal_point, update_time, route_id )
					VALUES ('".$shuttle_id."',
							'".$info->heading."',
							GeomFromText( 'POINT(" . $info->latitude . " " . $info->longitude . ")' ),
							'".$info->speed."',
							'".$info->public_status_msg."',
							'".$info->cardinal_point."',
							NOW(),                                                   
                            (SELECT route FROM shuttle_eta WHERE shuttle_id = '".$shuttle_id."' LIMIT 1)
						) ");
		return mysql_insert_id();
	}

	function delete($shuttle_id) {
		return db_delete('shuttle_coords',$shuttle_id,'shuttle_id');
	}
	
	function deleteOlderThan($days = 2)
	{
		$sql = "DELETE
					FROM shuttle_coords
					WHERE update_time < DATE_SUB(NOW(),INTERVAL $days DAY)
					";
		return mysql_query($sql);
	}
		
}

class Route {
	function get($route_id='') {
		$sql = "SELECT * FROM routes
					WHERE 1 ";
		if ($route_id) 
			$sql .= " AND route_id = '".$route_id."'";
		return db_query_array($sql);
	}
	function insert($route_id,$info) {
		$ret = mysql_query("INSERT INTO routes (route_id , name, color, width	)
					VALUES ('".$route_id."',
							'".addslashes($info->name)."',
							'".addslashes($info->color)."',
							'".addslashes($info->width)."'
						) ");
		return mysql_insert_id();
	}
	function update($route_id,$info) {
		
		$ret = mysql_query("UPDATE routes
					 SET name = '".addslashes($info->name)."',
						 color ='".addslashes($info->color)."',
						 width ='".addslashes($info->width)."'
					 WHERE route_id = '" . addslashes($route_id)."'");

		return mysql_affected_rows( );
	}

	function delete($route_id) {
		return db_delete('routes',$route_id);
	}
}
  
class RouteCoords {
	function get($route_id='',$seq='') {
		$sql = "SELECT * FROM route_coords
					WHERE 1 ";
		if ($route_id) 
			$sql .= " AND route_id = '".$route_id."'";
		if ($seq) 
			$sql .= " AND seq = '".$seq."'";
		return db_query_array($sql);
	}
	function insert($route_id,$seq,$info) {
		$ret = mysql_query("INSERT INTO route_coords (route_id , seq, location	)
					VALUES ('".$route_id."',
							".$seq.",
							GeomFromText( 'POINT(" . $info->latitude . " " . $info->longitude . ")' )
						) ");
		return mysql_insert_id();
	}
	function update($route_id,$info) {
		
		$ret = mysql_query("UPDATE route_coords
					 SET location = GeomFromText( 'POINT(" . $info->latitude . " " . $info->longitude . ")' )
					 WHERE route_id = '" . addslashes($route_id)."'");
		return mysql_affected_rows( );
	}

	function delete($route_id) {
		return db_delete('route_coords',$route_id,'route_id');
	}
}
  
class ShuttleEta {
	function get($shuttle_id='',$stop_id='') {
		$sql = "SELECT * FROM shuttle_etas 
					WHERE 1 ";
		if ($shuttle_id) 
			$sql .= " AND shuttle_id = '".$shuttle_id."'";
		if ($stop_id) 
			$sql .= " AND stop_id = '".$stop_id."'";
		return db_query_array($sql);
	}
	function insert($info) {
		return db_insert('shuttle_etas',$info);
	}
	function update($shuttle_id,$stop_id,$info) {
		return db_update('shuttle_etas',array($shuttle_id,$stop_id),$info,array('shuttle_id','stop_routes'));
	}
	function delete($shuttle_id,$stop_id) {
		return db_delete('shuttle_etas',array($shuttle_id,$stop_id));
	}
}

function getUrl($url) {
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 30);
    curl_setopt($ch, CURLOPT_TIMEOUT, 5);
    curl_setopt($ch, CURLOPT_FOLLOWLOCATION, 1);
    //curl_setopt($ch, CURLOPT_REFERER, $_SERVER['REQUEST_URI']);
    $dataJSON = curl_exec($ch);
    curl_close($ch);	
	return $dataJSON;
}



function distanceBetweenLatLongs($lat1, $lng1, $lat2, $lng2, $miles = true) {
	 /* return distance between LAT-LONGS */
	 $pi80 = M_PI / 180;
	 $lat1 *= $pi80;
	 $lng1 *= $pi80;
	 $lat2 *= $pi80;
	 $lng2 *= $pi80;
	 $r = 6372.797; // mean radius of Earth in km
	 $dlat = $lat2 - $lat1;
	 $dlng = $lng2 - $lng1;
	 $a = sin($dlat / 2) * sin($dlat / 2) + cos($lat1) * cos($lat2) * sin($dlng / 2) * sin($dlng / 2);
	 $c = 2 * atan2(sqrt($a), sqrt(1 - $a));
	 $km = $r * $c;
	 return ($miles ? ($km * 0.621371192) : $km);
}

function distanceBetweenLatLongsDB($lat, $lng, $within = 10, $table = 'stop', $latFld = 'latitude', $lonFld = 'longitude') {
	 /* return distances between given LAT-LONG and all records in table having latlongs within X miles */
	 $sql = "SELECT *, ((ACOS(SIN($lat * PI() / 180) * SIN(" . $latFld. " * PI() / 180) + COS($lat * PI() / 180) * COS(" . $latFld. " * PI() / 180) * COS(($lon - " . $lonFld. ") * PI() / 180)) * 180 / PI()) * 60 * 1.1515) AS distance FROM ".$table." HAVING distance<='".$within."' ORDER BY distance ASC";
	 return db_query_array($sql);		 
}
	


?>