const express = require("express");
const api = express.Router();
const smartContractActions = require('./eosSmartContractActions');
const GetTable = require('./eosGetTable');
const schedule = require('node-schedule');

let rankgameRewardAction; //rankgamerewardaction
let rankgamefinishTime; //rankgameendtime
let rankgameinfo; //rankgameinfo

let accountname; //account_name
let timeout; //timeout check
let exit; //강제종료 check
let touchlistcheck = false; //player가 랜덤숫자를 순서대로 클릭했는지 여부 
let starttime; //게임시작시간
let endtime; //게임종료시간
let gamescore; //게임 점수
let playtime = 0; //playtime

//사용자로부터 랭크게임 결과를 받음 
api.post('/rankgame', function (req, res) {
    accountname = req.body.accountname;
    timeout = req.body.timeout;
    exit = req.body.exit;
    gamescore = req.body.gamescore;
    starttime = req.body.starttime;
    endtime = req.body.endtime;

    //player가 랜덤숫자를 순서대로 클릭했는지 여부 check
    for(var number = 0; number<req.body.touchlist.length; number++){
        if(number+1 != req.body.touchlist[number]){
            touchlistcheck = true;
        }
    }  

    if(touchlistcheck == true){
        //사용자에게 결과값 돌려주기 - 부정행위
        res.send('Cheating');
    }else if(timeout == true){
        console.log("Timeout");
        res.send('Timeout');
    }else if(exit == true){
        console.log("exit");
        res.send('Exit');
    }else{
        //time 조작 체크
        playtime = (endtime - starttime)/1000;
        if(180 < playtime){
            timeout = true;
            console.log("Timeout Error");
            res.send('Timeout Cheating');
        }
    }

    smartContractActions.rankresult(accountname,timeout,exit,touchlistcheck,gamescore,playtime,starttime,endtime).then((msg) => {
        if(msg == "Success"){
            res.send('Success');
        }else{
            res.send('Error');
        }
    });
    console.log(
        "Name:"+accountname+" / "+
        "Timeout:"+timeout+" / "+
        "Exit:"+exit+" / "+
        "Game_Score:"+gamescore+" / "+
        "Touchcheck:"+touchlistcheck+" / "+
        "StartTime:"+starttime+" / "+
        "EndTime:"+endtime+" / "+
        "PlayTime:"+playtime
    );
});

//rankgameinfo를 1시간마다 받아오는 schedule - 테스트를 위해 1분
schedule.scheduleJob('*/1 * * * *', function(){
    GetTable.getrankinfotable().then((msg) => {
        if(msg[0] === "Success" && msg[1].rows[0] != null){
            if(msg[1].rows[0].status === "wait"){
                //rankgameinfo
                rankgameinfo = msg[1].rows[0];

                //rankgameinfo의 게임 끝나는 시간을 받아옴
                //docker-compose up 로 패키지를 돌리는 경우는 new Date() 값이 한국시간 으로 나오지만 
                //backend 폴더에서 node app.js 로 따로 돌리는 경우에는 GMT+0900 (한국 표준시) 로 나오게된다
                //앱구동을 편리하게 하기위해 docker 패키지화를 했음으로 서버 타임존은 그냥 GMT를 사용하기로 한다.
                rankgamefinishTime = new Date(20+(rankgameinfo.end_time).substring(0,2),(rankgameinfo.end_time).substring(2,4)-1,(rankgameinfo.end_time).substring(4,6),(rankgameinfo.end_time).substring(6,8)-9,(rankgameinfo.end_time).substring(8,10),00);

                //게임종료시간 schedule이 설정되있지않으면 schedule을 등록함 
                if(rankgameRewardAction == null ){
                    //보상 처리 schedule
                    rankgameRewardAction = schedule.scheduleJob(rankgamefinishTime, function(){
                        //보상액션 실행
                        smartContractActions.rankreward().then((msg) => {
                            if(msg == "Success"){
                                //rankgame info close
                                smartContractActions.pushrankinfo(rankgameinfo.start_time,rankgameinfo.end_time,rankgameinfo.reward,rankgameinfo.bet_asset,"close",rankgameinfo.key).then((msg) => {
                                    if(msg == "Success"){       
                                        //rank 기록삭제
                                        smartContractActions.deletedata("rank",100).then((msg) => {
                                            if(msg == "Success"){       
                                                console.log("RankGame Success!");
                                            }else{
                                                console.log('deletedataError');
                                            }
                                        });
                                    }else{
                                        console.log('rankgamefinishError');
                                    }
                                });
                            }else{
                                console.log('RewordError');
                            }
                            rankgameRewardAction = null; //보상스케줄지우기
                        });
                    });
                }
            }
        }
    });
}); 

// 86400000ms는 1day를 의미한다.
// 1s = 1,000ms
// 1m = 60s * 1,000ms = 60,000ms
// 1h = 60m * 60,000ms = 3,600,000ms
// 1d = 24h * 3,600,000ms = 86,400,000ms

module.exports = api;