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
    
    function drawETAs($route,$stop='',$fav)
    {
        $etas = DataServiceData::getNextEta($route, $stop);
        /* display the ETA information */
        ?><ul data-role="listview" data-theme="g"> <?
        if (is_array($etas) && count($etas)) {
            foreach ($etas as $eta) {
                ?> 
                <li><a href="details.php?<?=$eta[stop_name]?>"></a> <p class="ui-li-aside">
                <?
                if ($eta[route] == 1 && $fav == true) {
                    echo "West  "; 
                }
                else if ($fav == true) {
                    echo "East  "; 
                }
                echo date("h:ia",time() + ($eta[eta] / 1000));
                ?>
                </p></li>
                
                <?       
            }
        }
        else {
            ?><li>This shuttle data is too old to display.</li><?
        }
        ?></ul><?
    }     
    
    function displayETAs()
    { //$etas = DataServiceData::getNextEta();
    
?>
        
        <div id="favorite" data-role="collapsible">
			<h3>Favorites</h3>
        <?
        dataService::drawETAs("2","union",true);
        
        ?>
        </div>

        <div id="west" data-role="collapsible">
			<h3>West Route</h3>
        <?
        dataService::drawETAs("1",'',false);
        ?>
        </div>
		
        <div id="east" data-role="collapsible">
			<h3>East Route</h3>
        <?
        dataService::drawETAs("2",'',false);
        ?>
        </div>
<?
    }
}
?>