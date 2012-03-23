<?php
  /* DataService class - provides functions for getting data from the DB */
include("data_service_data.php");
class DataService
{
    function getNextEta($route_id='',$stop_id='')
    {
        return json_encode(DataServiceData::getNextEta($route_id,$stop_id));
    }
    static function getAllEta($route_id='', $shuttle_id='')
    {
        return json_encode(DataServiceData::getAllEta($route_id, $shuttle_id));
    }
    static function getShuttlePositions()
    {
        return json_encode(DataServiceData::getShuttlePositions());
    }                                                                                                       
    static function getAllExtraEta($route_id='', $shuttle_id='', $stop_id='')
    {
        return json_encode(DataServiceData::getAllExtraEta($route_id, $shuttle_id, $stop_id));
    }
    
    static function drawETAs($route,$stop='',$fav)
    {
        if (is_array($route)) {
            $etas = array();
            foreach($route as $r)
                $etas = array_merge($etas,DataServiceData::getNextEta($r, $stop));
        } else
            $etas = DataServiceData::getNextEta($route, $stop);
        /* display the ETA information */
        ob_start();
        if (is_array($route))
            $route_name = "Favorites";
        if ($route == "1")
            $route_name = "West";
        else if ($route == "2")  
            $route_name = "East";
        ?>
		
        <div id="<?= $fav ? "favorite" : strtolower($route_name) ?>"<?= $fav ? '' : ' data-collapsed="true"' ?> data-role="collapsible">
        <h3><? if ($fav) echo "Favorites"; else echo $route_name; ?></h3>
        <ul data-role="listview" data-theme="c" data-split-theme="c" <?=$fav?" data-split-icon=\"delete\"":" data-split-icon=\"star\""?>>
        <?
        if (is_array($etas) && count($etas)) {
            foreach ($etas as $eta) {
                $route = $eta['route']; /* reset it to handle favorites */
                ?> 
                <? if (!$fav || ($fav && isset($_COOKIE["__CJ_fav-".$route."-".$eta['stop_id']])) ) { ?>
                    <li>
                    <a href="details.php?stop=<?=$eta['stop_id']?>&route=<?=$route?>"><?=$eta['stop_name']?></a><span style="width: auto; font-size: 12pt;" class="ui-li-count">
                    <?
                    if ($eta['route'] == 1 && $fav == true) {
                        echo "West  "; 
                    }
                    else if ($fav == true) {
                        echo "East  "; 
                    }
                    $less = "";
                    if (($time = round(($eta['eta'] / 1000) / 60)) == 1)
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
                    </span>
                    <? if ($fav) { ?>
                        <a onclick="cookiejar.remove('fav-<?=$route?>-<?=$eta['stop_id']?>');$('#refresh').click();">remove this favorite</a>
                    <? } else { ?>
                        <a onclick="cookiejar.put('fav-<?=$route?>-<?=$eta['stop_id']?>',1);$('#refresh').click();">make this a favorite</a>
                    <? } ?>
                    </li>
                <? } ?>
                
                <?       
            }
        }
        else {
            ?><li>This shuttle data is too old to display.</li><?
        }
        ?></ul>
        </div>
        <? 
        
        $ret = ob_get_contents();
        ob_end_clean();
        return $ret;
        
        
    }
    
    
    static function weekdayETA($route,$stop='',$fav)
    {
        
    }
    
    static function drawExtraETA($route,$stop)
    {
         $etas = DataServiceData::getAllExtraEta($route,"",$stop);
         
         ?>
         <ul data-role="listview" data-theme="c">
            <li>
                <h3><?=$etas["name"]?></h3>
                <p><? if ($route == 1) echo "West Route"; else echo "East Route"; ?></p>
            </li>
             <?
             if (is_array($etas) && count($etas)) 
             {
                foreach ($etas["eta"] as $eta_id => $eta) 
                { 
                    ?>
                    <li><?=date("h:ia",time() + ($eta / 1000))?></li>
                    <?
                }
             }
             else
             {
                 echo "<li>No Extra ETAs available for this stop at this time.</li>";
             }  
             ?>
         </ul>
         
         <?
    } 
       
    
    static function displayETAs()
    { //$etas = DataServiceData::getNextEta();
        $etas["fav"] = dataService::drawETAs(array("1","2"),'',true);
        
        $etas["west"] = dataService::drawETAs("1",'',false);

        $etas["east"] = dataService::drawETAs("2",'',false);

        return json_encode($etas);
    }
}
?>
