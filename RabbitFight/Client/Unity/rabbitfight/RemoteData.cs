
//상대방의 데이터를 받아서 저장 - 다른클래스에서 이정보들을 가져다가 자신의 화면에서의 상대 캐릭터를 변환시킴
public class RemoteData
{
    public int Room; //방번호
    public int PlayerIndex; //방에서의 유저 인덱스 번호 , 최초 생성위치 - 1P Position -1 , 2P Position -2 
    public int ID;   //유저 아이디
    public int Separator; //구분자값

    //위치데이터
    public bool Drag;  //드래그여부
    public float PositionX;  //위치정보 x값
    public float PositionZ;  //위치정보 z값
    public float RotationX;  //회전정보 x값
    public float RotationY;  //회전정보 y값
     
    //격투데이터 - 두개가 나누어져있는 이유는 상대방이 맞지않았을때도 모션은 동작해야되기때문에 모션만을 담당하는 Motion 이 있고 데미지를 입히거나 입은 경우 동작하는 Action이 있음 
    public string Motion;  //모션 값 - 모션만 보여줌 데미지계산이 있는 모션이아님 
    public int Action; //액션 값 - 데미지계산이 들어가면서 자신이 맞거나 remote가 맞았을때의 액션을 보여주기 위함 

    //HP
    public float SecondPlayerHP;  //2P PlayerHP 값
    public float FirstPlayerHP;  //1P PlayerHP 값
}
