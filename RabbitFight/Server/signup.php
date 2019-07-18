<?php
error_reporting(E_ALL);
ini_set('display_errors',1);   // 오류 찾기 코드
//db연결
require_once("./dbconfig.php");

//안드로이드 코드의 postparameters 변수에 적어준 이름을 가지고 값을 전달 받음
$email=isset($_POST['Semail']) ? $_POST['Semail'] : '';   //전달된 값이 없으면 변수에 ''을 저장
$nickname=isset($_POST['Snickname']) ? $_POST['Snickname'] : '';
$password=isset($_POST['Spassword']) ? $_POST['Spassword'] : '';
$image=isset($_POST['Simage']) ? $_POST['Simage'] : '';

//DB에 들어가 추가 값
$update_date=date("Y-m-d H:i:s"); // update_date 컬럼에 저장하는 현재 날짜와시간 - 마지막 업데이트 날짜와시간
$file_path = "";
//프로필사진이 없으면 서버에 저장을 안함
if($image!=null){
  $file_path = "signupimage/$nickname.jpg";  //서버에 저장할 프로필사진 이미지를 유저 닉네임.jpg로 함
  file_put_contents($file_path,base64_decode($image)); // 서버에 저장
}

$SHA256password = base64_encode(hash('sha256',$password,true)); //저장된 비밀번호를 암호화한 형식으로 변경한 값


  //sql 문을 실행 하여 데이터를 db 서버의 signup 테이블에 저장
  if ($email !="" and $nickname !="" and $password !=""){  //변수에 값이 있으면 저장
      $sql="insert into user(user_email,user_nickname,user_password,user_image,signup_date) values('$email','$nickname','$SHA256password','$file_path','$update_date')";
      $result=mysqli_query($mysqli,$sql);

      if($result){
        $rankingsql="insert into ranking(ranking_nickname,ranking_victory_count,ranking_defeat_count,ranking_adds) values('$nickname','0','0','0')";
        $result=mysqli_query($mysqli,$rankingsql);
        if($result){
            echo "회원가입성공";
        }else{
            echo "SQL문 처리중 에러 발생 : ";
            echo mysqli_error($mysqli);
        }
      }else{
         echo "SQL문 처리중 에러 발생 : ";
         echo mysqli_error($mysqli);
      }

  } else {
      echo "데이터를 입력하세요 ";
  }
?>
