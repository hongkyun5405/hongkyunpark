<?php
error_reporting(E_ALL);
ini_set('display_errors',1);

$mysqli=mysqli_connect("211.249.62.8","hong","zldeja521","hongdb");
if (!$mysqli)
{
   echo "MySQL 접속 에러 : ";
   echo mysqli_connect_error();
   exit();
}
mysqli_set_charset($mysqli,"UTF8");
?>
