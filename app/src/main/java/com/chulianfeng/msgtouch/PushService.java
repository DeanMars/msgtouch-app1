package com.chulianfeng.msgtouch;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import com.google.protobuf.InvalidProtocolBufferException;
import com.msgtouch.common.proto.MsgTest;
import com.msgtouch.network.settings.SocketClientSetting;
import com.msgtouch.network.socket.NetClientEngine;
import com.msgtouch.network.socket.client.MsgTouchClientApi;
import com.msgtouch.network.socket.listener.AbstractPBMsgPushedListener;
import com.msgtouch.network.socket.packet.MsgPBPacket;


public class PushService extends Service {
    private static int messageNotificationID;
    public PushService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new PushBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        connect();
        return super.onStartCommand(intent, flags, startId);
    }

    private void connect(){
        Thread thread= new Thread(new Runnable(){
            @Override
            public void run() {
                SocketClientSetting socketClientSetting=new SocketClientSetting();
                socketClientSetting.host="192.168.21.40";
                socketClientSetting.port=8001;
                socketClientSetting.timeOutSecond=20;

                try {
                    MsgTouchClientApi msgTouchClientApi= NetClientEngine.startPBPacketClient(socketClientSetting);
                    msgTouchClientApi.addPushedListener(new AbstractPBMsgPushedListener() {
                        @Override
                        public void msgReceived0(MsgPBPacket.Packet.Builder packet) {
                            try {
                                MsgTest.MsgTestRequest msgTestRequest = MsgTest.MsgTestRequest.parseFrom(packet.getEBody());
                                int messageNotificationID=notice(msgTestRequest.getMsg());
                                MsgTest.MsgTestResponse.Builder response= MsgTest.MsgTestResponse.newBuilder();
                                response.setMsg("Client Received:"+response.getMsg());
                                packet.setEBody(response.build().toByteString());

                                Intent intent=new Intent();
                                intent.putExtra("data","Server Pushed:"+msgTestRequest.getMsg());
                                intent.putExtra("messageNotificationID",messageNotificationID);
                                intent.setAction("com.msgtouch.myapplication.PushService");
                                sendBroadcast(intent);
                            } catch (InvalidProtocolBufferException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    ClientApi.getInstance().initCompoent(msgTouchClientApi,"testApp","050200","0000000","v2.6.3","dfsgdsfdsfsdf");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setDaemon(true);
        thread .start();


    }


    private int notice(String context){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        messageNotificationID++;
        Intent messageIntent = new Intent(this,MainActivity.class);
        messageIntent.putExtra("data", "Server Pushed:"+context);
        messageIntent.putExtra("messageNotificationID", messageNotificationID);
        builder.setSmallIcon(R.drawable.niuicon).setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentTitle("新消息")
                .setContentText(context)
                .setTicker(context)
                .setContentIntent(PendingIntent.getActivity(this, 0, messageIntent, 0));

        Notification messageNotification = builder.build();
        //发布消息
        NotificationManager messageNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        messageNotificationManager.notify(messageNotificationID, messageNotification);
        return messageNotificationID;
    }

    public class PushBinder extends Binder{
        public PushService getService(){
            return PushService.this;
        }
    }

}
