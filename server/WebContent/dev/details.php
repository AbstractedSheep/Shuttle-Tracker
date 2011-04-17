<?php
    include_once("application.php");

include_once("apps/data_service.php");  
include_once("apps/routecoorddistances.php");
$stop=$_GET["stop"];
$route=$_GET["route"];
?>
<div data-role="page" id="eta">
    <div  data-role="header" class="ui-bar" data-inline="true"> 
        <h1>RPI Shuttle Tracking</h1>
    </div>
        <div data-role="content">
            <?
             DataService::drawExtraETA($route,$stop);
            ?>
        </div>

</div>    
<?
    include("footer.php");
?>