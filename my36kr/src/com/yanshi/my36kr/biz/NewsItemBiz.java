package com.yanshi.my36kr.biz;

import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.bean.NewsItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析html字符串，获取头条内容、timeline内容的业务类
 * 作者：yanshi
 * 时间：2014-10-24 15:35
 */
public class NewsItemBiz {

    /**
     * 获取头条内容
     * @param html 要解析的html
     * @return
     */
    public List<NewsItem> getHeadLines(String html) {
        List<NewsItem> newsItems = new ArrayList<NewsItem>();
        NewsItem newsItem;

        if (null != html) {
            Document doc = Jsoup.parse(html, Constant.INDEX_URL);
            /**
             * 头条内容
             */
            Elements headlines = doc.getElementsByClass("headline__news");

            for (int i = 0; i < headlines.size(); i++) {
                newsItem = new NewsItem();

                Element headlines_ele = headlines.get(i);
                /**
                 * 标题
                 */
                Elements h1_ele = headlines_ele.getElementsByTag("h1");
                if (h1_ele.size() != 0) {
                    String title = h1_ele.get(0).text();

                    newsItem.setTitle(title);
                }

                /**
                 * 链接
                 */
                Elements a_ele = headlines_ele.getElementsByTag("a");
                if (a_ele.size() != 0) {
                    String attr = a_ele.attr("href");
                    if (!attr.startsWith("http://")) {
                        attr = Constant.INDEX_URL + attr;
                    }

                    newsItem.setUrl(attr);
                }

                /**
                 * 图片
                 */
                Elements imgs_ele = headlines_ele.getElementsByTag("img");
                if (imgs_ele.size() != 0) {
                    String imgUrl = imgs_ele.get(0).attr("data-src");

                    newsItem.setImgUrl(imgUrl);
                }

                newsItems.add(newsItem);
            }
        }
        return newsItems;
    }

    /**
     * 获取timeline新闻内容
     *
     * @param html 要解析的html
     * @return
     */
    public List<NewsItem> getNewsItems(String html) {
        List<NewsItem> newsItems = new ArrayList<NewsItem>();
        NewsItem newsItem;

        if (null != html) {
            Document doc = Jsoup.parse(html, Constant.INDEX_URL);
            /**
             * timeline内容
             */
            Elements items = doc.getElementsByClass("posts");

            for (int i = 0; i < items.size(); i++) {
                newsItem = new NewsItem();

                Element items_ele = items.get(i);
                /**
                 * 类型
                 */
                Elements newsType_a_ele = items_ele.getElementsByTag("a");
                if (newsType_a_ele.size() != 0) {
                    String newsType = newsType_a_ele.get(0).text();

                    if(newsType.contains("8点1氪晚间版")) {
                        newsItem.setNewsType("8点1氪晚间版");
                    } else {
                        newsItem.setNewsType(newsType);
                    }
                }

                /**
                 * 日期、作者信息
                 */
                Elements postmeta_ele = items_ele.getElementsByClass("postmeta");
                Elements timeago_ele = items_ele.getElementsByClass("timeago");
                String info;
                if (timeago_ele.size() != 0) {
                    info = postmeta_ele.text() + timeago_ele.attr("title");
                } else {
                    info = postmeta_ele.text();
                }
                if ("".equals(info.trim())) {
                    info = "";
                }

                newsItem.setDate(info);

                /**
                 * 图片url
                 */
                Elements imgs_ele = items_ele.getElementsByTag("img");
                if (imgs_ele.size() != 0) {
                    String imgUrl = imgs_ele.get(0).attr("data-src");

                    if (null != imgUrl && !"".equals(imgUrl.trim())) {
                        newsItem.setImgUrl(imgUrl);
                    } else {
                        String nightImgUrl = imgs_ele.get(0).attr("src");
                        newsItem.setImgUrl(nightImgUrl);
                    }

                }

                /**
                 * 标题
                 */
                Elements h1_ele = items_ele.getElementsByTag("h1");
                if (h1_ele.size() != 0) {
                    String title = h1_ele.get(0).text();

                    newsItem.setTitle(title);

                    Elements h1_ele_a = h1_ele.get(0).getElementsByTag("a");
                    /**
                     * 链接
                     */
                    if (null != h1_ele_a && h1_ele_a.size() != 0) {
                        String attr = h1_ele_a.get(0).attr("href");
                        if (!attr.startsWith("http://")) {
                            attr = Constant.INDEX_URL + attr;
                        }
                        newsItem.setUrl(attr);
                    } else {
                        Element a_ele = items_ele.getElementsByTag("a").get(0);
                        String attr = a_ele.attr("href");
                        if (!attr.startsWith("http://")) {
                            attr = Constant.INDEX_URL + attr;
                        }
                        newsItem.setUrl(attr);
                    }
                }

                /**
                 * 摘要
                 */
                Elements p_ele = items_ele.getElementsByTag("p");
                if (p_ele.size() != 0) {
                    String content = p_ele.text();

                    newsItem.setContent(content);
                }

                newsItems.add(newsItem);
            }
        }
        return newsItems;
    }

}
