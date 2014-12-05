package com.yanshi.my36kr.bean.bmob;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobRelation;

/**
 * 用户
 * 作者：yanshi
 * 时间：2014-12-04 16:01
 */
public class User extends BmobUser {

    private BmobFile avatar;
    private String nickname;
    private String sex;
    private String signature;
    private BmobRelation favoriteNews;
    private BmobRelation favoriteNextProduct;

    public BmobFile getAvatar() {
        return avatar;
    }

    public void setAvatar(BmobFile avatar) {
        this.avatar = avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public BmobRelation getFavoriteNews() {
        return favoriteNews;
    }

    public void setFavoriteNews(BmobRelation favoriteNews) {
        this.favoriteNews = favoriteNews;
    }

    public BmobRelation getFavoriteNextProduct() {
        return favoriteNextProduct;
    }

    public void setFavoriteNextProduct(BmobRelation favoriteNextProduct) {
        this.favoriteNextProduct = favoriteNextProduct;
    }

    @Override
    public String toString() {
        return "User{" +
                "avatar=" + avatar +
                ", nickname='" + nickname + '\'' +
                ", sex='" + sex + '\'' +
                ", signature='" + signature + '\'' +
                ", favoriteNews=" + favoriteNews +
                ", favoriteNextProduct=" + favoriteNextProduct +
                '}';
    }
}
