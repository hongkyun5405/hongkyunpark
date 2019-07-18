using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;


public class RemoteDamageHandling : MonoBehaviour
{
    public Animator UserAnimator; // User 캐릭터
    public Animator RemoteAnimator;  //Remote 캐릭터
    public Slider RemoteSlider;

    void Start()
    {
        this.enabled = true;
        RemoteSlider = GameObject.Find("RemoteSlider").GetComponent<Slider>();  // 캐릭터가 생성 될때 체력바를 같이 생성해주어야 한다.
    }                                                                       // 그렇지않으면 project에 있는 prefab을 가져다 쓰기때문에 UI가 변경되지 않는다.

    void OnTriggerEnter(Collider other)
    {

        if (other.gameObject.tag == "A_R_Hand" && Buttons.AbuttonSend == true)
        {  //상대가 버튼을 누른것이 맞고 자기자신에게 상대의 주먹 collider 가 들어왔다면 자신이 맞았다는 판정값 
            RemoteSlider.value -= 0.1f;
            RemoteAnimator.SetTrigger("ATK1return"); //애니메이터 hit_L1 애니메이션 연결
            if (RemoteSlider.value == 0f)
            {
                RemoteAnimator.SetTrigger("death"); // 체력이 0이면 죽는 애니메이션 호출
            }
            Buttons.AbuttonSend = false;
        }
        else if (other.gameObject.tag == "A_R_Hand" && Buttons.BbuttonSend == true)
        {
            RemoteSlider.value -= 0.2f;
            RemoteAnimator.SetTrigger("ATK1return"); //애니메이터 hit_F1 애니메이션 연결        
            if (RemoteSlider.value == 0f)
            {
                RemoteAnimator.SetTrigger("death");// 체력이 0이면 죽는 애니메이션 호출
            }
            Buttons.BbuttonSend = false;
        }
        else if (other.gameObject.tag == "skill" && Buttons.CbuttonSend == true)
        {
            RemoteSlider.value -= 0.34f;
            RemoteAnimator.SetTrigger("ATK3return"); //애니메이터 hit_F2 애니메이션 연결
            Buttons.CbuttonSend = false;
        }
        Buttons.AbuttonSend = false;
        Buttons.BbuttonSend = false;
        Buttons.CbuttonSend = false;
        Buttons.DbuttonSend = false;
    }

    void OnTriggerExit(Collider other)
    {

    }

}
