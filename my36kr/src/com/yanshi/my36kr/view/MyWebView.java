package com.yanshi.my36kr.view;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import com.yanshi.my36kr.view.observableScrollview.ObservableWebView;

/**
 * 作者：yanshi
 * 时间：2014-10-27 10:59
 */
public class MyWebView extends ObservableWebView {

    private void init(Context context) {

        WebSettings webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setLoadsImagesAutomatically(true);//支持自动加载图片
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

}
