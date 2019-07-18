const net = require("net"); //tcp 소켓 모듈
const Redis = require('redis'); //[nosql] 채팅
let date = require('date-and-time'); //날짜와시간 가져오는 모듈
const port = 8080; //포트번호
const server = net.createServer(); //채팅서버 만들기
let roomuserlist = new Array(); //각 채널 유저리스트
let room = new Map(); // key - 채널번호 / value - 각채널의 유저리스트
let totaluserlist = new Map(); //전체유저의 소켓,현재채널 리스트
let userlist = new Array(); //전체유저 소켓 리스트
let userinfo = new Map(); //heartbeat 응답완료 list
const redis = Redis.createClient(6379,'127.0.0.1', null);

//server.maxConnections: 서버가 최대로 받아들일 수 있는 연결 수를 지정
//server.getConnections(): 현재 서버의 동시 연결 수를 알 수 있음
//socket.remoteAddress: 접속한 클라이언트의 원격 IP를 돌려줍니다.
//socket.write(): 소켓으로 데이터를 보내는데 만약 보낼 수 없을 때는 데이터를 큐에 넣고 추후에 다시 전송합니다.
//socket.bufferSize: 현재 버퍼의 크기를 알 수 있습니다. 버퍼에 존재하는 문자열은 실제 데이터를 보낼 때 인코딩되기 때문에
//                    socket.bufferSize가 알려주는 크기는 인코딩되기 전의 문자 크기입니다.


//redis 에러 출력
redis.on("error", function (err) {
    console.log("Error " + err);
});

//채널번호 별로 유저리스트를 만듬
for(var i = 1; i<=1000; i++){
  roomuserlist[i] = new Array();
  room.set(i,roomuserlist[i]);
}

// TCP 서버 접속 대기
server.on('listening', function(){
  console.log('listening......');
});

//TCP서버 접속 종료
server.on('close', function(){
  console.log('server closed...');
socket.destroy();

})

//TCP서버 접속 에러
server.on('error', function(err){
  console.log('error'+err.massage);
socket.destroy();
})

//연결되있는 소켓 수
setInterval(function(){
    server.getConnections(function(error,count){
        console.log("connection socket - " + count);
      });
  }, 3000);


//TCP서버 접속 완료
server.on('connection', function(socket){
  socket.setEncoding('utf8');
  totaluserlist.set(socket,1); //전체유저리스트에 추가
  roomuserlist[1].push(socket)//접속한 사용자를 기본1번채널의 userlist에 추가
  userlist.push(socket);
  console.log('connection...');
  var jsondata = new Object();
  jsondata.heartbeat = "server";
  jsondata.message = "ping";
socket.write(JSON.stringify(jsondata)+"\n");
console.log(socket.remoteAddress+"[heartbeat]"+'server - ping');

    setInterval(function(){
    for(var user=0; user<userlist.length; user++) {
        if(userlist.length != 0){
            if(userinfo.delete(userlist[user]) == false){
            console.log('Client socket close..');
            roomuserlist[totaluserlist.get(userlist[user])].splice(roomuserlist[totaluserlist.get(userlist[user])].indexOf(userlist[user]), 1); //채널에서 해당유저 제거 
            totaluserlist.delete(userlist[user]); //전체유저리스트에서도 제거
            userlist[user].destroy();
            userlist.splice(user,1)
            console.log("remove  totaluserlist - "+ totaluserlist.size);
            console.log("remove  userinfo - "+ userinfo.size);
            }
        }
    }

    if(userlist.length != 0){
        for(var user=0; user<userlist.length; user++) {
            var jsondata = new Object();
            jsondata.heartbeat = "server";
            jsondata.message = "ping";
            userlist[user].write(JSON.stringify(jsondata)+"\n");
            console.log(userlist[user].remoteAddress+"[heartbeat]"+'server - ping');
        }
    }

    }, 5000);



  //클라에서 FIN패킷을 받으면 실행
  socket.on('end', function(){
    console.log('Client socket close........');
    roomuserlist[totaluserlist.get(socket)].splice(roomuserlist[totaluserlist.get(socket)].indexOf(socket), 1); //채널에서 해당유저 제거
    totaluserlist.delete(socket); //전체유저리스트에서도 제거
    userlist.splice(userlist.indexOf(socket),1) //유저 리스트에서 제거
    socket.destroy();
  });


  //데이터 수신
  socket.on('data', function(data){
    var message = JSON.parse(data); //클라에서 받은 JSON 1차 파싱
    var message2 = JSON.parse(message); //클라에서 받은 JSON 2차 파싱 채널- message2.channel , 메시지 - message2.message , 유저닉네임 - message2.usernickname , 유저이메일 - message2.useremail

    //현재 날짜 실시간 갱신
    now = new Date(); //현재날짜 가저오기위해서 객체생성
    today = date.format(now, 'YYYY-MM-DD HH:mm:ss'); //현재날짜와시간
    chattingdata = date.format(now, 'YYYY-MM-DD'); 

    //redis 로그데이터 => 날짜및시간/채널/이메일/닉네임/ip/msg
    let array = {
      date: today,
      channel: message2.channel,
      email: message2.useremail,
      nickname: message2.usernickname,
      ip: socket.remoteAddress,
      msg: message2.message};
    var chatting =JSON.stringify(array);

    //채널변경
    if(message2.channelchange != null){
      var changechannel = message2.channelchange;
      totaluserlist.delete(socket); //전체유저리스트 에서 채널 업데이트
      totaluserlist.set(socket,changechannel);//전체유저리스트 에서 채널 업데이트
      roomuserlist[message2.channel].splice(roomuserlist[message2.channel].indexOf(socket), 1); //기존의 방에서 유저 삭제
      roomuserlist[message2.channelchange].push(socket); //유저를 이동한 방에 유저리스트로 넣어줌
      var jsondata = new Object();
      jsondata.channelchange = changechannel;
      jsondata.message = "채널변경완료";
      socket.write(JSON.stringify(jsondata)+"\n");
      redis.lpush(chattingdata,chatting); //redis에 채팅 로그 저장

      
    //heartbeat read
    }else if(message2.heartbeat != null){
      console.log(socket.remoteAddress+"[heartbeat]"+'client - ' + message2.message);
      if(userinfo.has(socket) == false ){  //heartbeat 메시지를 수신하였을때 목록에 없으면 추가 있으면 추가안함 
         userinfo.set(socket,"heartbeat");  
      }

    //system msg
    }else if(message2.system != null){
        if(message2.message == "logout"){  //로그아웃인경우 유저 제거
            redis.lpush(chattingdata,chatting);//redis에 채팅 로그 저장
            roomuserlist[totaluserlist.get(socket)].splice(roomuserlist[totaluserlist.get(socket)].indexOf(socket), 1); //채널에서 해당유저 제거
            totaluserlist.delete(socket); //전체유저리스트에서도 제거
            userinfo.delete(socket);  //하트비트리스트에서 제거
            userlist.splice(userlist.indexOf(socket),1) //유저 리스트에서 제거
            socket.destroy(); //소켓 제거
        }
    
    }else{
      //모든유저에게 메시지를 전송
      for(var i=1; i<=roomuserlist[message2.channel].length; i++) {
          (room.get(message2.channel))[i-1].write(message+"\n"); //클라에서 readLine으로 읽어서 서버에서 개행문자를 넣어준다
           console.log((room.get(message2.channel))[i-1].remoteAddress+":"+data);
           redis.lpush(chattingdata,chatting); //redis에 채팅 로그 저장
      }
    }

})
})

// TCP서버 접속 대기 시작
server.listen(port);
console.log("start server!");