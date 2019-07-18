package com.example.pado0.rabbitfight;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
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
import com.bumptech.glide.Glide;
import com.example.pado0.rabbitfight.databinding.ActivitySignupBinding;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    ActivitySignupBinding signupBinding;  // Data binding

    private String emailduplicate =""; //이메일 체크버튼을 눌렀는지 검사하기 위한 값
    private String nicknameduplicate =""; //닉네임 체크버튼을 눌렀는지 검사하기 위한 값
    private static final int PICK_FROM_ALBUM = 1; //앨범에 대한 onActivityResult에서 requestCode로 반환되는 값이다.
    private static final int PICK_FROM_CAMERA = 2; //카메라에 대한 onActivityResult에서 requestCode로 반환되는 값이다.
    private File tempFile; //앨범으로 부터 받아온 이미지를 File 형식으로 저장 할 변수
    private Bitmap bitmap_temp; //회전각을 잡기전 이미지 비트맵
    private Bitmap profileimage; //프로필이미지를 서버에 보내기위한 비트맵
    static Context mContext; //context

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  // 상단 타이틀바 제거
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // 핸드폰 상단바 제거
        signupBinding = DataBindingUtil.setContentView(this,R.layout.activity_signup); //databinding 선언
        signupBinding.setSignup(this); //xml과 activity연결

        mContext = this.getBaseContext(); //context를 이 파일로 정함


        //비밀번호 일치검사 - 초록색이면 일치 빨간색이면 불일치
        signupBinding.signuppassword2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }


            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String password = signupBinding.signuppassword.getText().toString();
                String confirm = signupBinding.signuppassword2.getText().toString();


                if (password.equals(confirm)) {
                    signupBinding.signuppassword.setBackgroundColor(Color.GREEN);
                    signupBinding.signuppassword2.setBackgroundColor(Color.GREEN);
                } else {
                    signupBinding.signuppassword.setBackgroundColor(Color.GREEN);
                    signupBinding.signuppassword2.setBackgroundColor(Color.RED);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        //이메일체크 버튼을 클릭하면 동작 - 서버로 전송해서 이메일체크
        signupBinding.signupemailduplicatebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (signupBinding.signupemail.getText().toString().length() == 0) {
                    Toast.makeText(SignupActivity.this, "Email을 입력하세요!", Toast.LENGTH_SHORT).show();
                    return;
                }else if (!Pattern.matches("^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$", signupBinding.signupemail.getText().toString())) {
                    Toast.makeText(SignupActivity.this, "올바른 email형식으로 입력하세요!", Toast.LENGTH_SHORT).show();
                    signupBinding.signupemail.requestFocus();
                    return;
                }

                String Semail = signupBinding.signupemail.getText().toString();  //입력받은 이메일값
                signupemailcheck signupemailcheck = new signupemailcheck(); //서버이메일 체크하는 곳 으로 보내기 위해 객체 선언
                signupemailcheck.execute(Semail);  //이메일 체크하는 php로 전송

            }
        });

        //닉네임체크 버튼을 클릭하면 동작 - 서버로 전송해서 닉네임 체크
        signupBinding.signupnicknameduplicatebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (signupBinding.signupnickname.getText().toString().length() == 0) {
                    Toast.makeText(SignupActivity.this, "닉네임을 입력하세요!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String Snickname = signupBinding.signupnickname.getText().toString(); //입력받은 닉네임값
                signupnicknamecheck signupnicknamecheck = new signupnicknamecheck(); //서버이메일 체크하는 곳 으로 보내기 위해 객체 선언
                signupnicknamecheck.execute(Snickname); //닉네임 체크하는 php로 전송

            }
        });


        //회원가입 버튼을 눌렀을때
        signupBinding.signupbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //이메일 입력했는지 확인 및 이메일체크 했는지 확인
                if (signupBinding.signupemail.getText().toString().length() == 0) {
                    Toast.makeText(SignupActivity.this, "Email을 입력하세요!", Toast.LENGTH_SHORT).show();
                    signupBinding.signupemail.requestFocus();
                    return;
                } else if (!Pattern.matches("^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$", signupBinding.signupemail.getText().toString())) {
                    Toast.makeText(SignupActivity.this, "올바른 email형식으로 입력하세요!", Toast.LENGTH_SHORT).show();
                    signupBinding.signupemail.requestFocus();
                    return;
                } else {
                    if (emailduplicate == "on") {
                    } else {
                        Toast.makeText(SignupActivity.this, "이메일체크를 해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }


                //닉네임 입력했는지 확인 및 닉네임체크 했는지 확인
                if (signupBinding.signupnickname.getText().toString().length() == 0) {
                    Toast.makeText(SignupActivity.this, "닉네임을 입력하세요!", Toast.LENGTH_SHORT).show();
                    signupBinding.signupnickname.requestFocus();
                    return;
                } else {
                    if (nicknameduplicate == "on") {
                    } else {
                        Toast.makeText(SignupActivity.this, "닉네임체크를 해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                //비밀번호 입력했는지 확인 및 비밀번호 정규식에 맞게입력 했는지 확인
                if (signupBinding.signuppassword.getText().toString().length() == 0) {
                    Toast.makeText(SignupActivity.this, "비밀번호를 입력하세요!", Toast.LENGTH_SHORT).show();
                    signupBinding.signuppassword.requestFocus();
                    return;
                } else if (!Pattern.matches("^(?=.*\\d)(?=.*[~`!@#$%\\^&*()-])(?=.*[a-zA-Z]).{8,20}$", signupBinding.signuppassword.getText().toString())) {
                    Toast.makeText(SignupActivity.this, "올바른 password형식으로 입력하세요!", Toast.LENGTH_SHORT).show();
                    signupBinding.signuppassword.requestFocus();
                    return;
                }

                //비밀번호_확인 입력했는지 확인 및 비밀번호 정규식에 맞게입력했는지 확인
                if (signupBinding.signuppassword2.getText().toString().length() == 0) {
                    Toast.makeText(SignupActivity.this, "비밀번호 확인을 입력하세요!", Toast.LENGTH_SHORT).show();
                    signupBinding.signuppassword2.requestFocus();
                    return;
                } else if (!Pattern.matches("^(?=.*\\d)(?=.*[~`!@#$%\\^&*()-])(?=.*[a-zA-Z]).{8,20}$", signupBinding.signuppassword2.getText().toString())) {
                    Toast.makeText(SignupActivity.this, "올바른 password형식으로 입력하세요!", Toast.LENGTH_SHORT).show();
                    signupBinding.signuppassword2.requestFocus();
                    return;
                }

                //비밀번호 일치 확인
                if (!signupBinding.signuppassword.getText().toString().equals(signupBinding.signuppassword2.getText().toString())) {
                    Toast.makeText(SignupActivity.this, "비밀번호가 일치하지 않습니다!!!", Toast.LENGTH_SHORT).show();
                    signupBinding.signuppassword.setText("");
                    signupBinding.signuppassword2.setText("");
                    return;
                }

                //서버로 회원가입 정보전송
                signup();
            }
        });


        //프로필 이미지 등록
        signupBinding.signupprofilepicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
            }
        });

    }   //oncreate 끝

    //취소하기 버튼을 눌렀을때 동작 - 회원가입액티비티 종료
    public void finish(View view){
            finish();
        };

    //카메라 사용권한 이용자에게 묻기
    private void tedPermission(){
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                //권한요청성공
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                //권한요청실패
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA)
                .check();
    }

    //이미지 뷰를 클릭했을때 보여주는 다이얼로그
    void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("사진불러오기");
        builder.setMessage("사진불러올곳을 선택해주세요.");
        builder.setPositiveButton("앨범가기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                goToAlbum();
            }
        });
        builder.setNeutralButton("사진찍기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                takePhoto();
            }
        });
        builder.show();
        tedPermission(); //권한요청함수
    }

    //프로필 이미지를 등록할때 다이얼로그에서 앨범을 클릭할 시 앨범을 호출하는 함수
    private void goToAlbum(){
        // 앨범 호출
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    //프로필 이미지를 등록할때 다이얼로그에서 사진찍기를 클릭할 시 카메라를 호출하는 함수
    private void takePhoto(){
        // 카메라 호출
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //카메라 호출

        try{
            tempFile = createImageFile();
        }catch (IOException e){
            Toast.makeText(this, "이미지 처리 오류! 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }

        if(tempFile != null){
            //안드로이드 누가 하위버전에서는 provider로 uri를 감싸주지않으면 동작하지 않을 수 있기 때문에 버전을 구분하여서 처리
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                Uri photoUri = FileProvider.getUriForFile(this, "{com.example.pado0.rabbitfight}.provider",tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri); //tempFile의 Uri 경로를 intent에 추가해 주어야한다. 이는 카메라에서 찍은 사진이 저장될 주소를 의미한다.
                startActivityForResult(intent,PICK_FROM_CAMERA); //결과를 처리하는곳으로 카메라사용 상수 값을 전달한다.
            }else{
                Uri photoUri = Uri.fromFile(tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri); //tempFile의 Uri 경로를 intent에 추가해 주어야한다. 이는 카메라에서 찍은 사진이 저장될 주소를 의미한다.
                startActivityForResult(intent, PICK_FROM_CAMERA);  //결과를 처리하는곳으로 카메라사용 상수 값을 전달한다.
            }
        }
    }

    //카메라에서 찍은 사진을 저장할 파일 만들기
    private File createImageFile() throws IOException {
        // 이미지 파일 이름 ( rabbitfight_{시간}_ )
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "rabbitfight" + timeStamp + "_";

        // 이미지가 저장될 폴더 이름 ( blackJin )
        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.example.pado0.rabbitfight/files/Pictures/"+""+imageFileName);
        if (!storageDir.exists()) storageDir.mkdirs();

        // 빈 파일 생성
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        return image;
    }

    //프로필이미지를 String형식으로 변환해주는 함수
    private String imageToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgBytes,Base64.DEFAULT);
    }

    //카메라와 앨범에서 가져온 이미지처리함수
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        //setResult를 통해 받아온 요청번호, 상태, 데이터
        Log.d("RESULT", requestCode + "");
        Log.d("RESULT", resultCode + "");
        Log.d("RESULT", data + "");

        //이미지가져오기 예외사항 처리 - 앨범화면으로 이동 후 선택을 하지않고 뒤로갔을경우, 카메라 촬영후 저장하지않고 뒤로 가기를 한경우
        if(resultCode != Activity.RESULT_OK){
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show(); //취소메시지 보여주기

            //사진촬영 중 취소를 하게 되면 tempFile이 빈 썸네일로 저장되기때문에 이를 삭제해주는 작업업
            if(tempFile != null){
                if(tempFile.exists()){
                    if(tempFile.delete()){
                        Log.d("imageException",tempFile.getAbsolutePath() + " 삭제성공");
                        tempFile = null;
                    }
                }
            }
            return;
        }

        // requestcode값이 PICK_FROM_ALBUM 이면 아래 로직이 실행됨
        if(requestCode == PICK_FROM_ALBUM){
            Uri photoUri = data.getData(); //갤러리에서 선택한 이미지의 Uri를 받아옴
            Cursor cursor = null;
            try{
                //Uri 스키마를 content:/// 에서 file:/// 로 변경한다.
                String[] proj = { MediaStore.Images.Media.DATA };

                assert photoUri != null;
                cursor = getContentResolver().query(photoUri,proj,null,null,null); //content:// 에서 file://로 변경해줌 사진의 절대 경로를 받아오는 과정
                assert cursor != null;
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                tempFile = new File(cursor.getString(column_index));

            }finally {
                if(cursor != null)
                    cursor.close();
            }
            setImage();

        // requestcode값이 PICK_FROM_CAMERA 이면 아래 로직이 실행됨
        }else if(requestCode == PICK_FROM_CAMERA){
            setImage();
        }
    }

    //갤러리에서 받아온 이미지를 이미지뷰에 넣는 함수
    private void setImage(){

        BitmapFactory.Options options = new BitmapFactory.Options();
        bitmap_temp = BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options); //서버에 보내기위해 지정된 변수에 넣음

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(tempFile.getAbsolutePath());
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int exifDegree = getEXIFDegrees(exifOrientation);
            bitmap_temp = setImageRotate(bitmap_temp, exifDegree);
        } catch (IOException e) {
            e.printStackTrace();
        }
        profileimage = bitmap_temp; //서버에보내는 bitmap변수에 담음

        Glide.with(this).load(tempFile).into(signupBinding.signupprofilepicture); //이미지뷰에 넣기
    }

    //이미지를 회전시킴
    public static Bitmap setImageRotate(Bitmap bitmap, int degrees) {
        if(degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2,
                    (float) bitmap.getHeight() / 2);
            try {
                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0,bitmap.getWidth(), bitmap.getHeight(), m, true);
                if(bitmap != converted) {
                    bitmap.recycle();
                    bitmap = converted;
                }
            }catch(OutOfMemoryError ex) {
                // 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
            }
        }
        return bitmap;
    }

    //EXIF 정보에서 회전각을 받아옴
    public static int getEXIFDegrees(int exifOrientation) {
        if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        }
        else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        }
        else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    //서버로 데이터 전송  (이메일 중복체크)
    class signupemailcheck extends AsyncTask<String, Void, String>{
        ProgressDialog progressDialog; //백그라운드 작업(서버로 데이터 전송후)에 쓰이는 프로그레스다이얼로그

        //백그라운드 작업을 실행하기 전에 실행됨 , 서버와 통신중일때 표시될 다이얼로그
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressDialog = ProgressDialog.show(SignupActivity.this, "잠시만 기다려 주세요.", null,true,true);  // 서버와 통신중일때 표시될 다이얼로그
        }

        //doInBackground에서 받아온 데이터 값을 사용하는 장소 - 서버로 부터 받은값을 메인엑티비티로 전달
        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);

            progressDialog.dismiss(); // 모든 통신이 끝나면 진행중다이얼로그 종료

            Log.d("result","서버에서 받아온 값" + result);

            if(result.equals("이메일사용가능")){
                Toast.makeText(SignupActivity.this, "사용가능한 이메일 입니다.", Toast.LENGTH_SHORT).show();
                emailduplicate = "on";
            }else if(result.equals("이메일사용불가능")){
                Toast.makeText(SignupActivity.this, "이미 사용중인 이메일 입니다.", Toast.LENGTH_SHORT).show();
                signupBinding.signupemail.setText(null); //이메일 입력란을 비움
                signupBinding.signupemail.requestFocus(); //이메일 입력란에 커서를 맞춤
                return;
            }else{
                Toast.makeText(SignupActivity.this, "네트워크 연결이 불안정합니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

        }

        //백그라운드에서 서버로 데이터를 전송
        @Override
        protected String doInBackground(String... params){

            String Semail = (String)params[0]; //로그인 버튼을 눌렀을때 서버로 전송할 이메일 값을 보낸것을 여기서 받아서 처리

            String serverURL = "http://211.249.62.8/signupemailcheck.php"; //서버의 php파일을 실행 시킬 수 있는 주소와 로그인데이터를 받을 php파일명

            //HTTP 메세지 본문에 포함되어 전송되기 때문에 따로 데이터를 준비해야함
            //전송할 데이터는 "이름=값" 형식 , 여러개를 보낼때는 &를 추가
            //여기에 적은 이름을 php 에서 사용하여 값을얻음

            String postParameters = "Semail=" + Semail; //서버로 보낼 이메일 값 //두개이상을 보낼때에는 두번째부터 앞에 &를 추가하여 보낸다.

            try{
                //httpURLConnection 클래스를 사용하여 POST 방식으로 데이터를 전송한다.
                URL url = new URL(serverURL); //서버주소가 저장된 변수를 사용
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection(); //설정한 서버주소와 연결을 위한 함수
                httpURLConnection.setReadTimeout(5000); //5초안에 응답이 오지 않으면 예외 발생
                httpURLConnection.setConnectTimeout(5000); // 5초안에 연결이 되지 않으면 예외 발생
                httpURLConnection.setRequestMethod("POST"); //요청방식을 POST로 함
                httpURLConnection.connect(); //설정한 서버주소와 연결

                OutputStream outputStream = httpURLConnection.getOutputStream(); //데이터 전송을 위한 함수
                outputStream.write(postParameters.getBytes("UTF-8")); //전송할 데이터가 저장된 변수를 입력, 전송할 데이터 준비완료
                outputStream.flush(); //데이터 전송
                outputStream.close(); //데이터를 보내는 스트림 종료

                int responseStatusCode = httpURLConnection.getResponseCode(); //데이터 전송에 대한 서버로 부터의 답변
                InputStream inputStream; //서버로 부터 답장을 받아올 스트림

                //서버에서 답장을 보내줄 준비가 되었는지 검사
                if(responseStatusCode == HttpURLConnection.HTTP_OK){
                    inputStream = httpURLConnection.getInputStream();  //서버에서 답장을 보내줄 준비가 되었다면 답장을 받을 스트림 작동
                }else{
                    inputStream = httpURLConnection.getErrorStream(); //서버에서 답장을 보낼줄 준비가 되지않았다면 오류 발생
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8"); //서버에서 보낸 데이터를 스트림에서 읽을 함수
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader); //스트림에서 읽어온값을 버퍼에 저장하기위한 읽기버퍼

                StringBuilder sb = new StringBuilder(); //읽기버퍼를 읽는 객체
                String line = null; //버퍼에서 읽어올값을 저장할 변수

                //버퍼에서 값을 읽어서 값이있다면 line변수에 저장
                while((line = bufferedReader.readLine()) != null){
                    sb.append(line); //서버가 보낸 데이터를 전부다 붙여줌
                }

                bufferedReader.close(); //서버가 보낸값을 모두 읽은후 버퍼 종료

                return sb.toString(); // 서버가 보낸 값을 모두 가지고있는 값을 반환함

            }catch(Exception e){ //5초안에 연결이 되지않거나 응답이오지않을 경우 즉 네트워크연결이 불안정할때
                e.printStackTrace();
                return new String("Error: "+e.getMessage());
            }
        }

    }

    //서버로 데이터 전송  (닉네임 중복체크)
    class signupnicknamecheck extends AsyncTask<String, Void, String>{
        ProgressDialog progressDialog; //백그라운드 작업(서버로 데이터 전송후)에 쓰이는 프로그레스다이얼로그

        //백그라운드 작업을 실행하기 전에 실행됨 , 서버와 통신중일때 표시될 다이얼로그
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressDialog = ProgressDialog.show(SignupActivity.this, "잠시만 기다려 주세요.", null,true,true);  // 서버와 통신중일때 표시될 다이얼로그
        }

        //doInBackground에서 받아온 데이터 값을 사용하는 장소 - 서버로 부터 받은값을 메인엑티비티로 전달
        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);

            progressDialog.dismiss(); // 모든 통신이 끝나면 진행중다이얼로그 종료

            Log.d("result","서버에서 받아온 값" + result);

            if(result.equals("닉네임사용가능")){
                Toast.makeText(SignupActivity.this, "사용가능한 닉네임 입니다.", Toast.LENGTH_SHORT).show();
                nicknameduplicate = "on";
            }else if(result.equals("닉네임사용불가능")){
                Toast.makeText(SignupActivity.this, "이미 사용중인 닉네임 입니다.", Toast.LENGTH_SHORT).show();
                signupBinding.signupnickname.setText(null); //닉네임 입력란을 비움
                signupBinding.signupnickname.requestFocus(); //닉네임 입력란에 커서를 맞춤
                return;
            }else{
                Toast.makeText(SignupActivity.this, "네트워크 연결이 불안정합니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

        }

        //백그라운드에서 서버로 데이터를 전송
        @Override
        protected String doInBackground(String... params){

            String Snickname = (String)params[0]; //로그인 버튼을 눌렀을때 서버로 전송할 이메일 값을 보낸것을 여기서 받아서 처리

            String serverURL = "http://211.249.62.8/signupnicknamecheck.php"; //서버의 php파일을 실행 시킬 수 있는 주소와 로그인데이터를 받을 php파일명

            //HTTP 메세지 본문에 포함되어 전송되기 때문에 따로 데이터를 준비해야함
            //전송할 데이터는 "이름=값" 형식 , 여러개를 보낼때는 &를 추가
            //여기에 적은 이름을 php 에서 사용하여 값을얻음

            String postParameters = "Snickname=" + Snickname; //서버로 보낼 닉네임 값 //두개이상을 보낼때에는 두번째부터 앞에 &를 추가하여 보낸다.

            try{
                //httpURLConnection 클래스를 사용하여 POST 방식으로 데이터를 전송한다.
                URL url = new URL(serverURL); //서버주소가 저장된 변수를 사용
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection(); //설정한 서버주소와 연결을 위한 함수
                httpURLConnection.setReadTimeout(5000); //5초안에 응답이 오지 않으면 예외 발생
                httpURLConnection.setConnectTimeout(5000); // 5초안에 연결이 되지 않으면 예외 발생
                httpURLConnection.setRequestMethod("POST"); //요청방식을 POST로 함
                httpURLConnection.connect(); //설정한 서버주소와 연결

                OutputStream outputStream = httpURLConnection.getOutputStream(); //데이터 전송을 위한 함수
                outputStream.write(postParameters.getBytes("UTF-8")); //전송할 데이터가 저장된 변수를 입력, 전송할 데이터 준비완료
                outputStream.flush(); //데이터 전송
                outputStream.close(); //데이터를 보내는 스트림 종료

                int responseStatusCode = httpURLConnection.getResponseCode(); //데이터 전송에 대한 서버로 부터의 답변
                InputStream inputStream; //서버로 부터 답장을 받아올 스트림

                //서버에서 답장을 보내줄 준비가 되었는지 검사
                if(responseStatusCode == HttpURLConnection.HTTP_OK){
                    inputStream = httpURLConnection.getInputStream();  //서버에서 답장을 보내줄 준비가 되었다면 답장을 받을 스트림 작동
                }else{
                    inputStream = httpURLConnection.getErrorStream(); //서버에서 답장을 보낼줄 준비가 되지않았다면 오류 발생
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8"); //서버에서 보낸 데이터를 스트림에서 읽을 함수
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader); //스트림에서 읽어온값을 버퍼에 저장하기위한 읽기버퍼

                StringBuilder sb = new StringBuilder(); //읽기버퍼를 읽는 객체
                String line = null; //버퍼에서 읽어올값을 저장할 변수

                //버퍼에서 값을 읽어서 값이있다면 line변수에 저장
                while((line = bufferedReader.readLine()) != null){
                    sb.append(line); //서버가 보낸 데이터를 전부다 붙여줌
                }

                bufferedReader.close(); //서버가 보낸값을 모두 읽은후 버퍼 종료

                return sb.toString(); // 서버가 보낸 값을 모두 가지고있는 값을 반환함

            }catch(Exception e){ //5초안에 연결이 되지않거나 응답이오지않을 경우 즉 네트워크연결이 불안정할때
                e.printStackTrace();
                return new String("Error: "+e.getMessage());
            }
        }

    }

    //비밀번호 암호화
    public String SHA256(String str){
        String SHA = "";
        try{
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.update(str.getBytes());
            byte byteData[] = sh.digest();
            StringBuffer sb = new StringBuffer();
            for(int i = 0 ; i < byteData.length ; i++){
                sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
            }
            SHA = sb.toString();
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
            SHA = null;
        }
        return SHA;
    }

    //서버로 데이터 전송 (회원가입)
    private void signup() {
        final ProgressDialog progressDialog; //백그라운드 작업(서버로 데이터 전송후)에 쓰이는 프로그레스다이얼로그
        progressDialog = ProgressDialog.show(SignupActivity.this, "잠시만 기다려 주세요.", null,true,true);  // 서버와 통신중일때 표시될 다이얼로그

        final String Semail = signupBinding.signupemail.getText().toString(); //입력받은 이메일값
        final String Snickname = signupBinding.signupnickname.getText().toString(); //입력받은 닉네임값
        final String Spassword = SHA256(signupBinding.signuppassword.getText().toString()); //입력받은 비밀번호값 암호화처리까지 완료한 값

        StringRequest sendtoserver = new StringRequest(Request.Method.POST,"http://211.249.62.8/signup.php",
                new Response.Listener<String>(){

                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss(); //프로그래스바 종료

                        Log.d("response","서버에서 받아온 값" + response);

                        if(response.equals("회원가입성공")){
                            Toast.makeText(SignupActivity.this, "회원가입되었습니다. 감사합니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        }else{
                            Toast.makeText(SignupActivity.this, "네트워크 연결이 불안정합니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                            return;
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
                params.put("Semail", Semail);
                params.put("Snickname", Snickname);
                params.put("Spassword", Spassword);
                //프로필사진이 없다면 이미지정보는 전송하지 않음
                if(profileimage!=null){
                    params.put("Simage", imageToString(profileimage));
                }
                return params;
            }
        };
        MySingleton.getInstance(SignupActivity.this).addToRequestQue(sendtoserver);

    }


}
