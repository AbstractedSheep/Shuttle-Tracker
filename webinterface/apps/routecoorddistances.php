<?php
    
class RouteCoordDistances {
    function loadDistanceTable() {
        self::truncate();
        $sql = "INSERT INTO route_coord_distances 
            SELECT r1.route_id AS route_id, r1.seq AS seq_1, r2.seq AS seq_2, (
                (
                ACOS( SIN( X( r1.location ) * PI( ) /180 ) * SIN( X( r2.location ) * PI( ) /180 ) + COS( X( r1.location ) * PI( ) /180 ) * COS( X( r2.location ) * PI( ) /180 ) * COS( (
                Y( r1.location ) - Y( r2.location ) ) * PI( ) /180 )
                ) *180 / PI( )
                ) *60 * 1.1515
                ) AS distance
                FROM route_coords r1, route_coords r2
                WHERE ((r1.seq = r2.seq - 1) OR
                       (r1.seq = (SELECT MAX(seq) FROM route_coords r3 WHERE r3.route_id = r1.route_id) AND r2.seq = 0)
                      )
                AND r1.route_id = r2.route_id";
        mysql_query($sql);
    }
    
    function truncate() {
        $sql = "TRUNCATE TABLE route_coord_distances";
        mysql_query($sql);
    }
    function get($route_id='',$seq1='',$seq2='') {
        $sql = "SELECT * FROM route_coord_distances
                    WHERE 1 ";
        if ($route_id) 
            $sql .= " AND route_id = '".$route_id."'";
        if ($seq1) 
            $sql .= " AND seq1 = '".$seq1."'";
        if ($seq2) 
            $sql .= " AND seq2 = '".$seq2."'";
        return db_query_array($sql);
    }
    
    function calcDistances() {
        /* this part inserts records for the distance between each node on the route - NOT WORKING */
        $sql = "INSERT INTO route_coord_distances 
        SELECT r1.route_id, r1.seq_1, r2.seq_2, SUM( r2.distance ) AS distance
                FROM `route_coord_distances` r1, `route_coord_distances` r2
                WHERE r1.route_id = r2.route_id
                AND (
                r2.seq_2
                BETWEEN r1.seq_1
                AND r2.seq_2
                )
                GROUP BY r1.route_id, r1.seq_1, r2.seq_2
        UNION
                SELECT r1.route_id, r1.seq_1, r2.seq_2, SUM( r2.distance ) AS distance
                FROM `route_coord_distances` r1, `route_coord_distances` r2
                WHERE r1.route_id = r2.route_id
                AND (
                    (r1.seq_2 BETWEEN r2.seq_2 AND (SELECT MAX(seq) FROM route_coords WHERE route_id = r1.route_id)) 
                      OR
                    (r1.seq_2 BETWEEN 0 AND r2.seq_1)
                )
                GROUP BY r1.route_id, r1.seq_1, r2.seq_2";        
                
        mysql_query($sql);
        
    }
}
  

?>