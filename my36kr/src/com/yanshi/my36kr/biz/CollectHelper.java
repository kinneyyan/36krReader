package com.yanshi.my36kr.biz;

import android.app.Activity;
import android.text.TextUtils;
import android.view.MenuItem;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.SaveListener;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.bean.NewsItem;
import com.yanshi.my36kr.bean.NextItem;
import com.yanshi.my36kr.bean.bmob.FavoriteNews;
import com.yanshi.my36kr.bean.bmob.FavoriteNext;
import com.yanshi.my36kr.utils.ToastFactory;
import com.yanshi.my36kr.view.dialog.LoadingDialogFragment;

/**
 * 收藏至Bmob的工具类
 * Created by kingars on 2015/2/1.
 */
public class CollectHelper {

    public interface CollectListener {
        public void onSuccess(String objectId);
        public void onFailed();
    }

    /**
     * 收藏新闻
     * @param activity
     * @param newsItem
     * @param userId
     */
    public static void collectNews(final Activity activity, NewsItem newsItem, String userId, final MenuItem menuItem, final CollectListener listener) {
        if (null == activity || null == newsItem || TextUtils.isEmpty(userId) || null == menuItem) return;
        final LoadingDialogFragment dialog = new LoadingDialogFragment();
        dialog.setParams(activity.getString(R.string.loading_dialog_title));
        dialog.show(activity.getFragmentManager(), "collect_news_dialog");

        final FavoriteNews fNews = new FavoriteNews();
        fNews.setTitle(newsItem.getTitle());
        fNews.setContent(newsItem.getContent());
        fNews.setUrl(newsItem.getUrl());
        fNews.setImgUrl(newsItem.getImgUrl());
        fNews.setNewsType(newsItem.getNewsType());
        fNews.setUserId(userId);

        fNews.save(activity, new SaveListener() {
            @Override
            public void onSuccess() {
                ToastFactory.getToast(activity, activity.getString(R.string.collect_success)).show();
                dialog.dismiss();
                menuItem.setIcon(R.drawable.ic_action_favorite);
                if (null != listener) listener.onSuccess(fNews.getObjectId());
            }

            @Override
            public void onFailure(int i, String s) {
                ToastFactory.getToast(activity, activity.getString(R.string.collect_failed) + s).show();
                dialog.dismiss();
                menuItem.setIcon(R.drawable.ic_action_not_favorite);
                if (null != listener) listener.onFailed();
            }
        });

    }

    /**
     * 取消收藏新闻
     * @param activity
     * @param objectId
     * @param menuItem
     */
    public static void unCollectNews(final Activity activity, String objectId, final MenuItem menuItem, final CollectListener listener) {
        if (null == activity || TextUtils.isEmpty(objectId) || null == menuItem) return;
        final LoadingDialogFragment dialog = new LoadingDialogFragment();
        dialog.setParams(activity.getString(R.string.loading_dialog_title));
        dialog.show(activity.getFragmentManager(), "collect_news_dialog");

        final FavoriteNews fNews = new FavoriteNews();
        fNews.setObjectId(objectId);
        fNews.delete(activity, new DeleteListener() {
            @Override
            public void onSuccess() {
                ToastFactory.getToast(activity, activity.getString(R.string.un_collect_success)).show();
                dialog.dismiss();
                menuItem.setIcon(R.drawable.ic_action_not_favorite);
                if (null != listener) listener.onSuccess(fNews.getObjectId());
            }

            @Override
            public void onFailure(int i, String s) {
                ToastFactory.getToast(activity, activity.getString(R.string.un_collect_failed)+s).show();
                dialog.dismiss();
                menuItem.setIcon(R.drawable.ic_action_favorite);
                if (null != listener) listener.onFailed();
            }
        });
    }

    /**
     * 收藏NEXT
     * @param activity
     * @param nextItem
     * @param userId
     * @param menuItem
     */
    public static void collectNext(final Activity activity, NextItem nextItem, String userId, final MenuItem menuItem, final CollectListener listener) {
        if (null == activity || null == nextItem || TextUtils.isEmpty(userId) || null == menuItem) return;
        final LoadingDialogFragment dialog = new LoadingDialogFragment();
        dialog.setParams(activity.getString(R.string.loading_dialog_title));
        dialog.show(activity.getFragmentManager(), "collect_news_dialog");

        final FavoriteNext fNext = new FavoriteNext();
        fNext.setTitle(nextItem.getTitle());
        fNext.setContent(nextItem.getContent());
        fNext.setUrl(nextItem.getUrl());
        fNext.setUserId(userId);

        fNext.save(activity, new SaveListener() {
            @Override
            public void onSuccess() {
                ToastFactory.getToast(activity, activity.getString(R.string.collect_success)).show();
                dialog.dismiss();
                menuItem.setIcon(R.drawable.ic_action_favorite);
                if (null != listener) listener.onSuccess(fNext.getObjectId());
            }

            @Override
            public void onFailure(int i, String s) {
                ToastFactory.getToast(activity, activity.getString(R.string.collect_failed) + s).show();
                dialog.dismiss();
                menuItem.setIcon(R.drawable.ic_action_not_favorite);
                if (null != listener) listener.onFailed();
            }
        });

    }

    /**
     * 取消收藏NEXT
     * @param activity
     * @param objectId
     * @param menuItem
     */
    public static void unCollectNext(final Activity activity, String objectId, final MenuItem menuItem, final CollectListener listener) {
        if (null == activity || TextUtils.isEmpty(objectId) || null == menuItem) return;
        final LoadingDialogFragment dialog = new LoadingDialogFragment();
        dialog.setParams(activity.getString(R.string.loading_dialog_title));
        dialog.show(activity.getFragmentManager(), "collect_news_dialog");

        final FavoriteNext fNext = new FavoriteNext();
        fNext.setObjectId(objectId);
        fNext.delete(activity, new DeleteListener() {
            @Override
            public void onSuccess() {
                ToastFactory.getToast(activity, activity.getString(R.string.un_collect_success)).show();
                dialog.dismiss();
                menuItem.setIcon(R.drawable.ic_action_not_favorite);
                if (null != listener) listener.onSuccess(fNext.getObjectId());
            }

            @Override
            public void onFailure(int i, String s) {
                ToastFactory.getToast(activity, activity.getString(R.string.un_collect_failed)+s).show();
                dialog.dismiss();
                menuItem.setIcon(R.drawable.ic_action_favorite);
                if (null != listener) listener.onFailed();
            }
        });
    }
}
