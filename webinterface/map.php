<?php
    include("header.php");    
?>
<body>
<script type="text/javascript">
    var shuttleMap;

    $('.page-map').live("pagecreate", function() {
        shuttleMap = new ShuttleMap;
        shuttleMap.refresh();
        
        /* enable the refresh button */ 
        $("#refresh").click(function(){
            shuttleMap.refresh();
        });
                
    });
    
</script>

<style>
    .page-map, .ui-content, #map-canvas { width: 100%; height: 100%; padding: 0; }
    .ui-btn-inner {
        height: 30px !important;
    }
    .ui-navbar, .ui-navbar-noicons {
        height: 40px;
}
</style>

<div data-role="page" id="eta" class="page-map">
    <div  data-role="header" class="ui-bar" > 
        <h1>RPI Shuttles</h1> 
    </div>
    <div data-role="navbar">
        <ul>                                                                                                                 
            <li><a href="index.php" rel=external>ETAs</a></li>
            <li><a href="map.php" rel=external class="ui-btn-active">Map</a></li>
        </ul>
    </div>    
    <div data-role="content" id="etalist">
        <div id="map_canvas" style="height: 100%;"></div>
    </div>
</div>

</body>
</html>