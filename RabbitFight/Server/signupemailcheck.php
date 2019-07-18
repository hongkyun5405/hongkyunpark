<?php
error_reporting(E_ALL);
ini_set('display_errors',1);   // 오류 찾기 코드
//db연결
require_once("./dbconfig.php");

//클라이언트로 부터 받아온 값
$email=isset($_POST['Semail']) ? $_POST['Semail'] : ''; //이메일 값

//서버에 저장된 email 값을 꺼내옴
$check="SELECT*FROM user WHERE user_email ='$email'";
$result=$mysqli->query($check);

if($result->num_rows==0){
  echo "이메일사용가능";
}else{
  echo "이메일사용불가능";
}
?>
