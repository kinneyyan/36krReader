package com.yanshi.my36kr.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.bean.NewsItem;
import com.yanshi.my36kr.bean.NextItem;
import com.yanshi.my36kr.bean.bmob.User;
import com.yanshi.my36kr.biz.CollectHelper;
import com.yanshi.my36kr.biz.UserProxy;
import com.yanshi.my36kr.dao.NewsItemDao;
import com.yanshi.my36kr.dao.NextItemDao;
import com.yanshi.my36kr.activity.base.BaseActivity;
import com.yanshi.my36kr.common.utils.ScreenUtils;
import com.yanshi.my36kr.common.utils.StringUtils;
import com.yanshi.my36kr.common.utils.ToastUtils;
import com.yanshi.my36kr.common.view.MyWebView;

import cn.bmob.v3.BmobObject;

/**
 * 新闻详情页/NEXT详情页
 * 作者：yanshi
 * 时间：2014-10-27 9:54
 */
public class DetailActivity extends BaseActivity implements ObservableScrollViewCallbacks {

    private LinearLayout toolBarWithPb;
    private Toolbar toolBar;
    private ProgressBar progressBar;
    private MyWebView webView;
    private Button reloadBtn;

    private NewsItem newsItem;
    private NextItem nextItem;
    private String title;
    private String webUrl;

    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        if (UserProxy.isLogin(this)) user = UserProxy.getCurrentUser(this);

        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            newsItem = (NewsItem) bundle.getSerializable(Constant.NEWS_ITEM);
            nextItem = (NextItem) bundle.getSerializable(Constant.NEXT_ITEM);
            if (null != newsItem) { //新闻
                title = newsItem.getTitle();
                webUrl = newsItem.getUrl();
            } else if (null != nextItem) {  //NEXT
                title = nextItem.getTitle();
                webUrl = nextItem.getUrl();
            }

            //重新调用一次onCreateOptionsMenu，更新收藏状态
            this.invalidateOptionsMenu();
        }
        findViews();
        setListener();
        initWebView();
        doRequest();
    }

    private void setListener() {
        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRequest();
            }
        });
    }

    //请求接口获取html
    private void doRequest() {
        if (!StringUtils.isBlank(webUrl)) {
            webView.loadUrl(webUrl);

//            HttpUtils.doGetAsyn(webUrl, new HttpUtils.CallBack() {
//                @Override
//                public void onRequestComplete(String result) {
//                    mHandler.obtainMessage(0, result).sendToTarget();
//                }
//            });
        }
    }

//    private Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            String result = (String) msg.obj;
//            if (!TextUtils.isEmpty(result)) {
//                reloadBtn.setVisibility(View.GONE);
//
//                if (null != newsItem) result = filtHtmlStr(result);
//                webView.loadDataWithBaseURL(webUrl, result, "text/html", "UTF-8", null);
//            }
//            else {
//                reloadBtn.setVisibility(View.VISIBLE);
//            }
//        }
//    };

    //去除html字符串一些标签
//    private String filtHtmlStr(String result) {
//        int start = result.indexOf("<header class=\"header header-normal\">");
//        int end = result.indexOf("</header>", start)+"</header>".length();
//
//        //获取html中文章的第一张图片url
////        int divStart = result.indexOf("<div class=\"single-post-header__headline\">");
////        int divEnd = result.indexOf("</div>", divStart)+"</div>".length();
////        String imgStr = result.substring(divStart, divEnd);
////        int imgStart = imgStr.indexOf("src=\"")+"src=\"".length();
////        int imgEnd = imgStr.indexOf("\"", imgStart);
////        firstImgUrl = imgStr.substring(imgStart, imgEnd);
////        Log.d("yslog", "firstPic url:" + firstImgUrl);
//
////        return result.replace(result.substring(start, end), "").replace(imgStr, "");
//        return result.replace(result.substring(start, end), "");
//    }

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
                ToastUtils.show(DetailActivity.this, "Oh no! " + description);
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

    }

    private void findViews() {
        toolBar = (Toolbar) findViewById(R.id.toolbar);
        ((LinearLayout.LayoutParams) toolBar.getLayoutParams()).setMargins(0, ScreenUtils.getStatusBarHeight(this), 0, 0);
        setSupportActionBar(toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolBarWithPb = (LinearLayout) findViewById(R.id.news_detail_tool_bar_with_progress_bar);
        progressBar = (ProgressBar) this.findViewById(R.id.news_detail_pb);
        webView = (MyWebView) this.findViewById(R.id.news_detail_wb);
        reloadBtn = (Button) this.findViewById(R.id.news_detail_reload_btn);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.news_detail_activity_actions, menu);

        if (UserProxy.isLogin(this)) {
            //收藏按钮
            MenuItem collectItem = menu.findItem(R.id.action_collect);
            if (null != newsItem) {
                NewsItem localItem = NewsItemDao.findItemByTitle(newsItem.getTitle());
                if (null != localItem) {
                    newsItem = localItem;
                    collectItem.setIcon(R.drawable.ic_action_favorite);
                    setCollected(true);
                }
            } else if (null != nextItem) {
                NextItem localItem = NextItemDao.findItemByTitle(nextItem.getTitle());
                if (null != localItem) {
                    collectItem.setIcon(R.drawable.ic_action_favorite);
                    nextItem = localItem;
                    setCollected(true);
                }
            }
        }

        //分享按钮
//        MenuItem item = menu.findItem(R.id.action_send);
//        mShareActionProvider = (ShareActionProvider) item.getActionProvider();
//        if (mShareActionProvider != null) {
//            Intent sendIntent = new Intent();
//            sendIntent.setAction(Intent.ACTION_SEND);
//            sendIntent.putExtra(Intent.EXTRA_TEXT, title + "【" + getResources().getString(R.string.app_name) + "】" + " - " + webUrl);
//            sendIntent.setType("text/plain");
////            startActivity(Intent.createChooser(sendIntent, "分享到"));
//
//            mShareActionProvider.setShareIntent(sendIntent);
//        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_send:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, title + "【" + getResources().getString(R.string.app_name) + "】" + " - " + webUrl);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "分享到"));
                break;
            case R.id.action_collect:
                if (!UserProxy.isLogin(this) || null == user) {
                    ToastUtils.show(this, getString(R.string.personal_login_first));
                    return true;
                }
                //新闻详情
                if (newsItem != null) {
                    if (!isCollected) { //未收藏时
                        CollectHelper.collectNews(this, newsItem, user.getObjectId(), item, new CollectHelper.CollectListener() {
                            @Override
                            public void onSuccess(BmobObject bmobObject) {
                                newsItem.setBmobId(bmobObject.getObjectId());
                                NewsItemDao.add(newsItem);
                                setCollected(true);
                                setResult(RESULT_OK);
                            }

                            @Override
                            public void onFailed() {

                            }
                        });
                    } else {    //已收藏时
                        CollectHelper.unCollectNews(this, newsItem.getBmobId(), item, new CollectHelper.CollectListener() {
                            @Override
                            public void onSuccess(BmobObject bmobObject) {
                                NewsItemDao.deleteByItem(newsItem);
                                setCollected(false);
                                setResult(RESULT_OK);
                            }

                            @Override
                            public void onFailed() {

                            }
                        });
                    }
                    //NEXT详情
                } else if (nextItem != null) {
                    if (!isCollected) { //未收藏时
                        CollectHelper.collectNext(this, nextItem, user.getObjectId(), item, new CollectHelper.CollectListener() {
                            @Override
                            public void onSuccess(BmobObject bmobObject) {
                                nextItem.setBmobId(bmobObject.getObjectId());
                                NextItemDao.add(nextItem);
                                setCollected(true);
                                setResult(RESULT_OK);
                            }

                            @Override
                            public void onFailed() {

                            }
                        });
                    } else {    //已收藏时
                        CollectHelper.unCollectNext(this, nextItem.getBmobId(), item, new CollectHelper.CollectListener() {
                            @Override
                            public void onSuccess(BmobObject bmobObject) {
                                NextItemDao.deleteByItem(nextItem);
                                setCollected(false);
                                setResult(RESULT_OK);
                            }

                            @Override
                            public void onFailed() {

                            }
                        });
                    }
                }
                break;
            case R.id.action_copy_link:
                if (webUrl != null) {
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboardManager.setPrimaryClip(ClipData.newPlainText(null, webUrl));
                    ToastUtils.show(this, getResources().getString(R.string.has_copied_tip));
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

    private boolean isCollected = false;//是否收藏

    private void setCollected(boolean collected) {
        isCollected = collected;
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
//        if (null != mHandler) {
//            mHandler.removeCallbacksAndMessages(null);
//        }
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

    // js通信接口
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

}