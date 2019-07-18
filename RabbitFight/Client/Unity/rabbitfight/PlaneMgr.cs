using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using GoogleARCore;
using GoogleARCore.Examples.Common;

public class PlaneMgr : MonoBehaviour
{
    public Camera arCamera;
    public GameObject andy;
    private bool unitcreate;
    private GameObject PlaneVisuallizer;

    void Start()
    {
        arCamera = Camera.main;
        unitcreate = false;
    }

    
    void Update()
    {
        Touch touch;
        touch = Input.GetTouch(0);

        //맵생성
        if (unitcreate == false) 
        {
            TrackableHit hit;
            TrackableHitFlags flags = TrackableHitFlags.PlaneWithinPolygon | TrackableHitFlags.FeaturePointWithSurfaceNormal;

            if (Frame.Raycast(touch.position.x, touch.position.y, flags, out hit))
            {          
                var anchor = hit.Trackable.CreateAnchor(hit.Pose);
                Instantiate(andy, new Vector3(0, 0, 0),new Quaternion(0, 0, 0, 0), anchor.transform);
                GameObject.Find("start").SetActive(false);
                //    Instantiate(andy, hit.Pose.position, hit.Pose.rotation, anchor.transform);
            }
            unitcreate = true;
             

        }
            
    }

}
