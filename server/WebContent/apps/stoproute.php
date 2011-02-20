<?php
  
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
  
?>
