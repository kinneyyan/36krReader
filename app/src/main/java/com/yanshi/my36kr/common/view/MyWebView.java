package com.yanshi.my36kr.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;

import com.github.ksoichiro.android.observablescrollview.ObservableWebView;
import com.yanshi.my36kr.common.utils.NetUtils;

/**
 * app自定义的WebView，封装了一些设置
 * 作者：yanshi
 * 时间：2014-10-27 10:59
 */
public class MyWebView extends ObservableWebView {

    private void init(Context context) {
        WebSettings webSettings = getSettings();
        if (NetUtils.isConnected(context)) {
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        webSettings.setJavaScriptEnabled(true);
    }

    public MyWebView(Context context) {
        super(context);
        init(context);
    }

    public MyWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void clearCache() {
        this.clearCache(true);
        this.clearHistory();
    }
}
