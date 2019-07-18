const constants = require('../constants/constants');

module.exports = {
    rankresult: function (accountname,timeout,exit,touchlistcheck,gamescore,playtime,starttime,endtime){
        return new Promise((resolve, reject) => {
            constants.api.transact({
                actions: [
                    {
                        account: 'eosfastclick',
                        name: 'pushresult',
                        authorization: [
                            {
                                actor: 'eosfastclick',
                                permission: 'active',
                            }
                        ],
                        data: {
                            user: accountname,
                            timeout: timeout,
                            forcequit: exit,
                            touchlistcheck: touchlistcheck,
                            game_score: gamescore,
                            play_time: playtime,
                            start_time: starttime,
                            end_time: endtime,
                        },
                    },
                ],
            }, {
                blocksBehind: 3,
                expireSeconds: 30
            })
            .then((response) => {
                resolve("Success")
            })
            .catch (e =>  {
                console.log('\nCaught exception: ' + e);
                reject("Error")
            })
        })
    },
    rankreward: function (){
        return new Promise((resolve, reject) => {
            constants.api.transact({
                actions: [
                    {
                        account: 'eosfastclick',
                        name: 'rankreward',
                        authorization: [
                            {
                                actor: 'eosfastclick',
                                permission: 'active',
                            }
                        ],
                        data: {
                        },
                    },
                ],
            }, {
                blocksBehind: 3,
                expireSeconds: 30
            })
            .then((response) => {
                resolve("Success")
            })
            .catch (e =>  {
                console.log('\nCaught exception: ' + e);
                reject("Error")
            })
        })
    },
    deletedata: function (tablename,limit){
        return new Promise((resolve, reject) => {
            constants.api.transact({
                actions: [
                    {
                        account: 'eosfastclick',
                        name: 'deletedata',
                        authorization: [
                            {
                                actor: 'eosfastclick',
                                permission: 'active',
                            }
                        ],
                        data: {
                            table: tablename,
                            limit: limit
                        },
                    },
                ],
            }, {
                blocksBehind: 3,
                expireSeconds: 30
            })
            .then((response) => {
                resolve("Success")
            })
            .catch (e =>  {
                console.log('\nCaught exception: ' + e);
                reject("Error")
            })
        })
    },
    pushrankinfo: function (start_time,end_time,reward,bet_asset,status,limit){
        return new Promise((resolve, reject) => {
            constants.api.transact({
                actions: [
                    {
                        account: 'eosfastclick',
                        name: 'pushrankinfo',
                        authorization: [
                            {
                                actor: 'eosfastclick',
                                permission: 'active',
                            }
                        ],
                        data: {
                            start_time: start_time,
                            end_time: end_time,
                            reward: reward,
                            bet_asset: bet_asset,
                            status: status,
                            limit: limit
                        },
                    },
                ],
            }, {
                blocksBehind: 3,
                expireSeconds: 30
            })
            .then((response) => {
                resolve("Success")
            })
            .catch (e =>  {
                console.log('\nCaught exception: ' + e);
                reject("Error")
            })
        })
    },
}