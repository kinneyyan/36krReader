package com.yanshi.my36kr.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;

import com.yanshi.my36kr.R;
import com.yanshi.my36kr.activity.base.BaseWebViewActivity;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.bean.NewsItem;
import com.yanshi.my36kr.bean.NextItem;
import com.yanshi.my36kr.bean.bmob.User;
import com.yanshi.my36kr.biz.CollectHelper;
import com.yanshi.my36kr.biz.UserProxy;
import com.yanshi.my36kr.common.utils.ToastUtils;
import com.yanshi.my36kr.dao.NewsItemDao;
import com.yanshi.my36kr.dao.NextItemDao;

import cn.bmob.v3.BmobObject;

/**
 * 新闻详情页/NEXT详情页
 * 作者：yanshi
 * 时间：2014-10-27 9:54
 */
public class DetailActivity extends BaseWebViewActivity {

    private NewsItem newsItem;
    private NextItem nextItem;
    private String title;
    private User user;

    @Override
    protected void initParam() {
        if (UserProxy.getInstance().isLogin(this)) user = UserProxy.getInstance().getCurrentUser(this);

        newsItem = (NewsItem) getIntent().getSerializableExtra(Constant.NEWS_ITEM);
        nextItem = (NextItem) getIntent().getSerializableExtra(Constant.NEXT_ITEM);
        if (null != newsItem) { //新闻
            title = newsItem.getTitle();
            url = newsItem.getUrl();
        } else if (null != nextItem) {  //NEXT
            title = nextItem.getTitle();
            url = nextItem.getUrl();
        }

        //重新调用一次onCreateOptionsMenu，更新收藏状态
        invalidateOptionsMenu();
    }

    @Override
    protected void onStartedLoad(WebView webView, String url) {

    }

    @Override
    protected void onFinishedLoad(WebView webView, String url) {

    }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.news_detail_activity_actions, menu);

        if (UserProxy.getInstance().isLogin(this)) {
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
                sendIntent.putExtra(Intent.EXTRA_TEXT, title + "【" + getResources().getString(R.string.app_name) + "】" + " - " + url);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "分享到"));
                break;
            case R.id.action_collect:
                if (!UserProxy.getInstance().isLogin(this) || null == user) {
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
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, url));
                ToastUtils.show(this, getResources().getString(R.string.has_copied_tip));
                break;
            case R.id.action_open_by_browser:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isCollected = false;//是否收藏

    private void setCollected(boolean collected) {
        isCollected = collected;
    }

}