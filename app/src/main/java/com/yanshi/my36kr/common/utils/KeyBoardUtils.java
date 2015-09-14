package com.yanshi.my36kr.common.utils;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * 软键盘相关辅助类
 * Created by kingars on 2014/10/25.
 */
public class KeyBoardUtils {
    /**
     * 打卡软键盘
     *
     * @param mEditText 输入框
     * @param mContext  上下文
     */
    public static void openKeybord(EditText mEditText, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditText, InputMethodManager.RESULT_SHOWN);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /**
     * 关闭软键盘
     *
     * @param mEditText 输入框
     * @param mContext  上下文
     */
    public static void closeKeybord(EditText mEditText, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }
}
