using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Giftbox_Item_Action : MonoBehaviour
{
    GameObject player;
    GameObject playerEquipPoint;
    bool isPlayerEnter;

    // Start is called before the first frame update
    void Awake()
    {
        player = GameObject.FindGameObjectWithTag("Rabbit");
        playerEquipPoint = GameObject.FindGameObjectWithTag("EquipPoint");
    }

    void OnTriggerEnter(Collider other)
    {
        if (other.gameObject == player)
        {
            isPlayerEnter = true;
        }
    }

    void OnTriggerExit(Collider other)
    {
        if (other.gameObject == player)
        {
            isPlayerEnter = false;
        }
    }

    // Update is called once per frame
    void Update()
    {
        if (Buttons.BbuttonSend == true && isPlayerEnter)
        {
            transform.SetParent(playerEquipPoint.transform);
            transform.localPosition = Vector3.zero;
            transform.rotation = new Quaternion(0, 0, 0, 0);
            isPlayerEnter = false;
            Buttons.BbuttonSend = false;
        }
    }
}
