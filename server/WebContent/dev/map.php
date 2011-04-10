<?php
?>
<!DOCTYPE html>
<html>
<head>
<title>Map</title>
</head>
<body>
<div data-role="page">
<div data-role="header" data-id="header">
    <div  data-role="header" class="ui-bar" data-inline="true"> 
        <h1>RPI Shuttle Tracking</h1> 
            
    </div>
</div><!-- /header -->  
<div data-role="content">
<iframe style="width:100%; height:100%" src="mapdisplay.php"></iframe>
</div><!-- /content -->
<div data-role="footer" data-position="fixed" class="ui-bar-a" role="banner" data-id="footer">
    <div data-role="navbar" role="navigation">
        <ul class="ui-grid-a">
            <li class="ui-block-a"><a href="map.php" data-role="button" id="map">Map</a>  </li>
            <li class="ui-block-b"><a href="#eta" data-role="button" id="eta">ETA</a></li>
        </ul>
    </div>
</div><!-- /header -->
</div><!-- /page -->

</body>
</html>