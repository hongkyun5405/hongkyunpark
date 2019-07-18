package com.example.pado0.rabbitfight;

import android.os.Handler;
import android.os.Message;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class MatchingReceiveThread extends Thread{
    private Socket socket;
    BufferedReader input = null;

    MatchingReceiveThread receive; // 메시지를 수신하는 쓰레드


    Handler matchinghandler = MainActivity.matchinghandler; //채팅 채널변경 핸들러
    private UserinformationSigleton userinfo = UserinformationSigleton.getInstance(); //유저 정보 싱글톤

    public MatchingReceiveThread(Socket socket){  //아이피 와 포트번호가 넘어옴
        this.socket=socket;
    }

        public void run() {
            try {
                //채팅서버로부터 메시지를 받기 위한 스트림 생성
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                while (true) {
                    Thread.sleep(500);
                    String msg = input.readLine(); //서버에서 보낸 메시지를 읽어온다
                    if (msg != null) {
                        JSONObject json = (JSONObject) new JSONParser().parse(msg); // 서버에서 보낸 string 값을 JSON 형식으로 다시 파싱
                        if (json.get("matchinginfo") != null) {

                            Message matchingreceive = matchinghandler.obtainMessage();  //핸들러로 보낼 메시지
                            matchingreceive.obj = "matchingstart"; //JSON 형식에서 원하는 키값을 꺼냄 키 - 채널 : channel, 메시지 : message
                            //핸들러에게 메시지 전달(화면 변경 요청)
                            matchinghandler.sendMessage(matchingreceive);
                            socket.close();
                        }
                    }
                }
            }catch (IOException e) {
                input = null;
                socket = null;
                e.printStackTrace();
            }catch (Exception e) {
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
