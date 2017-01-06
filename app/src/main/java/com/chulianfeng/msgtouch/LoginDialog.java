package com.chulianfeng.msgtouch;


import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;
import com.msgtouch.network.socket.packet.MsgPBPacket;

import static android.support.v7.appcompat.R.styleable.AlertDialog;


/**
 * Created by Dean on 2016/12/15.
 */
public class LoginDialog {


    public static   void showLoginDialog(final Context context){
        AlertDialog.Builder ad=new AlertDialog.Builder(context);
        ad.setTitle("登录")
        .setIcon(android.R.drawable.ic_dialog_info)
        .setView(R.layout.loginlayout);
        final AlertDialog alertDialog=ad.create();

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,"确定",
                new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText customerIdText=(EditText)alertDialog.findViewById(R.id.gameId);
                        EditText uidText=(EditText)alertDialog.findViewById(R.id.uid);
                        String customerId=customerIdText.getText().toString();
                        String uid=uidText.getText().toString();
                        SoftInputUtils.toggleSoftInput(context);
                        if(customerId==null||uid==null){
                            Toast.makeText(context,"gameId和uid不能为空",Toast.LENGTH_SHORT).show();
                        }else{
                            try {
                                MsgPBPacket.Packet.Builder ret=ClientApi.getInstance().login(Long.parseLong(uid),customerId);
                                if(ret.getRetCode()== MsgPBPacket.RetCode.OK){
                                    Toast.makeText(context,"登录成功",Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(context,"登录失败："+e.getMessage(),Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"取消", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SoftInputUtils.toggleSoftInput(context);
                alertDialog.cancel();
            }
        });
        alertDialog.show();
    }

}
