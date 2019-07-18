using Newtonsoft.Json.Linq;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class systemtext : MonoBehaviour
{
    public Text textmsg;
    float countDown = 5.0f;
    bool isCountDown = true;


    // Start is called before the first frame update
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        if (SR.rcv != null)
        {
            JObject msg = JObject.Parse(SR.rcv);

            if ((string)msg["System"] == "wait")
            {
                textmsg.text = "상대방이 준비중입니다.";
                GameObject.Find("systemtext").transform.localScale = new Vector3(1.0f,1.0f,1.0f);
            }
            else if((string)msg["System"] == "start")
            {
                textmsg.text = "게임을 시작합니다.!";
                if (isCountDown)
                {
                    countDown -= Time.deltaTime;
                    textmsg.text = "" + ((int)countDown + 1);
                    if (countDown < 0)
                    {
                        textmsg.text = "Fight!";
                        Color color = textmsg.color;
                        color.a = 0.0f;
                        textmsg.color = color;
                        isCountDown = false;
                        //조작버튼 보이기
                        GameObject.Find("Canvas").transform.Find("A").gameObject.SetActive(true);
                        GameObject.Find("Canvas").transform.Find("B").gameObject.SetActive(true);
                        GameObject.Find("Canvas").transform.Find("C").gameObject.SetActive(true);
                        GameObject.Find("Canvas").transform.Find("D").gameObject.SetActive(true);
                        GameObject.Find("Canvas").transform.Find("JoyStickBackGround").gameObject.SetActive(true);
                    }
                }
            }
        }
    }


    public void startCountDown()
    {
        isCountDown = true;
        countDown = 5.0f;
        textmsg.text = "" + (int)countDown;
        Color color = textmsg.color;
        color.a = 1.0f;
        textmsg.color = color;
    }



}
