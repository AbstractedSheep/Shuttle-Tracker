<?php
  /* DataService class - provides functions for getting data from the DB */
class DataService
{
    
    function connect_db()
    {
        $usr = "usr";
        $pwd = "pass";
        $db = "db261968011";
        $host = "db1696.perfora.net";
        
        $cid = mysql_connect($host, $usr, $pwd);                                    // database connection
        
        @mysql_select_db($db) or die("Unable to select database");                    // select the db and error check
    }

    function getData($shuttleNo)
    {
        //connect_db();
        /* read the database for the tracking info */
        $data_from_db = array('sn'=>1, 'speed'=>35, 'lat'=>12.4252, 'long'=>-74.24515, 'eta'=>'04:34'); // sample data for now
        return $data_from_db;
    }
}
?>
