package com.example.pado0.rabbitfight;

public class ChattingSingleton {

    /*
    Multi-Thread의 동기화문제를 해결하면서도 성능저하를 발생시키지않는 Initialization on demand holder idiom 방식의 싱글톤
    */

    private String ip; //ip 정보
    private Integer port; //port 정보

    private ChattingSingleton(){
        //생성자정보
        ip = "211.249.62.8"; //서버 아이피 정보
        port = 8080; //서버 포트 정보
    }

    private  static class ChattingSigletonHolder {
        public static final ChattingSingleton INSTANCE = new ChattingSingleton();
    }

    public static ChattingSingleton getInstance(){
        return ChattingSigletonHolder.INSTANCE;
    }

    public String getIp(){
        return ip;
    }

    public Integer getPort(){
        return port;
    }
}
