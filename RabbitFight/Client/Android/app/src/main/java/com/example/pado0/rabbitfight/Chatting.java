package com.example.pado0.rabbitfight;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.pado0.rabbitfight.MainActivity.mainchattingchannelchange;
import static com.example.pado0.rabbitfight.MainActivity.mainchattingreceivehandler;


public class Chatting extends Service {

    private final IBinder chattingBinder = new ChattingBinder(); //컴포넌트에 반환되는 IBinder

    ChattingSocketClient client; // 서버와 소켓연결하는 쓰레드
    ChattingReceiveThread receive; // 메시지를 수신하는 쓰레드
    ChattingSendThread send; // 메시지를 전송하는 쓰레드
    Thread chatting;

    String ip = "211.249.62.8"; //서버 ip
    Integer port = 8080; //서버 port
    Socket socket; //서버와 연결할 소켓

    Handler chattingchannelchange = mainchattingchannelchange; //채팅 채널변경 핸들러
    Handler chattingreceivehandler = mainchattingreceivehandler; //채팅 리시브 핸들러

    public static final String RECEIVE = "RECEIVE"; //리시브 핸들러 시작 flag
    public static final String SEND = "SEND";

    private UserinformationSigleton userinfo = UserinformationSigleton.getInstance(); //유저 정보 싱글톤
    private ChattingSingleton chattingSingleton= ChattingSingleton.getInstance(); // 채팅정보가져오는 싱글톤

    PrintWriter PW; //채팅 전송 스트림

    // 바인더로 연결을 하면 해당 채팅서비스를 반환함
     class ChattingBinder extends Binder {
        Chatting getService(){
            return Chatting.this;
        }
    }

    //다른 컴포넌트가 bindservice()를 호출해서 서비스와 연결을 시도하면 이 메소드가 호출됨, 이 메소드에서 IBinder를 반환
    //해서 서비스와 컴포넌트가 통신하는데 사용하는 인터페이스를 제공해야 한다. 만약 시작 타입의 서비스를 구현하면 null 반환
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("bind","OK");
        return chattingBinder;  //Messenger 객체에서 binder를 꺼내 전달한다.
    }

    //바인드를 끊는곳
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("unbind","OK");
        // All clients have unbound with unbindService()
        return super.onUnbind(intent);
    }

    //서비스가 처음으로 생성되면 호출됨, 서비스가 이미 실행중이면 호출되지 않음
    @Override
    public void onCreate(){
        chatting =  new Thread(new Runnable() {
          @Override
            public void run() {
                try{
                    if(socket==null){
                        socket=new Socket(ip,port);
                    }
                    receive = new ChattingReceiveThread(socket);
                    receive.start();
                    Log.d("serviceoncreate","zzz");
                    //서버에 메시지를 전달하기 위한 스트림 생성
                    PW = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

                    JsonParser JP = new JsonParser();

                    JP.clientmsg.put("channel",new Integer(userinfo.getUserchannel())); //JSON 형태의 보낼 메시지
                    JP.clientmsg.put("message",userinfo.getUsernickname()+":Ch.1 입장"); //JSON 형태의 보낼 메시지
                    JP.clientmsg.put("usernickname", userinfo.getUsernickname());
                    JP.clientmsg.put("useremail", userinfo.getUseremail());
                    String strJson = JSONValue.toJSONString(JP.clientmsg.toString()); // JSON 형태를 String 형태로 변환
                    PW.println(strJson); //메시지 입력
                    PW.flush(); //메시지 전송
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

        });
        chatting.start();
        super.onCreate();
    }

    //앱이 강제종료됬을 때 처리하는곳
    @Override
    public void onTaskRemoved(Intent rootIntent){
        //강제종료될때 처리해야할 코드적기
        try {
            socket.close();
            onDestroy(); //서비스 종료
            stopSelf();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("Task",":"+rootIntent);
    }

    //다른 컴포넌트가 startService()를 호출해서 서비스가 시작되면 이 메소드가 호출됨, 연결 타입에서는 필요없음
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    //    receive = new ChattingReceiveThread(socket);
    //    receive.start();
        return START_NOT_STICKY;
    }

    //채팅 메시지 보내기
    public void ChattingSend(final JSONObject sendmsg){
        send = new ChattingSendThread(socket,sendmsg);
        send.start();
    }

    //각액티비티의 채팅 채널 변경 핸들러 받는곳
    public void channelchange(Handler h){
        chattingchannelchange = h;
    }

    //각액티비티의 채팅 리시브 핸들러 받는곳
    public void chattingreceive(Handler h1){
        chattingreceivehandler = h1;
    }

    // json 파싱
    public class JsonParser {
        public JSONObject clientmsg;

        public JsonParser() {
            clientmsg = new JSONObject();
        }
    }

    // 접속 못할 경우 재시도를 위한 메소드
    private boolean reopen() throws IOException {
        boolean result = false;
        Log.d("socket","소켓 재연결 시작");
        socket.close(); //소켓 제거

        //2초에 한번씩 재연결중인 메시지 출력
        TimerTask adTast = new TimerTask() {
            public void run() {
                String msg = "재연결 중입니다...";
                Message chatreceive = chattingreceivehandler.obtainMessage();  //핸들러로 보낼 메시지
                chatreceive.obj = msg; //JSON 형식에서 원하는 키값을 꺼냄 키 - 채널 : channel, 메시지 : message
                //핸들러에게 메시지 전달(화면 변경 요청)
                chattingreceivehandler.sendMessage(chatreceive);
            }
        };
        Timer timer = new Timer();
        //  timer.schedule(adTast , 2000);  // 2초후 실행하고 종료
        timer.schedule(adTast, 0, 2000); // 0초후 첫실행, 2초마다 계속실행

        //채널값 ??로 변경
        String channelmsg = "??";
        Message chatchannelchangesend = chattingchannelchange.obtainMessage();  //핸들러로 보낼 메시지
        chatchannelchangesend.obj = channelmsg; //JSON 형식에서 원하는 키값을 꺼냄 키 - 채널 : channel, 메시지 : message
        //핸들러에게 메시지 전달(화면 변경 요청)
        chattingchannelchange.sendMessage(chatchannelchangesend);


        while(!result) { //socket.getInputStream().read()== -1
            try {
                socket = new Socket(ip, port);
                result = socket.isConnected();
            } catch (Exception e) {

            }
        }
        timer.cancel();

        //채널값 1로 변경
        String channelmsg2 = "1";
        Message chatchannelchangesend2 = chattingchannelchange.obtainMessage();  //핸들러로 보낼 메시지
        chatchannelchangesend2.obj = channelmsg2; //JSON 형식에서 원하는 키값을 꺼냄 키 - 채널 : channel, 메시지 : message
        //핸들러에게 메시지 전달(화면 변경 요청)
        chattingchannelchange.sendMessage(chatchannelchangesend2);

        receive.interrupt();
        receive = new ChattingReceiveThread(socket);
        receive.start();
        return true;
    }

    //메시지 수신하는 쓰레드
    public class ChattingReceiveThread extends Thread {
        Socket socket; //서버와 연결할 소켓
        BufferedReader input = null;
        public ChattingReceiveThread(Socket socket) { //소캣을 받음
            this.socket = socket;
        }

        public void run() {
            try {
                //채팅서버로부터 메시지를 받기 위한 스트림 생성
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(500);
                    String msg = input.readLine(); //서버에서 보낸 메시지를 읽어온다
                    if (msg != null) {
                        JSONObject json = (JSONObject) new JSONParser().parse(msg); // 서버에서 보낸 string 값을 JSON 형식으로 다시 파싱
                        if (json.get("channelchange") != null) {
                            Message chatchannel = chattingchannelchange.obtainMessage();  //핸들러로 보낼 메시지
                            chatchannel.obj = json.get("channelchange"); //JSON 형식에서 원하는 키값을 꺼냄 키 - 채널 : channel, 메시지 : message
                            //핸들러에게 메시지 전달(화면 변경 요청)
                            chattingchannelchange.sendMessage(chatchannel);
                        } else if(json.get("heartbeat") != null){
                            Log.d("pingpong","ping");
                            JsonParser JP = new JsonParser();
                            JSONObject JO = new JSONObject();
                            JP.clientmsg.put("sendchat",JO); //JSON 형태의 보낼 메시지
                            JO.put("chatinfo","heartbeat"); //JSON 형태의 보낼 메시지
                            JO.put("message","pong"); //JSON 형태의 보낼 메시지
                            ChattingSend(JP.clientmsg);
                        }else if(json.get("system") != null){
                            Log.d("systemlogout","aaa");
                            socket.close();
                            socket=null;
                            stopSelf();
                        }else{
                            Message chatreceive = chattingreceivehandler.obtainMessage();  //핸들러로 보낼 메시지
                            chatreceive.obj = json.get("message"); //JSON 형식에서 원하는 키값을 꺼냄 키 - 채널 : channel, 메시지 : message
                            //핸들러에게 메시지 전달(화면 변경 요청)
                            chattingreceivehandler.sendMessage(chatreceive);
                        }
                    }
                }
            }catch (IOException e) {
                try {
                    socket=null;
                    if(reopen());
                    Log.d("재접속","OK");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }catch (Exception e) {
                e.printStackTrace();

            }finally {
                Log.d("chattingreadthread","ok");
            }
        }
    }

    //시작타입 서비스라면 종료시 호출, 연결타입이라면 모든 바인딩이 끊기면 호출, 강제종료시는 호출X
    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d("service","destroy");
    }

}
