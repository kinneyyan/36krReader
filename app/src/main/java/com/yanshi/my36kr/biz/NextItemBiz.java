package com.yanshi.my36kr.biz;

import android.os.Handler;
import android.os.Message;

import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.bean.NextItem;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析html字符串，获取NEXT栏目的feed流
 * Created by kingars on 2014/10/29.
 */
public class NextItemBiz {

    private static List<NextItem> list;

    private static OnParseListener<NextItem> onParseListener;

    private static final int MSG_SUCC = 1;
    private static final int MSG_FAIL = 0;

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

    public static void getFeed(final String html, OnParseListener<NextItem> onParseListener) {
        NextItemBiz.onParseListener = onParseListener;
        new Thread() {
            @Override
            public void run() {
                Document doc = Jsoup.parse(html, Constant.NEXT_URL);
                if (null == doc) {
                    mHandler.sendEmptyMessage(MSG_FAIL);
                    return;
                }
                if (list == null) {
                    list = new ArrayList<NextItem>();
                } else {
                    list.clear();
                }

                Elements product_ele = doc.getElementsByClass("product-item");
                for (int i = 0; i < product_ele.size(); i++) {
                    NextItem nextItem = new NextItem();
                    //投票数
                    Elements vote_ele = product_ele.get(i).getElementsByClass("vote-count");
                    int voteCount = 0;
                    if (vote_ele.size() != 0) voteCount = Integer.parseInt(vote_ele.get(0).text());

                    nextItem.setVoteCount(voteCount);

                    //链接、标题
                    Elements post_ele = product_ele.get(i).getElementsByClass("post-url");
                    String url = "";
                    String title = "";
                    if (post_ele.size() != 0) {
                        url = post_ele.get(0).attr("href").replace("/hit", "");
                        title = post_ele.get(0).text();
                    }
                    if (!url.startsWith("http://")) {
                        url = Constant.NEXT_URL + url;
                    }

                    nextItem.setUrl(url);
                    nextItem.setTitle(title);

                    //内容
                    Elements content_ele = product_ele.get(i).getElementsByClass("post-tagline");
                    String content = "";
                    if (content_ele.size() != 0) content = content_ele.get(0).text();

                    nextItem.setContent(content);

                    //评论数
                    Elements comment_ele = product_ele.get(i).getElementsByClass("product-comment");
                    int commentCount = 0;
                    if (comment_ele.size() != 0)
                        commentCount = Integer.parseInt(comment_ele.get(0).text());

                    nextItem.setCommentCount(commentCount);


                    list.add(nextItem);
                }

                Elements post_ele = doc.getElementsByClass("post");
                //三个日期的字符串
                String[] days = new String[3];
                //每个日期下的条目数
                int[] perCount = new int[3];
                for (int i = 0; i < post_ele.size(); i++) {
                    days[i] = post_ele.get(i).getElementsByTag("small").text();
                    Elements item_ele = post_ele.get(i).getElementsByClass("product-item");
                    perCount[i] = item_ele.size();

                }
                //判断三个日期的条目总数量是否等于之前解析所有条目的数量
                int totalCount = perCount[0] + perCount[1] + perCount[2];
                if (list.size() == totalCount) {
                    for (int i = 0; i < totalCount; i++) {
                        if (i <= perCount[0] - 1) {
                            list.get(i).setDate(days[0]);
                        } else if (i <= totalCount - perCount[2] - 1) {
                            list.get(i).setDate(days[1]);
                        } else if (i <= totalCount - 1) {
                            list.get(i).setDate(days[2]);
                        }
                    }
                }

                if (!list.isEmpty()) {
                    mHandler.sendEmptyMessage(MSG_SUCC);
                } else {
                    mHandler.sendEmptyMessage(MSG_FAIL);
                }
            }
        }.start();
    }

}
