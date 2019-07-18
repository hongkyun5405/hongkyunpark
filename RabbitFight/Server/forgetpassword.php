<?php
error_reporting(E_ALL);
ini_set('display_errors',1);   // 오류 찾기 코드
//db연결
require_once("./dbconfig.php");


//안드로이드 코드의 postparameters 변수에 적어준 이름을 가지고 값을 전달 받음
$forgetemail=isset($_POST['Sforgetemail']) ? $_POST['Sforgetemail'] : '';   //전달된 값이 없으면 변수에 ''을 저장


//임시비밀번호 생성기
function random_char($length){
  $str = 'abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    $max = strlen($str) - 1;
    $chr = '';
    $len = abs($length);

    for($i=0; $i<$len; $i++){
        $chr .= $str[random_int(0,$max)];
    }
    return $chr;
}

$temporarypassword = random_char(10); //10자리의 임시비밀번호
$SHA256temporarypassword = hash("sha256",$temporarypassword); //임시 비밀번호 암호화 1차(안드로이드 SHA256과 같은 방식으로 암호화한것)
$user_newpassword = base64_encode(hash('sha256',$SHA256temporarypassword,true)); //새로 바뀐 비밀번호 암호화 2차완료

$update_date=date("Y-m-d H:i:s"); // update_date 컬럼에 저장하는 현재 날짜와시간 - 마지막 업데이트 날짜와시간

$check="SELECT*FROM user WHERE user_email = '$forgetemail'";  //클라이언트에서 입력한 이메일이 DB에 있다면
$result=$mysqli->query($check);

// 임시비밀번호 암호화한것과 update날짜를 같이 update함
if($result->num_rows==1){
  $update="UPDATE user set
  user_password = '$user_newpassword'
  , update_date = '$update_date'
  WHERE user_email = '$forgetemail'";
  $result=$mysqli->query($update);


    include_once('./mailer.lib.php');  //mail을 보내기위한 설정값

    // mailer("보내는 사람 이름", "받는 사람 메일주소", "보내는 사람 메일주소", "제목", "내용", "1");
    mailer("RabbitFight팀", $forgetemail, "pado0007@naver.com", "RabbitFight팀입니다.", "요청하신 임시비밀번호 : ".$temporarypassword." 를 보내드립니다. 이용해주셔서 감사합니다.", 1);

    echo "임시비밀번호전송완료";
}else{
    echo "존재하지않는이메일입니다";
}
?>
