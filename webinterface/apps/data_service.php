<?php
  /* DataService class - provides functions for getting data from the DB */
include("data_service_data.php");
class DataService
{
    function getNextEta($route_id='')
    {
        return json_encode(DataServiceData::getNextEta($stop_id));
    }
    function getAllEta($route_id='', $shuttle_id='')
    {
        return json_encode(DataServiceData::getAllEta($route_id, $shuttle_id));
    }
    function getShuttlePositions()
    {
        return json_encode(DataServiceData::getShuttlePositions());
    }
    
    function drawETAs($route, $stop='')
    {
        $etas = DataServiceData::getNextEta($route, $stop);
        
        /* display the ETA information */
        if (is_array($etas) && count($etas)) {
            echo "<table class=\"ETAtable\">";
            echo "<tr>";
                echo "<th>Stop</th>";
                echo "<th class=\"favorite\"></th>";
                echo "<th>Route</th>";
                echo "<th>ETA</th>";
                //echo "<th>Shuttle Name</td>";
                echo "</tr>";
            foreach ($etas as $eta) {
                echo "<tr>";
                echo "<td>" . $eta[stop_name] . "</td>";
                echo "<td class=\"favorite\"><a href=\"#" . $eta[stop_id] . "\" id=\"" . $eta[stop_id] . "\" onclick=\"\$.cookie('fav', 'union');\">Fav</a></td>";
                echo "<td>" . $eta[route] . "</td>";
                echo "<td class=\"ETA\">" . date("h:ia",time() + ($eta[eta] / 1000)) . " (" . round((($eta[eta] / 1000) / 60)) . " minutes from now)</td>";
                //echo "<td>" . $eta[shuttle_name] . "</td>";
                echo "</tr>";       
            }                      
            echo "</table>";
            
        } else {
            echo "Sorry, there is no shuttle ETA information at this time.";
        }
        
    }
    
    function displayETAs()
    { //$etas = DataServiceData::getNextEta();
?>
        <h1>Track Shuttles @ RPI</h1>
        <h3><a href="#" id="allButton" onclick="$('#west').slideToggle(); $('#east').slideToggle();">Show/Hide All</a></h3>
        
        <h3><a href="#" onclick="$('#favorite').slideToggle();">Favorite Stops</a></h3>
        <div id="favorite">   
        <?
        dataService::drawETAs("1","union");
        
        ?>
        </div>
        
        

        <h3 class="west"><a href="#" id="westButton" onclick="$('#west').slideToggle();">West Route</a></h3>
        <div id="west">   
        <?
        dataService::drawETAs("1");
        ?>
        </div>
        <h3 class="east"><a href="#" id="eastButton" onclick="$('#east').slideToggle();">East Route</a></h3>
        <div id="east">
        <?
        dataService::drawETAs("2");
        ?>
        </div>
        <?
    }
    
}
   
