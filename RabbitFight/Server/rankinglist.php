<?php
error_reporting(E_ALL);
ini_set('display_errors',1);   // 오류 찾기 코드

//db 연동
$mysqli=mysqli_connect("211.249.62.8","hong","zldeja521","hongdb");
if (!$mysqli)
{
   echo "MySQL 접속 에러 : ";
   echo mysqli_connect_error();
   exit();
}
mysqli_set_charset($mysqli,"UTF8");

$check="SELECT
       CASE
       WHEN @prev_value = ranking_adds THEN @vRank
       WHEN @prev_value := ranking_adds THEN @vRank := @vRank + 1
       ELSE @vRank := @vRank + 1
       END AS rank,ranking_nickname,ranking_victory_count,ranking_defeat_count,
       ranking_adds
FROM   ranking AS p, (SELECT @vRank := 0, @prev_value := NULL) AS r
ORDER BY ranking_adds DESC";

//$check="SELECT*FROM gameresult ORDER BY ranking_adds DESC" ;

$result=mysqli_query($mysqli,$check);
$data =array();

if($result){

while($row=mysqli_fetch_array($result)){
  array_push($data,
  array('ranking_rank'=>$row[0],
        'ranking_nickname'=>$row[1],
        'ranking_victory_count'=>$row[2],
        'ranking_defeat_count'=>$row[3],
        'ranking_adds'=>$row[4]
      ));
    }

//  header('Content-Type: application/json; charset=utf8');
  $json = json_encode(array("ranking"=>$data),JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
  echo $json;
  //$arr =array('roomname'=>$row['roomname'],'peoplecount'=>$row['peoplecount']);
  //echo json_encode($arr,JSON_UNESCAPED_UNICODE); //JSON_PRETTY_PRINT
}else{
  echo "SQL문 처리중 에러 발생";
}

?>