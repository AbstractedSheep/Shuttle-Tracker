<?php
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
?>
