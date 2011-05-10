<?php
  $pic = $_REQUEST['pic']
?>
<div id="info">What happened?</div>
<div id="main">
    <textarea class="storyInput"></textarea>
    <br>
    <input type="button" value="It is true" id="storySubmit">
    <br>
    <img src="images/<?=$pic?>.png" class="watermark">
</div>