<?php

	//function getUrl($url) {
//		$ch = curl_init();
//		curl_setopt($ch, CURLOPT_URL, $url);
//		curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
//		curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 30);
//		curl_setopt($ch, CURLOPT_TIMEOUT, 5);
//		curl_setopt($ch, CURLOPT_FOLLOWLOCATION, 1);
		//curl_setopt($ch, CURLOPT_REFERER, $_SERVER['REQUEST_URI']);
//		$dataJSON = curl_exec($ch);
//		curl_close($ch);	
//		return $dataJSON;
//	}

	function db_query($query) 
	{
	    return mysql_query($query);
	}

	function db_query_array($query) 
	{
	    $res = mysql_query($query);
	    if (!$res) return false;

	    while ($row=mysql_fetch_array($res,MYSQL_ASSOC))
	        $ret[] = $row;

	    mysql_free_result($res);

	    return @$ret;
	}

	function db_insert($tbl,$dat)
	{
	    $sql =  'INSERT INTO `' . $tbl . '` ';
	    $sql .= '(`'.implode('`,`', array_keys($dat)).'`) ';
	    $sql .= ' VALUES (\'' . implode('\',\'', array_map('mysql_real_escape_string', array_values($dat))) . '\')';

	    mysql_query($sql);

	    return mysql_insert_id();
	}

	function db_update($tbl,$uid,$dat,$key)
	{
	    $sql = "UPDATE `$tbl` SET ";

	    if (is_array($dat)) 
    		foreach($dat as $k=>$v) {
				$sql .= "`$k`='".mysql_real_escape_string($v)."'";
				if ($v!=end($dat)) $sql .= ",";
			}
		
		$sql .= " WHERE ";

	    if (is_array($key)) {
	        foreach ($key as $k) {
	            list($idx,$v) = each($uid);
	            $sql .= " `$k`='".mysql_real_escape_string($v)."'";
	            if ($k!=end($key)) $sql .= " AND";
	        }
	    } else     	
	        $sql .= "`$key`='".mysql_real_escape_string($uid)."'";
	    
	    mysql_query($sql);

	    return mysql_affected_rows( );
	}

	function db_delete($tbl,$did,$key)
	{
	    $where = '';
	    if (is_array($key))
	        foreach ($key as $k) {
	            list($idx,$v) = each($did);
	            $where .= " `$k`='".mysql_real_escape_string($v)."'";
	            if ($k!=end($key)) $where .= " AND";
	        }
	    else
    		$where = "`$key`='".mysql_real_escape_string($did)."'";
	    
	    $sql = "DELETE FROM `$tbl` WHERE $where";

	    mysql_query($sql);

	    return mysql_affected_rows();
	}	
?>
