<template>
  <div id="app">
      <p class="firstinfo">Rankgame will {{ this.$store.state.rankgametimeinfo }}</p>
        <img src="http://211.249.62.8/eosgameimage/loginimg.png" width="55" height="55">
      <div>
        <b class="loginbuttontext" @click.prevent="userinfo">{{ this.$store.state.accountname }}</b>
      </div>
        <div class = "space"></div>
      <div>
        <button class = "mainbutton" style="background-color: #28bc1e;" @click.prevent="GameInfo">GameInfo!</button>
      </div>
        <div class = "space"></div>
      <div>
        <button class = "mainbutton" style="background-color: #ff7800;" @click.prevent="RankView">RankView!</button>
      </div>
        <div class = "space"></div>
      <div>
        <button class = "mainbutton" style="background-color: #3e1ebc;" @click.prevent="FreeGame">FreeGame!</button>
      </div>
        <div class = "space"></div>
      <div>
        <img @click.prevent="rankgameinfo" class="infoicon" src="http://211.249.62.8/eosgameimage/info.png">
        <button class = "mainbutton" style="background-color: #cd0fe2;" @click.prevent="RankGame">RankGame!</button>
      </div>
      <!--userinfo 팝업-->
      <div class="popup" v-if="userinfovisible">
        <div class="popup-content card">
            <button class="popupclose" @click.prevent="popupclose">x</button>
            <h1 class="popuptitle">UserInfo</h1>
            <div class="popupitem">
                <div class="popupitemimage">
                  <img alt="" src="http://211.249.62.8/eosgameimage/eoslogoitem.png" style="width: 60%;"><p style="margin: 12px; font-weight: bold; font-size: 3vh;">EOS</p> 
                </div>
                <span class="popupitemtext">{{ this.$store.state.usereos }}</span>
            </div>
            <div class="popupitem">
              <div class="popupitemimage">
                <img alt="" src="http://211.249.62.8/eosgameimage/userprofile.png" style="width: 60%;"><p style="margin: 12px; font-weight: bold; font-size: 3vh;">User</p>
                </div>
                <span class="popupitemtext">{{ this.$store.state.accountname }}</span>
            </div>
            <img @click.prevent="logout" alt="" src="http://211.249.62.8/eosgameimage/profile_logout.png" style="width: 30%;">
        </div></div>
      <!--RankGameinfo 팝업-->  
      <div class="popup" v-if="rankgameinfovisible">
        <div class="popup-content card">
          <button class="popupclose" @click.prevent="popupclose">x</button>
            <h1 class="popuptitle">RankingGame!</h1>
              <ul class="game-rules-list">
                <li class="game-rules-list-item">
                  <span class="game-rules-list-title">GameRule</span>
                  It runs for {{this.$store.state.rankgameplaytime-3}} Minutes and can only have one record per account.
                </li>
                <li class="game-rules-list-item">
                  <span class="game-rules-list-title">Reward</span>
                  The Total Reward : {{ this.$store.state.rankgamereward.substring(0,3) }} EOS is paid to 1,2,3 etc.
                  1st : 50% / 2nd : 30% / 3rd : 20%
                </li>
                <li class="game-rules-list-item">
                  <span class="game-rules-list-title">Cost</span>
                  The cost is {{ this.$store.state.rankgamebet.substring(0,3) }} EOS
                </li>
              </ul> 
        </div></div>
      <!--GameInfo 팝업-->  
      <div class="popup" v-if="gameinfo">
        <div class="popup-content card">
            <button class="popupclose" @click.prevent="popupclose">x</button>
            <h1 class="popuptitle">GameRule!</h1>
              <ul class="game-rules-list">
                <li class="game-rules-list-item">Pressing the 50th digit randomly from 1 to 50 in rapid succession will end the game.</li>
                <li class="game-rules-list-item">Press the last digit to stop the timer.</li>
                <li class="game-rules-list-item">Timeout: 3 Minutes</li>
                </br>
                <p>Score Bonus!</p>
                <p>180~175/s -> x7</p>
                <p>175~170/s -> x6</p>
                <p>170~160/s -> x5</p>
                <p>160~140/s -> x4</p>
                <p>140~130/s -> x3</p>
                <p>130~110/s -> x2</p>
                <p>110~0/s -> x1</p>
              </ul>
        </div></div>
      <!--안내문구 팝업-->  
      <div class="popup" v-if="informaionpopupvisible">
        <div class="popup-content card">
            <button class="popupclose" @click.prevent="popupclose">x</button>
            <h1 class="popuptitle">Informaion!</h1>
            <p style="font-weight: bold; color: #fff; font-size: 3vh; margin-top: 10px; margin-left: 5px; margin-right: 5px; align-items: center;">{{this.informationmessage}}</p>
        </div></div>
      <!--RankGame 팝업-->  
      <div class="popup" v-if="rankgamestartvisible">
        <div class="popup-content card">
            <button class="popupclose" @click.prevent="popupclose">x</button>
            <h1 class="popuptitle">RankGame!</h1>
            <p style="font-weight: bold; color: #fff; font-size: 3vh; margin-top: 10px; margin-left: 5px; margin-right: 5px; align-items: center;">If the rank record already exists, it will be overwritten!</p>
            <p style="font-weight: bold; color: #fff; font-size: 3vh; margin-top: 10px; align-items: center;">Price: {{ this.$store.state.rankgamebet.substring(0,3) }} EOS</p>
            <p style="font-weight: bold; color: #fff; font-size: 3vh; margin-top: 10px; align-items: center;">Timeout: 3 Minutes</p>
            <button class="rankgamestart" :disabled="this.$store.state.rankgamestatus === 'Wait'" @click.prevent="rankgamestart">{{ this.$store.state.rankgamestatus }}!</button>
        </div></div>
  </div>
</template>
<script>
export default {
  name: 'app',
  data() {
    return {
      rankgamelimittimer: '',
      rankgamestartvisible: false,
      informaionpopupvisible: false,
      gameinfo: false,
      userinfovisible: false,
      rankgameinfovisible: false,
      informationmessage: ''
    };
  },
  methods: {
    userinfo: function() {
        if(this.$store.state.loggedIn === false){
            this.$emit('login');
        }else{
            this.userinfovisible = !this.userinfovisible;
        }
    },
    logout() {
        this.userinfovisible = false;
        this.$emit('logout');
    },
    popupclose: function() {
      this.userinfovisible = false;
      this.rankgameinfovisible = false;
      this.gameinfo = false;
      this.informaionpopupvisible = false;
      this.rankgamestartvisible = false;
    },
    GameInfo: function() {
      this.gameinfo = !this.gameinfo;
    },
    FreeGame: function() {
      this.$store.commit('SwichView', 'FreeGame');
    },
    RankGame: function() {
      this.rankgamestartvisible = !this.rankgamestartvisible;
    },
    RankView: function() {
      this.$store.commit('SwichView', 'RankView');
    },
    rankgamestart: async function() {
      if(await this.$store.state.loggedIn === false) return this.rankgamestartvisible = false ,
      this.informationmessage = 'Please Login!' ,this.informaionpopupvisible = !this.informaionpopupvisible; 
     
      if(await this.$store.state.usereos >= this.$store.state.rankgamebet){
        //수정해야됨
        //this.$store.commit('SwichView', 'RankGame'); //테스트용 eos 전송안함
        this.$emit('rankgamestart');  
      }else{
        this.informationmessage = 'EOS is lacking!';
        this.informaionpopupvisible = !this.informaionpopupvisible;
      }
      this.rankgamestartvisible = false; 
    },
    rankgameinfo: function() {
      this.rankgameinfovisible = !this.rankgameinfovisible;
    }
  }
};
</script>
<style>
.infoicon{
  position: absolute;
  width: 40px;
  height: 40px;
  border-top-left-radius: 15px;
  border-top-right-radius: 10px;
  border-bottom-left-radius: 10px;
  border-bottom-right-radius: 10px;
}
.space{
  margin-top: 15px;
}
.game-rules-list-title {
  color: #fff;
  font-weight: bold;
  display: block;
  font-size: 3vh;
  font-weight: 900;
  margin-bottom: 8px;
}
.game-rules-list-item {
  font-weight: bold;
  color: #fff;
  font-size: 2.5vh;
  margin-top: 5px;
}
.game-rules-list {
  font-weight: bold;
  color: #fff;
  margin-top: 8px;
}
.popupitemtext{
  margin-bottom: 16px;
  font-size: 3vh;
  font-weight: bold;
  width: 55%;
  text-align: right;
  margin: 2.5vh;
  box-sizing: border-box;
}
.popupitemimage{
  width: 30%;
  text-align: left;
  display: flex;
  align-items: center;
}
.popupitem{
  margin-bottom:15px;
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  box-sizing: border-box;
}
.popupclose{
  font-family: 'Dosis', Helvetica, sans-serif;
  font-weight: bold;
  font-size: 40px;
  top: 0px;
  right: 15px;
  color: #000000;
  position: absolute;
}
.card {
  max-height:auto;
  width:500px;
  background: #292c8e;
  border-radius: 0 0 24px 24px;
  box-shadow: 2px 2px 35px 1px #4a4747;
  padding-bottom: 10px;
}
.popup-content {
  max-width: 50vh;
  border-radius: 8px;
  position: relative;
  flex-direction: column;
}
.popuptitle {
  margin: 10px;
  font-size: 4vh;
  font-weight: 900;
  text-align: center;
}
.popup {
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
.loginbuttontext{
  font-size: 4vh;
  font-weight: bold;
  text-align: center;
  color: #f90d0d;
}
.firstinfo{
  font-size: 5vh;
  text-align: center;
  color: #fff;
  font-family: 'Dosis', Helvetica, sans-serif;
  font-weight: bold;
  background-color: #0cfaf2;
  border-top-left-radius: 10px;
  border-top-right-radius: 10px;
  border-bottom-left-radius: 10px;
  border-bottom-right-radius: 10px;
}
.mainbutton{
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
.rankgamestart{
  background: #f00;
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
  width: 100px;
  height: 40px;
}
</style>