package com.yanshi.my36kr.biz;

import android.app.Activity;
import android.text.TextUtils;
import android.view.MenuItem;

import com.yanshi.my36kr.R;
import com.yanshi.my36kr.bean.NewsItem;
import com.yanshi.my36kr.bean.NextItem;
import com.yanshi.my36kr.common.utils.ToastUtils;
import com.yanshi.my36kr.common.view.dialog.LoadingDialogFragment;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * 收藏至Bmob的工具类
 * Created by kingars on 2015/2/1.
 */
public class CollectHelper {

    public interface CollectListener {
        void onSuccess(BmobObject bmobObject);
        void onFailed();
    }

    /**
     * 收藏新闻
     * @param activity
     * @param newsItem
     * @param userId
     */
    public static void collectNews(final Activity activity, final NewsItem newsItem, String userId, final MenuItem menuItem, final CollectListener listener) {
        if (null == activity || null == newsItem || TextUtils.isEmpty(userId) || null == menuItem) return;
        final LoadingDialogFragment dialog = new LoadingDialogFragment();
        dialog.setParams(activity.getString(R.string.loading_dialog_title));
        dialog.show(activity.getFragmentManager(), "collect_news_dialog");

        newsItem.setUserId(userId);
        newsItem.save(activity, new SaveListener() {
            @Override
            public void onSuccess() {
                ToastUtils.show(activity, activity.getString(R.string.collect_success));
                dialog.dismiss();
                menuItem.setIcon(R.drawable.ic_action_favorite);
                if (null != listener) listener.onSuccess(newsItem);
            }

            @Override
            public void onFailure(int i, String s) {
                ToastUtils.show(activity, activity.getString(R.string.collect_failed) + s);
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

        final NewsItem newsItem = new NewsItem();
        newsItem.setObjectId(objectId);
        newsItem.delete(activity, new DeleteListener() {
            @Override
            public void onSuccess() {
                ToastUtils.show(activity, activity.getString(R.string.un_collect_success));
                dialog.dismiss();
                menuItem.setIcon(R.drawable.ic_action_not_favorite);
                if (null != listener) listener.onSuccess(newsItem);
            }

            @Override
            public void onFailure(int i, String s) {
                ToastUtils.show(activity, activity.getString(R.string.un_collect_failed) + s);
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
    public static void collectNext(final Activity activity, final NextItem nextItem, String userId, final MenuItem menuItem, final CollectListener listener) {
        if (null == activity || null == nextItem || TextUtils.isEmpty(userId) || null == menuItem) return;
        final LoadingDialogFragment dialog = new LoadingDialogFragment();
        dialog.setParams(activity.getString(R.string.loading_dialog_title));
        dialog.show(activity.getFragmentManager(), "collect_news_dialog");

        nextItem.setUserId(userId);
        nextItem.save(activity, new SaveListener() {
            @Override
            public void onSuccess() {
                ToastUtils.show(activity, activity.getString(R.string.collect_success));
                dialog.dismiss();
                menuItem.setIcon(R.drawable.ic_action_favorite);
                if (null != listener) listener.onSuccess(nextItem);
            }

            @Override
            public void onFailure(int i, String s) {
                ToastUtils.show(activity, activity.getString(R.string.collect_failed) + s);
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

        final NextItem nextItem = new NextItem();
        nextItem.setObjectId(objectId);
        nextItem.delete(activity, new DeleteListener() {
            @Override
            public void onSuccess() {
                ToastUtils.show(activity, activity.getString(R.string.un_collect_success));
                dialog.dismiss();
                menuItem.setIcon(R.drawable.ic_action_not_favorite);
                if (null != listener) listener.onSuccess(nextItem);
            }

            @Override
            public void onFailure(int i, String s) {
                ToastUtils.show(activity, activity.getString(R.string.un_collect_failed) + s);
                dialog.dismiss();
                menuItem.setIcon(R.drawable.ic_action_favorite);
                if (null != listener) listener.onFailed();
            }
        });
    }
}
