<?php
    include_once("application.php");

include_once("apps/data_service.php");  
include_once("apps/routecoorddistances.php");
$stop=$_GET["stop"];
$route=$_GET["route"];
//$favs = $_COOKIE["favs"];
//$favArray = explode(";",$favs);
//foreach ($favArray as $favStopRoutePair)
//{
//    $separated = explode($favStopRoutePair);
//    $favStop = $separated[0];
//    $favStop = $separated[0];
//    
//}
?>
<div data-role="page" id="eta">
    <div  data-role="header" class="ui-bar" data-inline="true"> 
        <h1>RPI Shuttle Tracking</h1>
        <a href="#" data-icon="plus" class="ui-btn-right" data-theme="c">Fav</a>
    </div>
        <div data-role="content">
            <?
             DataService::drawExtraETA($route,$stop);
            ?>
        </div>
    <?
    //include("footer.php");
    ?>
</div>    
