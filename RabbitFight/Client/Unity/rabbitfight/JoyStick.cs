using System.Collections;
using System.Collections.Generic;
using System.Threading;
using UnityEngine;
using UnityEngine.EventSystems;
using Newtonsoft.Json.Linq;
using Newtonsoft.Json;

public class JoyStick : MonoBehaviour
{

    // 공개
    public Transform Player;        // 플레이어.
    public Transform Stick;         // 조이스틱.

    // 비공개
    public static Vector3 StickFirstPos;  // 조이스틱의 처음 위치.
    private Vector3 JoyVec;         // 조이스틱의 벡터(방향)
    private float Radius;           // 조이스틱 배경의 반 지름.
    public static bool MoveFlag;    // 플레이어 움직임 스위치.
 
    void Start()
    {
        Radius = GetComponent<RectTransform>().sizeDelta.y * 0.25f;
        StickFirstPos = Stick.transform.position;

        // 캔버스 크기에대한 반지름 조절.
        float Can = transform.parent.GetComponent<RectTransform>().localScale.x;
        Radius *= Can;

        MoveFlag = false;
    }

    void Update()
    {
        if (MoveFlag)
        Player.transform.Translate(Vector3.forward * Time.deltaTime * 0.75f);
    }

    // 드래그
    public void Drag(BaseEventData _Data)
    {       
            MoveFlag = true;
            PointerEventData EventData = _Data as PointerEventData;
            Vector3 Pos = EventData.position;

            // 조이스틱을 이동시킬 방향을 구함.(오른쪽,왼쪽,위,아래)
            JoyVec = (Pos - StickFirstPos).normalized;

            // 조이스틱의 처음 위치와 현재 내가 터치하고있는 위치의 거리를 구한다.
            float Dis = Vector3.Distance(Pos, StickFirstPos);

            // 거리가 반지름보다 작으면 조이스틱을 현재 터치하고 있는곳으로 이동.
            if (Dis < Radius)
                Stick.position = StickFirstPos + JoyVec * Dis;
            // 거리가 반지름보다 커지면 조이스틱을 반지름의 크기만큼만 이동.
            else
                Stick.position = StickFirstPos + JoyVec * Radius;

            Player.eulerAngles = new Vector3(0, Mathf.Atan2(JoyVec.x, JoyVec.y) * Mathf.Rad2Deg, 0);

        var jsondata = new JObject();
        jsondata.Add("Room", SR.PlayerData.Room); //방번호
        jsondata.Add("PlayerIndex", SR.PlayerData.PlayerIndex); //방에서의 플레이어 인덱스번호
        jsondata.Add("ID", GameClient.randomID);  // 아이디값
        jsondata.Add("Separator", 1000); //구분자 - 1000은 이동데이터  
        jsondata.Add("Drag", true); //드래그값
        jsondata.Add("PositionX", (Player.transform.position.x).ToString("N2")); //캐릭터 좌표값 x
        jsondata.Add("PositionZ", (Player.transform.position.z).ToString("N2")); //캐릭터 좌표값 z
        jsondata.Add("RotationX", (JoyVec.x).ToString("N2")); //캐릭터 회전값 x
        jsondata.Add("RotationY", (JoyVec.y).ToString("N2")); //캐릭터 회전값 y
        string sendjson = JsonConvert.SerializeObject(jsondata);

        SR.Send(sendjson); 
    }

    // 드래그 끝.
    public void DragEnd()
    {
        Stick.position = StickFirstPos; // 스틱을 원래의 위치로.
        JoyVec = Vector3.zero;          // 방향을 0으로.
        MoveFlag = false;

        var jsondata = new JObject();
        jsondata.Add("Room", SR.PlayerData.Room); //방번호
        jsondata.Add("PlayerIndex", SR.PlayerData.PlayerIndex); //방에서의 플레이어 인덱스번호
        jsondata.Add("ID", GameClient.randomID);  // 아이디값
        jsondata.Add("Separator", 1000); //구분자 - 1000은 이동데이터
        jsondata.Add("Drag", false); //드래그값
        jsondata.Add("PositionX", (Player.transform.position.x).ToString("N2")); //캐릭터 좌표값 x
        jsondata.Add("PositionZ", (Player.transform.position.z).ToString("N2")); //캐릭터 좌표값 z
        jsondata.Add("RotationX", (JoyVec.x).ToString("N2")); //캐릭터 회전값 x
        jsondata.Add("RotationY", (JoyVec.y).ToString("N2")); //캐릭터 회전값 y
        string sendjson = JsonConvert.SerializeObject(jsondata);

        SR.Send(sendjson);
    }
}
