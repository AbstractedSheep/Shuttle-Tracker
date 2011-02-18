<?php
    
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
        $ret = mysql_query("INSERT INTO route_coords (route_id , seq, location    )
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
  

?>
