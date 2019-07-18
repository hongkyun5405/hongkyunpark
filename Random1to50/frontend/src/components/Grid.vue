<template>
	<div>
		<table class="grid">
		  <tr>
		    <cell id="1"></cell>
		    <cell id="2"></cell>
		    <cell id="3"></cell>
            <cell id="4"></cell>
            <cell id="5"></cell>
		  </tr>
		  <tr>
		    <cell id="6"></cell>
		    <cell id="7"></cell>
		    <cell id="8"></cell>
            <cell id="9"></cell>
            <cell id="10"></cell>
		  </tr>
		  <tr>
		    <cell id="11"></cell>
		    <cell id="12"></cell>
		    <cell id="13"></cell>
            <cell id="14"></cell>
            <cell id="15"></cell>
		  </tr>
          <tr>
		    <cell id="16"></cell>
		    <cell id="17"></cell>
		    <cell id="18"></cell>
            <cell id="19"></cell>
            <cell id="20"></cell>
		  </tr>
          <tr>
		    <cell id="21"></cell>
		    <cell id="22"></cell>
		    <cell id="23"></cell>
            <cell id="24"></cell>
            <cell id="25"></cell>
		  </tr> 
		</table>
	</div>
</template>

<script>
import Cell from './Cell.vue';

export default {
  components: { Cell },
  data() {
    return {
      numbertemp: null,
      renumber: null,
      sellnumbercheck: 1,
      firstnum: 1,
      lastnum: 25,
      numberlist: new Array(),
      standardrandomlist: new Array()
    };
  },
  methods: {
    standardrandom(info) {
      //1부터 25까지있는 리스트를 만듬
      for (var num = 1; num <= 25; num++) {
        this.standardrandomlist.push(num);
      }

      //값을 서로 섞기
      for (var mix = 0; mix < this.standardrandomlist.length; mix++) {
        this.renumber = Math.floor(Math.random() * 23 + 1); //난수발생
        this.numbertemp = this.standardrandomlist[mix];
        this.standardrandomlist[mix] = this.standardrandomlist[
          this.renumber - 1
        ];
        this.standardrandomlist[this.renumber - 1] = this.numbertemp;
      }

      //1~25 랜덤리스트를 어디로 보낼지 처리
      if (info == 'createcell') {
        for (var change = 1; change <= 25; change++) {
          this.numberlist[change - 1] = this.standardrandomlist[change - 1];
        }
        Event.$emit('numberlist', this.numberlist); //셀로 값전달
      }
    }
  },
  created() {
    Event.$on('standardrandom', info => {
      this.standardrandom(info);
    });
    //cellNumber - cell 인덱스 번호
    //removevalue - numberlist에서 방금클릭한 인덱스번호에서 꺼낸값(즉 유저가 클릭한값)
    Event.$on('touch', cellNumber => {
      var removevalue = this.numberlist[cellNumber - 1]; //유저가 클릭한값 //1~50리스트에서 삭제할 값

      //마지막 번호 클릭했는지 체크
      if (removevalue != 50) {
        this.sellnumbercheck++; //다음으로 터치해야할 버튼값

        if (this.lastnum != 50) {
          //마지막값이 50이면 더이상 lastnumber값을 늘리지않음
          this.lastnum++;
        }
        this.firstnum++;

        this.numberlist.length = 0; //numberlist 초기화

        //다음 number 값이 26이상이면 리스트에서 난수를 생성한후 26 ~ 클릭한값을 x 로 바꾼다
        if (removevalue > 25) {
          //유저가 클릭한 값을 제거하고 다음숫자를 가져온후 리스트 다시구성
          for (var num = 26; num <= 50; num++) {
            this.numberlist.push(num);
          }

          //값을 서로 섞기
          for (var xplusmix = 0; xplusmix < 25; xplusmix++) {
            this.renumber = Math.floor(Math.random() * (50 - 26 + 1) + 26); //난수발생
            this.numbertemp = this.numberlist[xplusmix];
            this.numberlist[xplusmix] = this.numberlist[this.renumber - 26];
            this.numberlist[this.renumber - 26] = this.numbertemp;
          }
          //26 ~ 클릭한값을 x 로 바꾼다
          for (var xplus = 26; xplus <= removevalue; xplus++) {
            this.numberlist[this.numberlist.indexOf(xplus)] = 'X';
          }
        } else {
          //유저가 클릭한 값을 제거하고 다음숫자를 가져온후 리스트 다시구성
          for (var relist = this.firstnum; relist <= this.lastnum; relist++) {
            this.numberlist.push(relist);
          }

          //값을 서로 섞기
          for (var mix = 0; mix < this.numberlist.length; mix++) {
            this.renumber = Math.floor(
              Math.random() * (this.lastnum - this.firstnum + 1) + this.firstnum
            ); //난수발생
            this.numbertemp = this.numberlist[mix];
            this.numberlist[mix] = this.numberlist[
              this.renumber - this.firstnum
            ];
            this.numberlist[this.renumber - this.firstnum] = this.numbertemp;
          }
        }
    
        Event.$emit('numberlist', this.numberlist); //셀로 값전달
        Event.$emit('numbercheck', this.sellnumbercheck); //다음 터치해야할값
      } else {
        Event.$emit('numberlist', 'gameend'); //셀로 값전달
        Event.$emit('timerstop'); //timer로 값전달
      }
    });
    // listens for a restart button press
    // the data of the component is reinitialized
    // it is called by the App component
    Event.$on('GameReset', () => {
      this.numberlist.length = 0;
      this.standardrandomlist.length = 0;
      Event.$emit('clearCell');
      this.sellnumbercheck = 1;
      this.firstnum = 1;
      this.lastnum = 25;
    });
  }
};
</script>
<style>
.grid {
  background-color: #34495e;
  color: #fff;
  width: 100%;
  height: 100%;
  border-collapse: collapse;
}
</style>
