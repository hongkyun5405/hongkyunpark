<?php
error_reporting(E_ALL);
ini_set('display_errors',1);   // 오류 찾기 코드
//db연결
require_once("./dbconfig.php");

//안드로이드 코드의 postparameters 변수에 적어준 이름을 가지고 값을 전달 받음
$email=isset($_POST['Gemail']) ? $_POST['Gemail'] : '';   //전달된 값이 없으면 변수에 ''을 저장
$nickname=isset($_POST['Gnickname']) ? $_POST['Gnickname'] : '';   //전달된 값이 없으면 변수에 ''을 저장

//DB에 들어가 추가 값
$update_date=date("Y-m-d H:i:s"); // update_date 컬럼에 저장하는 현재 날짜와시간 - 마지막 업데이트 날짜와시간


$check="SELECT*FROM user WHERE user_email ='$email'";
$result=$mysqli->query($check);

  //sql 문을 실행 하여 데이터를 db 서버의 signup 테이블에 저장
  if ($result->num_rows==1){  //정보가있다면 그냥 통과
    echo "회원정보있음";
} else {
    $sql="insert into user(user_email,user_nickname,user_password,signup_date) values('$email','$nickname','GoogleLogin','$update_date')";
    $result=mysqli_query($mysqli,$sql);
    $rankingsql="insert into ranking(ranking_nickname,ranking_victory_count,ranking_defeat_count,ranking_adds) values('$nickname','0','0','0')";
    $result=mysqli_query($mysqli,$rankingsql);
    echo "회원정보저장";
}
?>