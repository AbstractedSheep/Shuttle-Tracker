<?php
setcookie("favs", "union:1;union:2;blitman:1");

//RouteCoordDistances::loadDistanceTable();
//RouteCoordDistances::calcDistances();

//var_dump($etas);
include("header.php");

?>
 

<!--<div id="boxes">
 
 
    <div id="dialog" class="window">
        Are you still here?
        <br /> 
        <!-- close button is defined as close class 
        <a href="#" class="close">I'm here!</a>    
        
 
    </div>
 
    <div id="mask"></div>
</div> -->


<div data-role="page" id="eta">
    <div data-role="header" class="ui-bar" data-inline="true"> 
        <h1>RPI Shuttle Tracking</h1>
        <a href="#" data-role="button" data-icon="refresh" data-iconpos="notext" id="refresh">Refresh</a> 
        <a href="map.php" data-role="button" id="map">map</a>    
    </div>
<div data-role="content" id="etalist">
    <div id="favorite">   
    </div>
    <div id="west">
    </div>
    <div id="east">
    </div>
</div>
<? 
//include("footer.php"); 
?>
</div>

</body>
</html>