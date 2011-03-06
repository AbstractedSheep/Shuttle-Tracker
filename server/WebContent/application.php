<?php
include("stdlib.php");
 
include("passwords.php"); //$usr = "";  $pwd = "";  $db = "";  $host = "";

$cid = mysql_connect($host, $usr, $pwd);                                    // database connection

@mysql_select_db($db) or die("Unable to select database");                    // select the db and error check

?>
