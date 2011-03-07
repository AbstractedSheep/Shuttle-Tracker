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
    
    function displayETAs()
    { //$etas = DataServiceData::getNextEta();
?>
        <h1>Track Shuttles @ RPI</h1>
        
        <h3><a href="#" onclick="toggle('east'); toggle('west');">Show/Hide All</a></h3>

        <?
        
        $etas = DataServiceData::getNextEta("1");
?>
        <h3><a href="#" onclick="toggle('west');">West Route</a></h3>
        <div id="west">   
        <?
        /* display the ETA information */
        if (is_array($etas) && count($etas)) {
            echo "<table>";
            echo "<tr>";
                echo "<th>Stop</td>";
                echo "<th>ETA</td>";
                echo "<th>Shuttle Name</td>";
                echo "</tr>";
            foreach ($etas as $eta) {
                echo "<tr>";
                echo "<td>" . $eta[stop_name] . "</td>";
                echo "<td>" . date("m/d/Y H:ia",time() + ($eta[eta] / 1000)) . "</td>";
                echo "<td>" . $eta[shuttle_name] . "</td>";
                echo "</tr>";       
            }                      
            echo "</table>";
            
        } else {
            echo "Sorry, there is no shuttle ETA information at this time.";
        }
        ?>
        </div>
        <?
        
        $etas = DataServiceData::getNextEta("2");
?>
        <h3><a href="#" onclick="toggle('east');">East Route</a></h3>
        <div id="east">
        <?
        /* display the ETA information */
        if (is_array($etas) && count($etas)) {
            echo "<table>";
            echo "<tr>";
                echo "<th>Stop</td>";
                echo "<th>ETA</td>";
                echo "<th>Shuttle Name</td>";
                echo "</tr>";
            foreach ($etas as $eta) {
                echo "<tr>";
                echo "<td>" . $eta[stop_name] . "</td>";
                echo "<td>" . date("m/d/Y H:ia",time() + ($eta[eta] / 1000)) . "</td>";
                echo "<td>" . $eta[shuttle_name] . "</td>";
                echo "</tr>";       
            }                      
            echo "</table>";
            
        } else {
            echo "Sorry, there is no shuttle ETA information at this time.";
        }
        ?>
        </div>
        <?
    }
}
   
