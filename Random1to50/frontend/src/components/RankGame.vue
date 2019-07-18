<template>
    <div id="app">
        <div class="timer">Timer : [ <span id="minutes">0:</span><span id="seconds">0:</span><span id=decimas>0</span> ] </div>
        <div class="score">Score : {{ gamescore }}</div>
        <div class="startcount">{{ count }}</div>
        <grid></grid>
        <!--팝업-->  
        <div class="rankgamepopup" v-if="popup">
            <div class="rankgamepopup-content rankgamecard">
                <h1 class="rankgamepopuptitle">{{this.informationtitle}}</h1>
                <p class="rankgamepopupitemtext">{{this.informationmessage}}</p>
                <button class="rankgamepopupitemtext" v-if="this.informationtitle == 'GameWarning!'"></button>
                <button class="rankgamepopupitemtext" v-else-if="this.informationtitle == 'Wait!'"></button>
                <button class="rankgamepopupbutton" v-else @click.prevent="reset">OK!</button>
            </div>
        </div>
    </div>    
</template>

<script>
import Grid from './Grid.vue';
import axios from 'axios';
export default {
components: { Grid },
name: 'app',
data() {
    return {
        gameplaystatus: false,
        scoretime: 180,
        gamescore: 0,
        starttime: 0,
        endtime: 0,
        touchnumberlist: [],
        timeout: false,
        exitselection: false,
        popup: false,
        informationmessage: '',
        informationtitle:'',
        count: 3,
        countdown: null,
        minutesText: document.querySelector('#minutes'),
        secondsText: document.querySelector('#seconds'),
        decimasText: document.querySelector('#decimas'),
        decimas: 0,
        seconds: 0,
        minutes: 0,
        decimasTimer: null,
        decimasScoreTimer:null
    };
},
methods: {
    reset: function(){
        document.location.reload(true);
    },
    sendserver : function() {
        this.informationtitle = 'Wait!';
        this.informationmessage = 'Loading game results ...';
        this.popup = !this.popup; //팝업창띄우기
        
        const accountname = this.$store.state.accountname;
        const timeout = this.timeout;
        const gamescore = this.gamescore;
        const exit = this.exitselection;
        const touchlist = this.touchnumberlist;
        const starttime = this.starttime;
        const endtime = this.endtime;

        const url = 'http://211.249.62.8:3000/api/rankgame';
        const params = { 
            accountname,
            timeout,
            exit,
            touchlist,
            gamescore,
            starttime,
            endtime
        };
        const configs = { XMLHttpRequestResponseType: "JSON" };
        axios.post(url, params, configs)
        .then((response) => {

            if(response.data == "Success"){
                if(this.timeout == false && this.exitselection == false){
                    this.informationtitle = 'GameClose!';
                    this.informationmessage = 'Your records can be found in the ranking.';
                }else if(this.timeout != false){
                    this.informationtitle = 'Timeout!';
                    this.informationmessage = 'The time limit is 3 minutes!!!';
                }else if(this.exitselection != false){
                    this.informationtitle = 'Cheating!';
                    this.informationmessage = 'You tried to refresh or go back You can not find a record in the rank!!!';
                }
                this.gameplaystatus = true; //게임플레이 status
            }else{
                this.informationtitle = 'Error!';
                this.informationmessage = "An error occurred in the network environment. I'm sorry. I will contact the manager to take action. email: pado0007@naver.com";
                this.gameplaystatus = true; //게임플레이 status
                console.log("rankgame result Send Error:"+response.data)
            } 
        })
        .catch(e => {
            console.log(e);
        });
    },
    timer : function() {
        //게임플레이 시간을 계산하는 타이머
        this.decimasTimer = setInterval(() => {
            if(this.minutes == 3){
                clearInterval(this.decimasTimer);
                this.endtime = Date.now();
                this.timeout = true;
                this.exitselection = false;
                this.sendserver(); //서버로 결과전송
            }

            this.minutesText = document.querySelector('#minutes');
            this.secondsText = document.querySelector('#seconds');
            this.decimasText = document.querySelector('#decimas');   

            if (this.decimas < 10) {
                this.decimas++;
                if (this.decimas === 10) {
                    this.decimas = 0;
                    this.seconds++;
                }
                if (this.seconds === 60) {
                    this.seconds = 0;
                    this.minutes++;
                }
            }
            this.decimasText.textContent = this.decimas;
            this.secondsText.textContent = this.seconds + ':';
            this.minutesText.textContent = this.minutes + ':';           
        }, 100);
    },
    socoretimer : function() {
        //score점수 계산하는 타이머
        this.decimasScoreTimer = setInterval(() => {
            if(this.scoretime == 0){
                clearInterval(this.decimasScoreTimer);
            }else{
                this.scoretime--;
            }
        }, 1000);
    },
    gamestart: function() {
        this.countdown = setInterval(() => {
            if (this.count > 0) {
                this.count--;
            } else if (this.count == 0) {
                document.querySelector('.startcount').style.visibility = 'hidden';
                clearInterval(this.countdown);
                Event.$emit('standardrandom', 'createcell');
                this.timer(); //타이머 작동시작
                this.socoretimer(); //ScoreTimer 작동시작
                this.starttime = Date.now();
            }
        }, 1000);  
    }
},
created() {
    //gamestart defore infomessage
    this.informationtitle = 'GameWarning!';
    this.informationmessage = 'If you go back or refresh, the game ends immediately and is not reflected in the Ranking!!!';
    this.popup = !this.popup;

    //새로고침 or 뒤로가기
    window.onbeforeunload = () => {
        if(this.gameplaystatus == false){
            this.exitselection = true;

            if(this.starttime == 0){
                this.starttime = Date.now();
                this.endtime = Date.now();
            }else{
                this.endtime = Date.now();
            }
            this.sendserver(); //서버로 결과전송
        }
    };

    //gamestart
    setTimeout(() => {
        this.popup = false;
        //카운트다운 시작
        this.gamestart(); 
    }, 3000);  

    //50번까지 버튼을 다눌렀을때 타이머를 멈춤
    Event.$on('timerstop', () => {
        clearInterval(this.decimasTimer);
        clearInterval(this.decimasScoreTimer);
        this.endtime = Date.now();
        this.exitselection = false;
        this.sendserver(); //서버로 결과전송
    });

    //user가 올바르게 터치한 number를 받아 리스트에 저장함 
    Event.$on('touchnumber', number => {
        this.touchnumberlist.push(number);
        //score 계산
        if(this.scoretime > 175){
            this.gamescore = this.gamescore+(number*7);
        }else if(this.scoretime > 170){
            this.gamescore = this.gamescore+(number*6);
        }else if(this.scoretime > 160){
            this.gamescore = this.gamescore+(number*5);
        }else if(this.scoretime > 140){
            this.gamescore = this.gamescore+(number*4);
        }else if(this.scoretime > 130){
            this.gamescore = this.gamescore+(number*3);
        }else if(this.scoretime > 110){
            this.gamescore = this.gamescore+(number*2);
        }else if(this.scoretime > 0){
            this.gamescore = this.gamescore+(number*1);
        }
    }); 
    
}

};
</script>

<style>
.rankgamepopupbutton{
  background-color: #28bc1e;
  color: #fff;
  border: 0px;
  border-top-left-radius: 10px;
  border-top-right-radius: 10px;
  border-bottom-left-radius: 10px;
  border-bottom-right-radius: 10px;
  font-family: 'Dosis', Helvetica, sans-serif;
  font-weight: bold;
  margin: 1px;
  font-size: 2em;
  width: 250px;
  height: 50px;
  padding: 5px;
}
.score {
  line-height: 150%;
  color: #f00;
  font-size: 5vh;
  height:8vh;
  width: 100%;
  font-weight: bold;
  background-color: #2b3db5;
  font-family: 'Dosis', Helvetica, sans-serif;
}
.timer {
  line-height: 150%;
  color: #fff;
  font-size: 5vh;
  height:8vh;
  width: 100%;
  font-weight: bold;
  background-color: #f1c40f;
  font-family: 'Dosis', Helvetica, sans-serif;
}
.startcount {
  font-family: 'Dosis', Helvetica, sans-serif;
  height: 456.5px;
  vertical-align: middle;
  max-width: 700px;
  position: absolute;
  background-color: #ffffff;
  opacity: 0.5;
  text-align: center;
  width: 100%;
  font-size: 40vh;
  font-weight: bold;
  color: #fb0303;
}
.rankgamepopupclose{
  color: #fff;
  border: 0px;
  border-top-left-radius: 10px;
  border-top-right-radius: 10px;
  border-bottom-left-radius: 10px;
  border-bottom-right-radius: 10px;
  font-family: 'Dosis', Helvetica, sans-serif;
  font-weight: bold;
  margin: 1px;
  font-size: 2em;
  width: 250px;
  height: 50px;
  padding: 5px;
}
.rankgamepopupitemtext{
  color: #f00;  
  margin-bottom: 16px;
  font-size: 4vh;
  align-items: center;
  font-weight: bold;
  margin: 12px;
  box-sizing: border-box;
}
.rankgamepopupitemimage{
  width: 30%;
  text-align: left;
  display: flex;
  align-items: center;
}
.rankgamepopupitem{
  margin-bottom:15px;
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  box-sizing: border-box;
}
.rankgamecard {
  max-height:400px ;
  width:500px;
  background: #292c8e;
  border-radius: 0 0 24px 24px;
  box-shadow: 2px 2px 35px 1px #4a4747;
  padding-bottom: 10px;
}
.rankgamepopup-content {
  max-width: 280px;
  border-radius: 8px;
  position: relative;
  flex-direction: column;
}
.rankgamepopuptitle {
  margin: 10px;
  font-size: 24px;
  font-weight: 900;
  text-align: center;
}
.rankgamepopup {
  color: #fff;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  position: fixed;
  padding-top: 120px;
  align-items: flex-start;
  justify-content: center;
  background: rgba(0,0,0,.5);
}
</style>