<?php
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
        $ret = mysql_query("INSERT INTO shuttle_coords (shuttle_id, heading, location, speed, public_status_msg, cardinal_point, update_time )
                    VALUES ('".$shuttle_id."',
                            '".$info->heading."',
                            GeomFromText( 'POINT(" . $info->latitude . " " . $info->longitude . ")' ),
                            '".$info->speed."',
                            '".$info->public_status_msg."',
                            '".$info->cardinal_point."',
                            NOW()
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

?>
