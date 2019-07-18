<?php
error_reporting(E_ALL);
ini_set('display_errors',1);   // 오류 찾기 코드

//db연결
require_once("./dbconfig.php");

$useremail = isset($_POST['user']) ? $_POST['user'] : ''; //user 아이디
$gameresult = isset($_POST['gameresult']) ? $_POST['gameresult'] : '';// 승패데이터


$check="SELECT*FROM user WHERE user_email ='$useremail'";
$result=$mysqli->query($check);

// 결과값으로 받아온 이메일 값으로 닉네임값을 꺼냄 
if($result->num_rows==1){
  $row=$result->fetch_array(MYSQLI_ASSOC);
  $nickname = $row['user_nickname'];

 if($gameresult == "승리")
{
  $sql="UPDATE ranking set ranking_victory_count=ranking_victory_count+1 WHERE ranking_nickname='$nickname'";
  $result = mysqli_query($mysqli, $sql);

  $check="SELECT*FROM ranking WHERE ranking_nickname='$nickname'";
  $result=$mysqli->query($check);
  $row=$result->fetch_array(MYSQLI_ASSOC);

  //echo "승리";

  if($row['ranking_victory_count']>0)
  {
    $shiftresult=($row['ranking_victory_count']/($row['ranking_victory_count']+$row['ranking_defeat_count'])) * 100;
    $shiftresult100 = round($shiftresult,1);
    $sql3="UPDATE ranking set ranking_adds=$shiftresult100 WHERE ranking_nickname='$nickname'";
    $result = mysqli_query($mysqli, $sql3);
  }
}
else
{
  $sql2="UPDATE ranking set ranking_defeat_count=ranking_defeat_count+1 WHERE ranking_nickname='$nickname'";
  $result = mysqli_query($mysqli, $sql2);

  $check="SELECT*FROM ranking WHERE ranking_nickname='$nickname'";
  $result=$mysqli->query($check);
  $row=$result->fetch_array(MYSQLI_ASSOC);

  if($row['ranking_victory_count']>0)
  {
    $shiftresult=($row['ranking_victory_count']/($row['ranking_victory_count']+$row['ranking_defeat_count'])) * 100;
    $shiftresult100 = round($shiftresult,1);
    $sql4="UPDATE ranking set ranking_adds=$shiftresult100 WHERE ranking_nickname='$nickname'";

    $result = mysqli_query($mysqli, $sql4);
  }
}


}else{
  echo "이메일 없음";
}
?>
