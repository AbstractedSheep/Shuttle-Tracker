<?php
  class Stop {
    function get($stop_id) {
        $sql = "SELECT * FROM stops WHERE stop_id = '".$stop_id."'";
        return db_query_array($sql);
    }
    function insert($info) {
        $ret = mysql_query("INSERT INTO stops (stop_id , location , name    )
                    VALUES ('".$info->short_name."',
                        GeomFromText( 'POINT(" . $info->latitude . " " . $info->longitude . ")' ), 
                        '".addslashes($info->name)."') ");
        return mysql_insert_id();
    }
    function update($stop_id,$info) {
        
        $ret = mysql_query("UPDATE stops
                     SET location = GeomFromText( 'POINT(" . $info->latitude . " " . $info->longitude . ")' ),
                         name = '".addslashes($info->name)."'
                     WHERE stop_id = '" . addslashes($info->short_name)."'");
                
        return mysql_affected_rows( );
    }
    function delete($stop_id) {
        return db_delete('stops',$stop_id);
    }
}

?>
