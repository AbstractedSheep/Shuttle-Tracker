<?php
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
  

?>
