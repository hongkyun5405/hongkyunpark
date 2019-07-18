const constants = require('../constants/constants');

module.exports = {
    getranktable: function(){
        return new Promise((resolve, reject) => {
            constants.rpc.get_table_rows({
                json: true,
                code: 'eosfastclick',
                scope: 'eosfastclick',
                table: 'rank',
                limit: -1,
            })
            .then((response) => {
                resolve(["Success", response]);
            })
            .catch (e =>  {
                console.log('\nCaught exception: ' + e);
                reject(["Error"]);
            })
        })
    },
    getrankinfotable: function(){
        return new Promise((resolve, reject) => {
            constants.rpc.get_table_rows({
                json: true,
                code: 'eosfastclick',
                scope: 'eosfastclick',
                table: 'rankgameinfo',
                index_position:'2',
                key_type:'i64',
                lower_bound: 'wait',
                upper_bound: 'wait',
                table_key:'',
                limit: 1,
            })
            .then((response) => {
                resolve(["Success", response]);
            })
            .catch (e =>  {
                console.log('\nCaught exception: ' + e);
                reject(["Error"]);
            })
        })
    }
}