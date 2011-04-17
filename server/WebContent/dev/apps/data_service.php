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
    function getAllExtraEta($route_id='', $shuttle_id='', $stop_id='')
    {
        return json_encode(DataServiceData::getAllExtraEta($route_id, $shuttle_id, $stop_id));
    }
    
    function drawETAs($route,$stop='',$fav)
    {
        if (false)
        {
            return DataServiceData::getAllWeekendETA($route, "", $stop);
        }
        else
        {
            $etas = DataServiceData::getNextEta($route, $stop);
        }
        
        
        /* display the ETA information */
        ob_start();
        if ($route)
        {
            $route = "West";
        }
        else
        {
            $route = "East";
        }
        ?>
        <h3><? if ($fav) echo "Favorites"; else echo $route; ?></h3><ul data-role="listview" data-theme="c">
        <?
        if (is_array($etas) && count($etas)) {
            foreach ($etas as $eta) {
                ?> 
                <li><a href="details.php?stop=<?=$stop?>&route=<?=$route?>"><?=$eta[stop_name]?></a><span class="ui-li-aside">
                <?
                if ($eta[route] == 1 && $fav == true) {
                    echo "West  "; 
                }
                else if ($fav == true) {
                    echo "East  "; 
                }
                echo date("h:ia",time() + ($eta[eta] / 1000));
                ?>
                </span></li>
                
                <?       
            }
        }
        else {
            ?><li>This shuttle data is too old to display.</li><?
        }
        ?></ul><?
        
        $ret = ob_get_contents();
        ob_end_clean();
        return $ret;
        
        
    }
    
    
    function weekdayETA($route,$stop='',$fav)
    {
        
    }
    
    function drawExtraETA($route,$stop)
    {
         $etas = DataServiceData::getAllExtraEta($route,"",$stop);
         ?> <h3><?=$etas[0][stop_name]?></h3><p><? if ($route == 1) echo "West Route"; else echo "East Route"; ?></p><ul data-role="listview" data-theme="c"> <?
         if (is_array($etas) && count($etas)) {
            foreach ($etas as $eta) {
         ?>
            <li><?=$eta[eta]?></li>
         <?
            }
         }
         else
         {
             echo "<li>You broke it!</li>";
         }
         ?>
         </ul>
         <?
    } 
       
    
    function displayETAs()
    { //$etas = DataServiceData::getNextEta();
    
        $etas["fav"] = dataService::drawETAs("2","union",true);
        
        $etas["west"] = dataService::drawETAs("1",'',false);

        $etas["east"] = dataService::drawETAs("2",'',false);

        return json_encode($etas);
    }
}
?>