import axios from 'axios';
const accountname = 'hongkyunpark';
const timeout = 'false';
const gamescore = '2324';
const exit = 'false';
const starttime = '21435346';
const endtime = '124235263';


        const url = 'http://211.249.62.8:3000/api/rankgame';
        const params = { 
            accountname,
            timeout,
            exit,
            gamescore,
            starttime,
            endtime
        };
        const configs = { XMLHttpRequestResponseType: "JSON" };
        axios.post(url, params, configs)
        .then((response) => {
          console.log(response);
          /*  if(response.statusText == "RankAddSuccess"){
                setTimeout(() => {this.$store.commit('SwichView', 'Login');}, 3000);
            }else{
                console.log("rankgame result Send Error:"+response.statusText)
            } */
        })
        .catch(e => {
            console.log(e);
        });