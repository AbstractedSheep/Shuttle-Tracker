<?php
  /* DataService class - provides functions for getting data from the DB */
class DataServiceData
{
    static function getData($shuttleNo)
    {
        /* read the database for the tracking info */
        $data_from_db = array('sn'=>1, 'speed'=>35, 'lat'=>12.4252, 'long'=>-74.24515, 'eta'=>'04:34'); // sample data for now
        return $data_from_db;
    }
    
    static function getNextEta($route_id='', $stop_id='') {
        /* get stop ETA time of next shuttle
            if stop_id not supplied, retrieve ETAs for all stops 
            TIMEDIFF(now(), shuttle_eta.eta) <= 0
        */
	if (!$route_id) $route_id="%";
        $today = getdate();
        if ($today["wday"] == 0 || $today["wday"] == 6) $table = "weekend_eta"; else $table = "shuttle_eta"; 
        $table = "shuttle_eta"; 
        $sql = "SELECT  shuttle_eta.stop_id, 
                        stops.name 'stop_name',  
                        shuttle_eta.eta AS eta,
                        shuttle_eta.route
                    FROM ( 
                        SELECT stop_id, min(shuttle_eta.eta) AS eta_min 
                           FROM shuttle_eta
                           WHERE shuttle_eta.route LIKE '" . $route_id . "' 
                           GROUP BY stop_id
                         ) as best_etas
                    LEFT JOIN shuttle_eta ON shuttle_eta.stop_id = best_etas.stop_id AND shuttle_eta.eta = best_etas.eta_min
                    LEFT JOIN stops         ON stops.stop_id = best_etas.stop_id 
                    LEFT JOIN shuttles      ON shuttles.shuttle_id = shuttle_eta.shuttle_id
                    WHERE shuttle_eta.eta_id = 0";
        //if ($route_id)
        //   $sql .= " AND shuttle_eta.route = '". $route_id . "' ";

        if ($stop_id && !is_array($stop_id)) {
            $sql .= " AND " . $table . ".stop_id = '". $stop_id . "' "; }
        else if ($stop_id && is_array($stop_id)) {
            foreach ($stop_id as $stop) {
                $sql .= " OR " . $table . ".stop_id = '". $stop . "' ";
            }
        }
            
        $sql .= " ORDER BY eta";
        $result = db_query_array($sql);
        if ($result != null)
        {
            return $result;
        }  
        else 
        {
            $empty = array();
            return $empty;
        }
    }
    static function recordStats($function) {
        /* Record IP, access time, and access function */
        // $headers = getallheaders();
        // $ip=$_SERVER['REMOTE_ADDR'];
        // if (strstr($headers["User-Agent"],"Dalvik")) { 
            // $sql = "INSERT INTO stats (ip,device,comment) VALUES ('$ip','Android','$function') ";
        // }
        // else if (strstr($headers["User-Agent"],"Shuttle-Tracker")) {
            // $sql = "INSERT INTO stats (ip,device,comment) VALUES ('$ip','iPhone','$function') ";
        // }
        // else {
            // $sql = "INSERT INTO stats (ip,device,comment) VALUES ('$ip','Web','$function') ";
        // }
        // db_query($sql);
    }
    
    
    static function getAllEta($route_id='', $shuttle_id='', $stop_id='') {
        /* get stop ETA time of next shuttle
            if stop_id not supplied, retrieve ETAs for all stops 
        */
        $today = getdate();
        if ($today["wday"] == 0 || $today["wday"] == 6) {$table = "weekend_eta";} else {$table = "shuttle_eta";}
        $table = "shuttle_eta";
        $sql = "SELECT " . $table . ".shuttle_id, " . $table . ".stop_id, " . $table . ".eta, " . $table . ".route, stops.name FROM " . $table . " LEFT JOIN stops ON stops.stop_id = " . $table . ".stop_id WHERE 1 ";
        if ($route_id)
            $sql .= " AND " . $table . ".route = '". $route_id . "' ";
        if ($stop_id)
            $sql .= " AND " . $table . ".stop_id = '". $stop_id . "' ";
        if ($shuttle_id)
            $sql .= " AND " . $table . ".shuttle_id = '". $shuttle_id . "' ";
        $sql .= "ORDER BY " . $table . ".eta ASC ";     
        $result = db_query_array($sql);
        if ($result && $table == "weekend_eta")
        {
            foreach ($result as $key => $pair)
            {
                $currStopId = $result[$key]["stop_id"];
                $nextStopId = $result[$key + 1]["stop_id"];            
                $eta[] = $pair["eta"];
                if ($nextStopId != $currStopId) $group_end = true;
                if ($group_end) {
                     $newResult[] = array("name" => $result[$key]["name"], "eta" => $eta, "route" => $result[$key]["route"], "arrival_or_departure" => $result[$key]["arrival_or_departure"]);
                }
                         
            }
            
            
           return $newResult;  
        }
        else if ($result && $table = "shuttle_eta")
        {
            return $result;
        }  
        else 
        {
            $empty = array();
            return $empty;
        }
    }
    static function getAllExtraEta($route_id='', $shuttle_id='', $stop_id='') {
        /* get stop ETA time of next shuttle
            if stop_id not supplied, retrieve ETAs for all stops 
        */
        $today = getdate();
        if ($today["wday"] == 0 || $today["wday"] == 6) $table = "weekend_eta"; else $table = "shuttle_eta"; 
        $table = "shuttle_eta";
        $sql = "SELECT " . $table . ".eta, stops.name FROM " . $table . " LEFT JOIN stops ON stops.stop_id = " . $table . ".stop_id WHERE 1 ";
        if ($route_id)
            $sql .= " AND " . $table . ".route = '". $route_id . "' ";
        if ($stop_id)
            $sql .= " AND " . $table . ".stop_id = '". $stop_id . "' ";
        //if ($shuttle_id)                        
//            $sql .= " AND " . $table . ".shuttle_id = '". $shuttle_id . "' ";
        $sql .= "ORDER BY " . $table . ".eta ASC ";     
        $result = db_query_array($sql);      
        
        
        if ($result)
        {   
            foreach ($result as $pair)
            {
                $eta[] = $pair["eta"];
            }
            if (isset($result[0]["arrival_or_departure"])) {
                $newResult = array("name" => $result[0]["name"], "eta" => $eta, "arrival_or_departure" => $result[0]["arrival_or_departure"]); 
            }
            else {
                $newResult = array("name" => $result[0]["name"], "eta" => $eta);
            }
            
            
           return $newResult; 
        }  
        else 
        {
            $empty = array();
            return $empty;
        }
    }

    static function getShuttlePositions() {
       /* Get all current shuttle positions from the DB */
       $sql = "SELECT s1.shuttle_id, s1.heading, X(s1.location) AS latitude, Y(s1.location) AS longitude, s1.speed, s1.cardinal_point, s1.update_time, s1.route_id, shuttles.name FROM shuttle_coords AS s1
                JOIN shuttles ON s1.shuttle_id = shuttles.shuttle_id, (
                SELECT shuttle_id, MAX( update_time ) AS maxdate
                FROM shuttle_coords
                GROUP BY shuttle_id
       ) AS s2
       
       WHERE s1.shuttle_id = s2.shuttle_id
       AND s1.update_time = s2.maxdate";
       $result = db_query_array($sql); 
       if ($result)
        { 
            return $result;
        }  
        else 
        {
            $empty = array();
            return $empty;
        }
        
    }
}
?>
