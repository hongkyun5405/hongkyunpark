using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Buttons : MonoBehaviour
{
    //버튼값
    public static bool AbuttonSend;
    public static bool BbuttonSend;
    public static bool CbuttonSend;
    public static bool DbuttonSend;

    //버튼 쿨타임값
    public static bool ACoolTime;
    public static bool BCoolTime;
    public static bool CCoolTime;
    public static bool DCoolTime;

    void Start()
    {
        AbuttonSend = false;
        BbuttonSend = false;
        CbuttonSend = false;
        DbuttonSend = false;

        ACoolTime = false;
        BCoolTime = false;
        CCoolTime = false;
        DCoolTime = false;
    }




}
