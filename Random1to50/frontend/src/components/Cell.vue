<template>
	<td class="cell" @click="touch">{{ mark }}</td>
</template>

<script>
export default {
  props: ['id'],
  data() {
    return {
      mark: '', //각셀의 text
      numbercheck: 1 //클릭해야할 버튼값
    };
  },
  methods: {
    touch() {
      //유저가 터치한 버튼값과 해야할 값이 같으면 다음번호로 넘어감
      if (this.mark == this.numbercheck) {
        Event.$emit('touchnumber', this.mark);
        Event.$emit('touch', this.id);
      }
    },
    numberlist(cell) {
      if (cell == 'gameend') {
        this.mark = 'X';
      } else {
        this.mark = cell[this.id - 1];
      }
    }
  },
  created() {
    Event.$on('clearCell', () => {
      this.mark = '';
      this.numbercheck = 1;
    });
    Event.$on('numberlist', cell => {
      this.numberlist(cell);
    });
    Event.$on('numbercheck', number => {
      this.numbercheck = number;
    });
  }
};
</script>

<style>
.cell {
  width: 20%;
  height: 90px;
  border: 6px solid #2c3e50;
  font-size: 3.5em;
  font-family: 'Gochi Hand', sans-serif;
}
.cell:hover {
  background-color: #7f8c8d;
}
.cell::after {
  content: '';
  display: block;
}
</style>
