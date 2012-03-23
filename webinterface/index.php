<?php
//setcookie("favs", "union:1;union:2;blitman:1");

//RouteCoordDistances::loadDistanceTable();
//RouteCoordDistances::calcDistances();

//var_dump($etas);
include("header.php");

?>
<body>
<noscript><b>At this time, Javascript is required to use the Mobile Shuttle Tracker. Stay tuned for an HTML version.</b></noscript>
<script type="text/javascript">
    $(document).ready(function() {
         
        loadETAPage();         
        
        function loadETAPage() {
            jQuery.ajax({
                url:'loadETA.php',
                dataType: "json", /* can put parameter list here like :  action=abc&id=123   etc*/
                success:function(obj){
                    if(obj){
                        if(obj.error){
                            alert(obj.error);
                        } else {
                            // Change to a stop button
                            $('#favorite')
                                .html(obj.fav).page();
                            $('#west')
                                .html(obj.west).page();
                            $('#east')
                                .html(obj.east).page();
                            jQuery('#eta').page("destroy").page();   
                        }
                    }
                }

            }); 
        }

        $("#refresh").click(function(){
            loadETAPage();
        });
    }); 
</script>

<script type="text/javascript">
//alert(cookiejar.get('fav-div'));
</script>

<div data-role="page" id="eta">
    <div data-role="header" class="ui-bar" data-inline="true"> 
        <h1>RPI Shuttles</h1>
        <a href="#" data-role="button" data-icon="refresh" data-iconpos="notext" id="refresh">Refresh</a> 
    </div>
    <div data-role="navbar">
        <ul>                                                                                                                 
            <li><a href="index.php" rel=external class="ui-btn-active">ETAs</a></li>
            <li><a href="map.php" rel=external>Map</a></li>
        </ul>
    </div>
    <div data-role="content" id="etalist">
        <div data-role="collapsible-set">
            <div id="favorite">
            </div>
            <div id="west">
            </div>
            <div id="east">
            </div>
        </div>
    </div>
    <? 
    //include("footer.php"); 
    ?>
</div>
<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>
</body>
</html>