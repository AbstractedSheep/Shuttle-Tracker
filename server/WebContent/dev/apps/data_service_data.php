<?php
  /* DataService class - provides functions for getting data from the DB */
class DataServiceData
{
    function getData($shuttleNo)
    {
        /* read the database for the tracking info */
        $data_from_db = array('sn'=>1, 'speed'=>35, 'lat'=>12.4252, 'long'=>-74.24515, 'eta'=>'04:34'); // sample data for now
        return $data_from_db;
    }
    
    function getNextEta($route_id, $stop_id='') {
        /* get stop ETA time of next shuttle
            if stop_id not supplied, retrieve ETAs for all stops 
            TIMEDIFF(now(), shuttle_eta.eta) <= 0
        */
        $sql = "SELECT  shuttle_eta.stop_id, 
                        stops.name 'stop_name', 
                        shuttle_eta.shuttle_id,  
                        shuttle_eta.eta AS eta,
                        shuttle_eta.route
                    FROM ( 
                        SELECT stop_id, min(shuttle_eta.eta) AS eta_min 
                            FROM shuttle_eta
                           WHERE shuttle_eta.route = " . $route_id . " 
                           GROUP BY stop_id
                         ) as best_etas
                    LEFT JOIN shuttle_eta   ON shuttle_eta.stop_id = best_etas.stop_id AND shuttle_eta.eta = best_etas.eta_min
                    LEFT JOIN stops         ON stops.stop_id = best_etas.stop_id 
                    LEFT JOIN shuttles      ON shuttles.shuttle_id = shuttle_eta.shuttle_id
                    WHERE 1 ";
        //if ($route_id)
        //   $sql .= " AND shuttle_eta.route = '". $route_id . "' ";
		if ($stop_id)
            $sql .= " AND shuttle_eta.stop_id = '". $stop_id . "' ";

        return db_query_array($sql);
    }
    function recordStats($function) {
        /* Record IP, access time, and access function */
        $headers = getallheaders();
        $ip=$_SERVER['REMOTE_ADDR'];
        if (strstr($headers["User-Agent"],"Dalvik")) {
            $sql = "INSERT INTO stats (ip,device,comment) VALUES ('$ip','Android','$function') ";
        }
        else if (strstr($headers["User-Agent"],"Shuttle-Tracker")) {
            $sql = "INSERT INTO stats (ip,device,comment) VALUES ('$ip','iPhone','$function') ";
        }
        else {
            $sql = "INSERT INTO stats (ip,device,comment) VALUES ('$ip','Web','$function') ";
        }
        db_query($sql);
    }
    
    
    function getAllEta($route_id='', $shuttle_id='', $stop_id='') {
        /* get stop ETA time of next shuttle
            if stop_id not supplied, retrieve ETAs for all stops 
        */
        $sql = "SELECT shuttle_eta.*, stops.name FROM shuttle_eta LEFT JOIN stops ON stops.stop_id = shuttle_eta.stop_id WHERE 1 ";
        if ($route_id)
            $sql .= " AND shuttle_eta.route = '". $route_id . "' ";
		if ($stop_id)
            $sql .= " AND shuttle_eta.stop_id = '". $stop_id . "' ";
        if ($shuttle_id)
            $sql .= " AND shuttle_eta.shuttle_id = '". $shuttle_id . "' ";     
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
    function getAllExtraEta($route_id='', $shuttle_id='', $stop_id='') {
        /* get stop ETA time of next shuttle
            if stop_id not supplied, retrieve ETAs for all stops 
        */
        $sql = "SELECT extra_eta.eta, stops.name FROM extra_eta LEFT JOIN stops ON stops.stop_id = extra_eta.stop_id WHERE 1 ";
        if ($route_id)
            $sql .= " AND extra_eta.route = '". $route_id . "' ";
        if ($stop_id)
            $sql .= " AND extra_eta.stop_id = '". $stop_id . "' ";
        if ($shuttle_id)
            $sql .= " AND extra_eta.shuttle_id = '". $shuttle_id . "' ";     
        $result = db_query_array($sql);
        print_r($result);
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
    function getAllWeekendETA($route_id='', $shuttle_id='', $stop_id='')
    {
        
    }
    function getShuttlePositions() {
       /* Get all current shuttle positions from the DB */
       $sql = "SELECT s1.shuttle_id, s1.heading, X(s1.location) AS latitude, Y(s1.location) AS longitude, s1.speed, s1.cardinal_point, s1.update_time, s1.route_id, shuttles.name FROM shuttle_coords AS s1
                JOIN shuttles ON s1.shuttle_id = shuttles.shuttle_id, (
                SELECT shuttle_id, MAX( update_time ) AS maxdate
                FROM shuttle_coords
                GROUP BY shuttle_id
       ) AS s2
       
       WHERE s1.shuttle_id = s2.shuttle_id
       AND s1.update_time = s2.maxdate";
       
       return db_query_array($sql); 
    }
}
?>
