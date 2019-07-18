//gameplayserver
const dgram = require('dgram'); //udp 소켓 모듈
let server = dgram.createSocket('udp4'); //소켓 생성, udp4는 IPv4 udp6은 IPv6
const Userinfoclass = require('./User.js'); //User 정보를 가지고 있는 클래스
let Room = new Array(); //각각의 방 
let Roomlist = new Map(); //방 리스트 
var Userinfo; // 유저정보 클래스 
var Roomno ; // 방번호
var RemotePlayerIndex; //상대플레이어 Index 
var HitDamage; // 타격 대미지
//POST
var request = require('request');

//matchingserver
const net = require("net"); //tcp 소켓 모듈
const matchingserver = net.createServer(); //매칭서버
let matchinguserlist = new Map(); // 매칭유저리스트  - key: socket , value - email

server.bind(7070);

server.on('listening', () => {
    var addr = server.address();
    console.log('GamePlay listening event port : ' + addr.port);
});

/*
    //최초좌표값   - 0000
    //이동데이터   - 1000
    //모션데이터   - 2000
    //HP감소데이터 - 3000
      ㄴ3001 : ATK1, 3002 : ATK2, 3003 : ATK3
*/ 

//수신메시지
server.on("message", function (message,rinfo) {
    var msg = JSON.parse(message); //클라에서 받은 JSON 파싱

    //게임종료
    if(msg.System == "exit"){

        //게임결과값 전송
        if(Roomlist.get(msg.Room)[msg.PlayerIndex-1].hp > 0){
            //헤더 부분
            var headers = {
                'User-Agent': 'Super Agent/0.0.1',
                'Content-Type': 'application/x-www-form-urlencoded'
            }
            //요청 세부 내용
            var options = {
                url: 'http://211.249.62.8/ranking.php',
                method: 'POST',
                headers: headers,
                form: {'user':Roomlist.get(msg.Room)[msg.PlayerIndex-1].id,'gameresult':'승리'}
            }
            request(options, function(error, response, body){
                if(!error && response.statusCode == 200){
                    console.log(body);
                }
            })
        }else{
             //헤더 부분
            var headers = {
                'User-Agent': 'Super Agent/0.0.1',
                'Content-Type': 'application/x-www-form-urlencoded'
            }
            //요청 세부 내용
            var options = {
                url: 'http://211.249.62.8/ranking.php',
                method: 'POST',
                headers: headers,
                form: {'user':Roomlist.get(msg.Room)[msg.PlayerIndex-1].id,'gameresult':'패배'}
            }
            request(options, function(error, response, body){
                if(!error && response.statusCode == 200){
                    console.log(body);
                }
            })
        }
        Roomlist.get(msg.Room)[msg.PlayerIndex-1] = null; //serinfo class 지우기
        if(Roomlist.get(msg.Room)[0] == null && Roomlist.get(msg.Room)[1] == null){  // 방지우기
            Roomlist.delete(msg.Room);
        }
    }else{
    //최초좌표값 요청 - 0000
    if(msg.Separator == 0000){
        var playerid = msg.ID;  //최초에 플레이어 아이디로 플레이어정보를 찾기위해 저장

        //방찾아서 기본 정보 전송하기
        if(Roomlist.get(msg.Room)[0].id == undefined){
            //1player 추가정보입력
            Roomlist.get(msg.Room)[0].port = rinfo.port;
            Roomlist.get(msg.Room)[0].address = rinfo.address;
            Roomlist.get(msg.Room)[0].id = playerid;



            //최초실행시에만 1Player에 대해서 구분하기위한 구분자
            var MMT = true;

            //클라로 자신의 정보를 전송함
            var jsondata = new Object();
            jsondata.PlayerIndex = 1; 
            jsondata.Room = Roomlist.get(msg.Room)[0].roomno;
            jsondata.Separator = 0000;
            jsondata.ID = Roomlist.get(msg.Room)[0].id;
            jsondata.System = "wait";  
            message = JSON.stringify(jsondata);

            Roomno = Roomlist.get(msg.Room)[0].roomno;

        }else {
            //2player 추가정보입력
            Roomlist.get(msg.Room)[1].port = rinfo.port;
            Roomlist.get(msg.Room)[1].address = rinfo.address;
            Roomlist.get(msg.Room)[1].id = playerid;

            //클라로 자신의 정보를 전송함
            var jsondata = new Object();
            jsondata.PlayerIndex = 2;  
            jsondata.Room = Roomlist.get(msg.Room)[1].roomno;
            jsondata.Separator = 0000;
            jsondata.ID = Roomlist.get(msg.Room)[1].id;
            jsondata.System = "start";  
            message = JSON.stringify(jsondata);

            Roomno = Roomlist.get(msg.Room)[1].roomno;
        }
    }   
    //이동데이터 - 1000
    else if(msg.Separator == 1000){
        Roomno = msg.Room;

        Roomlist.get(Roomno)[msg.PlayerIndex-1].drag = msg.Drag;
        Roomlist.get(Roomno)[msg.PlayerIndex-1].positionX = msg.PositionX;
        Roomlist.get(Roomno)[msg.PlayerIndex-1].positionZ = msg.PositionZ;
        Roomlist.get(Roomno)[msg.PlayerIndex-1].rotationX = msg.RotationX;
        Roomlist.get(Roomno)[msg.PlayerIndex-1].rotationY = msg.RotationY;     

        //클라로 자신의 정보를 전송함
        var jsondata = new Object();
        jsondata.Separator = 1000;
        jsondata.ID = msg.ID;
        jsondata.PlayerIndex = msg.PlayerIndex;  
        jsondata.Room = msg.Room;
        jsondata.Drag = Roomlist.get(Roomno)[msg.PlayerIndex-1].drag;
        jsondata.PositionX = Roomlist.get(Roomno)[msg.PlayerIndex-1].positionX;
        jsondata.PositionZ = Roomlist.get(Roomno)[msg.PlayerIndex-1].positionZ;
        jsondata.RotationX = Roomlist.get(Roomno)[msg.PlayerIndex-1].rotationX;
        jsondata.RotationY = Roomlist.get(Roomno)[msg.PlayerIndex-1].rotationY;
        message = JSON.stringify(jsondata);

    }
    //HP감소데이터 - 3000
    else if(msg.Separator == 3000){
        Roomno = msg.Room;

        switch(msg.Action){  //공격데이터 구분 대미지 : 3001 - 0.04 , 3002 - 0.08 , 3003 - 0.15
            case 3001:
            HitDamage = 5;
            break;

            case 3002:
            HitDamage = 10;
            break;

            case 3003:
            HitDamage = 15;
            break;
        }
        
        if(msg.PlayerIndex==1){  //데미지 받을 User 구분
            
            RemotePlayerIndex = 2;

            Roomlist.get(Roomno)[RemotePlayerIndex-1].hp -=HitDamage; //HP감소시킴

            //클라로 자신의 정보를 전송함
            var jsondata = new Object();
            jsondata.Separator = 3000;
            jsondata.ID = msg.ID;
            jsondata.PlayerIndex = msg.PlayerIndex;  
            jsondata.Room = msg.Room;
            jsondata.Action = msg.Action;
            jsondata.FirstPlayerHP = Roomlist.get(Roomno)[msg.PlayerIndex-1].hp
            jsondata.SecondPlayerHP = Roomlist.get(Roomno)[RemotePlayerIndex-1].hp;
            message = JSON.stringify(jsondata);

        }else{

            RemotePlayerIndex = 1;

            Roomlist.get(Roomno)[RemotePlayerIndex-1].hp -=HitDamage; //HP감소시킴

            //클라로 자신의 정보를 전송함
            var jsondata = new Object();
            jsondata.Separator = 3000;
            jsondata.ID = msg.ID;
            jsondata.PlayerIndex = msg.PlayerIndex;  
            jsondata.Room = msg.Room;
            jsondata.Action = msg.Action;
            jsondata.SecondPlayerHP = Roomlist.get(Roomno)[msg.PlayerIndex-1].hp
            jsondata.FirstPlayerHP = Roomlist.get(Roomno)[RemotePlayerIndex-1].hp;
            message = JSON.stringify(jsondata);
        }
    }

        //게임시작 최초에만 1player를 구분
        if(MMT == true){
            server.send(message,0,message.length,Roomlist.get(Roomno)[0].port,Roomlist.get(Roomno)[0].address);
            console.log("server msg : " + message);
            MMT = false;
        }else{
            //데이터 전송
            for(var roomuser = 1; roomuser <= Roomlist.get(Roomno).length; roomuser++){      
                server.send(message,0,message.length,Roomlist.get(Roomno)[roomuser-1].port,Roomlist.get(Roomno)[roomuser-1].address);
                console.log("server msg : " + message);
            }
        }
    }
});

//error
server.on('error', (err) => {
  console.log('server error : '+err);
  server.close();
});
 




/*-----------------------------------------------------------------------MatchingServer--------------------------------------------------------------------------------------------*/





// TCP 서버 접속 대기
matchingserver.on('listening', function(){
    console.log('Matching listening event port : ' + "9090");
  });
  
  //TCP서버 접속 종료
  matchingserver.on('close', function(){
    console.log('matchingserver closed...');
  socket.destroy();
  
  })
  
  //TCP서버 접속 에러
  matchingserver.on('error', function(err){
    console.log('error'+err.massage);
  socket.destroy();
  })

  //연결되있는 소켓 수
  setInterval(function(){
    matchingserver.getConnections(function(error,count){
        console.log("matching wait user count - " + count);
      });
  }, 3000); 


  //매칭서버 접속 완료
  matchingserver.on('connection', function(socket){
    socket.setEncoding('utf8');

    //클라에서 FIN패킷을 받으면 실행
    socket.on('end', function(){
        console.log('Client socket close........');
        matchinguserlist.delete(socket);                  
        socket.destroy();
    });

    socket.on('data', function(data){
      var message = JSON.parse(data); //클라에서 받은 JSON 1차 파싱
      var message2 = JSON.parse(message); //클라에서 받은 JSON 2차 파싱 채널- message2.channel , 메시지 - message2.message , 유저닉네임 - message2.usernickname , 유저이메일 - message2.useremail
  
        console.log("client msg :"+data);

        if(message2.matchinginfo == "matching"){

            matchinguserlist.set(socket,message2.useremail); //matching wait userlist key-socket, value - email 

            if(matchinguserlist.size >= 2)
            {
                // 빈방을 찾아서 방에 매칭된 두명의 유저정보 넣기 
                for(var roomno = 0; roomno <=100; roomno++){

                    if(Roomlist.has(roomno) == false){ //빈방 찾기
                        Room[roomno] = new Array();  //실제방 생성

                        for(var matchingno=0; matchingno<=1; matchingno++){
                          Userinfo = new Userinfoclass();  //유저정보 클래스 생성
 
                          Userinfo.roomno = roomno; //방번호를 넣어줌 
                          Userinfo.playerIndex = matchingno+1; //playerIndex ,최초포지션 값

                          Room[roomno].push(Userinfo); //실제방에 userinfo를 넣어줌
                          Roomlist.set(roomno,Room[roomno]); //roomlist - key : 방번호 , value : 실제방(즉,유저 정보를 담을 수 있는 array)         
                       

                          //매칭 완료 메시지 보내기
                          var jsondata = new Object();
                          jsondata.matchinginfo = "matching";
                          jsondata.matchingroom = roomno;
                          Array.from(matchinguserlist.keys())[0].write(JSON.stringify(jsondata)+"\n");
                          console.log("server msg :"+Array.from(matchinguserlist.keys())[0].remoteAddress+"/"+JSON.stringify(jsondata));
                          matchinguserlist.delete(Array.from(matchinguserlist.keys())[0]);                  
                        }
                        break;
                    }
                }

            }
        }
    })

  })

  matchingserver.listen(9090);