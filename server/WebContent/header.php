<?php
?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>

<link rel="shortcut icon" href="/favicon.ico" type="image/x-icon" /> 
<meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;"/>

<title><?= $title?></title>

<link rel="stylesheet" type="text/css" href="css/my.css">
<script src="js/jquery.min.js"></script>
<script type="text/javascript" src="jquery.cookie.js"></script>
<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false&amp;key=ABQIAAAAOCTHylaOYOTc5g-_pCj8shSvS8Smej8DQ7j_pE7rX2KkSsk7qxQvVVhsMNeDyqo6NAc2ZwJet-r3CA">
</script>
<script type="text/javascript">


var refreshId;

 $(document).ready(function() {
     $("#mapdisplay").hide();
     $("#output").show();
     $("#output").load("loadETA.php");
     refreshId = setInterval(function() {
      $("#output").load('loadETA.php');
      }, 5000);
     
     $("#map").click(function(){
        //$("#mapdisplay2").load('map.html'); 
        //initialize();
        $("#mapdisplay").show(); 
        $("#output").hide(); 
        clearInterval(refreshId);
     });
        
     $("#eta").click(function(){
        $("#output").load("loadETA.php");
        $("#mapdisplay").hide();
        $("#output").show();
        refreshId = setInterval(function() {
      $("#output").load('loadETA.php') }, 5000);
     });
      
   
});
</script>

</head>
<body>
