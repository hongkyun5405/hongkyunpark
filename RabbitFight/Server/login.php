<?php
error_reporting(E_ALL);
ini_set('display_errors',1);   // 오류 찾기 코드
//db연결
require_once("./dbconfig.php");

//안드로이드 코드의 postparameters 변수에 적어준 이름을 가지고 값을 전달 받음
$email=isset($_POST['Semail']) ? $_POST['Semail'] : '';   //전달된 값이 없으면 변수에 ''을 저장
$password=isset($_POST['Spassword']) ? $_POST['Spassword'] : '';


$check="SELECT*FROM user WHERE user_email ='$email'";
$result=$mysqli->query($check);

$SHA256password = base64_encode(hash('sha256',$password,true)); //저장된 비밀번호를 암호화한 형식으로 변경한 값

//페이지 넘어올때
if($result->num_rows==1){
  $row=$result->fetch_array(MYSQLI_ASSOC);
  if($row['user_password']==$SHA256password){

    if($result->num_rows==1){
      $arr =array('user_nickname'=>$row['user_nickname'],'user_image'=>$row['user_image'],'user_email'=>$row['user_email']);
        echo json_encode($arr,JSON_UNESCAPED_UNICODE);
    }else{
        echo "아이디나 비밀번호를 확인해주세요.";
    }

  }else{
    echo "아이디나 비밀번호를 확인해주세요.";
  }
}else{
  echo "아이디나 비밀번호를 확인해주세요.";
}
?>
