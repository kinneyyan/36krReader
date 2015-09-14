package com.yanshi.my36kr.common.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Toast工具类
 *
 * @author adison
 */
public class ToastUtils {

    private static Toast toast;

    public static void show(Context context, String text) {
        if (toast == null) {
            toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        }
        toast.setText(text);
        toast.show();
    }

}
