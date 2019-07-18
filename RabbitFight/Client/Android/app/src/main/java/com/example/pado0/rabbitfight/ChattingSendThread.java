package com.example.pado0.rabbitfight;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ChattingSendThread extends Thread {

    private Socket socket; //서버와 연결할 소켓
    JSONObject sendmsg; //서버로 전송할 메시지
    PrintWriter PW;

    private UserinformationSigleton userinfo = UserinformationSigleton.getInstance(); //유저 정보 싱글톤

    public ChattingSendThread(Socket socket,JSONObject message){
        this.socket = socket;
        this.sendmsg =message;
    }

    public void run(){
        if(socket !=null) {
            try {
                //채팅서버로 메시지를 보내기 위한 스트림 생성
                PW = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

                if (sendmsg != null) {  //메시지가 null이아니면
                    ChattingSendThread.JsonParser JP = new ChattingSendThread.JsonParser();

                    JSONObject sendchat = (JSONObject) sendmsg.get("sendchat");  // 보내는 메시지 전체 JSON
                    Object chatinfo = sendchat.get("chatinfo"); //보내는 메시지 전체에서 채팅 종류를 꺼냄냄


                    if (chatinfo.toString().equals("channelchange")) {
                        JP.clientmsg.put("channel", new Integer(userinfo.getUserchannel())); //JSON 형태의 보낼 메시지
                        JP.clientmsg.put("channelchange", sendchat.get("message").toString()); //JSON 형태의 보낼 메시지
                        JP.clientmsg.put("message", "채널변경"); //JSON 형태의 보낼 메시지
                        JP.clientmsg.put("usernickname", userinfo.getUsernickname());
                        JP.clientmsg.put("useremail", userinfo.getUseremail());
                    } else if (chatinfo.toString().equals("chatting")) {
                        JP.clientmsg.put("channel", new Integer(userinfo.getUserchannel())); //JSON 형태의 보낼 메시지
                        JP.clientmsg.put("message", sendchat.get("message").toString()); //JSON 형태의 보낼 메시지
                        JP.clientmsg.put("usernickname", userinfo.getUsernickname());
                        JP.clientmsg.put("useremail", userinfo.getUseremail());
                    } else if (chatinfo.toString().equals("heartbeat")) {
                        JP.clientmsg.put("channel", new Integer(userinfo.getUserchannel())); //JSON 형태의 보낼 메시지
                        JP.clientmsg.put("heartbeat", "client"); //JSON 형태의 보낼 메시지
                        JP.clientmsg.put("message", sendchat.get("message").toString()); //JSON 형태의 보낼 메시지
                        JP.clientmsg.put("usernickname", userinfo.getUsernickname());
                        JP.clientmsg.put("useremail", userinfo.getUseremail());
                    } else {
                        JP.clientmsg.put("channel", new Integer(userinfo.getUserchannel())); //JSON 형태의 보낼 메시지
                        JP.clientmsg.put("system", "logout"); //JSON 형태의 보낼 메시지
                        JP.clientmsg.put("message", sendchat.get("message").toString()); //JSON 형태의 보낼 메시지
                        JP.clientmsg.put("usernickname", userinfo.getUsernickname());
                        JP.clientmsg.put("useremail", userinfo.getUseremail());
                    }

                    String strJson = JSONValue.toJSONString(JP.clientmsg.toString()); // JSON 형태를 String 형태로 변환
                    PW.println(strJson); //메시지 입력
                    PW.flush(); //메시지 전송
                } //메시지가 없으면 전송안함
            } catch (Exception e) {
                e.printStackTrace();
            }
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
