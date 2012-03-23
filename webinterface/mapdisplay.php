<?php
    /*include_once("apps/data_service.php");
    include_once("application.php");
    $shuttlePositions = DataService::getShuttlePositions();
    echo $shuttlePositions[0]; */
?>

<!DOCTYPE html>
<html>
<head>
<style type="text/css">
  html { height: 100% }
  body { height: 100%; margin: 0px; padding: 0px }
  #map_canvas { height: 100% }
</style>
<script type="text/javascript"
    src="http://maps.googleapis.com/maps/api/js?key=AIzaSyDrctp7C-T6sKrIgzwi7WYBO7lN7ZrQfGk&sensor=false">
</script>
<script type="text/javascript">
  function initialize() {
    var latlng = new google.maps.LatLng(42.729697,-73.677174);
    var myOptions = {
      zoom: 6,
      center: latlng,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    var map = new google.maps.Map(document.getElementById("map_canvas"),
        myOptions);
        var url_end = (new Date()).valueOf();
        var kml = "http://shuttles.rpi.edu/displays/netlink.kml?nocache=" + url_end;
        var ctaLayer = new google.maps.KmlLayer(kml);
    ctaLayer.setMap(map);
    
  }

</script>
</head>
<body onload="initialize()">
  <div id="map_canvas" style="width:100%; height:100%"></div>
</body>
</html>