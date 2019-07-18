<template>
    <div id="app" data-app>
        <v-layout row justify-center>
            <v-dialog v-model="systemprogress" persistent content content-class="centered-dialog">
            <v-container fill-height>
                <v-layout column justify-center align-center>
                <v-progress-circular indeterminate :size="100" :width="10" :color="progressColor"></v-progress-circular>
                <h1 v-if="message != null">{{message}}</h1>
                </v-layout>
            </v-container>
            </v-dialog>
        </v-layout>
        <component @rankgamestart="rankgamestart" @login="login" @logout="logout" :is="SwichView"/>
    </div>
</template>
<script>
import { Api, JsonRpc, RpcError } from 'eosjs';
import ScatterJS from 'scatterjs-core';
import ScatterEOS from 'scatterjs-plugin-eosjs2';
import ScatterLynx from 'scatterjs-plugin-lynx';
import constants from './constants/constants';
import axios from 'axios';

ScatterJS.plugins(new ScatterEOS(), new ScatterLynx({ Api, JsonRpc }));

const network = ScatterJS.Network.fromJson(constants.network);
const rpc = new JsonRpc(network.fullhost());

let eos;

window.ScatterJS = null;

export default {
    name: 'app',
    components:{
        'Login': () => import('./components/Login'),
        'FreeGame': () => import('./components/FreeGame'),
        'RankGame': () => import('./components/RankGame'),
        'RankView': () => import('./components/RankView')
    },
    data() {
        return {
            systemprogress: this.value,
            scatter: null,
            usereos: ''
        };
    },
    props: {
        value: {type: Boolean, default: false},
        message: {type: String, default: "wait..."},
        progressColor: {type: String, default: 'red'},
    },
    computed:{
        SwichView (){
            switch (this.$store.state.view) {
                case 'FreeGame':
                    return 'FreeGame'
                case 'RankGame':
                    return 'RankGame'
                case 'Login':
                    return 'Login'
                case 'RankView':
                    return 'RankView'
            }
        },
    },
    methods: {
        rankgameinfo() {
            //info 요청
            const url = 'https://eos.greymass.com:443/v1/chain/get_table_rows';
            const params = '{"scope":"eosfastclick","code":"eosfastclick","table":"rankgameinfo","json":true,"index_position":"2","key_type":"i64","lower_bound":"wait","upper_bound":"wait","table_key":"","limit":1}';
            const configs = {'content-type': 'application/x-www-form-urlencoded; charset=UTF-8'};
             axios.post(url, params, configs)
             .then((response) => {
                 if(response.data.rows[0] != null){
                     this.$store.commit('RankGameInfo', response.data.rows[0]);
                 }
             })
             .catch(e => {
                 console.log(e);
             });
        },
        logout() {
            this.scatter.logout();
            this.$store.commit('LoginStatus', false);
        },
        login: async function(){
            if(this.$store.state.loggedIn === true){
            }else{
                await ScatterJS.scatter.connect("Random1to50").then(connected => {
                if (!connected) return alert('Failed to connect with Scatter!');
                this.scatter = ScatterJS.scatter;
                });

                await this.scatter.getIdentity({ accounts: [network] }).then(() => {
                this.account = this.scatter.identity.accounts.find(
                    e => e.blockchain === 'eos'
                );
                });
                
                if (this.account === null) return alert('If you are entering through mobile browser, go to EOS Lynx mobile app > explore page > enter');
                
                this.eosinstance(); 
                this.getuserinfo();
            }
        },
        rankgamestart() {
            this.systemprogress = true;

            if(! this.account){
                alert('Account not connected');
                this.systemprogress = false;
                return;
            }

            eos.transact({
                actions: [
                {
                    account: 'eosio.token',
                    name: 'transfer',
                    authorization: [
                    {
                        actor: this.account.name,
                        permission: this.account.authority,
                    }
                    ],
                    data: {
                        from: this.account.name,
                        to: 'eosfastclick',
                        quantity: this.$store.state.rankgamebet,
                        memo: 'rankgame betting'+this.$store.state.rankgamebet,
                    },
                },
                ],
            }, {
                blocksBehind: 3,
                expireSeconds: 30
            })
            .then((response) => {
                this.systemprogress = false;
                this.$store.commit('SwichView', 'RankGame');
            })
            .catch (e =>  {
                this.systemprogress = false;
                if (e instanceof RpcError) {
                if (e.json && e.json.code && e.json.code === 500) {
                    if (e.json.error.code === 3050003) {
                        alert('You have insufficient balance');
                    }
                }
                }
            });
        },
        eosinstance () {
            eos = this.account ? this.scatter.eos(network, Api, { beta3: true, rpc }, 'http') : new Api({ rpc });
        },
        getuserinfo () {
            this.$store.commit('AccountName', this.account.name);
            const account_name = this.account.name
            const url = 'https://eos.greymass.com:443/v1/chain/get_account';
            const params = { account_name };
            const configs = { XMLHttpRequestResponseType: "JSON" };
            axios.post(url, params, configs)
            .then((response) => {
            this.usereos = response.data.core_liquid_balance.replace(/[^\'\c0-9]/gi,'');
            this.$store.commit('UserEOS', this.usereos);
            })
            .catch(e => {
            console.log(e);
            })
            this.$store.commit('LoginStatus', true);    
        }
    },
    created(){
        this.rankgameinfo();
        window.onbeforeunload = () => 'Are you sure?';
        this.login();
    }
};
</script>
<style>
.dialog.centered-dialog,
.v-dialog.centered-dialog
{
    text-align: center;
    min-width: 100%;
    min-height: 100%;
    background: #282c2dad;
    box-shadow: none;
    color: #fff;
}
body {
    background-color: #fff;
    color: #fff;
    font-family: 'Dosis', Helvetica, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: center;
    margin: 0px;
    background-image: url(http://211.249.62.8/eosgameimage/background.jpg);
}
#app {
    background: #fff;
    margin: 0 auto;
    max-width: 700px;
    min-height: 500px;
    max-height: 700px;
    color: #34495e;
    border-top-left-radius: 30px;
    border-top-right-radius: 30px;
    border-bottom-left-radius: 30px;
    border-bottom-right-radius: 30px;
}
</style>