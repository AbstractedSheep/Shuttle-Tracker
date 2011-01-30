<!-- This is the the web interface to the shuttle ETA system. It displays the ETA in a formatted table.-->
 
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<meta http-equiv="Expires" content="Fri, Jan 01 1900 00:00:00 GMT">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Lang" content="en">
<meta name="author" content="">
<meta http-equiv="Reply-to" content="@.com">
<meta name="generator" content="PhpED 5.8">
<meta name="description" content="">
<meta name="keywords" content="">
<meta name="creation-date" content="01/01/2009">
<meta name="revisit-after" content="15 days">
<title>When is the shuttle getting here?</title>

<link rel="stylesheet" type="text/css" href="css\my.css">


</head>
<body>
    <h1>Track Shuttles @ RPI</h1>
    <br />   
    <p>Blitman: 1:31</p>
<table>
<?php
    $shuttleOutput = file("c:\\shuttleOutputData.txt");
    //var_dump($shuttleOutput);  

    // The following is sample data. It will read in the real ETA data when I get it via file or URL.
?>
    <tr>
        <th>Stop</th>
        <th>ETA</th>
    </tr>
    
    <tr>
    <td>Blitman</td>
    <td>1:30</td>
    </tr>
    
    <tr>
    <td>Union</td>
    <td>0:32</td>
    </tr>
    
    <tr>
    <td>Some stop</td>
    <td>3:51</td>
    </tr>
    
    <tr>
    <td>Other stop</td>
    <td>0:01</td>
    </tr>
    
</table>
<br />

</body>
</html>


