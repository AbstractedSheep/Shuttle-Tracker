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
    
    function getNextEta($stop_id='') {
        /* get stop ETA time of next shuttle
            if stop_id not supplied, retrieve ETAs for all stops 
        */
        $sql = "SELECT  shuttle_eta.stop_id, 
                        stops.name 'stop_name', 
                        shuttle_eta.shuttle_id, 
                        shuttles.name 'shuttle_name', 
                        shuttle_eta.eta AS eta
                    FROM ( 
                        SELECT stop_id, min(shuttle_eta.eta) AS eta_min 
                            FROM shuttle_eta
                           WHERE TIMEDIFF(now(), shuttle_eta.eta) <= 0
                           GROUP BY stop_id
                         ) as best_etas
                    LEFT JOIN shuttle_eta   ON shuttle_eta.stop_id = best_etas.stop_id AND shuttle_eta.eta = best_etas.eta_min
                    LEFT JOIN stops         ON stops.stop_id = best_etas.stop_id 
                    LEFT JOIN shuttles      ON shuttles.shuttle_id = shuttle_eta.shuttle_id
                    WHERE 1 ";
        if ($stop_id)
            $sql .= " AND shuttle_eta.stop_id = '". $stop_id . "' ";

        return db_query_array($sql);
    }
    
    function getAllEta($stop_id='', $shuttle_id='') {
        /* get stop ETA time of next shuttle
            if stop_id not supplied, retrieve ETAs for all stops 
        */
        $sql = "SELECT * FROM shuttle_eta WHERE 1 ";
        if ($stop_id)
            $sql .= " AND shuttle_eta.stop_id = '". $stop_id . "' ";
        if ($shuttle_id)
            $sql .= " AND shuttle_eta.shuttle_id = '". $shuttle_id . "' ";    

        return db_query_array($sql);
    }
    function getShuttlePositions() {
       /* Get all current shuttle positions from the DB */
       $sql = "SELECT s1.shuttle_id, s1.heading, X(s1.location) AS latitude, Y(s1.location) AS longitude, s1.speed, s1.cardinal_point, s1.update_time FROM shuttle_coords AS s1, (
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
