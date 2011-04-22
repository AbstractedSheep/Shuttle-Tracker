<?php
  
class Route {
    function get($route_id='') {
        $sql = "SELECT * FROM routes
                    WHERE 1 ";
        if ($route_id) 
            $sql .= " AND route_id = '".$route_id."'";
        return db_query_array($sql);
    }
    function insert($route_id,$info) {
        $ret = mysql_query("INSERT INTO routes (route_id , name, color, width    )
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

?>
