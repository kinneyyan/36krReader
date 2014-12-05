package com.yanshi.my36kr.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.utils.ToastFactory;
import com.yanshi.my36kr.view.MyWebView;

/**
 * 北极社区
 * 作者：yanshi
 * 时间：2014-10-29 10:59
 */
public class BbsFragment extends Fragment {

    private String title;
    private String webUrl;
    private ProgressBar progressBar;
    private MyWebView webView;
    private RelativeLayout handleRl;
    private ImageView backIv, forwardIv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (null != bundle) {
            title = bundle.getString(Constant.TITLE, "");
            webUrl = Constant.FORUM_URL;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_webview, null);
        initView(view);
        initWebView();
        initListener();
        return view;
    }

    private void initWebView() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.contains("/hit")) {
                    view.loadUrl(url);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
                return true;
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                handleRl.setVisibility(View.VISIBLE);
            }
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                ToastFactory.getToast(getActivity(), "Oh no! " + description).show();
            }
        });
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
                super.onProgressChanged(view, newProgress);
            }
        });
        webView.loadUrl(webUrl);
    }

    private void initView(View view) {
        progressBar = (ProgressBar) view.findViewById(R.id.my_webview_pb);
        webView = (MyWebView) view.findViewById(R.id.my_webview_wb);
        handleRl = (RelativeLayout) view.findViewById(R.id.my_webview_rl);
        backIv = (ImageView) view.findViewById(R.id.my_webview_back_iv);
        forwardIv = (ImageView) view.findViewById(R.id.my_webview_forward_iv);
    }

    private void initListener() {
        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoBack()) webView.goBack();
            }
        });
        forwardIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoForward()) webView.goForward();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (null != webView) webView.onPause();
    }

    @Override
    public void onResume() {
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
}