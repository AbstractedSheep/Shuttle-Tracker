<?
?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>

<link rel="shortcut icon" href="/favicon.ico" type="image/x-icon" /> 

<!--<link rel="stylesheet" type="text/css" href="css/my.css">
 <script src="js/jquery.min.js"></script>
<script type="text/javascript" src="js/jquery.cookie.js"></script>  -->

<link rel="stylesheet" href="http://code.jquery.com/mobile/1.0a4.1/jquery.mobile-1.0a4.1.min.css" />
<script src="http://code.jquery.com/jquery-1.5.2.min.js"></script>
<script type="text/javascript">

//var refreshId;
//var checkInactivity;


//function launchWindow(id)
//{
//     
        //Get the screen height and width
//        var maskHeight = $(document).height();
//        var maskWidth = $(window).width();
//     
        //Set height and width to mask to fill up the whole screen
//        $('#mask').css({'width':maskWidth,'height':maskHeight});
//        $('#dialog').css({'display':'block'}); 
//         
        //transition effect    
//        $('#mask').fadeIn(1000);   
//        $('#mask').fadeTo("slow",0.8); 
//     
        //Get the window height and width
//        var winH = $(window).height();
//        var winW = $(window).width();
//               
        //Set the popup window to center
//        $(id).css('top',  winH/2-$(id).height()/2);
//        $(id).css('left', winW/2-$(id).width()/2);
//     
        //transition effect
//        $(id).fadeIn(2000);
//     
//     
    //if close button is clicked
//    $('.window .close').click(function (e) {
        //Cancel the link behavior
//        e.preventDefault();
//        refreshId = setInterval(function() {
//        $("#output").load("loadETA.php"); }, 5000);
//        $('#mask, .window').fadeOut("slow");
//        checkInactivity = setTimeout(function() {
//        clearInterval(refreshId);
//        launchWindow("#dialog");
//      
//      }, 300000);
//    });    
//     
    //if mask is clicked
//    $('#mask').click(function () {

//        refreshId = setInterval(function() {
//        $("#output").load("loadETA.php"); }, 5000);
//        $('#mask, .window').fadeOut("slow");
//        checkInactivity = setTimeout(function() {
//        clearInterval(refreshId);
//        launchWindow("#dialog");
//      
//      }, 300000);
//    });
//}
 $(document).ready(function() {
     
          
    
  //functions for auto-refreshing every 5 seconds and switching from map to ETA    
//     
//     $("#output").load("loadETA.php");
//     refreshId = setInterval(function() {
//      $("#output").load("loadETA.php"); }, 5000);
//      checkInactivity = setTimeout(function() {
//      clearInterval(refreshId);
//      launchWindow("#dialog");
//      
//      }, 300000);                
//     
//     $("#map").click(function(){
//        $.mobile.changePage("map.php", "slideup", false, false);
//     });
//        
//     $("#eta").click(function(){
//        $.mobile.changePage("loadETA.php", "slideup", false, false);
//        refreshId = setInterval(function() {
//      $("#output").load("loadETA.php"); }, 5000);
//     }); 

$("#refresh").click(function(){
    jQuery.ajax({
            url:'loadETA.php',
            data: "", /* can put parameter list here like :  action=abc&id=123   etc*/
            success:function(obj){
                if(obj){
                    if(obj.error){
                        alert(obj.error);
                    } else {
                        // Change to a stop button
                        $('#favorite')
                            .html(obj.favorite);
                        $('#west')
                            .html(obj.west);
                        $('#east')
                            .html(obj.east);
                    }
                }
            }

        });
});
      
   
});


</script>
<script src="http://code.jquery.com/mobile/1.0a4.1/jquery.mobile-1.0a4.1.min.js"></script>


</head>
<body>
