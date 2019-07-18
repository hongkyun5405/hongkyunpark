package com.example.pado0.rabbitfight;


import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.pado0.rabbitfight.databinding.ActivityMainBinding;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import static java.lang.Integer.parseInt;


public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mainBinding; //databinding

    Chatting chatting; //service

    boolean isService = false; //service 연결여부

    public static Handler mainchattingreceivehandler; //[채팅] 채팅 리시브 핸들러
    public static Handler mainchattingchannelchange; //[채팅] 채팅 채널 핸들러
    public static Handler matchinghandler; //[매칭] 핸들러
    private Intent Sintent;

    PrintWriter PW; //매칭메시지 전송 스트림

    final UserinformationSigleton userinfo = UserinformationSigleton.getInstance(); //유저 정보 싱글톤

    Socket matchingsocket;

    Thread matching;

    MatchingReceiveThread matchingreceive;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  // 상단 타이틀바 제거
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // 핸드폰 상단바 제거
        mainBinding = DataBindingUtil.setContentView(this,R.layout.activity_main); //databinding + include xml 연결
        mainBinding.setMain(this);

        Sintent = new Intent(MainActivity.this,Chatting.class); //서비스 시작 현재화면에서 -> 서비스
        startService(Sintent);
        bindService(Sintent,chattingconnect, Context.BIND_AUTO_CREATE);  //서비스와 연결 chattingconnect는 서비스와 연결 정의

        mainBinding.mainframe.mainframenickname.setText(userinfo.getUsernickname()); // 로그인정보에서 받아온 닉네임값


        //로그인 판별값에 따라 이미지 뷰에 다르게 적용
        if(userinfo.getUserloginvalue().equals("googlelogin")){
            Glide.with(this).load(userinfo.getUserimage()).into(mainBinding.mainframe.mainframeprofilepicture); //로그인정보에서 받아온 이미지 값 - 구글 로그인만 사용
        }else{
            //로그인정보에서 받아온 이미지 값 - 일반 로그인만 닉네임값으로 서버에서 검색해 이미지 값을 가져와 사용
            new DownloadImageTask((ImageView) mainBinding.mainframe.mainframeprofilepicture)
                    .execute("http://211.249.62.8/signupimage/"+userinfo.getUsernickname()+".jpg");
        }

        Glide.with(this).load(userinfo.getUserimage()).into(mainBinding.mainframe.mainframeprofilepicture); //로그인정보에서 받아온 이미지 값
        mainBinding.mainframe.mainframechatchanelbutton.setText("Ch."+userinfo.getUserchannel()); //자신의 채널값 가져오기


        // 채팅 channel 핸들러
         mainchattingchannelchange=new Handler(){
            @Override
            public void handleMessage(Message msg) {//여러군데에서 호출할때 구분
                mainBinding.mainframe.mainframechatchanelbutton.setText("Ch."+msg.obj.toString()); //입력받은값으로 채널 변경
                userinfo.setUserchannel(msg.obj.toString()); //현재채널 변경
                mainBinding.mainframe.mainframemSV.fullScroll(View.FOCUS_DOWN);  //최하단으로 포커스 자동 조절
                if(msg.obj.toString().equals("??")){}else{
                    mainBinding.mainframe.mainframetxtMessage.append("Ch."+msg.obj.toString()+" 채널입장!" + "\n");
                }
            }
        };

        // 채팅 recevied 핸들러
        mainchattingreceivehandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {//여러군데에서 호출할때 구분
                mainBinding.mainframe.mainframetxtMessage.append(msg.obj.toString() + "\n");
                mainBinding.mainframe.mainframemSV.fullScroll(View.FOCUS_DOWN);  //최하단으로 포커스 자동 조절
            }
        };


        //  매칭 recevied 핸들러
        matchinghandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {//여러군데에서 호출할때 구분
                Log.d("test","aaaa");
                    Intent intent = new Intent(MainActivity.this,UnityPlayerActivity.class);
                    startActivity(intent);
            }
        };

        //채팅버튼 눌렀을때
        mainBinding.mainframe.mainframechat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainBinding.mainframe.mainframechatcancel.setVisibility(View.VISIBLE); //채팅취소 버튼 보이게 하기
                mainBinding.mainframe.mainframechat.setVisibility(View.GONE); //채팅버튼 안보이게 하기
                mainBinding.mainframe.mainframechatview.setVisibility(View.VISIBLE); // 채팅 화면 보이게 하기
            }
        });

        //채팅취소버튼 눌렀을때
        mainBinding.mainframe.mainframechatcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainBinding.mainframe.mainframechatcancel.setVisibility(View.GONE); //채팅취소 버튼 보이게 하기
                mainBinding.mainframe.mainframechat.setVisibility(View.VISIBLE); //채팅버튼 안보이게 하기
                mainBinding.mainframe.mainframeeditMessage.setText(""); //채팅입력란초기화
                mainBinding.mainframe.mainframechannelchange.setText(""); //채널변경입력란초기화
                mainBinding.mainframe.mainframechatview.setVisibility(View.GONE); // 채팅 화면 안보이게 하기
                mainBinding.mainframe.mainframechannelchangelayout.setVisibility(View.GONE); // 채널변경 화면 안보이게 하기

            }
        });

        //채팅내용 입력할때 (안드로이드)키보드의 확인버튼을 클릭시 동작
        mainBinding.mainframe.mainframeeditMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){

                    JsonParser JP = new JsonParser();
                    JSONObject JO = new JSONObject();
                    JP.clientmsg.put("sendchat",JO); //JSON 형태의 보낼 메시지
                    JO.put("chatinfo","chatting"); //JSON 형태의 보낼 메시지
                    JO.put("message",userinfo.getUsernickname()+":"+mainBinding.mainframe.mainframeeditMessage.getText().toString()); //JSON 형태의 보낼 메시지
                    chatting.ChattingSend(JP.clientmsg); //service로 메시지 전달
                    mainBinding.mainframe.mainframeeditMessage.setText(""); //내용초기화
                    return false; //창닫기
                }
                return false;  //창닫기
            }
        });

        //채널버튼을 눌렀을때
        mainBinding.mainframe.mainframechatchanelbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainBinding.mainframe.mainframechannelchangelayout.setVisibility(View.VISIBLE); // 채널변경 화면 보이게 하기
            }
        });

        //채널 변경확인
        mainBinding.mainframe.mainframechannelchangebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{ //채널입력란에 아무것도입력하지 않고 확인버튼을 눌렀을때 발생할 예외처리

                    if(parseInt(mainBinding.mainframe.mainframechannelchange.getText().toString()) > 0 && parseInt(mainBinding.mainframe.mainframechannelchange.getText().toString()) < 1000){ //입력값이 1~999사이이면 채널이동
                        JsonParser JP = new JsonParser();
                        JSONObject JO = new JSONObject();
                        JP.clientmsg.put("sendchat",JO); //JSON 형태의 보낼 메시지
                        JO.put("chatinfo","channelchange"); //JSON 형태의 보낼 메시지
                        JO.put("message",Integer.parseInt(mainBinding.mainframe.mainframechannelchange.getText().toString())); //JSON 형태의 보낼 메시지

                        chatting.ChattingSend(JP.clientmsg); //service로 메시지 전달
                        mainBinding.mainframe.mainframechannelchangelayout.setVisibility(View.GONE); // 채널변경 화면 안보이게 하기
                        mainBinding.mainframe.mainframechannelchange.setText(""); //내용초기화
                    }else if(mainBinding.mainframe.mainframechannelchange.getText().toString().length() == 0 || mainBinding.mainframe.mainframechannelchange.getText().toString().equals("")){ //입력값이 없으면 메시지 출력
                        Toast.makeText(MainActivity.this, "1~999 사이의 숫자만 입력가능 합니다.", Toast.LENGTH_SHORT).show();
                        mainBinding.mainframe.mainframechannelchange.setText(""); //내용초기화
                        return;
                    }else{ //그외 처리
                        Toast.makeText(MainActivity.this, "1~999 사이의 숫자만 입력가능 합니다.", Toast.LENGTH_SHORT).show();
                        mainBinding.mainframe.mainframechannelchange.setText(""); //내용초기화
                        return;
                    }

                }catch (NumberFormatException e){
                    Toast.makeText(MainActivity.this, "이동할 채널번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        //채널 변경취소
        mainBinding.mainframe.mainframechannelchangecancelbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainBinding.mainframe.mainframechannelchange.setText(""); //내용초기화
                mainBinding.mainframe.mainframechannelchangelayout.setVisibility(View.GONE); // 채널변경 화면 안보이게 하기
            }
        });

        //옵션버튼을 눌렀을때
        mainBinding.mainframe.mainframeoptionbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainBinding.mainframe.mainframebacktomain.setVisibility(View.VISIBLE); //뒤로가기 버튼 보이게 하기
                mainBinding.mainframe.mainframeoptionbutton.setVisibility(View.GONE); //메뉴버튼 안보이게 하기
                mainBinding.mainframe.mainframeoptionlayout.setVisibility(View.VISIBLE); // 옵션 화면 보이게 하기
                mainBinding.mainframe.mainframelogoutbutton.setVisibility(View.VISIBLE); //로그아웃 버튼 보이게 하기
                mainBinding.mainframe.mainframegamestartbutton.setVisibility(View.VISIBLE); //게임시작 버튼 보이게 하기
                //옵션화면의 로그아웃 버튼
                mainBinding.mainframe.mainframelogoutbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                        JsonParser JP = new JsonParser();
                        JSONObject JO = new JSONObject();
                        JP.clientmsg.put("sendchat",JO); //JSON 형태의 보낼 메시지
                        JO.put("chatinfo","system"); //JSON 형태의 보낼 메시지
                        JO.put("message","logout"); //JSON 형태의 보낼 메시지
                        chatting.ChattingSend(JP.clientmsg); //service로 메시지 전달
                        userinfo.setUserchannel("1");
                      // chatting.EXIT(); //채팅소켓 연결 종료
                        unbindService(chattingconnect); //서비스바인딩 종료
                        stopService(Sintent); //서비스종료
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        overridePendingTransition(R.anim.not_move_activity,R.anim.not_move_activity); //화면 고정된상태에서 전환
                        finish();
                    }
                });

                //옵션화면의 게임시작 버튼
                mainBinding.mainframe.mainframegamestartbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mainBinding.mainframe.mainframematchinglayout.setVisibility(View.VISIBLE); //matching 창 띄우기
                        mainBinding.mainframe.mainframematchingcancelbutton.setVisibility(View.VISIBLE); //matching 취소버튼 띄우기
                        mainBinding.mainframe.mainframematchingprogress.setVisibility(View.VISIBLE); //matching 프로그레스바 띄우기
                        mainBinding.mainframe.mainframematchingtext.setVisibility(View.VISIBLE); //matching 매칭중text 띄우기

                        matching = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    matchingsocket=new Socket("211.249.62.8",9090);
                                    matchingreceive = new MatchingReceiveThread(matchingsocket);
                                    matchingreceive.start();
                                    //서버에 메시지를 전달하기 위한 스트림 생성
                                    PW = new PrintWriter(new OutputStreamWriter(matchingsocket.getOutputStream()));
                                    JsonParser JP = new JsonParser();
                                    JP.clientmsg.put("matchinginfo", "matching");
                                    JP.clientmsg.put("useremail", userinfo.getUseremail());
                                    String strJson = JSONValue.toJSONString(JP.clientmsg.toString()); // JSON 형태를 String 형태로 변환
                                    PW.println(strJson); //메시지 입력
                                    PW.flush(); //메시지 전송
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }

                        });
                        matching.start();
                    }
                });

            }
        });

        //옵션화면의 게임시작 버튼을 눌렀을때 매칭 취소버튼
        mainBinding.mainframe.mainframematchingcancelbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainBinding.mainframe.mainframematchinglayout.setVisibility(View.GONE); //matching 창 숨기기
                mainBinding.mainframe.mainframematchingcancelbutton.setVisibility(View.GONE); //matching 취소버튼 숨기기
                mainBinding.mainframe.mainframematchingprogress.setVisibility(View.GONE); //matching 프로그레스바 숨기기
                mainBinding.mainframe.mainframematchingtext.setVisibility(View.GONE); //matching 매칭중text 숨기기
                try {
                    if(matchingsocket != null){
                        matchingsocket.close();
                  //      matchingreceive.interrupt();
                        }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //뒤로가기 버튼을 눌렀을때
        mainBinding.mainframe.mainframebacktomain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainBinding.mainframe.mainframebacktomain.setVisibility(View.GONE); //뒤로가기 버튼 안보이게 하기
                mainBinding.mainframe.mainframeoptionbutton.setVisibility(View.VISIBLE); //메뉴버튼 보이게 하기
                mainBinding.mainframe.mainframelogoutbutton.setVisibility(View.GONE); //로그아웃 버튼 안보이게 하기
                mainBinding.mainframe.mainframeoptionlayout.setVisibility(View.GONE); // 옵션 화면 안보이게하기
            }
        });


    } //onCreate 끝

    //ServiceConnection 인터페이스 구현 객체
    ServiceConnection chattingconnect = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Chatting.ChattingBinder CB = (Chatting.ChattingBinder) service; //서비스와 연결되었을때 호출되는 메서드 서비스객체를 전역변수로 저장
            chatting = CB.getService(); //서비스가 제공하는 메소드 호출하여 서비스쪽 객체를 전달 받을 수 있음
            isService = true;

            chatting.channelchange(mainchattingchannelchange); //service로 메시지 전달
            chatting.chattingreceive(mainchattingreceivehandler); //service로 메시지 전달
      //      Intent intent = new Intent(MainActivity.this,Chatting.class); //서비스 시작 현재화면에서 -> 서비스
      //      intent.setAction(Chatting.RECEIVE);
      //      startService(intent);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(chatting, "서비스 연결 해제", Toast.LENGTH_SHORT).show();
        }
    };

    // json 파싱
    public class JsonParser {
        public JSONObject clientmsg;

        public JsonParser() {
            clientmsg = new JSONObject();
        }
    }

    //서비스 화면으로 이동
    public void servicebutton(View view){
        startActivity(new Intent(getApplicationContext(), ServiceActivity.class));
        overridePendingTransition(R.anim.not_move_activity,R.anim.not_move_activity); //화면 고정된상태에서 전환
        finish();
        unbindService(chattingconnect); //현재 액티비티와 서비스바인딩 종료 - 서비스를 종료한것은아님 현재액티비티랑 서비스만 종료함
    }

    //메인에서 back버튼눌렀을때
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("앱 종료");
        builder.setMessage("종료하시겠습니까?");
        builder.setPositiveButton("종료하기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                userinfo.setUserchannel("1");
             //   chatting.EXIT();
                unbindService(chattingconnect);
                stopService(Sintent);
                ActivityCompat.finishAffinity(MainActivity.this);
            }
        });
        builder.setNegativeButton("취소하기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                return;
            }
        });
        builder.show();
    }

    // 매칭서버와 연동




    //서버에서 이미지 불러오기
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView mainprofilepicture;

        public DownloadImageTask(ImageView mainprofilepicture) {
            this.mainprofilepicture = mainprofilepicture;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }
        //이미지 뷰에 이미지 출력
        protected void onPostExecute(Bitmap result) {
            mainBinding.mainframe.mainframeprofilepicture.setImageBitmap(result);
        }
    }
}
