<template>
    <div id="app">
        <button class = "Back" @click.prevent="Back">{{ backbutton }}</button>
        <div class="timer">Timer : [ <span id="minutes">0:</span><span id="seconds">0:</span><span id=decimas>0</span> ] </div>
        <div class="score">Score : {{ gamescore }}</div>
        <div class="startcount">{{ count }}</div>
        <grid></grid>
        <button class="GameButton" @click.prevent="button">{{ buttonname }}</button>
        <!--Timeout 팝업-->  
        <div class="gamepopup" v-if="gametimeout">
            <div class="gamepopup-content gamecard">
                <h1 class="gamepopuptitle">Timeout!</h1>
                <p class="gamepopupitemtext">{{this.informationmessage}}</p>
                <button class="gamepopupbutton" @click.prevent="popupclose">Close</button>
            </div>
        </div>
    </div>
</template>

<script>
import Grid from './Grid.vue';
export default {
  components: { Grid },
  name: 'app',
  data() {
        return {
            scoretime: 180,
            gamescore: 0,
            gametimeout: false,
            informationmessage: 'The time limit is 3 minutes!!!',
            backbutton: 'Back!',
            buttonname: 'Start!',
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
    Back: function() {
        location.reload();
    },
    reset: function(){
        //reset
        this.scoretime = 180;
        this.gamescore = 0;
        clearInterval(this.decimasScoreTimer);
        this.minutesText = document.querySelector('#minutes');
        this.secondsText = document.querySelector('#seconds');
        this.decimasText = document.querySelector('#decimas');
        this.buttonname = 'Start!'; //버튼이름 초기화
        this.count = 3; //시작카운트 초기화
        this.decimas = 0;
        this.seconds = 0;
        this.minutes = 0;
        this.decimasText.textContent = 0;
        this.secondsText.textContent = 0 + ':';
        this.minutesText.textContent = 0 + ':';
        clearInterval(this.decimasTimer);
        document.querySelector('.startcount').style.visibility = 'visible';
        Event.$emit('GameReset');
    },
    popupclose: function(){
        //reset
        this.gametimeout = false;
        this.reset();
    },
    timer : function() {
        //게임플레이 시간을 계산하는 타이머
        this.decimasTimer = setInterval(() => {

            if(this.minutes == 3){
                clearInterval(this.decimasTimer);
                this.gametimeout = !this.gametimeout;
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
    button: function() {
      if (this.buttonname == 'Reset') {
          this.reset();
      } else if (this.buttonname == 'Start!') {
        document.querySelector('.GameButton').style.visibility = 'hidden';
        this.buttonname = 'Reset';

        this.countdown = setInterval(() => {
          if (this.count > 0) {
            this.count--;
          } else if (this.count == 0) {
            document.querySelector('.startcount').style.visibility = 'hidden';
            document.querySelector('.GameButton').style.visibility = 'visible';   
            clearInterval(this.countdown);
            Event.$emit('standardrandom', 'createcell');
            this.timer(); //타이머 작동시작
            this.socoretimer(); //ScoreTimer 작동시작
          }
        }, 1000);
      }
    }
  },
  created() {
    //back 키 눌렀을때 경고창 무시
    window.onbeforeunload = function() { 
        window.close();
    };

    //user가 터치한 번호를 점수 계산함
    Event.$on('touchnumber', number => {
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

    //50번까지 버튼을 다눌렀을때 타이머를 멈춤
    Event.$on('timerstop', () => {
        clearInterval(this.decimasTimer);
        clearInterval(this.decimasScoreTimer);
    });
  }
};
</script>

<style>
.gamepopupbutton{
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
.GameButton {
  background-color: #e74c3c;
  color: #fff;
  border-bottom-left-radius: 30px;
  border-bottom-right-radius: 30px;
  font-family: 'Dosis', Helvetica, sans-serif;
  font-size: 5vh;
  font-weight: bold;
  width: 100%;
  height:8vh;
}
.GameButton:hover {
  background-color: #c0392b;
  cursor: pointer;
}
.startcount {
  font-family: 'Dosis', Helvetica, sans-serif;
  height: 457px;
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
.Back {
  font-family: 'Dosis', Helvetica, sans-serif;
  font-weight: bold;
  border-collapse: collapse;
  background-color: #28bc1e;
  color: #fff;
  border-top-left-radius: 30px;
  border-top-right-radius: 30px;
  font-size: 5vh;
  width: 100%;
  height:8vh;
}
.gamepopupclose{
  color: #000000;
  border: 0px;
  border-top-left-radius: 10px;
  border-top-right-radius: 10px;
  border-bottom-left-radius: 10px;
  border-bottom-right-radius: 10px;
  font-family: 'Dosis', Helvetica, sans-serif;
  font-weight: bold;
  margin: 1px;
  font-size: 4vh;
  padding: 5px;
}
.gamepopupitemtext{
  color: #f00;
  margin-bottom: 16px;
  font-size: 4vh;
  font-weight: bold;
  width: 100%;
  text-align: center;
  margin: 12px;
  box-sizing: border-box;
}
.gamepopupitemimage{
  width: 30%;
  text-align: left;
  display: flex;
  align-items: center;
}
.gamepopupitem{
  margin-bottom:15px;
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  box-sizing: border-box;
}
.gamecard {
  max-height:50vh ;
  width:80vh;
  background: #292c8e;
  border-radius: 0 0 24px 24px;
  box-shadow: 2px 2px 35px 1px #4a4747;
  padding-bottom: 10px;
}
.gamepopup-content {
  max-width: 50vh;
  border-radius: 8px;
  position: relative;
  flex-direction: column;
}
.gamepopuptitle {
  margin: 10px;
  font-size: 4vh;
  font-weight: 900;
  text-align: center;
}
.gamepopup {
  color: #fff;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  position: fixed;
  padding-top: 140px;
  align-items: flex-start;
  justify-content: center;
  background: rgba(0,0,0,.5);
}
</style>
