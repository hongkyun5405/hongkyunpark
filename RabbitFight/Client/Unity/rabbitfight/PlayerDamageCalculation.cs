using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using UnityEngine;
using UnityEngine.UI;

public class PlayerDamageCalculation : MonoBehaviour
{
    public Animator PlayerAnimator; //player 애니메이터
    public Slider PlayerSlider; //player 체력바

    void Start()
    {
        //캐릭터를 생성할때 체력바를 생성시켜주어야한다 그렇지않으면 project에 있는 prefab을 가져다 쓴다. 
        //따라서 UI변경이 이루어 지지않는다.
        PlayerSlider = GameObject.Find("UserSlider").GetComponent<Slider>();
    }

    void Update()
    {
        if (SR.RemoteData != null)
        {
            if (SR.RemoteData.Separator == 3000)
            {
                //자신이 1player일때
                if (SR.PlayerData.PlayerIndex == 1)
                {
                    PlayerSlider.value = SR.RemoteData.FirstPlayerHP;

                    if (PlayerSlider.value <= 0)
                    {
                        //애니메이터 Death 애니메이션 연결 
                        PlayerAnimator.Play("Death");
                        //버튼 숨기기
                        GameObject.Find("A").SetActive(false);
                        GameObject.Find("B").SetActive(false);
                        GameObject.Find("C").SetActive(false);
                        GameObject.Find("D").SetActive(false);
                        GameObject.Find("JoyStickBackGround").SetActive(false);
                        //패배메시지 띄우기
                        GameObject.Find("defeat").transform.localScale = new Vector3(4.5f, 4.5f, 4.5f);
                        //게임종료
                        Invoke("Quit", 2);
                    }
                    else
                    {
                        //맞는 모션
                        switch (SR.RemoteData.Action)
                        {
                            case 3001:
                                PlayerAnimator.Play("ATK1return"); //애니메이터 ATK1return 애니메이션 연결                       
                                break;

                            case 3002:
                                PlayerAnimator.Play("ATK2return"); //애니메이터 ATK2return 애니메이션 연결                       
                                break;

                            case 3003:
                                PlayerAnimator.Play("ATK3return"); //애니메이터 ATK3return 애니메이션 연결                       
                                break;
                        }
                    }
                }
                //자신이 2player일때
                else
                {
                    PlayerSlider.value = SR.RemoteData.SecondPlayerHP;

                    if (PlayerSlider.value <= 0)
                    {
                        //애니메이터 Death 애니메이션 연결
                        PlayerAnimator.Play("Death");
                        //버튼 숨기기
                        GameObject.Find("A").SetActive(false);
                        GameObject.Find("B").SetActive(false);
                        GameObject.Find("C").SetActive(false);
                        GameObject.Find("D").SetActive(false);
                        GameObject.Find("JoyStickBackGround").SetActive(false);
                        //패배메시지 띄우기
                        GameObject.Find("defeat").transform.localScale = new Vector3(4.5f, 4.5f, 4.5f);
                        //게임종료
                        Invoke("Quit", 2.0f);
                    }
                    else
                    {
                        //맞는 모션
                        switch (SR.RemoteData.Action)
                        {
                            case 3001:
                                PlayerAnimator.Play("ATK1return"); //애니메이터 ATK1return 애니메이션 연결                       
                                break;

                            case 3002:
                                PlayerAnimator.Play("ATK2return"); //애니메이터 ATK2return 애니메이션 연결                       
                                break;

                            case 3003:
                                PlayerAnimator.Play("ATK3return"); //애니메이터 ATK3return 애니메이션 연결                       
                                break;
                        }
                    }
                }
                SR.RemoteData.Separator = -1;  //받은데이터 초기화
            }
        }
    }

    //종료
   public void Quit()
    {
        var jsondata = new JObject();
        jsondata.Add("Room", SR.PlayerData.Room); //방번호
        jsondata.Add("PlayerIndex", SR.PlayerData.PlayerIndex); //방에서의 플레이어 인덱스번호
        jsondata.Add("ID", GameClient.randomID);  // 아이디값
        jsondata.Add("System", "exit"); //종료 메시지
        string sendjson = JsonConvert.SerializeObject(jsondata);

        SR.Send(sendjson);
        Application.Quit();
    }
}
