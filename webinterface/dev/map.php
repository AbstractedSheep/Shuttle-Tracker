<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>

<link rel="shortcut icon" href="/favicon.ico" type="image/x-icon" /> 
<meta name="viewport" content="width=device-width, minimum-scale=1, maximum-scale=1">
<link rel="stylesheet" href="http://shuttles.abstractedsheep.com/dev/css/jquery.mobile-1.0a4.1.css" />
<script src="http://shuttles.abstractedsheep.com/dev/js/jquery.min.js"></script>
<script src="http://shuttles.abstractedsheep.com/dev/js/jquery.mobile-1.0a4.1.min.js"></script>
<script type="text/javascript"
    src="http://maps.google.com/maps/api/js?sensor=true&amp;key=ABQIAAAA_T0LgHopbfLS6-Rj2fUl5BReimdJurms3pIzvgaPNMSnY_82uhQ1nzdUW5hv9wxYNqMuMQmJktMIPA">
</script>
<script type="text/javascript">

 $(document).ready(function() {
     
     initialize();         
        
    $("#refresh").click(function(){
        initialize();
    });
}); 

</script>
</head>
<body>

<script type="text/javascript">
    
      function ShuttleMap() {
        var routes;
        var routeOptions;
        var stops;
        var shuttles;
        var map;
        var infowindow;
        var bounds; /* keep track of map bounds */
    
        this.routes = new Array();
        this.routeOptions = new Array();
        
        /* marker arrays */
        this.shuttles = new Array();
        this.stops = new Array();
        this.bounds = new google.maps.LatLngBounds();
        
        this.infowindow = new google.maps.InfoWindow({
            content: ""
        });
        //var latlng = new google.maps.LatLng(42.729697,-73.677174);
        var myOptions = {
              zoom: 15,
              /* center: latlng, */
              mapTypeId: google.maps.MapTypeId.ROADMAP
        };
    
        this.map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);

        var netlink = {"stops":[{"latitude":"42.7302712352","longitude":"-73.6765441399","name":"Student Union","short_name":"union","routes":[{"id":1,"name":"West Campus"},{"id":2,"name":"East Campus"}]},{"latitude":"42.7316602341","longitude":"-73.669705848","name":"BARH","short_name":"barh","routes":[{"id":2,"name":"East Campus"}]},{"latitude":"42.7326945747","longitude":"-73.6651957135","name":"Sunset Terrace","short_name":"sunset","routes":[{"id":2,"name":"East Campus"}]},{"latitude":"42.7345435388","longitude":"-73.6634750765","name":"Beman Lane","short_name":"beman","routes":[{"id":2,"name":"East Campus"}]},{"latitude":"42.7363264617","longitude":"-73.6705348752","name":"Colonie Apartments","short_name":"colonie","routes":[{"id":2,"name":"East Campus"}]},{"latitude":"42.7328677775","longitude":"-73.6824515003","name":"9th and Sage","short_name":"9th_sage","routes":[{"id":2,"name":"East Campus"}]},{"latitude":"42.731491317","longitude":"-73.6813858548","name":"West Hall","short_name":"west","routes":[{"id":1,"name":"West Campus"},{"id":2,"name":"East Campus"}]},{"latitude":"42.7309557796","longitude":"-73.6790031891","name":"Sage","short_name":"sage","routes":[{"id":1,"name":"West Campus"},{"id":2,"name":"East Campus"}]},{"latitude":"42.7357265852","longitude":"-73.6658566796","name":"Brinsmade Terrace","short_name":"brinsmade","routes":[{"id":2,"name":"East Campus"}]},{"latitude":"42.7293609807","longitude":"-73.6777055364","name":"Footbridge","short_name":"footbridge","routes":[{"id":1,"name":"West Campus"}]},{"latitude":"42.7228252768","longitude":"-73.679778884","name":"Polytech Apartments","short_name":"polytech","routes":[{"id":1,"name":"West Campus"}]},{"latitude":"42.7307421853","longitude":"-73.6864277539","name":"Blitman Residence Commons","short_name":"blitman","routes":[{"id":1,"name":"West Campus"}]},{"latitude":"42.7267473729","longitude":"-73.6781695585","name":"15th and College","short_name":"15th_college","routes":[{"id":1,"name":"West Campus"}]}],"routes":[{"color":"#E1501B","id":1,"name":"West Campus","width":4,"coords":[{"latitude":"42.72276","longitude":"-73.67982"},{"latitude":"42.72326","longitude":"-73.68052"},{"latitude":"42.72356","longitude":"-73.68076"},{"latitude":"42.72421","longitude":"-73.68107"},{"latitude":"42.72514","longitude":"-73.68142"},{"latitude":"42.72639","longitude":"-73.68164"},{"latitude":"42.72658","longitude":"-73.68177"},{"latitude":"42.72668","longitude":"-73.6819"},{"latitude":"42.72735","longitude":"-73.68379"},{"latitude":"42.72748","longitude":"-73.68406"},{"latitude":"42.72773","longitude":"-73.68543"},{"latitude":"42.72787","longitude":"-73.68585"},{"latitude":"42.72823","longitude":"-73.68666"},{"latitude":"42.72834","longitude":"-73.6871"},{"latitude":"42.72834","longitude":"-73.6871"},{"latitude":"42.73113","longitude":"-73.68647"},{"latitude":"42.7336798463","longitude":"-73.6857145463"},{"latitude":"42.7333331033","longitude":"-73.6844860945"},{"latitude":"42.7331203283","longitude":"-73.6835258637"},{"latitude":"42.7328996719","longitude":"-73.6825495396"},{"latitude":"42.732746","longitude":"-73.6823510562"},{"latitude":"42.7318830663","longitude":"-73.6816000376"},{"latitude":"42.7315993594","longitude":"-73.6814283763"},{"latitude":"42.7314575055","longitude":"-73.6812352572"},{"latitude":"42.731225022","longitude":"-73.680269662"},{"latitude":"42.7310082993","longitude":"-73.6788802777"},{"latitude":"42.7306773031","longitude":"-73.6766272221"},{"latitude":"42.7305058482","longitude":"-73.6766257882"},{"latitude":"42.7304290093","longitude":"-73.6766526103"},{"latitude":"42.7303699024","longitude":"-73.6766391993"},{"latitude":"42.7303127657","longitude":"-73.676609695"},{"latitude":"42.7302989741","longitude":"-73.6765533686"},{"latitude":"42.7302989741","longitude":"-73.6765533686"},{"latitude":"42.7303226169","longitude":"-73.6764460802"},{"latitude":"42.7303777833","longitude":"-73.676392436"},{"latitude":"42.7305235802","longitude":"-73.6763656139"},{"latitude":"42.7306082998","longitude":"-73.6763468385"},{"latitude":"42.7306752873","longitude":"-73.676392436"},{"latitude":"42.7307324693","longitude":"-73.6767184172"},{"latitude":"42.7307836949","longitude":"-73.677190486"},{"latitude":"42.72937","longitude":"-73.67762"},{"latitude":"42.72937","longitude":"-73.67762"},{"latitude":"42.72774","longitude":"-73.67809"},{"latitude":"42.72705","longitude":"-73.67807"},{"latitude":"42.72682","longitude":"-73.67811"},{"latitude":"42.7268","longitude":"-73.67811"},{"latitude":"42.72515","longitude":"-73.67868"},{"latitude":"42.72464","longitude":"-73.67879"},{"latitude":"42.72356","longitude":"-73.67913"},{"latitude":"42.72312","longitude":"-73.67937"},{"latitude":"42.72276","longitude":"-73.67982"}]},{"color":"#96C03A","id":2,"name":"East Campus","width":4,"coords":[{"latitude":"42.73077","longitude":"-73.67719"},{"latitude":"42.73077","longitude":"-73.67719"},{"latitude":"42.73304","longitude":"-73.67659"},{"latitude":"42.73304","longitude":"-73.67659"},{"latitude":"42.73304","longitude":"-73.67659"},{"latitude":"42.73239","longitude":"-73.67172"},{"latitude":"42.73239","longitude":"-73.67172"},{"latitude":"42.73567","longitude":"-73.67076"},{"latitude":"42.7366","longitude":"-73.67056"},{"latitude":"42.73827","longitude":"-73.6701"},{"latitude":"42.73777","longitude":"-73.66656"},{"latitude":"42.73777","longitude":"-73.66656"},{"latitude":"42.73597","longitude":"-73.66707"},{"latitude":"42.73597","longitude":"-73.66707"},{"latitude":"42.73567","longitude":"-73.66508"},{"latitude":"42.73567","longitude":"-73.66508"},{"latitude":"42.73564","longitude":"-73.66508"},{"latitude":"42.73567","longitude":"-73.66508"},{"latitude":"42.73567","longitude":"-73.66508"},{"latitude":"42.73545","longitude":"-73.66336"},{"latitude":"42.73537","longitude":"-73.66316"},{"latitude":"42.73537","longitude":"-73.66316"},{"latitude":"42.73521","longitude":"-73.66311"},{"latitude":"42.73446","longitude":"-73.66335"},{"latitude":"42.73399","longitude":"-73.66353"},{"latitude":"42.73268","longitude":"-73.66516"},{"latitude":"42.73218","longitude":"-73.66578"},{"latitude":"42.73109","longitude":"-73.66676"},{"latitude":"42.73091","longitude":"-73.667"},{"latitude":"42.73088","longitude":"-73.66726"},{"latitude":"42.73088","longitude":"-73.66726"},{"latitude":"42.73127","longitude":"-73.66825"},{"latitude":"42.73127","longitude":"-73.66825"},{"latitude":"42.73161","longitude":"-73.6691"},{"latitude":"42.73172","longitude":"-73.6697"},{"latitude":"42.73179","longitude":"-73.6702"},{"latitude":"42.73187","longitude":"-73.67047"},{"latitude":"42.73198","longitude":"-73.67064"},{"latitude":"42.73221","longitude":"-73.67079"},{"latitude":"42.73228","longitude":"-73.67092"},{"latitude":"42.7333","longitude":"-73.67846"},{"latitude":"42.73395","longitude":"-73.68189"},{"latitude":"42.7339812181","longitude":"-73.6822284604"},{"latitude":"42.7329528093","longitude":"-73.682518139"},{"latitude":"42.7328070182","longitude":"-73.6823035622"},{"latitude":"42.7325114946","longitude":"-73.682056799"},{"latitude":"42.731908622","longitude":"-73.681531086"},{"latitude":"42.7316130941","longitude":"-73.6813433314"},{"latitude":"42.731498823","longitude":"-73.6811341191"},{"latitude":"42.7313018032","longitude":"-73.6802972699"},{"latitude":"42.7310772","longitude":"-73.6788971568"},{"latitude":"42.7307","longitude":"-73.67657"},{"latitude":"42.7306477042","longitude":"-73.6766150594"},{"latitude":"42.7306161807","longitude":"-73.6766070127"},{"latitude":"42.7305688953","longitude":"-73.6766177416"},{"latitude":"42.7305354015","longitude":"-73.6766257882"},{"latitude":"42.7303876345","longitude":"-73.6766713858"},{"latitude":"42.7303462597","longitude":"-73.676636517"},{"latitude":"42.730306855","longitude":"-73.6765506864"},{"latitude":"42.730306855","longitude":"-73.6765480042"},{"latitude":"42.7303186764","longitude":"-73.6764621735"},{"latitude":"42.730358081","longitude":"-73.6764085293"},{"latitude":"42.7304329498","longitude":"-73.6763843894"},{"latitude":"42.7305117588","longitude":"-73.6763575673"},{"latitude":"42.7306004189","longitude":"-73.6763414741"},{"latitude":"42.7306772576","longitude":"-73.6763763428"},{"latitude":"42.7307087811","longitude":"-73.6765506864"},{"latitude":"42.73077","longitude":"-73.67719"}]}]};

        /* draw the stops on the map */                
          var stopsObj = netlink.stops;
        for (stopId in stopsObj) {
            this.setStop(stopsObj[stopId]);
        };

        /* draw the routes on the map */                
          var routesObj = netlink.routes;
          for (var i=0; i<routesObj.length; i++) {
            var routelist = new Array();
            for (var j=0; j<routesObj[i].coords.length; j++) {
                this.logBounds(routesObj[i].coords[j].latitude,routesObj[i].coords[j].longitude);
//                routelist.push(rll);
            }
            line1 = new google.maps.Polyline({
                path: routelist,
                strokeColor: routesObj[i].id=="1"?'#E1501B':'#96C03A',
                strokeOpacity: 1.0,
                strokeWeight: 4
              }); 
            line1.setMap(this.map);
        };        
    }
  
    function Shuttle(shuttleInfoObj,map) {
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
        
        this.map = map;        
        this.id = shuttleInfoObj.shuttle_id;
        this.lat = shuttleInfoObj.latitude;
        this.long = shuttleInfoObj.longitude;
        this.name = shuttleInfoObj.name;
        this.heading = shuttleInfoObj.heading;
        this.speed = shuttleInfoObj.speed;
        this.cardinal_point = shuttleInfoObj.cardinal_point;
        this.route = shuttleInfoObj.route_id;
        this.lastUpdated = new Date(mysqlTimeStampToDate(shuttleInfoObj.update_time));
        
        var iconsrc = 'images/shuttle_'+(this.route=='1'?'red':'green')+'_'+((this.cardinal_point.replace(" ","_")).toLowerCase())+'.png';
        var shuttlePositionlatlng = new google.maps.LatLng(this.lat, this.long);
        this.marker = new google.maps.Marker({
            position: shuttlePositionlatlng, 
            map: this.map,
            title: this.id,
            icon: iconsrc
        });                   
        google.maps.event.addListener( this.marker, 'click', 
            function (shuttleId) {
                return function () {
                    shuttleMap.displayShuttleBubble(shuttleId);
                }
            } (this.id)
        ); 
    }
      
      Shuttle.prototype = {
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
      
    function Stop(stopInfoObj,map) {
        var id;
        var lat;
        var long;
        var routes;
        var marker;
        var map;
        var name;
        this.id = stopInfoObj.short_name;
        this.map = map;
        
        this.lat = stopInfoObj.latitude;
        this.long = stopInfoObj.longitude;
        this.name = stopInfoObj.name;
        this.routes = stopInfoObj.routes;
        
        var stopiconsrc = 'images/stop_icon.png';
        var stoplatlng = new google.maps.LatLng(this.lat, this.long);
        this.marker = new google.maps.Marker({
            position: stoplatlng, 
            map: this.map,
            title: this.name,
            icon: stopiconsrc
        });                   
        google.maps.event.addListener( this.marker, 'click', 
            function (stopId) {
                return function () {
                    shuttleMap.displayStopBubble(stopId);
                }
            } (this.id)
        );        
    }
      
      Stop.prototype = {
    }
      
      ShuttleMap.prototype = {
          resizeMapToMarkers: function() {
            //  Fit these bounds to the map
           // this.map.fitBounds(this.bounds);    
          }
          ,
          displayShuttleBubble: function(shuttleId) {
              var msg = '<b>'+this.shuttles[shuttleId].name+'</b>';
              msg += '<br>Heading: <b>'+this.shuttles[shuttleId].cardinal_point;
              msg += ' at '+this.shuttles[shuttleId].speed+' mph'+'</b>';
              //msg += '<br>Last upd: '+this.shuttles[shuttleId].lastUpdated;
            this.infowindow.setContent(msg);
            this.infowindow.open(this.map,this.shuttles[shuttleId].marker);
          }
          ,
          displayStopBubble: function(stopId) {
              var msg = '<b>'+this.stops[stopId].name+'</b>';
              for (route in this.stops[stopId].routes)
                  msg += '<br>'+this.stops[stopId].routes[route].name+' route';
            this.infowindow.setContent(msg);
            this.infowindow.open(this.map,this.stops[stopId].marker);
          }
          ,
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
        logBounds: function(lat,long) {
            this.bounds.extend(new google.maps.LatLng(lat, long));
        }
        ,
        setShuttle: function(shuttleInfoObj) {
            if (!this.shuttles[shuttleInfoObj.shuttle_id]) 
                this.shuttles[shuttleInfoObj.shuttle_id] = new Shuttle(shuttleInfoObj,this.map);
            this.logBounds(this.shuttles[shuttleInfoObj.shuttle_id].lat, this.shuttles[shuttleInfoObj.shuttle_id].long);
        }        
        ,
        setStop: function(stopInfoObj) {
            if (!this.stops[stopInfoObj.short_name]) 
                this.stops[stopInfoObj.short_name] = new Stop(stopInfoObj,this.map);
            this.logBounds(this.stops[stopInfoObj.short_name].lat, this.stops[stopInfoObj.short_name].long);
        }        
        ,
          positionShuttle: function(id,lat,long) {
              if (!this.shuttles[id]) 
                  this.shuttles[id] = new Shuttle(id,this.map);
              this.shuttles[id].position(lat,long);
        }
        ,
        randomizeShuttles: function() {
            for (shuttleId in this.shuttles) {
                randLoc = this.routes[0][Math.floor(Math.random()*this.routes[0].length)];
                this.shuttles[shuttleId].position(randLoc.lat(), randLoc.lng());
            }
            window.setTimeout('shuttleMap.randomizeShuttles()',1000);
        }
        ,
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
            if (false) {
                var data = eval('[{"shuttle_id":"6","heading":"180","latitude":"42.72795","longitude":"-73.67632","speed":"0","cardinal_point":"South","update_time":"2011-04-27 16:16:37","route_id":"1","name":"Bus 97"},{"shuttle_id":"2","heading":"0","latitude":"42.72807","longitude":"-73.67607","speed":"11","cardinal_point":"North","update_time":"2011-04-27 16:20:47","route_id":"1","name":"Bus 85"},{"shuttle_id":"3","heading":"280","latitude":"42.72867","longitude":"-73.67627","speed":"0","cardinal_point":"West","update_time":"2011-04-27 19:20:32","route_id":"1","name":"Bus 95"},{"shuttle_id":"5","heading":"190","latitude":"42.72791","longitude":"-73.67617","speed":"0","cardinal_point":"South","update_time":"2011-04-27 20:57:27","route_id":"1","name":"Bus 91"},{"shuttle_id":"4","heading":"130","latitude":"42.72889","longitude":"-73.67632","speed":"9","cardinal_point":"South-East","update_time":"2011-04-27 22:59:32","route_id":"1","name":"Bus 93"},{"shuttle_id":"8","heading":"280","latitude":"42.73161","longitude":"-73.67004","speed":"16","cardinal_point":"West","update_time":"2011-04-27 23:10:01","route_id":"2","name":"Bus 92"},{"shuttle_id":"1","heading":"200","latitude":"42.72873","longitude":"-73.67612","speed":"0","cardinal_point":"South","update_time":"2011-04-27 23:14:26","route_id":"1","name":"Bus 94"}]');
            
              $.each(data, function(shuttleIdx,shuttleInfoObj) {
                      // setup new shuttles and set positions on all shuttles on map 
                    shuttleMap.setShuttle(shuttleInfoObj);
                    // shutdownShuttle any shuttle markers that are not in database 
                      shuttleMap.shutdownExpiredShuttles();
              });
            } else {
                $.getJSON('http://shuttles.abstractedsheep.com/dev/data_service.php?action=get_shuttle_positions', function(data) {
                      $.each(data, function(shuttleIdx,shuttleInfoObj) {
                      // setup new shuttles and set positions on all shuttles on map 
                            shuttleMap.setShuttle(shuttleInfoObj);
                            // shutdownShuttle any shuttle markers that are not in database 
                          shuttleMap.shutdownExpiredShuttles();

                      });
                });
            
            }
            //$('#eta').page("destroy").page(); 

            window.setTimeout('shuttleMap.refreshShuttles()',5000);
        }
    }
    var shuttleMap;
    function initialize() {
        shuttleMap = new ShuttleMap;
        //shuttleMap.randomizeShuttles();
        shuttleMap.refreshShuttles();
        shuttleMap.resizeMapToMarkers();
    }
    function mysqlTimeStampToDate(timestamp) {
        //function parses mysql datetime string and returns javascript Date object
        //input has to be in this format: 2007-06-05 15:26:02
        var regex=/^([0-9]{2,4})-([0-1][0-9])-([0-3][0-9]) (?:([0-2][0-9]):([0-5][0-9]):([0-5][0-9]))?$/;
        var parts=timestamp.replace(regex,"$1 $2 $3 $4 $5 $6").split(' ');
        return new Date(parts[0],parts[1]-1,parts[2],parts[3],parts[4],parts[5]);
    }

</script>

<style>
.page-map, .ui-content, #map-canvas { width: 100%; height: 100%; padding: 0; }
</style>

<div data-role="page" id="eta" class="page-map">
    <div  data-role="header" class="ui-bar" > 
        <h1>RPI Shuttle Tracking</h1> 
        <a href="#" data-role="button" data-icon="refresh" data-iconpos="notext" id="refresh">Refresh</a> 
            
    </div>
    <div data-role="content" id="etalist">
        <div id="map_canvas" style="height: 100%;"></div>
    </div>
</div>

</body>
</html>