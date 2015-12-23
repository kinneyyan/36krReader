package com.yanshi.my36kr.activity.base;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ObservableWebView;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.activity.ImageActivity;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.common.utils.NetUtils;
import com.yanshi.my36kr.common.utils.ScreenUtils;

/**
 * desc: 所有使用全屏webView的基类activity
 * author: kinney
 * date: 15/12/23
 */
public abstract class BaseWebViewActivity extends BaseActivity implements ObservableScrollViewCallbacks {

    private LinearLayout toolBarWithPb;
    private ProgressBar progressBar;
    private ObservableWebView webView;
    private Button reloadBtn;

    protected String url;
    private boolean isRedirected;// 要加载的网页是否为重定向的，防止onPageFinished调用2次

    /**
     * 页面开始加载时
     */
    protected abstract void onStartedLoad(WebView webView, String url);

    /**
     * 页面加载完成时
     */
    protected abstract void onFinishedLoad(WebView webView, String url);

    /**
     * 初始化参数,例如url
     */
    protected abstract void initParam();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_webview);
        findViews();
        setListener();
        initWebView();
        initParam();
        if (!TextUtils.isEmpty(url)) {
            webView.loadUrl(url);
        }
    }

    private void initWebView() {
        WebSettings webSettings = webView.getSettings();
        if (NetUtils.isConnected(this)) {// 无网络时读取缓存,无论过期与否
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {// 小于4.4时文字加载完后再加载图片
            webSettings.setLoadsImagesAutomatically(false);
        }
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webView.setScrollViewCallbacks(this);
        webView.addJavascriptInterface(new JsObject(this), "imageListener");

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    if (progressBar.getVisibility() == View.GONE) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                reloadBtn.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                if (!isRedirected) {
                    //Do something you want when starts loading
                    onStartedLoad(view, url);
                }
                isRedirected = false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!view.getSettings().getLoadsImagesAutomatically()) {
                    view.getSettings().setLoadsImagesAutomatically(true);
                }
                if (!isRedirected) {
                    //Do something you want when finished loading
                    onFinishedLoad(view, url);
                    // html加载完成之后，添加监听图片的点击js函数
                    addImageClickListener();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.contains("/hit")) {
                    view.loadUrl(url);
                    isRedirected = true;
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                url = failingUrl;
                webView.setVisibility(View.GONE);
                reloadBtn.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setListener() {
        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl(url);
            }
        });
    }

    private void findViews() {
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        ((LinearLayout.LayoutParams) toolBar.getLayoutParams()).setMargins(0, ScreenUtils.getStatusBarHeight(this), 0, 0);
        setSupportActionBar(toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolBarWithPb = (LinearLayout) findViewById(R.id.base_webview_toolbar_with_progress_bar);
        progressBar = (ProgressBar) this.findViewById(R.id.base_webview_pb);
        webView = (ObservableWebView) this.findViewById(R.id.base_webview_wb);
        reloadBtn = (Button) this.findViewById(R.id.base_webview_reload_btn);
    }

    // 注入js函数监听
    private void addImageClickListener() {
        // 这段js函数的功能就是，遍历所有的img几点，并添加onclick函数，在还是执行的时候调用本地接口传递url过去
        webView.loadUrl("javascript:(function(){" +
                "var objs = document.getElementsByTagName(\"img\"); " +
                "for(var i=0;i<objs.length;i++)  " +
                "{"
                + "    objs[i].onclick=function()  " +
                "    {  "
                + "        window.imageListener.openImage(this.src);  " +
                "    }  " +
                "}" +
                "})()");
    }

    public class JsObject {

        private Context context;

        public JsObject(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void openImage(String img) {
            Intent intent = new Intent(context, ImageActivity.class);
            intent.putExtra(Constant.URL, img);
            context.startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPause() {
        if (null != webView) webView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (null != webView) webView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        if (scrollState == ScrollState.UP) {
            hideViews();
        } else if (scrollState == ScrollState.DOWN) {
            showViews();
        }
    }

    private void hideViews() {
        int pbHeight = progressBar.getVisibility() == View.VISIBLE ? progressBar.getHeight() : 0;
        toolBarWithPb.animate().translationY(-toolBarWithPb.getHeight() + pbHeight).setInterpolator(new AccelerateInterpolator(2));
    }

    private void showViews() {
        toolBarWithPb.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }
}
