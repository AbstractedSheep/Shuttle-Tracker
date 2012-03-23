<?php
include_once("application.php");

include_once("apps/data_service.php");  
include_once("apps/routecoorddistances.php");
header("Content-Type: application/json");
echo DataService::displayETAs();
DataServiceData::recordStats("Get Next ETA"); 
?>