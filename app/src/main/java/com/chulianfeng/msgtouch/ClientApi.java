package com.chulianfeng.msgtouch;


import com.msgtouch.common.context.Constraint;
import com.msgtouch.network.socket.client.MsgTouchClientApi;
import com.msgtouch.network.socket.packet.MsgPBPacket;

/**
 * Created by Dean on 2016/12/13.
 */
public class ClientApi {

    private String appName;
    private String gameId;
    private String advno;
    private String sdkVersion;
    private String imei;
    private long uid;
    private String customerId;

    private static final ClientApi clientApi=new ClientApi();
    private static MsgTouchClientApi msgTouchClientApi;

    private ClientApi(){}
    public static ClientApi getInstance(){
        return clientApi;
    }

    public void initCompoent(MsgTouchClientApi msgTouchClientApi, String appName, String gameId, String advno,
                             String sdkVersion, String imei){
        this.msgTouchClientApi=msgTouchClientApi;
        this.appName=appName;
        this.gameId=gameId;
        this.advno=advno;
        this.sdkVersion=sdkVersion;
        this.imei=imei;
    }


    public  MsgPBPacket.Packet.Builder sendMsg(MsgPBPacket.Packet.Builder builder, String clusterName, String cmd) throws  Exception{
        MsgPBPacket.Packet.Builder ret=msgTouchClientApi.syncRpcCall(clusterName,cmd,builder);
        return ret;
    }

    public  MsgPBPacket.Packet.Builder login(long uid,String customerId)throws  Exception{
        MsgPBPacket.Packet.Builder builder=newPacket();
        builder.setUid(uid);
        builder.setCustomerId(customerId);
        builder.setCmd(Constraint.TOUCHER_SERVICE_LOGIN);
        MsgPBPacket.Packet.Builder ret= msgTouchClientApi.syncRpcCall(Constraint.MSGTOUCH_TOUCHER,Constraint.TOUCHER_SERVICE_LOGIN,builder);
        if(ret.getRetCode()== MsgPBPacket.RetCode.OK){
            this.uid=uid;
            this.customerId=customerId;
        }
        return ret;
    }


    public  MsgPBPacket.Packet.Builder newPacket(){
        MsgPBPacket.Packet.Builder builder=MsgPBPacket.Packet.newBuilder();
        builder.setUid(uid);
        builder.setCustomerId(customerId==null?"":customerId);
        builder.setPlatform(MsgPBPacket.AppPlatform.Android);
        builder.setAppName(appName==null?"":appName);
        builder.setGameId(gameId==null?"":gameId);
        builder.setAdvno(advno==null?"":advno);
        builder.setSdkVersion(sdkVersion==null?"":sdkVersion);
        builder.setImei(imei==null?"":imei);
        return builder;
    }



}
