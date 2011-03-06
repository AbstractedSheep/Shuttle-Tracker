<?php
?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;"/>

<title><?= $title?></title>

<link rel="stylesheet" type="text/css" href="css/my.css">

<script language="JavaScript">
function toggle(id) {
    var state = document.getElementById(id).style.display;
        if (state == 'block') {
            document.getElementById(id).style.display = 'none';
        } else {
            document.getElementById(id).style.display = 'block';
        }
    }
</script>
</head>
<body>
