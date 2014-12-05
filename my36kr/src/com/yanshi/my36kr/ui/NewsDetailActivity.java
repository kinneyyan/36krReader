package com.yanshi.my36kr.ui;

import android.app.ActionBar;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.bean.NewsItem;
import com.yanshi.my36kr.bean.NextItem;
import com.yanshi.my36kr.dao.NewsItemDao;
import com.yanshi.my36kr.dao.NextItemDao;
import com.yanshi.my36kr.ui.base.BaseActivity;
import com.yanshi.my36kr.utils.ToastFactory;
import com.yanshi.my36kr.view.MyWebView;
import com.yanshi.my36kr.view.observableScrollview.ObservableScrollViewCallbacks;
import com.yanshi.my36kr.view.observableScrollview.ScrollState;

/**
 * 新闻详情页/NEXT详情页
 * 作者：yanshi
 * 时间：2014-10-27 9:54
 */
public class NewsDetailActivity extends BaseActivity implements ObservableScrollViewCallbacks {

    private ActionBar actionBar;
    private ProgressBar progressBar;
    private MyWebView webView;

    private NewsItem newsItem;
    private NextItem nextItem;
    private String title = "";
    private String webUrl = "";

    private ShareActionProvider mShareActionProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_detail);

        if (savedInstanceState == null) {
            Bundle bundle = getIntent().getExtras();
            if (null != bundle) {
                newsItem = (NewsItem) bundle.getSerializable(Constant.OBJECT_1);
                nextItem = (NextItem) bundle.getSerializable(Constant.OBJECT_2);
                if (null != newsItem) {
                    //新闻
                    title = newsItem.getTitle();
                    webUrl = newsItem.getUrl();
                } else if (null != nextItem) {
                    //NEXT
                    title = nextItem.getTitle();
                    webUrl = nextItem.getUrl();
                }

            }
            initView();
            initWebView();
        }

    }

    private void initWebView() {
        webView.setScrollViewCallbacks(this);
        webView.addJavascriptInterface(new JsObject(this), "imageListener");
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
                // html加载完成之后，添加监听图片的点击js函数
                addImageClickListener();
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                ToastFactory.getToast(NewsDetailActivity.this, "Oh no! " + description).show();
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

        if (null != webUrl) webView.loadUrl(webUrl);

    }

    private void initView() {
        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        progressBar = (ProgressBar) this.findViewById(R.id.news_detail_pb);
        webView = (MyWebView) this.findViewById(R.id.news_detail_wb);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.news_detail_activity_actions, menu);

        MenuItem item = menu.findItem(R.id.action_send);
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();
        if (mShareActionProvider != null) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, title + "【" + getResources().getString(R.string.app_name) + "】" + " - " + webUrl);
            sendIntent.setType("text/plain");
//            startActivity(Intent.createChooser(sendIntent, "分享到"));

            mShareActionProvider.setShareIntent(sendIntent);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_collect:
                if (newsItem != null) {
                    if (new NewsItemDao(this).add(newsItem)) {
                        ToastFactory.getToast(this, getResources().getString(R.string.collect_success)).show();
                    } else {
                        ToastFactory.getToast(this, getResources().getString(R.string.collect_failed)).show();
                    }
                } else if (nextItem != null) {
                    if (new NextItemDao(this).add(nextItem)) {
                        ToastFactory.getToast(this, getResources().getString(R.string.collect_success)).show();
                    } else {
                        ToastFactory.getToast(this, getResources().getString(R.string.collect_failed)).show();
                    }
                }
                break;
            case R.id.action_copy_link:
                if (webUrl != null) {
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboardManager.setPrimaryClip(ClipData.newPlainText(null, webUrl));
                    ToastFactory.getToast(this, getResources().getString(R.string.has_copied_tip)).show();
                }
                break;
            case R.id.action_open_by_browser:
                if (webUrl != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));
                    startActivity(intent);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            // 返回键退回
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

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        if (scrollState == ScrollState.UP) {
            if (actionBar.isShowing()) {
                actionBar.hide();
            }
        } else if (scrollState == ScrollState.DOWN) {
            if (!actionBar.isShowing()) {
                actionBar.show();
            }
        }
    }

    // js通信接口
    public class JsObject {

        private Context context;

        public JsObject(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void openImage(String img) {
            Intent intent = new Intent(context, ImageTerminalActivity.class);
            intent.putExtra(Constant.URL, img);
            context.startActivity(intent);
        }
    }

}