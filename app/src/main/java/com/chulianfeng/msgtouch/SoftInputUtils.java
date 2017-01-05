package com.chulianfeng.msgtouch;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Dean on 2016/12/23.
 */
public class SoftInputUtils {

    public static void toggleSoftInput(Context context){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm.isActive()) {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
