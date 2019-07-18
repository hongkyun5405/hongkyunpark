package com.example.pado0.rabbitfight;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ChattingSocketClient extends Thread {
    private Socket socket; //서버와 연결할 소켓
    boolean threadAlive;  //쓰레드의 동작 여부  앱이 꺼지면 쓰레드도 종료되게 함
    String ip;
    Integer port;
    OutputStream outputStream=null;
    OutputStreamWriter output=null;
    PrintWriter PW;
    //ChattingReceiveThread receive; // 메시지를 수신하는 쓰레드


    private UserinformationSigleton userinfo = UserinformationSigleton.getInstance(); //유저 정보 싱글톤

    public ChattingSocketClient(Socket socket){  //아이피 와 포트번호가 넘어옴
        threadAlive=true;
        this.socket=socket;
    }
    public void run(){
        try{
            //서버에 메시지를 전달하기 위한 스트림 생성
            PW = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

            ChattingSocketClient.JsonParser JP = new ChattingSocketClient.JsonParser();

            JP.clientmsg.put("channel",new Integer(userinfo.getUserchannel())); //JSON 형태의 보낼 메시지
            JP.clientmsg.put("message",userinfo.getUsernickname()+":Ch.1 입장"); //JSON 형태의 보낼 메시지

            String strJson = JSONValue.toJSONString(JP.clientmsg.toString()); // JSON 형태를 String 형태로 변환
            PW.println(strJson); //메시지 입력
            PW.flush(); //메시지 전송
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    // json 파싱
    public class JsonParser {
        public JSONObject clientmsg;

        public JsonParser() {
            clientmsg = new JSONObject();
        }
    }

}
