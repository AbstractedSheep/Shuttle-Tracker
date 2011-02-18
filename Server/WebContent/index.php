<?php

include("application.php");

include("apps/data_service.php");  
include("apps/routecoorddistances.php");  

//RouteCoordDistances::loadDistanceTable();
//RouteCoordDistances::calcDistances();

//var_dump($etas);
$title = "When is the shuttle getting here?";
include("header.php");
DataService::displayETAs();
?>



</body>
</html>


