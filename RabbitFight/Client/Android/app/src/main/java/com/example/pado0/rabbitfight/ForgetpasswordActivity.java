package com.example.pado0.rabbitfight;

import android.app.ProgressDialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.pado0.rabbitfight.databinding.ActivityForgetpasswordBinding;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ForgetpasswordActivity extends AppCompatActivity {

    ActivityForgetpasswordBinding forgetpasswordBinding; //DataBinding

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  // 상단 타이틀바 제거
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // 핸드폰 상단바 제거
        forgetpasswordBinding = DataBindingUtil.setContentView(this, R.layout.activity_forgetpassword); //databinding 선언
        forgetpasswordBinding.setForgetPassword(this); // xml 과 activity 바인딩



        //서버로 이메일 값을 보내는 버튼
        forgetpasswordBinding.passwordrecivedbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(forgetpasswordBinding.forgetpasswordsendemail.getText().toString().length() == 0){
                    Toast.makeText(ForgetpasswordActivity.this, "이메일 주소를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    forgetpasswordBinding.forgetpasswordsendemail.requestFocus();
                    return;
                }else if (!Pattern.matches("^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$", forgetpasswordBinding.forgetpasswordsendemail.getText().toString())) {
                    Toast.makeText(ForgetpasswordActivity.this, "올바른 email형식으로 입력하세요!", Toast.LENGTH_SHORT).show();
                    forgetpasswordBinding.forgetpasswordsendemail.requestFocus();//이메일 입력부분에 포커스(커서)를 맞춤
                    return;
                }

                forgetpassword(); //서버로 이메일값 보내는 함수
            }
        });

    } //onCreate 끝

    //뒤로가기
    public void finish(View view){
        finish();
    }

    //서버로 데이터 전송 (비밀번호찾기)
    private void forgetpassword() {
        final ProgressDialog progressDialog; //백그라운드 작업(서버로 데이터 전송후)에 쓰이는 프로그레스다이얼로그
        progressDialog = ProgressDialog.show(ForgetpasswordActivity.this, "잠시만 기다려 주세요.", null,true,true);  // 서버와 통신중일때 표시될 다이얼로그

        StringRequest sendtoserver = new StringRequest(Request.Method.POST,"http://211.249.62.8/forgetpassword.php",
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss(); //프로그래스바 종료

                        Log.d("response","서버에서 받아온 값" + response);

                        if(response.equals("임시비밀번호전송완료")){
                            Toast.makeText(ForgetpasswordActivity.this, "임시비밀번호를 입력하신 이메일로 전송하였습니다.", Toast.LENGTH_SHORT).show();
                            forgetpasswordBinding.forgetpasswordsendemail.setText(""); //입력한 이메일 초기화
                            overridePendingTransition(R.anim.not_move_activity,R.anim.not_move_activity); //화면 고정된상태에서 전환
                            finish();
                        }else if(response.equals("존재하지않는이메일입니다")){
                            Toast.makeText(ForgetpasswordActivity.this, "존재하지 않는 이메일입니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show();
                            forgetpasswordBinding.forgetpasswordsendemail.setText(""); //입력한 이메일 초기화
                            forgetpasswordBinding.forgetpasswordsendemail.requestFocus(); //이메일 입력부분에 포커스(커서)를 맞춤
                        }else{
                            Toast.makeText(ForgetpasswordActivity.this, "네트워크 연결이 불안정합니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                        }

                        }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Log.d("volleyerror","서버에서 받아온 값" + error);
            }
        })

        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {  // 서버로 보낼데이터
                Map<String, String> params = new HashMap<>();
                params.put("Sforgetemail", forgetpasswordBinding.forgetpasswordsendemail.getText().toString()); //입력받은 이메일값
                return params;
            }
        };
        MySingleton.getInstance(ForgetpasswordActivity.this).addToRequestQue(sendtoserver);
    }
}
