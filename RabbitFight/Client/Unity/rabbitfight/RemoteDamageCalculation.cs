using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using UnityEngine;
using UnityEngine.UI;

public class RemoteDamageCalculation : MonoBehaviour
{
    public Animator RemotePlayerAnimator; //remoteplayer 애니메이터
    public Slider RemotePlayerSlider; //remoteplayer 체력바

    void Start()
    {
        //캐릭터를 생성할때 체력바를 생성시켜주어야한다 그렇지않으면 project에 있는 prefab을 가져다 쓴다. 
        //따라서 UI변경이 이루어 지지않는다.
        RemotePlayerSlider = GameObject.Find("RemoteSlider").GetComponent<Slider>();
    }


    void Update()
    {
        if (SR.PlayerData != null)
        {
            if (SR.PlayerData.Separator == 3000)
            {
                //자신이 1player일때
                if (SR.PlayerData.PlayerIndex == 1)
                {
                    RemotePlayerSlider.value = SR.PlayerData.SecondPlayerHP;

                    if (RemotePlayerSlider.value <= 0)
                    {
                        //애니메이터 Death 애니메이션 연결 
                        RemotePlayerAnimator.Play("Death");
                        //버튼 숨기기
                        GameObject.Find("A").SetActive(false);
                        GameObject.Find("B").SetActive(false);
                        GameObject.Find("C").SetActive(false);
                        GameObject.Find("D").SetActive(false);
                        GameObject.Find("JoyStickBackGround").SetActive(false);
                        //승리메시지 띄우기
                        GameObject.Find("victory").transform.localScale = new Vector3(4.5f, 4.5f, 4.5f);
                        //게임종료
                        Invoke("Quit", 2);
                    }
                    else
                    {
                        //맞는모션
                        switch (SR.PlayerData.Action)
                        {
                            case 3001:
                                RemotePlayerAnimator.Play("ATK1return"); //애니메이터 ATK1return 애니메이션 연결                       
                                break;

                            case 3002:
                                RemotePlayerAnimator.Play("ATK2return"); //애니메이터 ATK2return 애니메이션 연결                       
                                break;

                            case 3003:
                                RemotePlayerAnimator.Play("ATK3return"); //애니메이터 ATK3return 애니메이션 연결                       
                                break;
                        }
                    }
                }
                //자신이 2player일때
                else
                {
                    RemotePlayerSlider.value = SR.PlayerData.FirstPlayerHP;

                    if (RemotePlayerSlider.value <= 0)
                    {
                        //애니메이터 Death 애니메이션 연결 
                        RemotePlayerAnimator.Play("Death");
                        //버튼 숨기기
                        GameObject.Find("A").SetActive(false);
                        GameObject.Find("B").SetActive(false);
                        GameObject.Find("C").SetActive(false);
                        GameObject.Find("D").SetActive(false);
                        GameObject.Find("JoyStickBackGround").SetActive(false);
                        //승리메시지 띄우기
                        GameObject.Find("victory").transform.localScale = new Vector3(4.5f, 4.5f, 4.5f);
                        //게임종료
                        Invoke("Quit", 2);
                    }
                    else
                    {
                        //맞는모션
                        switch (SR.PlayerData.Action)
                        {
                            case 3001:
                                RemotePlayerAnimator.Play("ATK1return"); //애니메이터 ATK1return 애니메이션 연결                       
                                break;

                            case 3002:
                                RemotePlayerAnimator.Play("ATK2return"); //애니메이터 ATK2return 애니메이션 연결                       
                                break;

                            case 3003:
                                RemotePlayerAnimator.Play("ATK3return"); //애니메이터 ATK3return 애니메이션 연결                       
                                break;
                        }
                    }
                }
                SR.PlayerData.Separator = -1;
            }
        }
    }    

    void OnTriggerEnter(Collider other)
    {
        if (other.gameObject.name == "Fish" && Buttons.AbuttonSend == true)
        {
            var jsondata = new JObject();
            jsondata.Add("Room", SR.PlayerData.Room); //방번호
            jsondata.Add("PlayerIndex", SR.PlayerData.PlayerIndex); //방에서의 플레이어 인덱스번호
            jsondata.Add("ID", GameClient.randomID);  // 아이디값
            jsondata.Add("Separator", 3000); //구분자 - 3000은 데미지처리데이터
            jsondata.Add("Action", 3001);
            string sendjson = JsonConvert.SerializeObject(jsondata);
 
            SR.Send(sendjson);
        }
        else if (other.gameObject.name == "Fish" && Buttons.BbuttonSend == true)
        {
            var jsondata = new JObject();
            jsondata.Add("Room", SR.PlayerData.Room); //방번호
            jsondata.Add("PlayerIndex", SR.PlayerData.PlayerIndex); //방에서의 플레이어 인덱스번호
            jsondata.Add("ID", GameClient.randomID);  // 아이디값
            jsondata.Add("Separator", 3000); //구분자 - 3000은 데미지처리데이터
            jsondata.Add("Action", 3002);
            string sendjson = JsonConvert.SerializeObject(jsondata);

            SR.Send(sendjson);
        }
        else if (other.gameObject.name == "Fish" && Buttons.CbuttonSend == true)
        {
            var jsondata = new JObject();
            jsondata.Add("Room", SR.PlayerData.Room); //방번호
            jsondata.Add("PlayerIndex", SR.PlayerData.PlayerIndex); //방에서의 플레이어 인덱스번호
            jsondata.Add("ID", GameClient.randomID);  // 아이디값
            jsondata.Add("Separator", 3000); //구분자 - 3000은 데미지처리데이터
            jsondata.Add("Action", 3003);
            string sendjson = JsonConvert.SerializeObject(jsondata);

            SR.Send(sendjson);
        }

        Buttons.AbuttonSend = false;
        Buttons.BbuttonSend = false;
        Buttons.CbuttonSend = false;
        Buttons.DbuttonSend = false;
    }

    void OnTriggerExit(Collider other)
    {

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
