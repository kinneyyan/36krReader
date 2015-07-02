package com.yanshi.my36kr.biz;

import android.os.Handler;
import android.os.Message;

import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.bean.NewsItem;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析html字符串，获取头条、feed流的业务类
 * 作者：yanshi
 * 时间：2015-06-30 14:59
 */
public class NewsItemBiz {

    private static List<NewsItem> list;

    private static OnParseListener<NewsItem> onParseListener;

    public static final int MSG_SUCC = 1;
    public static final int MSG_FAIL = 0;

    private static DispatchStatusHandler mHandler = new DispatchStatusHandler();

    private static class DispatchStatusHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SUCC:
                    if (null != onParseListener) onParseListener.onParseSuccess(list);
                    break;
                case MSG_FAIL:
                    if (null != onParseListener) onParseListener.onParseFailed();
                    break;
            }
        }
    }

    public static void getFeed(final String html, OnParseListener<NewsItem> onParseListener) {
        NewsItemBiz.onParseListener = onParseListener;
        new Thread() {
            @Override
            public void run() {
                Document doc = Jsoup.parse(html, Constant.INDEX_URL);
                if (null == doc) {
                    mHandler.sendEmptyMessage(MSG_FAIL);
                    return;
                }
                list = new ArrayList<NewsItem>();

                //1-获取头条内容
                //jsoup使用样式class抓取数据时空格的处理：http://www.cnblogs.com/l_dragon/archive/2013/08/27/jsoup.html
                Elements elements1 = doc.select(".scrollers");
                handleHeadLinesElements(list, elements1);
                Elements elements2 = doc.select(".block").select(".article");
                handleHeadLinesElements(list, elements2);

                //2-获取feed流内容
                Elements elements = doc.getElementsByTag("article");
                handleFeedElements(list, elements);

                mHandler.sendEmptyMessage(MSG_SUCC);
            }
        }.start();
    }

    private static void handleHeadLinesElements(List<NewsItem> headLineList, Elements elements) {
        if (null != headLineList && elements.size() > 0) {
            for (int i = 0; i < elements.size(); i++) {
                NewsItem item = new NewsItem();
                Element element = elements.get(i);
                //标题
                Elements elementsSpan = element.getElementsByTag("span");
                if (elementsSpan.size() > 0) {
                    item.setTitle(elementsSpan.text());
                }
                //链接、图片
                Elements elementsLink = element.getElementsByTag("a");
                if (elementsLink.size() > 0) {
                    item.setUrl(elementsLink.attr("href"));
                    item.setImgUrl(elementsLink.attr("data-lazyload"));
                }

                headLineList.add(item);
            }
        }
    }

    private static void handleFeedElements(List<NewsItem> feedList, Elements elements) {
        if (null != feedList && elements.size() > 0) {
            for (int i = 0; i < elements.size(); i++) {
                NewsItem item = new NewsItem();
                Element element = elements.get(i);
                //标题、链接 class="title info_flow_news_title"
                Elements elementsTitle = element.select(".title").select(".info_flow_news_title");
                if (elementsTitle.size() > 0) {
                    item.setTitle(elementsTitle.text());
                    item.setUrl(elementsTitle.attr("href"));
                }
                //图片 class="pic info_flow_news_image"
                Elements elementsPic = element.select(".pic").select(".info_flow_news_image");
                if (elementsPic.size() > 0) {
                    item.setImgUrl(elementsPic.attr("data-lazyload"));
                }
                //时间 class="timeago"
                Elements elementsTime = element.getElementsByClass("timeago");
                if (elementsTime.size() > 0) {
                    item.setDate(elementsTime.text());
                }
                //概要 class="brief"
                Elements elementsBrief = element.getElementsByClass("brief");
                if (elementsBrief.size() > 0) {
                    item.setContent(elementsBrief.text());
                }

                if (null == item.getTitle() && null == item.getUrl() && null == item.getImgUrl() && null == item.getDate()
                        && null == item.getContent()) continue;//过滤掉web端的广告
                feedList.add(item);
            }
        }
    }

}
