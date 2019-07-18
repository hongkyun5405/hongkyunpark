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
import com.example.pado0.rabbitfight.databinding.ActivityServiceBinding;
import org.json.simple.JSONObject;
import java.io.InputStream;

import static java.lang.Integer.parseInt;


public class ServiceActivity extends AppCompatActivity {

    ActivityServiceBinding serviceBinding; //databinding

    Chatting chatting; //service

    boolean isService = false; //service 연결여부

    private Handler servicechattingreceivehandler; //[채팅] 채팅 리시브 핸들러
    private Handler servicechattingchannelchange; //[채팅] 채팅 채널 핸들러

    private Intent Sintent;

    final UserinformationSigleton userinfo = UserinformationSigleton.getInstance(); //유저 정보 싱글톤


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  // 상단 타이틀바 제거
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // 핸드폰 상단바 제거
        serviceBinding = DataBindingUtil.setContentView(this,R.layout.activity_service); //databinding + include xml 연결
        serviceBinding.setService(this);

        Sintent = new Intent(ServiceActivity.this,Chatting.class); //서비스 시작 현재화면에서 -> 서비스
        startService(Sintent);
        bindService(Sintent,chattingconnect, Context.BIND_AUTO_CREATE);  //서비스와 연결 chattingconnect는 서비스와 연결 정의

        serviceBinding.mainframe.mainframenickname.setText(userinfo.getUsernickname()); // 로그인정보에서 받아온 닉네임값

        //로그인 판별값에 따라 이미지 뷰에 다르게 적용
        if(userinfo.getUserloginvalue().equals("googlelogin")){
            Glide.with(this).load(userinfo.getUserimage()).into(serviceBinding.mainframe.mainframeprofilepicture); //로그인정보에서 받아온 이미지 값 - 구글 로그인만 사용
        }else{
            //로그인정보에서 받아온 이미지 값 - 일반 로그인만 닉네임값으로 서버에서 검색해 이미지 값을 가져와 사용
            new ServiceActivity.DownloadImageTask((ImageView) serviceBinding.mainframe.mainframeprofilepicture)
                    .execute("http://211.249.62.8/signupimage/"+userinfo.getUsernickname()+".jpg");
        }

        Glide.with(this).load(userinfo.getUserimage()).into(serviceBinding.mainframe.mainframeprofilepicture); //로그인정보에서 받아온 이미지 값
        serviceBinding.mainframe.mainframechatchanelbutton.setText("Ch."+userinfo.getUserchannel()); //자신의 채널값 가져오기


        // 채팅 channel 핸들러
        servicechattingchannelchange=new Handler(){
            @Override
            public void handleMessage(Message msg) {//여러군데에서 호출할때 구분
                serviceBinding.mainframe.mainframechatchanelbutton.setText("Ch."+msg.obj.toString()); //입력받은값으로 채널 변경
                userinfo.setUserchannel(msg.obj.toString()); //현재채널 변경
                serviceBinding.mainframe.mainframemSV.fullScroll(View.FOCUS_DOWN);  //최하단으로 포커스 자동 조절
                if(msg.obj.toString().equals("??")){}else{
                    serviceBinding.mainframe.mainframetxtMessage.append("Ch."+msg.obj.toString()+" 채널입장!" + "\n");
                }
            }
        };

        // 채팅 recevied 핸들러
        servicechattingreceivehandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {//여러군데에서 호출할때 구분
                serviceBinding.mainframe.mainframetxtMessage.append(msg.obj.toString() + "\n");
                serviceBinding.mainframe.mainframemSV.fullScroll(View.FOCUS_DOWN);  //최하단으로 포커스 자동 조절
            }
        };

        //채팅버튼 눌렀을때
        serviceBinding.mainframe.mainframechat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceBinding.mainframe.mainframechatcancel.setVisibility(View.VISIBLE); //채팅취소 버튼 보이게 하기
                serviceBinding.mainframe.mainframechat.setVisibility(View.GONE); //채팅버튼 안보이게 하기
                serviceBinding.mainframe.mainframechatview.setVisibility(View.VISIBLE); // 채팅 화면 보이게 하기
            }
        });

        //채팅취소버튼 눌렀을때
        serviceBinding.mainframe.mainframechatcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceBinding.mainframe.mainframechatcancel.setVisibility(View.GONE); //채팅취소 버튼 보이게 하기
                serviceBinding.mainframe.mainframechat.setVisibility(View.VISIBLE); //채팅버튼 안보이게 하기
                serviceBinding.mainframe.mainframeeditMessage.setText(""); //채팅입력란초기화
                serviceBinding.mainframe.mainframechannelchange.setText(""); //채널변경입력란초기화
                serviceBinding.mainframe.mainframechatview.setVisibility(View.GONE); // 채팅 화면 안보이게 하기
                serviceBinding.mainframe.mainframechannelchangelayout.setVisibility(View.GONE); // 채널변경 화면 안보이게 하기

            }
        });

        //채팅내용 입력할때 (안드로이드)키보드의 확인버튼을 클릭시 동작
        serviceBinding.mainframe.mainframeeditMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){

                    JsonParser JP = new JsonParser();
                    JSONObject JO = new JSONObject();
                    JP.clientmsg.put("sendchat",JO); //JSON 형태의 보낼 메시지
                    JO.put("chatinfo","chatting"); //JSON 형태의 보낼 메시지
                    JO.put("message",userinfo.getUsernickname()+":"+serviceBinding.mainframe.mainframeeditMessage.getText().toString()); //JSON 형태의 보낼 메시지

                    chatting.ChattingSend(JP.clientmsg); //service로 메시지 전달
                    serviceBinding.mainframe.mainframeeditMessage.setText(""); //내용초기화
                    return false; //창닫기
                }
                return false;  //창닫기
            }
        });

        //채널버튼을 눌렀을때
        serviceBinding.mainframe.mainframechatchanelbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceBinding.mainframe.mainframechannelchangelayout.setVisibility(View.VISIBLE); // 채널변경 화면 보이게 하기
            }
        });

        //채널 변경확인
        serviceBinding.mainframe.mainframechannelchangebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{ //채널입력란에 아무것도입력하지 않고 확인버튼을 눌렀을때 발생할 예외처리

                    if(parseInt(serviceBinding.mainframe.mainframechannelchange.getText().toString()) > 0 && parseInt(serviceBinding.mainframe.mainframechannelchange.getText().toString()) < 1000){ //입력값이 1~999사이이면 채널이동

                        JsonParser JP = new JsonParser();
                        JSONObject JO = new JSONObject();
                        JP.clientmsg.put("sendchat",JO); //JSON 형태의 보낼 메시지
                        JO.put("chatinfo","channelchange"); //JSON 형태의 보낼 메시지
                        JO.put("message",Integer.parseInt(serviceBinding.mainframe.mainframechannelchange.getText().toString())); //JSON 형태의 보낼 메시지


                        chatting.ChattingSend(JP.clientmsg); //service로 메시지 전달
                        serviceBinding.mainframe.mainframechannelchangelayout.setVisibility(View.GONE); // 채널변경 화면 안보이게 하기
                        serviceBinding.mainframe.mainframechannelchange.setText(""); //내용초기화
                    }else if(serviceBinding.mainframe.mainframechannelchange.getText().toString().length() == 0 || serviceBinding.mainframe.mainframechannelchange.getText().toString().equals("")){ //입력값이 없으면 메시지 출력
                        Toast.makeText(ServiceActivity.this, "1~999 사이의 숫자만 입력가능 합니다.", Toast.LENGTH_SHORT).show();
                        serviceBinding.mainframe.mainframechannelchange.setText(""); //내용초기화
                        return;
                    }else{ //그외 처리
                        Toast.makeText(ServiceActivity.this, "1~999 사이의 숫자만 입력가능 합니다.", Toast.LENGTH_SHORT).show();
                        serviceBinding.mainframe.mainframechannelchange.setText(""); //내용초기화
                        return;
                    }

                }catch (NumberFormatException e){
                    Toast.makeText(ServiceActivity.this, "이동할 채널번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        //채널 변경취소
        serviceBinding.mainframe.mainframechannelchangecancelbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceBinding.mainframe.mainframechannelchange.setText(""); //내용초기화
                serviceBinding.mainframe.mainframechannelchangelayout.setVisibility(View.GONE); // 채널변경 화면 안보이게 하기
            }
        });

        //옵션버튼을 눌렀을때
        serviceBinding.mainframe.mainframeoptionbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceBinding.mainframe.mainframebacktomain.setVisibility(View.VISIBLE); //뒤로가기 버튼 보이게 하기
                serviceBinding.mainframe.mainframeoptionbutton.setVisibility(View.GONE); //메뉴버튼 안보이게 하기
                serviceBinding.mainframe.mainframeoptionlayout.setVisibility(View.VISIBLE); // 옵션 화면 보이게 하기
                serviceBinding.mainframe.mainframelogoutbutton.setVisibility(View.VISIBLE); //로그아웃 버튼 보이게 하기
                serviceBinding.mainframe.mainframegamestartbutton.setVisibility(View.VISIBLE); //게임시작 버튼 보이게 하기

                //옵션화면의 로그아웃 버튼
                serviceBinding.mainframe.mainframelogoutbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(ServiceActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                        ServiceActivity.JsonParser JP = new ServiceActivity.JsonParser();
                        JSONObject JO = new JSONObject();
                        JP.clientmsg.put("sendchat",JO); //JSON 형태의 보낼 메시지
                        JO.put("chatinfo","system"); //JSON 형태의 보낼 메시지
                        JO.put("message","logout"); //JSON 형태의 보낼 메시지
                        chatting.ChattingSend(JP.clientmsg); //service로 메시지 전달
                        userinfo.setUserchannel("1");
                   //     chatting.EXIT();
                        unbindService(chattingconnect);
                        stopService(Sintent);
                        startActivity(new Intent(ServiceActivity.this, LoginActivity.class));
                        overridePendingTransition(R.anim.not_move_activity,R.anim.not_move_activity); //화면 고정된상태에서 전환
                        finish();
                    }
                });

                //옵션화면의 게임시작 버튼
                serviceBinding.mainframe.mainframegamestartbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent =new Intent(getApplicationContext(),UnityPlayerActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });

        //뒤로가기 버튼을 눌렀을때
        serviceBinding.mainframe.mainframebacktomain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceBinding.mainframe.mainframebacktomain.setVisibility(View.GONE); //뒤로가기 버튼 안보이게 하기
                serviceBinding.mainframe.mainframeoptionbutton.setVisibility(View.VISIBLE); //메뉴버튼 보이게 하기
                serviceBinding.mainframe.mainframelogoutbutton.setVisibility(View.GONE); //로그아웃 버튼 안보이게 하기
                serviceBinding.mainframe.mainframeoptionlayout.setVisibility(View.GONE); // 옵션 화면 안보이게하기
            }
        });


    } //onCreate 끝


    ServiceConnection chattingconnect = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Chatting.ChattingBinder CB = (Chatting.ChattingBinder) service; //서비스와 연결되었을때 호출되는 메서드 서비스객체를 전역변수로 저장
            chatting = CB.getService(); //서비스가 제공하는 메소드 호출하여 서비스쪽 객체를 전달 받을 수 있음
            isService = true;

            chatting.channelchange(servicechattingchannelchange); //service로 메시지 전달
            chatting.chattingreceive(servicechattingreceivehandler); //service로 메시지 전달
            Log.d("service1","aaa");
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

    //메인 화면으로 이동
    public void mainbutton(View view){
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
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
            //    chatting.EXIT();
                unbindService(chattingconnect);
                stopService(Sintent);
                ActivityCompat.finishAffinity(ServiceActivity.this);
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

    //서버에서 이미지 불러오기
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView servicemainprofilepicture;

        public DownloadImageTask(ImageView mainprofilepicture) {
            this.servicemainprofilepicture = mainprofilepicture;
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
            serviceBinding.mainframe.mainframeprofilepicture.setImageBitmap(result);
        }
    }
}
