<template>
    <div id="rankview">
        <button class = "Back" @click.prevent="Back">{{ backbutton }}</button>
        <div class="category">
            <span class="category-Rank">Rank</span>
            <span class="category-User">User</span>
            <span class="category-Time">Time</span>
            <span class="category-Score">Score</span>
        </div>       
        <div class = "list" v-for="(item, index) in paginatedData">
            <span class="list-left">{{(index+1)+(pageNum*pageSize)}}</span>
            <span class="list-left">{{rankinfo[index].user}}</span>
            <span class="list-right">{{rankinfo[index].play_time.substring(0,5)}}</span>
            <span class="list-right">{{rankinfo[index].game_score}}</span>
        </div>
        <p class = "infomessage" v-if="infomessage">Try a RankGame !!!</p>
        <div class="btn-cover">
          <button :disabled="pageNum === 0" @click="prevPage" class="page-btn">
            이전
          </button>
          <span class="page-count">{{ pageNum + 1 }} / {{ pageCount }} </span>
          <button :disabled="pageNum >= pageCount - 1" @click="nextPage" class="page-btn">
            다음
          </button>
        </div>
    </div>
</template>

<script>
import axios from 'axios';
export default {
  name: 'app',
  data() {
        return {
            infomessage: true,
            pageNum: 0,
            rankinfo: [],
            backbutton: 'Back!',
            pageSize: 10,
        };
  },
  computed: {
    pageCount () {
        if(this.rankinfo.length > 0 ){
            let listLeng = this.rankinfo.length,
                listSize = this.pageSize,
                page = Math.floor(listLeng / listSize);
            if (listLeng % listSize > 0) page += 1;
            return page;
        }else{
            let page = 1;
            return page;
        }
    },
    paginatedData () {
        if(this.rankinfo.length > 0 ){
            this.infomessage = false;
            const start = this.pageNum * this.pageSize,
                    end = start + this.pageSize;
            return this.rankinfo.slice(start, end);
        }
    }
  },
  methods: {
    nextPage () {
      this.pageNum += 1;
    },
    prevPage () {
      this.pageNum -= 1;
    },
    Back : function() {
        this.$store.commit('SwichView', 'Login');
    },
  },
  created() {
    //rankview 요청
    const url = 'https://eos.greymass.com:443/v1/chain/get_table_rows';
    const params = '{"scope":"eosfastclick","code":"eosfastclick","table":"rank","json":true,"index_position":"2","key_type":"i64","lower_bound":"","upper_bound":"","table_key":"","limit":100,"reverse":true}';
    const configs = {'content-type': 'application/x-www-form-urlencoded; charset=UTF-8'};
    axios.post(url, params, configs)
    .then((response) => {
        if(response.data.rows[0] != null){
            // rank 순위 퀵 정렬 - score
            function scoresort(list){
                const len = list.length;
                if(len === 0) return [];

                let left = [];
                let right = [];
                let pivot =list[0];
                for(let i=1; i<len; i++){
                    if(list[i].game_score < pivot.game_score){
                        left.push(list[i]);
                    }else if(list[i].game_score == pivot.game_score){
                        if(list[i].play_time > pivot.play_time){
                            left.push(list[i]);
                        }else{
                            right.push(list[i]);
                        }
                    }else{
                        right.push(list[i]);
                    }
                }
                return scoresort(right).concat(pivot, scoresort(left));
            }

        // 정렬된 리스트 반영
        this.rankinfo = scoresort(response.data.rows);
      }
    })
    .catch(e => {
      console.log(e);
    });
  },
};
</script>

<style>
#rankview {
    background: #fff;
    margin: 0 auto;
    max-width: 700px;
    min-height: 100vh;
    color: #34495e;
    border-top-left-radius: 30px;
    border-top-right-radius: 30px;
    border-bottom-left-radius: 30px;
    border-bottom-right-radius: 30px;
}
.infomessage{
  padding-top: 5vh;
  padding-bottom: 5vh;
  color: #0024ff;
  font-weight: bold;
  font-size:5vh;
}
.btn-cover {
  text-align: center;
  font-weight: bold;
}
.btn-cover .page-btn {
  background-color: #e74c3c;
  color: #fff;
  border-radius: 5px;
  font-family: 'Dosis', Helvetica, sans-serif;
  width: 7vh;
  height: 5vh;
  font-weight: bold;
}
.btn-cover .page-count {
  padding-left: 2vh;
  padding-right: 2vh;
  font-weight: bold;
  font-size:2vh;
}
.category-Rank{
  float:left;
  width :20%;
  margin-left: 10px;
  font-size:3vh;

}
.category-User{
  float:left;
  width: 20%;
  font-size:3vh;
}
.category-Score{
  font-size:3vh;
  float:right;
  width: 18%;
}
.category-Time{
  float:right;
  width: 20%;
  margin-right: 10px;
  font-size:3vh;
}
.list-left{
  float:left;
  width: 20%;
  font-size:2.5vh;
}
.list-right{
  float:right;
  font-size:2.5vh;
  width: 20%;
}
.category{
  background-color: #ff0000;
  color: #fff;
  font-family: 'Dosis', Helvetica, sans-serif;
  font-weight: bold;
  height: 4vh;
}
.list{
  background-color: #5d319f;
  color: #fff;
  border-radius: 10px;
  font-family: 'Dosis', Helvetica, sans-serif;
  margin: 10px;
  font-weight: bold;
  height:7vh;
  line-height: 7vh;
  vertical-align: middle;
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
</style>
