import Vue from 'vue';
import Vuex from 'vuex';
import constants from './constants/constants';

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    rankgametimeinfo: 'Comming Soon',
    rankgamereward: '',
    rankgamebet: '',
    rankgameplaytime: '',
    rankgamestatus: 'Wait',
    loggedIn: false,
    view: 'Login',
    accountname: 'Login',
    usereos: ''
  },
  mutations: {
    RankGameInfo(state, info){  
        if(info != null){
            state.rankgamereward = info.reward;
            state.rankgamebet = info.bet_asset;

            //root 유저가 정한 게임 시작 시간
            let rankgamestarttime = new Date(20+(info.start_time).substring(0,2),(info.start_time).substring(2,4)-1,(info.start_time).substring(4,6),(info.start_time).substring(6,8),(info.start_time).substring(8,10));
            
            //root 유저가 정한 게임 종료 시간 
            let rankgameendtime = new Date(20+(info.end_time).substring(0,2),(info.end_time).substring(2,4)-1,(info.end_time).substring(4,6),(info.end_time).substring(6,8),(info.end_time).substring(8,10));
                
            //rankgame playtime
            state.rankgameplaytime = (rankgameendtime.getTime()-rankgamestarttime.getTime())/60000;

            //서버에서 값을 받아왔다면 1초마다 타이머를 갱신
            setInterval(() => {     
                //시간차 구하기
                let starttime = new Date(rankgamestarttime.getTime() - new Date().getTime());
                let endtime = new Date(rankgameendtime.getTime() - new Date().getTime());
                  
                //현재시간과 비교하여 시작까지남은 시간 or 끝날때까지 남은 시간을 계산
                if(rankgamestarttime.getTime() > new Date().getTime()){
                    //시작까지 남은 시간이 1일이 넘어가는 경우
                    if((starttime.getDate()-1) > 0){
                        state.rankgametimeinfo = "Start in " + (starttime.getDate()-1) + "Day "+(starttime.getHours()-9) + "h " + starttime.getMinutes()+"m " + starttime.getSeconds()+"s";
                    }else{
                        state.rankgametimeinfo = "Start in " + (starttime.getHours()-9) + "h " + starttime.getMinutes()+"m " + starttime.getSeconds()+"s";
                    }
                }else if(new Date().getTime() > rankgamestarttime.getTime() && new Date().getTime() < (rankgameendtime.getTime() - 180000)){
                    //rankgameplaystatus
                    if(state.rankgamestatus == "Wait"){
                        state.rankgamestatus = "Start";
                    }          
                    state.rankgametimeinfo = "End in " + (endtime.getHours()-9) + "h " + (endtime.getMinutes()-3)+"m " + starttime.getSeconds()+"s";
                }else{
                    if(state.rankgamestatus == "Start"){
                        state.rankgamestatus = "Wait";
                    }else if(new Date().getTime() > rankgameendtime.getTime()){
                        state.rankgametimeinfo = "Comming Soon";
                    }else{
                        state.rankgametimeinfo = "Reward is given at " + rankgameendtime.getHours() + "h " + rankgameendtime.getMinutes()+"m " + rankgameendtime.getSeconds()+"s";
                    }
                }
            },1000);
        }
    },
    LoginStatus(state, status) {
        state.loggedIn = status;
      if(state.loggedIn === false){
        state.view = 'Login';
        state.accountname = 'Login';
        state.usereos = '';
      }
    },
    SwichView(state, view){
      state.view = view;
    },
    AccountName(state, accountname){
      state.accountname = accountname;
    },
    UserEOS(state, usereos){
      state.usereos = usereos;
    }
  },
  getters: {
    loggedIn: state => state.loggedIn,
    view: state => state.view,
    accountname: state => state.accountname,
    usereos: state => state.usereos,
    rankgametimeinfo: state => state.rankgametimeinfo,
    rankgamereward: state => state.reward,
    rankgamebet: state => state.bet_asset,
    rankgamestatus: state => state.rankgamestatus,
    rankgameplaytime: state => state.rankgameplaytime
  },
  actions: {}
});
