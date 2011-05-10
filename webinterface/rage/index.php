<?php
   
?>
<html>
<head>
<link rel="stylesheet" href="css/main.css" />
<script src="js/jquery.min.js"></script>
<!--<script src="js/main.js"></script>  -->
<script type="text/javascript">
$(document).ready(function() {  
    $('#info').fadeIn(1000);
    $('#main').fadeIn(2000);
    
    $('#main').delegate("a", "click", function() { 
        var contentPanelId = jQuery(this).attr("id");
        $('#info').fadeOut(1000);
        $('#main').fadeOut(2000, function() {
            $('#main').load("step2.php?pic="+contentPanelId+" #main");
            $('#info').load("step2.php?pic="+contentPanelId+" #info", function() {
                $('#main').fadeIn(2000);  $('#info').fadeIn(1000);  
            });
        });
    $('#storySubmit').click(function() {
        
     
    });
        
        
    });
});

</script>
   
</head>
<body>

<div class="info" id="info">
How was your game?
</div>
<div class="main" id="main">
<ul id="grid">
   <li><a href="#" id="yao"><img src="images/yao.png"></a></li> 
   <li><a href="#" id="better_than_expected"><img src="images/better_than_expected.png"></a></li> 
   <li><a href="#" id="raisin"><img src="images/raisin.png"></a></li> 
   <li><a href="#" id="pfft"><img src="images/pfft.png"></a></li> 
   <li><a href="#" id="f24u23"><img src="images/f24u23.png"></a></li> 
   <li><a href="#" id="megusta"><img src="images/megusta.png"></a></li> 
   <li><a href="#" id="troll"><img src="images/troll.png"></a></li> 
   <li><a href="#" id="fyea"><img src="images/fyea.png"></a></li> 
   <li><a href="#" id="okay"><img src="images/okay.png"></a></li>  

</ul>
<p>© Copyright 2011 | honsocrazy.com | All Rights Reserved</p>
</div>


</body>

</html>