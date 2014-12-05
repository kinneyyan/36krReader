package com.yanshi.my36kr.bean.bmob;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobRelation;

/**
 * 用户收藏的NEXT条目
 * 作者：yanshi
 * 时间：2014-12-04 16:14
 */
public class FavoriteNextProduct extends BmobObject {

    private String title;
    private String content;
    private String url;
    private BmobRelation user;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public BmobRelation getUser() {
        return user;
    }

    public void setUser(BmobRelation user) {
        this.user = user;
    }
}
