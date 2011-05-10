<?php
  /* DataService class - provides functions for getting data from the DB */
include("data_service_data.php");
class DataService
{
    function getNextEta($route_id='',$stop_id='')
    {
        return json_encode(DataServiceData::getNextEta($route_id,$stop_id));
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
        $etas = DataServiceData::getNextEta($route, $stop);
        
        print_r($etas);
        /* display the ETA information */
        ob_start();
        if ($route == "1")
        {
            $route_name = "West";
        }
        else if ($route == "2")  
        {
            $route_name = "East";
        }
        if (!$fav){
        ?>
        
            <div id="<?=strtolower($route_name);?>>" data-role="collapsible">
            <h3><?=$route_name; ?></h3><ul data-role="listview" data-theme="c">
        <?
        } 
        if (is_array($etas) && count($etas)) {
            foreach ($etas as $eta) {
                ?> 
                <li><a href="details.php?stop=<?=$eta[stop_id]?>&route=<?=$route?>"><?=$eta[stop_name]?></a><span style="width: auto; font-size: 12pt;" class="ui-li-count">
                <?
                if ($eta[route] == 1 && $fav == true) {
                    echo "West  "; 
                }
                else if ($fav == true) {
                    echo "East  "; 
                }
                $less = "";
                if (($time = round(($eta[eta] / 1000) / 60)) == 1)
                    $min = "minute";
                else if ($time < 1) {
                    $less = "<";
                    $time = " 1";
                    $min = "minute";  
                }
                else
                    $min = "minutes";            
                echo $less . $time . " " . $min;
                ?>
                </span></li>
                
                <?       
            }
        }
        else {
            ?><li>This shuttle data is too old to display.</li><?
        }
        if (!$fav){
        ?></ul>
        </div>
        <?
        }
        
        $ret = ob_get_contents();
        ob_end_clean();
        return $ret;
        
        
    }
    
    
    function drawExtraETA($route,$stop)
    {
         $etas = DataServiceData::getAllExtraEta($route,"",$stop);
         
         ?> <ul data-role="listview" data-theme="c">
         <li><h3><?=$etas["name"]?></h3><p><? if ($route == 1) echo "West Route"; else echo "East Route"; ?></p></li>
         <?
         if (is_array($etas) && count($etas)) {
            foreach ($etas["eta"] as $eta_id => $eta) {
         ?>
            <li><?=date("h:ia",time() + ($eta / 1000))?></li>
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
    { 
        $etas["fav"] = "<div id=\"favorite\" data-role=\"collapsible\">
            <h3>Favorites</h3><ul data-role=\"listview\" data-theme=\"c\">";
            
        $favs = $_COOKIE["favs"];
        $favArray = explode(";",$favs);
        foreach ($favArray as $favStopRoutePair)
        {
            $separated = explode($favStopRoutePair);
            $favStop = $separated[0];
            $favRoute = $separated[1];
            if ($favRoute == 1) {
                $westStopArray[] = $favStop;
                print_r($westStopArray);
            }
            else {
                $eastStopArray[] = $favStop;
                print_r($eastStopArray);
            }
                
                    
        }

        $etas["fav"] .= dataService::drawETAs("1",$westStopArray,true);
        $etas["fav"] .= dataService::drawETAs("2",$eastStopArray,true);
        $etas["fav"] .= "</ul></div>";
        
        
        $etas["west"] = dataService::drawETAs("1",'',false);

        $etas["east"] = dataService::drawETAs("2",'',false);

        return json_encode($etas);
    }
}
?>