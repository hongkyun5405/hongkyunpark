using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using UnityEngine;
using Newtonsoft.Json.Linq;
using Newtonsoft.Json;



public class GameClient : MonoBehaviour
{
    public GameObject Player; //자신의 캐릭터  
    public GameObject RemotePlayer; //상대방 캐릭터
 
    public float speed = 0.75f; //remoteplayer가 움직일 속도

    private float startTime; //remoteplayer가 컨트롤가능한 최초시간 - > 게임플레이 시작시간 

    private float journeyLength; //remoteplayer가 이동한 총거리

    Vector3 TargetPosition; //remoteplayer가 이동할 목적지

    //캐릭터 animator
    Animator RemoteAnimator;

    //수신 쓰레드
    private Thread clientReceiveThread;

    public static UdpClient client = new UdpClient();

    public static IPEndPoint sender = new IPEndPoint(IPAddress.Any, 0);
    public static IPEndPoint remote = new IPEndPoint(IPAddress.Parse("211.249.62.8"), Port);

    public static String rcv;  //서버에서 받은 데이터

    //IP주소
    public static string Host = "211.249.62.8";

    //포트번호
    public static int Port = 7070;

    //클라이언트 아이디값 
    public static int randomID;

    public void Start()
    {
        try
        {
            Debug.Log("client connected..");        
            randomID = UnityEngine.Random.Range(0, 10000); //서버에 보낼때 서버가 구분하기위한 구분자를 난수를생성해서 사용

            var jsondata = new JObject();
            jsondata.Add("Separator", 0000);
            jsondata.Add("ID", randomID);
            string jsonsend = JsonConvert.SerializeObject(jsondata);
            SR.Send(jsonsend);

            clientReceiveThread = new Thread(new ThreadStart(SR.Receive));
            clientReceiveThread.IsBackground = true;
            clientReceiveThread.Start();
            Debug.Log("[클라 수신쓰레드 시작]");
        }
        catch (IOException e)
        {
            Debug.Log("On client connect exception " + e);
        }
        catch (Exception e)
        {
            Debug.Log("[클라] Connect 오류" + e);
        }
        
    }
  
    //매 프레임마다 실행
    void Update()
    {
        FirstPosition(); //최초좌표값 받기
        MoveReceive(); //이동데이터 받기   
        MotionReceive(); //모션데이터 받기 
    }
    
    //최초캐릭터좌표값받기 - 0000
    void FirstPosition()
    {
        if (SR.PlayerData == null)
        {
        }
        else
        {
            //최초좌표값 받아오기 
            if (SR.PlayerData.Separator == 0000)
            {
                if (SR.PlayerData.ID == randomID)  //자신이 요청한값을 받음 
                {
                    if (SR.PlayerData.PlayerIndex == 1)
                    {
                        Player.transform.rotation = new Quaternion(0, 0, 0, 0);
                        RemotePlayer.transform.rotation = new Quaternion(0, 180.0f, 0, 0);
                        startTime = Time.time;  // 움직임이 시작된 시간을 기록해 둔다.
                        journeyLength = Vector3.Distance(RemotePlayer.transform.position, TargetPosition);// 움직인 길이를 계산
                    }
                    else if (SR.PlayerData.PlayerIndex == 2)
                    {
                        Vector3 RemotePlayerPosition = new Vector3(Player.transform.position.x, Player.transform.position.y, Player.transform.position.z);
                        Player.transform.position = RemotePlayer.transform.position;
                        RemotePlayer.transform.position = RemotePlayerPosition;
                        Player.transform.rotation = new Quaternion(0, 180.0f, 0, 0);
                        RemotePlayer.transform.rotation = new Quaternion(0, 0, 0, 0);
                        startTime = Time.time;  // 움직임이 시작된 시간을 기록해 둔다.
                        journeyLength = Vector3.Distance(RemotePlayer.transform.position, TargetPosition);// 움직인 거리를 계산
                    }
                    RemoteAnimator = RemotePlayer.GetComponent<Animator>(); //상대방 캐릭터 애니메이터
                }
                SR.PlayerData.Separator = -1; // 값초기화
            }
        }
    }
     
    //이동데이터 받기 - 1000
    void MoveReceive()
      {
          if (SR.RemoteData == null)
          {
          }
          else
          {
              //상대이동값 받아오기 
              if (SR.RemoteData.Separator == 1000)
              {
                    if (SR.RemoteData.ID != randomID)    //자기자신이 보낸 값이아니라면 clone2 의 position 변경 
                    {              
                        float distCovered = (Time.time - startTime) * speed; // remoteplayer의 위치추적
                 
                        float fracJourney = distCovered / journeyLength; //remoteplayer의 현재거리를 움직인 총거리로 나눈 값

                    if (SR.RemoteData.Drag == true)  //상대 플레이어의 position.y값을 안받는것은 자신의 화면의 y값에서 움직여야 하기 때문이다. 플레이어 자신과 상대는 각자의 화면의 position.y값이 다르기 때문에 ...
                          {
                              TargetPosition = new Vector3(SR.RemoteData.PositionX, Player.transform.position.y, SR.RemoteData.PositionZ);   // player 이동좌표 값 
                              RemotePlayer.transform.position = Vector3.Lerp(RemotePlayer.transform.position, TargetPosition, fracJourney); // 선형보간 캐릭터를 부드럽게 이동하기위해 사용 
                              RemotePlayer.transform.eulerAngles = new Vector3(0, Mathf.Atan2(SR.RemoteData.RotationX, SR.RemoteData.RotationY) * Mathf.Rad2Deg, 0);   //캐릭터 방향설정                                                                                       // 원 위치에서 타겟 위치로 이동시 일정한 속도로 움직인다.                                                  // Lerp(현재캐릭터의 위치, 캐릭터가 도착할 위취, 목표지점까지 도달하는데 걸리는 시간)
                              RemoteAnimator.Play("Move");  //이동애니메이션       
                          }
                          else if(SR.RemoteData.Drag == false)
                          {
                              TargetPosition = new Vector3(SR.RemoteData.PositionX, Player.transform.position.y, SR.RemoteData.PositionZ);   // player 이동좌표 값 
                              RemotePlayer.transform.position = Vector3.Lerp(RemotePlayer.transform.position, TargetPosition, fracJourney); // 선형보간 캐릭터를 부드럽게 이동하기위해 사용 
                              Vector3 vectorrotation = Vector3.zero;          // 방향을 0으로.
                              RemoteAnimator.Play("Idle");          // 멈춤애니메이션                                               
                          }                  
                    }
                SR.RemoteData.Separator = -1;
              }
          }          
     }

    //모션데이터 받기 - 2000
    void MotionReceive()
    {
        if (SR.RemoteData == null)
        {
        }
        else
        {
            //상대액션데이터값 받아오기 
            if (SR.RemoteData.Separator == 2000)
            {
                if (SR.RemoteData.ID != randomID)    //자기자신이 보낸 값이아니라면 clone2 의 position 변경 
                {
                    if (SR.RemoteData.Motion != null)  
                    {
                    //    RemoteAnimator.Rebind();
                        RemoteAnimator.Play(SR.RemoteData.Motion);  //모션  애니메이션       
                        if (RemoteAnimator.GetCurrentAnimatorStateInfo(0).normalizedTime >= 1f)
                        {
                            Buttons.AbuttonSend = false;
                            Buttons.BbuttonSend = false;
                            Buttons.CbuttonSend = false;
                            Buttons.DbuttonSend = false;
                        }

                    }
                }
                SR.RemoteData.Separator = -1;
            }            
        }
    }

}
