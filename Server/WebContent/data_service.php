<?php

/*
TRACKER WEB DATA SERVICE
input: call using URL:  domain/data_service.php?action=action&sh=sh&st=st  
    action - get_next_eta || get_all_eta
    sh - shuttle ID 
    st - stop ID
output: JSON formatted tracking information

sh and st are optional and give you specific eta about that shuttle or stop
    action - get_next_eta || get_all_eta
    sh - shuttle ID 
    st - stop ID

examples:
abstractedsheep.com/~ashulgach/dataservice.php?action=get_next_eta      # gets the next eta for all stops

abstractedsheep.com/~ashulgach/dataservice.php?action=get_next_eta&st=1 # gets the next eta for stop id 1

abstractedsheep.com/~ashulgach/dataservice.php?action=get_all_eta       # gets all the etas for all shuttles and stops

*/
include("application.php");

include("apps/data_service.php"); 
$data_service = new DataService();
$action     = $_REQUEST['action'];
$shuttle_id = $_REQUEST['sh'];
$stop_id    = $_REQUEST['st'];

switch ($action) {
    case 'get_next_eta' :
        echo $data_service->getNextEta($stop_id);
        exit;
    case 'get_all_eta' :
        echo $data_service->getAllEta($stop_id, $shuttle_id);
        exit;
    case 'get_shuttle_positions' :
        echo $data_service->getShuttlePositions();
        exit;
    case 'get_eta_and_positions' :
        echo $data_service->getNextEta($stop_id) . $data_service->getShuttlePositions();
        exit; 
    default :
        echo "Command not supported.";
}
exit;


/*
the above is the URL interface. if you use this for a website on the same server, then you
could just do this in your program:

 $data_service = new DataService();
 $data = $data_service->getData($shuttleNo);
 
 then format $data array returned as you wish
*/

?>

