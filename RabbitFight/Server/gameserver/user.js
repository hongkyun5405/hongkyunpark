//User 정보 클래스
module.exports = class User{

    //생성자
    constructor(){
        this.roomno; //속해있는 방번호
        this.playerIndex; //방에서의 플레이어 인덱스번호
        this.address; //User IP
        this.port;  //User Port
        this.id;  //User ID
        this.drag; //user 조이스틱 드래그 여부
        this.positionX; //User 위치 좌표 x 값
        this.positionZ; //User 위치 좌표 z 값
        this.rotationX; //User 방향 좌표 x 값
        this.rotationY; //User 방향 좌표 y 값
        this.hp = 100; //User HP
    }

}
