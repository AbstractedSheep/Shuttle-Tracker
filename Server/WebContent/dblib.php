<?php
  function db_query_array($query,$key='',$first_record=false,$unbuffered=false,$val_field='') 
{
    global $CFG, $DB_DEBUG;

    $result = mysql_query($query);

    $amt = mysql_num_rows($result);

    if ($key && !$first_record)
        while ($row=mysql_fetch_array($result,MYSQL_ASSOC))
            $return_arr[$row[$key]] = ($val_field) ? $row[$val_field] : $row;
    else
        while ($row=mysql_fetch_array($result,MYSQL_ASSOC))
            $return_arr[] = ($val_field) ? $row[$val_field] : $row;

    mysql_free_result($result); // clear memory

    if ($first_record && isset($return_arr[0]))
        return $return_arr[0];
    else if (!$first_record)
        return @$return_arr;
    else
        return false;
}

function db_insert($table,$info,$date='',$ignore=false,$silent=false,$echo_sql=false,$return_bool=false,$delayed=false)
{

    $ignore = ($ignore) ? 'IGNORE' : '';
    $delayed = ($delayed) ? 'DELAYED' : '';

    $sql = "INSERT $delayed $ignore INTO $table (";
    $vals = ") VALUES (";

    foreach($info as $key=>$val) {
        $sql .= "`$key`,";
        $vals .= "'".addslashes($val)."',";
    }

    if ($date) {
        $sql .= "`$date`,";
        $vals .= 'NOW(),';
    }

    // remove the trailing commas
    $sql = substr($sql,0,-1);
    $vals = substr($vals,0,-1);

    $sql .= "$vals)";
    
    if (!$silent){
        $return_val = mysql_query($sql);
    } else {
        $return_val = mysql_query($sql,false,false,true);
        if (!$return_val)
        return $return_val;
    }

    if ($echo_sql)
        echo "<div style='border:1px solid black'>{$sql}</div>";

    if ($return_bool)
        return $return_val;

    return mysql_insert_id();
}

function db_replace($table,$info,$date='',$return_bool=false)
{
    $sql = "REPLACE INTO $table (";
    $vals = ") VALUES (";

    foreach($info as $key=>$val) {
        $sql .= "`$key`,";
        $vals .= "'".addslashes($val)."',";
    }

    if ($date) {
        $sql .= "`$date`,";
        $vals .= 'NOW(),';
    }

    // remove the trailing commas
    $sql = substr($sql,0,-1);
    $vals = substr($vals,0,-1);

    $sql .= "$vals)";

    //echo $sql;
    //
    $return_val = mysql_query($sql);

    if ($return_bool)
    return $return_val;

    return mysql_insert_id();
}

function db_update($table,$id,$info,$pk='id',$date='',$echo_sql=false)
{
    $sql = "UPDATE $table SET ";

    if (is_array($info)) {
        foreach($info as $key=>$val) {
            $sql .= "`$key`='".addslashes($val)."',";
        }
    }

    if ($date) {
        $sql .= "`$date`=NOW(),";
    }

    if (!is_array($pk)) {
        $sql = substr($sql,0,-1)." WHERE `$pk`='".addslashes($id)."'";
    } else {
        $sql = substr($sql,0,-1)." WHERE";

        foreach ($pk as $key) {
            list(,$val) = each($id);
            $sql .= " `$key`='".addslashes($val)."' AND";
        }

        $sql = substr($sql,0,-3);
    }

    mysql_query($sql);

    if($echo_sql)
        echo $sql;

    return mysql_affected_rows( );
}

function db_delete($table,$id,$pk='id')
{
    if (!is_array($pk))
    $where = "`$pk`='".addslashes($id)."'";
    else {
        $where = '';

        foreach ($pk as $key) {
            list(,$val) = each($id);
            $where .= " `$key`='".addslashes($val)."' AND";
        }

        $where = substr($where,0,-3);
    }

    mysql_query("DELETE FROM $table WHERE $where");

    return mysql_affected_rows();
}

function db_disconnect()
{
    mysql_close();
}
?>
