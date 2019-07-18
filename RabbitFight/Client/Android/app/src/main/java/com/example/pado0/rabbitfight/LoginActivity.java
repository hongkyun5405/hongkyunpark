package com.example.pado0.rabbitfight;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.example.pado0.rabbitfight.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import org.json.JSONException;
import org.json.JSONObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    ActivityLoginBinding loginBinding; // Data binding

    private String rememberloginvalue = "";
    private String Semail; //이메일 값
    private String Spassword; //비밀번호 값

    //구글로그인
    private static final int RC_SIGN_IN = 1000;
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  // 상단 타이틀바 제거
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // 핸드폰 상단바 제거
        loginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login); //databinding 선언
        loginBinding.setLogin(this); // xml 과 activity 바인딩

        loginBinding.loginimage.getDrawable(); // 로그인화면 백그라운드 이미지
        loginBinding.googlesigninbutton.setSize(SignInButton.SIZE_STANDARD); //구글로그인 버튼 싸이즈



        //구글 로그인
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("896471089448-sqtt7hnfecus2l7pv3mgj0ugivdjckno.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance(); //FirebaseAuth 인스턴스 변수로 선언하여 Firebase 인증을 사용할 수 있게 초기화 해줌

        //구글로그인 버튼에 대한 이벤트
        loginBinding.googlesigninbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //이벤트 발생했을때, 구글로그인 버튼에 대한 (구글정보를 인텐트로 넘기는 값)
                //"방금 로그인한다고 하는사람이 구글 사용자니? "물어보는로직
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        //자동로그인체크
        SharedPreferences rememberlogincheck = getSharedPreferences("rememberlogincheck", Activity.MODE_PRIVATE);
        if (rememberlogincheck.contains("on")) {
            String rememberlogincheckvalue = rememberlogincheck.getString("on", "");
            String split[] = rememberlogincheckvalue.split("/");
            Semail = split[0];  //입력받은 이메일값을 서버로 보낼 서버용이메일 변수에 저장
            Spassword = split[1]; //입력받은 비밀번호값을 서버로 보낼 서버용비밀번호 변수에 암호화 한후 저장
            login();
            finish();
        }

        //자동로그인을 체크하면 동작
        loginBinding.rememberlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loginBinding.rememberlogin.isChecked()) {
                    Toast.makeText(LoginActivity.this, "자동로그인을 사용합니다, 개인정보가 유출될 우려가 있다면 사용을 자제해주세요.", Toast.LENGTH_SHORT).show();  // 자동로그인 안내메시지
                    rememberloginvalue = "on"; //자동로그인 옵션 선택
                } else {
                    rememberloginvalue = "off"; //자동로그인 옵션 끄기
                }
            }
        });

        //로그인버튼을 눌렀을때 동작
        loginBinding.loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //이메일을 입력했는지 확인
                if (loginBinding.loginemail.getText().toString().length() == 0) { //이메일 입력값이 없다면 실행
                    Toast.makeText(LoginActivity.this, "Email을 입력해주세요!", Toast.LENGTH_SHORT).show();  // 이메일 값이 없을때 이메일을 입력해 달라는 토스트 메시지
                    loginBinding.loginemail.requestFocus(); //이메일 입력부분에 포커스(커서)를 맞춤
                    return;
                } else if (!Pattern.matches("^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$", loginBinding.loginemail.getText().toString())) {
                    Toast.makeText(LoginActivity.this, "올바른 email형식으로 입력하세요!", Toast.LENGTH_SHORT).show();
                    loginBinding.loginemail.requestFocus();//이메일 입력부분에 포커스(커서)를 맞춤
                    return;
                }

                //비밀번호를 입력했는지 확인
                if (loginBinding.loginpassword.getText().toString().length() == 0) { //비밀번호 입력값이 없다면 실행행
                    Toast.makeText(LoginActivity.this, "Password를 입력해주세요!", Toast.LENGTH_SHORT).show(); //비밀번호 값이 없을때 비밀번호를 입력해 달라는 토스트 메시지
                    loginBinding.loginpassword.requestFocus(); //비밀번호 입력부분에 포커스(커서)를 맞춤
                    return;
                }

                //로그인 데이터 서버로 전송
                Semail = loginBinding.loginemail.getText().toString();  //입력받은 이메일값을 서버로 보낼 서버용이메일 변수에 저장
                Spassword = SHA256(loginBinding.loginpassword.getText().toString()); //입력받은 비밀번호값을 서버로 보낼 서버용비밀번호 변수에 암호화 한후 저장
                login();

                loginBinding.loginemail.setText(""); // 이메일 값 초기화
                loginBinding.loginpassword.setText(""); // 비밀번호 값 초기화
            }
        });

    } //onCreate 끝

    //회원가입 엑티비티로 이동
    public void SignupButton(View view){
        overridePendingTransition(R.anim.not_move_activity,R.anim.not_move_activity); //화면 고정된상태에서 전환
        startActivity(new Intent(getApplicationContext(),SignupActivity.class));
    }

    //비밀번호찾기 엑티비티로 이동
    public void ForgetPasswordButton(View view){
        overridePendingTransition(R.anim.not_move_activity,R.anim.not_move_activity); //화면 고정된상태에서 전환
        startActivity(new Intent(getApplicationContext(),ForgetpasswordActivity.class));
    }

    //비밀번호 암호화
    public String SHA256(String str) {
        String SHA = "";
        try {
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.update(str.getBytes());
            byte byteData[] = sh.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
            SHA = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            SHA = null;
        }
        return SHA;
    }

    //서버로 데이터 전송 (로그인)
    private void login() {
        final UserinformationSigleton userinfo = UserinformationSigleton.getInstance(); //유저 정보 싱글톤

        StringRequest sendtoserver = new StringRequest(Request.Method.POST, "http://211.249.62.8/login.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("response", "서버에서 받아온 값" + response);

                        if (response.equals("아이디나 비밀번호를 확인해주세요.")) {
                            Toast.makeText(LoginActivity.this, response, Toast.LENGTH_SHORT).show();
                        } else {
                            //자동로그인을 선택한 후 로그인을 하였을때 아이디와 비밀번호정보를 디바이스저장장치에 저장함
                            if (rememberloginvalue.equals("on")) {
                                SharedPreferences rememberlogincheck = getSharedPreferences("rememberlogincheck", Activity.MODE_PRIVATE);
                                SharedPreferences.Editor autoLogin = rememberlogincheck.edit();
                                autoLogin.putString("on", Semail + "/" + Spassword); //자동로그인이 체크일경우 키-on , 밸류는 아이디+/+비밀번호 로 디바이스에 저장한다.
                                autoLogin.apply();  //디바이스에 저장
                            }

                            //서버에서 닉네임과 프로필 사진 을 가져옴
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String nickname = jsonObject.get("user_nickname").toString();
                                String image = jsonObject.get("user_image").toString();
                                String email = jsonObject.get("user_email").toString();

                                //유저 정보 싱글톤에 저장
                                userinfo.setUseremail(email);
                                userinfo.setUsernickname(nickname);
                                userinfo.setUserimage(image);
                                userinfo.setUserloginvalue("RabbitFightlogin");

                                Toast.makeText(LoginActivity.this, nickname + "님 반갑습니다.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                overridePendingTransition(R.anim.not_move_activity,R.anim.not_move_activity); //화면 고정된상태에서 전환
                                startActivity(intent);
                                finish();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LoginActivity.this, "서버 연결을 실패했습니다. 다시시도해주세요.", Toast.LENGTH_SHORT).show();
                Log.d("volleyerror", "서버에서 받아온 값" + error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {  // 서버로 보낼데이터
                Map<String, String> params = new HashMap<>();
                params.put("Semail", Semail);
                params.put("Spassword", Spassword);
                return params;
            }
        };
        MySingleton.getInstance(LoginActivity.this).addToRequestQue(sendtoserver);
    }

    //구글 로그인 - Intent Result값 반환되는 로직
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) { //구글버튼 로그인 누르고, 구글사용자 확인되면 실행되는 로직
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();

                //   mStatusTextView.setText(getString(R.string.signed_in_fmt,account.getDisplayName()));
                Log.d("googlelogin", "account:" + account.getEmail());
                Log.d("googlelogin", "account:" + account.getId());
                Log.d("googlelogin", "account:" + account.getPhotoUrl());
                Log.d("googlelogin", "account:" + account.getDisplayName());
                firebaseAuthWithGoogle(account); //구글이용자 확인된 사람정보 파이어베이스로 넘기기
            } else {
                Log.d("googlelogin", "resultfail:" + result.getStatus().getStatusMessage());
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    //구글 계정 인증을 위한 파이어베이스로 값넘기기
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        final UserinformationSigleton googleuserinfo = UserinformationSigleton.getInstance(); //유저 정보 싱글톤
        //파이어베이스로 받은 구글사용자가 확인된 이용자의 값을 토큰으로 받고
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() { //구글로그인 서비스가 연결될때 애플리케이션에 콜백을 전송하는 인터페이스
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();  // 구글 계정에대한 정보 받아오는 함수

                            //유저 정보 싱글톤에 저장
                            googleuserinfo.setUseremail(user.getEmail());
                            googleuserinfo.setUsernickname(user.getDisplayName());
                            googleuserinfo.setUserimage(user.getPhotoUrl().toString());
                            googleuserinfo.setUserloginvalue("googlelogin");

                            Intent intent =new Intent(getApplicationContext(), MainActivity.class);
                            overridePendingTransition(R.anim.not_move_activity,R.anim.not_move_activity); //화면 고정된상태에서 전환
                            startActivity(intent);

                            Toast.makeText(LoginActivity.this, user.getDisplayName()+"님 반갑습니다.", Toast.LENGTH_SHORT).show();

                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "서버 연결을 실패했습니다. 다시시도해주세요.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
    }

    //구글 계정 인증을 위한 연결이 실패했을때 호출 됨
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(LoginActivity.this, "서버 연결을 실패했습니다. 다시시도해주세요.", Toast.LENGTH_SHORT).show();
        return;
    }

}
