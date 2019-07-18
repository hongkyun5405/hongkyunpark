using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class test : MonoBehaviour
{
    public GameObject Map;

    public void Start()
    {
        Instantiate(Map, new Vector3(0.0f, 0.0f, 0.0f), new Quaternion(0, 0, 0, 0));  //맵       
    }
}

