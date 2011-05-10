<?php
$action = $_REQUEST[action];
switch ($action) {
    case 'ajax_get_shuttle_positions' :
        $locs = array(
            array('lat'=>42.72276,'long'=>-73.67982 ),
            array('lat'=>42.72326,'long'=>-73.68052 ),
            array('lat'=>42.72356,'long'=>-73.68076 ),
            array('lat'=>42.72421,'long'=>-73.68107 ),
            array('lat'=>42.72514,'long'=>-73.68142 ),
            array('lat'=>42.72639,'long'=>-73.68164 ),
            array('lat'=>42.72658,'long'=>-73.68177 ),
            array('lat'=>42.72668,'long'=>-73.6819 ),
            array('lat'=>42.72735,'long'=>-73.68379 ),
            array('lat'=>42.72748,'long'=>-73.68406 ),
            array('lat'=>42.72773,'long'=>-73.68543 ),
            array('lat'=>42.72787,'long'=>-73.68585 ),
            array('lat'=>42.72823,'long'=>-73.68666 ),
            array('lat'=>42.72834,'long'=>-73.6871 ),
            array('lat'=>42.72834,'long'=>-73.6871 )
            );
        $shuttlepos = array('Shuttle 1'=> $locs[rand(0,count($locs)-1)], 'Shuttle 2'=>$locs[rand(0,count($locs)-1)], 'Shuttle 3'=>$locs[rand(0,count($locs)-1)]);
        header('Cache-Control: no-cache, must-revalidate');
        header('Expires: Mon, 26 Jul 1997 05:00:00 GMT');
        header('Content-type: application/json');
        echo json_encode($shuttlepos);
        exit;
        break;
    default :
        break;
}

?>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />  
<title>Map</title>
<script src="http://code.jquery.com/jquery.min.js" type="text/javascript"></script>
<script type="text/javascript"
    src="http://maps.google.com/maps/api/js?sensor=true&amp;key=ABQIAAAAOCTHylaOYOTc5g-_pCj8shSvS8Smej8DQ7j_pE7rX2KkSsk7qxQvVVhsMNeDyqo6NAc2ZwJet-r3CA">
</script>
</head>
<body  onload="initialize()">

<style type="text/css">
  html { height: 100% }
  body { height: 100%; margin: 0px; padding: 0px }
  #map_canvas { height: 100% }
</style>

<script type="text/javascript">
    
      function ShuttleMap() {
        var routes;
        var routeOptions;
        var shuttles;
        var map;
        var infowindow;
    
        this.routes = new Array();
        this.routeOptions = new Array();
        this.shuttles = new Array();
        this.infowindow = new google.maps.InfoWindow({
            content: ""
        });
        var latlng = new google.maps.LatLng(42.729697,-73.677174);
        var myOptions = {
              zoom: 15,
              center: latlng,
              mapTypeId: google.maps.MapTypeId.ROADMAP
        };
    
        this.map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
            
        this.routes.push( [
            new google.maps.LatLng(42.72276,-73.67982 ),
            new google.maps.LatLng(42.72326,-73.68052 ),
            new google.maps.LatLng(42.72356,-73.68076 ),
            new google.maps.LatLng(42.72421,-73.68107 ),
            new google.maps.LatLng(42.72514,-73.68142 ),
            new google.maps.LatLng(42.72639,-73.68164 ),
            new google.maps.LatLng(42.72658,-73.68177 ),
            new google.maps.LatLng(42.72668,-73.6819 ),
            new google.maps.LatLng(42.72735,-73.68379 ),
            new google.maps.LatLng(42.72748,-73.68406 ),
            new google.maps.LatLng(42.72773,-73.68543 ),
            new google.maps.LatLng(42.72787,-73.68585 ),
            new google.maps.LatLng(42.72823,-73.68666 ),
            new google.maps.LatLng(42.72834,-73.6871 ),
            new google.maps.LatLng(42.72834,-73.6871 ),
            new google.maps.LatLng(42.73113,-73.68647 ),
            new google.maps.LatLng(42.7336798463,-73.6857145463 ),
            new google.maps.LatLng(42.7333331033,-73.6844860945 ),
            new google.maps.LatLng(42.7331203283,-73.6835258637 ),
            new google.maps.LatLng(42.7328996719,-73.6825495396 ),
            new google.maps.LatLng(42.732746,-73.6823510562 ),
            new google.maps.LatLng(42.7318830663,-73.6816000376 ),
            new google.maps.LatLng(42.7315993594,-73.6814283763 ),
            new google.maps.LatLng(42.7314575055,-73.6812352572 ),
            new google.maps.LatLng(42.731225022,-73.680269662 ),
            new google.maps.LatLng(42.7310082993,-73.6788802777 ),
            new google.maps.LatLng(42.7306773031,-73.6766272221 ),
            new google.maps.LatLng(42.7305058482,-73.6766257882 ),
            new google.maps.LatLng(42.7304290093,-73.6766526103 ),
            new google.maps.LatLng(42.7303699024,-73.6766391993 ),
            new google.maps.LatLng(42.7303127657,-73.676609695 ),
            new google.maps.LatLng(42.7302989741,-73.6765533686 ),
            new google.maps.LatLng(42.7302989741,-73.6765533686 ),
            new google.maps.LatLng(42.7303226169,-73.6764460802 ),
            new google.maps.LatLng(42.7303777833,-73.676392436 ),
            new google.maps.LatLng(42.7305235802,-73.6763656139 ),
            new google.maps.LatLng(42.7306082998,-73.6763468385 ),
            new google.maps.LatLng(42.7306752873,-73.676392436 ),
            new google.maps.LatLng(42.7307324693,-73.6767184172 ),
            new google.maps.LatLng(42.7307836949,-73.677190486 ),
            new google.maps.LatLng(42.72937,-73.67762 ),
            new google.maps.LatLng(42.72937,-73.67762 ),
            new google.maps.LatLng(42.72774,-73.67809 ),
            new google.maps.LatLng(42.72705,-73.67807 ),
            new google.maps.LatLng(42.72682,-73.67811 ),
            new google.maps.LatLng(42.7268,-73.67811 ),
            new google.maps.LatLng(42.72515,-73.67868 ),
            new google.maps.LatLng(42.72464,-73.67879 ),
            new google.maps.LatLng(42.72356,-73.67913 ),
            new google.maps.LatLng(42.7231,-73.679372),
            new google.maps.LatLng(42.72276,-73.67982)
        ] );
        this.routeOptions.push( {
                strokeColor: '#FF0000',
                strokeOpacity: 1.0,
                strokeWeight: 4
                } );
      
        this.routes.push ([
              new google.maps.LatLng(42.7307,-73.677197),
            new google.maps.LatLng(42.7307,-73.677197),
            new google.maps.LatLng(42.7330,-73.676594),
            new google.maps.LatLng(42.7330,-73.676594),
            new google.maps.LatLng(42.7330,-73.676594),
            new google.maps.LatLng(42.7323,-73.671729),
            new google.maps.LatLng(42.7323,-73.671729),
            new google.maps.LatLng(42.7356,-73.670767),
            new google.maps.LatLng(42.736,-73.670566),
            new google.maps.LatLng(42.7382,-73.67017),
            new google.maps.LatLng(42.7377,-73.666567),
            new google.maps.LatLng(42.7377,-73.666567),
            new google.maps.LatLng(42.7359,-73.667077),
            new google.maps.LatLng(42.7359,-73.667077),
            new google.maps.LatLng(42.7356,-73.665087),
            new google.maps.LatLng(42.7356,-73.665087),
            new google.maps.LatLng(42.7356,-73.665084),
            new google.maps.LatLng(42.7356,-73.665087),
            new google.maps.LatLng(42.7356,-73.665087),
            new google.maps.LatLng(42.7354,-73.663365),
            new google.maps.LatLng(42.7353,-73.663167),
            new google.maps.LatLng(42.7353,-73.663167),
            new google.maps.LatLng(42.7352,-73.663111),
            new google.maps.LatLng(42.7344,-73.663356),
            new google.maps.LatLng(42.7339,-73.663539),
            new google.maps.LatLng(42.7326,-73.665168),
            new google.maps.LatLng(42.7321,-73.665788),
            new google.maps.LatLng(42.7310,-73.666769),
            new google.maps.LatLng(42.7309,-73.6671),
            new google.maps.LatLng(42.7308,-73.667268),
            new google.maps.LatLng(42.7308,-73.667268),
            new google.maps.LatLng(42.7312,-73.668257),
            new google.maps.LatLng(42.7312,-73.668257),
            new google.maps.LatLng(42.7316,-73.66911),
            new google.maps.LatLng(42.7317,-73.66972),
            new google.maps.LatLng(42.7317,-73.67029),
            new google.maps.LatLng(42.7318,-73.670477),
            new google.maps.LatLng(42.7319,-73.670648),
            new google.maps.LatLng(42.7322,-73.670791),
            new google.maps.LatLng(42.7322,-73.670928),
            new google.maps.LatLng(42.733,-73.678463),
            new google.maps.LatLng(42.7339,-73.681895),
            new google.maps.LatLng(42.733981218,-73.68222846041),
            new google.maps.LatLng(42.732952809,-73.6825181393),
            new google.maps.LatLng(42.732807018,-73.68230356222),
            new google.maps.LatLng(42.732511494,-73.6820567996),
            new google.maps.LatLng(42.73190862,-73.6815310862),
            new google.maps.LatLng(42.731613094,-73.68134333141),
            new google.maps.LatLng(42.73149882,-73.68113411913),
            new google.maps.LatLng(42.731301803,-73.68029726992),
            new google.maps.LatLng(42.731077,-73.67889715682),
            new google.maps.LatLng(42.730,-73.676577),
            new google.maps.LatLng(42.730647704,-73.67661505942),
            new google.maps.LatLng(42.730616180,-73.67660701277),
            new google.maps.LatLng(42.730568895,-73.67661774163),
            new google.maps.LatLng(42.730535401,-73.67662578825),
            new google.maps.LatLng(42.730387634,-73.67667138585),
            new google.maps.LatLng(42.730346259,-73.6766365177),
            new google.maps.LatLng(42.73030685,-73.67655068645),
            new google.maps.LatLng(42.73030685,-73.67654800425),
            new google.maps.LatLng(42.730318676,-73.67646217354),
            new google.maps.LatLng(42.73035808,-73.67640852931),
            new google.maps.LatLng(42.730432949,-73.67638438948),
            new google.maps.LatLng(42.730511758,-73.67635756738),
            new google.maps.LatLng(42.730600418,-73.67634147419),
            new google.maps.LatLng(42.730677257,-73.67637634286),
            new google.maps.LatLng(42.730708781,-73.67655068641),
            new google.maps.LatLng(42.73077,-73.67719)
        ]);
        this.routeOptions.push( {
                strokeColor: '#00FF00',
                strokeOpacity: 1.0,
                strokeWeight: 4
                } );

        /* draw the routes on the map */                
        for(i=0; i<this.routes.length; i++) {
              line1 = new google.maps.Polyline({
                path: this.routes[i],
                strokeColor: this.routeOptions[i].strokeColor,
                strokeOpacity: this.routeOptions[i].strokeOpacity,
                strokeWeight: this.routeOptions[i].strokeWeight
              });  
              line1.setMap(this.map);
        };

  }
      function Shuttle(id,map) {
        var id;
        var lat;
        var long;
        var heading; 
        var speed;
        var cardinal_point;
        var route;       
        var marker;
        var map;
        var name;
        var lastUpdated;
        this.id = id;
        this.name = null;
        this.map = map;
        this.marker = null;
        this.lastUpdated = null; 
      }
      
      Shuttle.prototype = {
        setAttribs: function(shuttleInfoObj) {
            this.lat = shuttleInfoObj.latitude;
            this.long = shuttleInfoObj.longitude;
            this.name = shuttleInfoObj.name;
            this.heading = shuttleInfoObj.heading;
            this.speed = shuttleInfoObj.speed;
            this.cardinal_point = shuttleInfoObj.cardinal_point;
            this.route = shuttleInfoObj.route_id;
            this.lastUpdated = new Date(mysqlTimeStampToDate(shuttleInfoObj.update_time));
            
            if (!this.marker) {
                var shuttlePositionlatlng = new google.maps.LatLng(this.lat, this.long);
                this.marker = new google.maps.Marker({
                    position: shuttlePositionlatlng, 
                    map: this.map,
                    title: this.id,
                    icon: 'images/shuttle_red.png' 
                });                   
                google.maps.event.addListener( this.marker, 'click', 
                    function (msg) {
                        return function () {
                            shuttleMap.infowindow.setContent(msg);
                            shuttleMap.infowindow.open(shuttleMap.map,this);
                        }
                    } (this.id)
                    );                    
            }
            
            if (this.route == 1) {
                this.marker.icon = "images/shuttle_red.png";   
            }
                else if (this.route == 2) {
                this.marker.icon = "images/shuttle_green.png";
            }
            
            var newshuttlePositionlatlng = new google.maps.LatLng(this.lat, this.long);
            this.marker.setPosition(newshuttlePositionlatlng);
        },
        shutdown: function() {
            this.marker.setMap(null); /* remove marker from map - NEED TO TEST */
        }
        ,
        expired: function() {
            var expirationSecs = 90;
            if (this.lastUpdated==null)
                return false;
            var now = new Date();
            var now_unix = now.getTime();
            return ((now_unix - this.lastUpdated.getTime()) / 1000 ) > expirationSecs;
        }
    }
      
      ShuttleMap.prototype = {
          startupShuttle: function(id) {
              if (!this.shuttles[id]) 
                  this.shuttles[id] = new Shuttle(id,this.map);
        }
        ,
          shutdownShuttle: function(id) {
              if (this.shuttles[id]) {
                  this.shuttles[id].shutdown(); 
                  delete this.shuttles[id];
              }
        }
        ,
          setShuttle: function(shuttleInfoObj) {
              if (!this.shuttles[shuttleInfoObj.shuttle_id]) 
                  this.shuttles[shuttleInfoObj.shuttle_id] = new Shuttle(shuttleInfoObj.shuttle_id,this.map);
              this.shuttles[shuttleInfoObj.shuttle_id].setAttribs(shuttleInfoObj);
        }
        ,
//        randomizeShuttles: function() {
//            for (shuttleId in this.shuttles) {
//                randLoc = this.routes[0][Math.floor(Math.random()*this.routes[0].length)];
//                this.shuttles[shuttleId].position(randLoc.lat(), randLoc.lng());
//            }
//            window.setTimeout('shuttleMap.randomizeShuttles()',1000);
//        }
//        ,
        shutdownExpiredShuttles: function() {
            for (id in this.shuttles) {
                if (this.shuttles[id].expired()) {
                    this.shutdownShuttle(id);
                }
            }            
        }
        ,
        refreshShuttles: function() {
            /* RUN EVERY 5 SECONDS TO UPDATE SHUTTLES */
            /* load shuttle positions from database (ajax) */
            $.getJSON('data_service.php?action=get_shuttle_positions', function(data) {
                  $.each(data, function(shuttleIdx,shuttleInfoObj) {
                  /* setup new shuttles and set positions on all shuttles on map */
                        shuttleMap.setShuttle(shuttleInfoObj);
                        /* shutdownShuttle any shuttle markers that are not in database */
                      shuttleMap.shutdownExpiredShuttles();

                  });
            });
            window.setTimeout('shuttleMap.refreshShuttles()',15000);
        }
    }
    var shuttleMap;
    function initialize() {
        shuttleMap = new ShuttleMap;
        
        /* TESTING */
        shuttleMap.refreshShuttles();
        
    }
function mysqlTimeStampToDate(timestamp) {
    //function parses mysql datetime string and returns javascript Date object
    //input has to be in this format: 2007-06-05 15:26:02
    var regex=/^([0-9]{2,4})-([0-1][0-9])-([0-3][0-9]) (?:([0-2][0-9]):([0-5][0-9]):([0-5][0-9]))?$/;
    var parts=timestamp.replace(regex,"$1 $2 $3 $4 $5 $6").split(' ');
    return new Date(parts[0],parts[1]-1,parts[2],parts[3],parts[4],parts[5]);
  }


</script>

<div id="map_canvas" style="width:100%; height:100%"></div>


</body>
</html>