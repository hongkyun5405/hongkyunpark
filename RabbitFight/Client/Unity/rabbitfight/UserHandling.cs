using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;


public class UserHandling : MonoBehaviour
{

    public Animator Myanimator;  //자신의 캐릭터 
    public Slider MySlider;     //자신의 체력바 
    public float DestroyTime = 3.0f;
    GameObject user;
    GameObject playerEquipPoint;
    public bool isPicking;
    void Start()
    {
        this.enabled = true;
        MySlider = GameObject.Find("UserSlider").GetComponent<Slider>();  // 캐릭터가 생성 될때 체력바를 같이 생성해주어야 한다.                                                                           // 그렇지않으면 project에 있는 prefab을 가져다 쓰기때문에 UI가 변경되지 않는다. 
    }

 
    public void Pickup (GameObject item)
    {
        SetEquip(item, true);
        isPicking = true;
    }

   public void Drop()
    {
        GameObject item = playerEquipPoint.GetComponentInChildren<Rigidbody>().gameObject;
        SetEquip(item, false);

        playerEquipPoint.transform.DetachChildren();
        isPicking = false;
    }


    void SetEquip(GameObject item, bool isEquip)
    {
        Collider[] itemColliders = item.GetComponents<Collider>();
        Rigidbody itemRigidbody = item.GetComponent<Rigidbody>();

        foreach(Collider itemCollider in itemColliders)
        {
            itemCollider.enabled = !isEquip;
        }

        itemRigidbody.isKinematic = isEquip;

    }

    void OnTriggerExit(Collider other)
    {

    }

}