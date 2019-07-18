using Newtonsoft.Json.Linq;
using Newtonsoft.Json;
using UnityEngine;
using System;
using System.Text;

public class SR : MonoBehaviour
{
    public static String rcv;  //서버에서 받은 데이터

    public static RemoteData RemoteData;

    public static PlayerData PlayerData;

    //메시지 송신 
    public static void Send(string values)
    {
        try
        {
            byte[] senddata = Encoding.UTF8.GetBytes(values);
        GameClient.client.Send(senddata, senddata.Length, GameClient.Host, GameClient.Port);

        Debug.Log("[클라] 송신데이터" + values.ToString());
        }
        catch (Exception err)
        {
            Debug.Log("[클라] 전송 오류"+err.ToString());
        }
    }



    //서버가 보낸 메시지 수신 쓰레드
    public static void Receive()
    {
        try
        {
            while (true)
            {
                byte[] rcvdata = GameClient.client.Receive(ref GameClient.remote);
                rcv = Encoding.UTF8.GetString(rcvdata);

                if (rcv != null)
                {
                    JObject msg = JObject.Parse(rcv);

                    if ((int)msg["ID"] == GameClient.randomID)
                    {
                        PlayerData = JsonConvert.DeserializeObject<PlayerData>(rcv); //데이터 Json 파싱

                        Debug.Log("[클라] Player 수신데이터" + "[" + PlayerData.ID + "] : " + PlayerData.Separator);
                    }
                    else
                    {
                        RemoteData = JsonConvert.DeserializeObject<RemoteData>(rcv); //데이터 Json 파싱

                        Debug.Log("[클라] RemotePlayer 수신데이터" + "[" + RemoteData.ID + "] : " + RemoteData.Separator+"/"+ RemoteData.FirstPlayerHP+"/"+RemoteData.SecondPlayerHP);
                    }
                }
            }
        }
        catch (Exception e)
        {
            Debug.Log("[클라] 수신쓰레드 오류" + e);
        }
    }
}
