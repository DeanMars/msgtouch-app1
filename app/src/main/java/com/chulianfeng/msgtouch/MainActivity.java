package com.chulianfeng.msgtouch;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.*;
import android.os.IBinder;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.msgtouch.common.context.Constraint;
import com.msgtouch.common.proto.MsgTest;
import com.msgtouch.network.socket.packet.MsgPBPacket;


import java.util.List;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {
    private EditText inputView;
    private TextView console;
    private Button sendButton,clearButton,loginButton;

    private ActivityManager myAM;
    private NotificationManager messageNotificationManager;

   // private PushService pushService;
    private ServiceConnection serviceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //PushService.PushBinder binder=(PushService.PushBinder)service;
            //返回一个MsgService对象
            //pushService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myAM = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);


        inputView=(EditText)findViewById(R.id.inputText);
        console=(TextView)findViewById(R.id.console);
        sendButton=(Button)findViewById(R.id.sendButton);
        loginButton=(Button)findViewById(R.id.loginButton);
        clearButton=(Button)findViewById(R.id.clearButton);

        console.setMovementMethod(ScrollingMovementMethod.getInstance());

        startService();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoftInputUtils.toggleSoftInput(MainActivity.this);
                String text=inputView.getText().toString();
                console.append("\n");
                console.append("User Send:"+text);
                console.setText(console.getText());


                MsgPBPacket.Packet.Builder packet=ClientApi.getInstance().newPacket();
                MsgTest.MsgTestRequest.Builder req=MsgTest.MsgTestRequest.newBuilder();
                req.setMsg(text);
                packet.setEBody(req.build().toByteString());
                try {
                    MsgPBPacket.Packet.Builder ret= ClientApi.getInstance().sendMsg(packet, Constraint.MSGTOUCH_TOUCHER,"pbTest");
                    consoleRet(ret);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginDialog.showLoginDialog(MainActivity.this);
            }
        });
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                console.setText("");
            }
        });

        registerReceiver(new MyBroadcastReceiver(),new IntentFilter("com.msgtouch.myapplication.PushService"));
    }


    private void consoleRet(MsgPBPacket.Packet.Builder ret) throws Exception{
        if(ret.getRetCode()==MsgPBPacket.RetCode.OK){
            MsgTest.MsgTestResponse response= MsgTest.MsgTestResponse.parseFrom(ret.getEBody());
            String msg= response.getMsg();
            console.setText(console.getText()+"\n"+msg);
        }else if(ret.getRetCode()==MsgPBPacket.RetCode.ERROR_NO_SESSION){
            Toast.makeText(MainActivity.this,"请先登录",Toast.LENGTH_SHORT).show();
        }else if(ret.getRetCode()==MsgPBPacket.RetCode.EXCEPTION){
            Toast.makeText(MainActivity.this,"服务器错误："+ret.getError(),Toast.LENGTH_SHORT).show();
        }else if(ret.getRetCode()==MsgPBPacket.RetCode.PROTO_ERROR){
            Toast.makeText(MainActivity.this,"请求数据错误，请重试"+ret.getError(),Toast.LENGTH_SHORT).show();
        }

    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int messageNotificationID=intent.getIntExtra("messageNotificationID",0);
            if(messageNotificationID!=0){
                messageNotificationManager.cancel(messageNotificationID);
            }
            String msg= intent.getStringExtra("data");
            console.setText(console.getText()+"\n"+msg);
        }
    }



    @Override
    protected void onResume() {
        logPushMsg();
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        //unbindService(serviceConnection);
        super.onDestroy();
    }

    private void logPushMsg(){
        Intent intent=getIntent();
        if(null!=intent) {
            String data=intent.getStringExtra("data");
            int messageNotificationID=intent.getIntExtra("messageNotificationID",0);
            if(0!=messageNotificationID){
                messageNotificationManager.cancel(messageNotificationID);
            }
            if(null!=data){
                console.setText(console.getText()+"\n"+data);
            }

        }
    }



    private void startService(){
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(100);
        boolean isWork=false;
        for(ActivityManager.RunningServiceInfo serviceInfo:myList){
            String serviceName=serviceInfo.service.getClassName();
            Log.i(TAG, "startService: ACTIVITY_SERVICE serviceName="+serviceName);
            if("com.msgtouch.myapplication.PushService".equals(serviceName)){
                isWork = true;
                break;
            }
        }
        if(!isWork) {
            this.startService(new Intent(this, PushService.class));
            //Intent intent = new Intent(this,PushService.class);
            //bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

    }


}
