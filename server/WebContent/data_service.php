<?php

/*
TRACKER WEB DATA SERVICE
input: call using URL:  domain/data_service.php?action=get
    action - 'get': access shuttle tracking info for shuttle 'sn'
    sn - shuttle number
output: JSON formatted tracking information
*/

include("apps/data_service.php"); 
$data_service = new DataService();
$action = $_REQUEST['action'];
$shuttleNo = $_REQUEST['sn'];

switch ($action) {
case 'get' :
    echo json_encode($data_service->getData($shuttleNo));
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

