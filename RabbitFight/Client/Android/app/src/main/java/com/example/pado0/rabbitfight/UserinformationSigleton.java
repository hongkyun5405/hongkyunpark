package com.example.pado0.rabbitfight;

public class UserinformationSigleton {

    /*
    Multi-Thread의 동기화문제를 해결하면서도 성능저하를 발생시키지않는 Initialization on demand holder idiom 방식의 싱글톤
    */


    private String useremail; //이메일 정보
    private String usernickname; //닉네임 정보
    private String userimage; //이미지 정보
    private String userloginvalue; //로그인정보
    private String userchannel; // 자신의 현재 채널 정보

    //생성자정보
    private UserinformationSigleton(){
        useremail = "";
        usernickname = "";
        userimage = "";
        userloginvalue = "";
        userchannel = "1";
    }

    //홀더 객체 생성
    private  static class userinformationSigletonHolder {
        public static final UserinformationSigleton INSTANCE = new UserinformationSigleton();
    }

    //싱글톤 인스턴스를 호출할때 사용되는 부분
    public static UserinformationSigleton getInstance(){
        return userinformationSigletonHolder.INSTANCE;
    }

    //이메일 정보 가져갈때
    public String getUseremail(){
        return useremail;
    }

    //이메일 정보 저장할때
    public synchronized void setUseremail(String useremail){
        this.useremail = useremail;
    }

    //닉네임 정보 가져갈때
    public String getUsernickname(){
        return usernickname;
    }

    //닉네임 정보 저장할때
    public synchronized void setUsernickname(String usernickname){
        this.usernickname = usernickname;
    }

    //이미지 정보 가져갈때
    public String getUserimage(){
        return userimage;
    }

    //이미지 정보 저장할때
    public synchronized void setUserimage(String userimage){
        this.userimage = userimage;
    }

    //로그인 정보 가져갈때
    public String getUserloginvalue(){
        return userloginvalue;
    }

    //이메일 정보 저장할때
    public synchronized void setUserloginvalue(String userloginvalue){
        this.userloginvalue = userloginvalue;
    }

    //자신의 현재 채널정보 가져갈때
    public String getUserchannel() {
        return userchannel;
    }

    //자신의 현재 채널정보 저장할때
    public synchronized void setUserchannel(String userchannel){
        this.userchannel = userchannel;
    }
}
