using UnityEngine;
using UnityEngine.UI;
using Newtonsoft.Json.Linq;
using Newtonsoft.Json;

public class UserInterfaceButtons : MonoBehaviour
{
    public Animator PlayerAnimator; //player 애니메이터

    string Attack3 = "ATK3";
    string Jump = "Jump";
    string Attack2 = "ATK2";
    string Attack1 = "ATK1";

    float AtimeAmt = 2;
    float Atime;
    float BtimeAmt = 3;
    float Btime;
    float CtimeAmt = 4;
    float Ctime;
    float DtimeAmt = 1;
    float Dtime;

    void Start()
    {
        Atime = AtimeAmt;
        Btime = BtimeAmt;
        Ctime = CtimeAmt;
        Dtime = DtimeAmt;
    }

    void Update()
    {
        if (Buttons.ACoolTime == true && Atime > 0)
        {
            if (GameObject.Find("ACoolTime").GetComponent<Image>().fillAmount == 1)
            {
                PlayerAnimator.Play("ATK1");
            }
            Atime -= Time.deltaTime;
            GameObject.Find("ACoolTime").GetComponent<Image>().fillAmount = Atime / AtimeAmt;
            if (GameObject.Find("ACoolTime").GetComponent<Image>().fillAmount == 0)
            {
                GameObject.Find("ACoolTime").GetComponent<Image>().fillAmount = 1;
                Buttons.ACoolTime = false;
                Atime = AtimeAmt;
            }
        }

        if (Buttons.BCoolTime == true && Btime > 0)
        {
            if (GameObject.Find("BCoolTime").GetComponent<Image>().fillAmount == 1)
            {
                PlayerAnimator.Play("ATK2");
            }
            Btime -= Time.deltaTime;
            GameObject.Find("BCoolTime").GetComponent<Image>().fillAmount = Btime / BtimeAmt;
            if (GameObject.Find("BCoolTime").GetComponent<Image>().fillAmount == 0)
            {
                GameObject.Find("BCoolTime").GetComponent<Image>().fillAmount = 1;
                Buttons.BCoolTime = false;
                Btime = BtimeAmt;
            }
        }

        if (Buttons.CCoolTime == true && Ctime > 0)
        {
            if (GameObject.Find("CCoolTime").GetComponent<Image>().fillAmount == 1)
            {
                PlayerAnimator.Play("ATK3");
            }
            Ctime -= Time.deltaTime;
            GameObject.Find("CCoolTime").GetComponent<Image>().fillAmount = Ctime / CtimeAmt;
            if (GameObject.Find("CCoolTime").GetComponent<Image>().fillAmount == 0)
            {
                GameObject.Find("CCoolTime").GetComponent<Image>().fillAmount = 1;
                Buttons.CCoolTime = false;
                Ctime = CtimeAmt;
            }
        }

        if (Buttons.DCoolTime == true && Dtime > 0)
        {
            if (GameObject.Find("DCoolTime").GetComponent<Image>().fillAmount == 1)
            {
                PlayerAnimator.Play("ATK4");
            }
            Dtime -= Time.deltaTime;
            GameObject.Find("DCoolTime").GetComponent<Image>().fillAmount = Dtime / DtimeAmt;
            if (GameObject.Find("DCoolTime").GetComponent<Image>().fillAmount == 0)
            {
                GameObject.Find("DCoolTime").GetComponent<Image>().fillAmount = 1;
                Buttons.DCoolTime = false;
                Dtime = DtimeAmt;
            }
        }
    }


    public void AbuttonSendClick()
    {
        if (Buttons.ACoolTime == false)
        {
            Buttons.AbuttonSend = true;

            var jsondata = new JObject();
            jsondata.Add("Room", SR.PlayerData.Room); //방번호
            jsondata.Add("PlayerIndex", SR.PlayerData.PlayerIndex); //방에서의 플레이어 인덱스번호
            jsondata.Add("ID", GameClient.randomID);  // 아이디값
            jsondata.Add("Separator", 2000); //구분자 - 2000은 모션데이터
            jsondata.Add("Motion", Attack1);
            string sendjson = JsonConvert.SerializeObject(jsondata);
            SR.Send(sendjson);
            Buttons.ACoolTime = true;
        }
    }

    public void BbuttonSendClick()
    {
        if (Buttons.BCoolTime == false)
        {
            Buttons.BbuttonSend = true;

            var jsondata = new JObject();
            jsondata.Add("Room", SR.PlayerData.Room); //방번호
            jsondata.Add("PlayerIndex", SR.PlayerData.PlayerIndex); //방에서의 플레이어 인덱스번호
            jsondata.Add("ID", GameClient.randomID);  // 아이디값
            jsondata.Add("Separator", 2000); //구분자 - 2000은 모션데이터
            jsondata.Add("Motion", Attack2);
            string sendjson = JsonConvert.SerializeObject(jsondata);

            SR.Send(sendjson);
            Buttons.BCoolTime = true;
        }
    }

    public void CbuttonSendClick()
    {
        if (Buttons.CCoolTime == false)
        {
            Buttons.CbuttonSend = true;

            var jsondata = new JObject();
            jsondata.Add("Room", SR.PlayerData.Room); //방번호
            jsondata.Add("PlayerIndex", SR.PlayerData.PlayerIndex); //방에서의 플레이어 인덱스번호
            jsondata.Add("ID", GameClient.randomID);  // 아이디값
            jsondata.Add("Separator", 2000); //구분자 - 2000은 모션데이터
            jsondata.Add("Motion", Attack3);
            string sendjson = JsonConvert.SerializeObject(jsondata);

            SR.Send(sendjson);
            Buttons.CCoolTime = true;
        }
    }

    public void DbuttonSendClick()
    {
        if (Buttons.DCoolTime == false)
        {
            Buttons.DbuttonSend = true;        

            var jsondata = new JObject();
            jsondata.Add("Room", SR.PlayerData.Room); //방번호
            jsondata.Add("PlayerIndex", SR.PlayerData.PlayerIndex); //방에서의 플레이어 인덱스번호
            jsondata.Add("ID", GameClient.randomID);  // 아이디값
            jsondata.Add("Separator", 2000); //구분자 - 2000은 모션데이터
            jsondata.Add("Motion", Jump);
            string sendjson = JsonConvert.SerializeObject(jsondata);

            SR.Send(sendjson);
            Buttons.DCoolTime = true;
        }
    }


}
