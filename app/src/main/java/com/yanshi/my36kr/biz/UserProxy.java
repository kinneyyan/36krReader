package com.yanshi.my36kr.biz;

import android.content.Context;

import com.yanshi.my36kr.R;
import com.yanshi.my36kr.bean.NewsItem;
import com.yanshi.my36kr.bean.NextItem;
import com.yanshi.my36kr.bean.bmob.User;
import com.yanshi.my36kr.common.utils.ToastUtils;
import com.yanshi.my36kr.dao.NewsItemDao;
import com.yanshi.my36kr.dao.NextItemDao;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * 用户操作代理类
 * 作者：yanshi
 * 时间：2014-12-04 17:29
 */
public class UserProxy {

    public interface RegisterListener {
        void onSuccess();

        void onFailure(String msg);
    }

    public interface LoginListener {
        void onSuccess();

        void onFailure(String msg);
    }

    public interface UserUpdateListener {
        void onSuccess();

        void onFailure(String msg);
    }

    /**
     * 注册
     *
     * @param context
     * @param username
     * @param password
     * @param email
     * @param registerListener
     */
    public static void register(Context context, String username, String password, String email, final RegisterListener registerListener) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setSex("男");
        user.signUp(context, new SaveListener() {
            @Override
            public void onSuccess() {
                if(null != registerListener) registerListener.onSuccess();
            }

            @Override
            public void onFailure(int code, String msg) {
                if(null != registerListener) registerListener.onFailure(msg);
            }
        });
    }

    /**
     * 登录
     *
     * @param context
     * @param username
     * @param password
     */
    public static void login(final Context context, String username, String password, final LoginListener loginListener) {
        final User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.login(context, new SaveListener() {
            @Override
            public void onSuccess() {
//                syncFavoriteToLocal(context);

                if (null != loginListener) loginListener.onSuccess();
            }

            @Override
            public void onFailure(int code, String msg) {
                if (null != loginListener) loginListener.onFailure(msg);
            }
        });
    }

    /**
     * 同步线上的收藏数据到本地数据库
     */
    private static void syncFavoriteToLocal(final Context context) {
        BmobQuery<NewsItem> newsQuery = new BmobQuery<>();
        newsQuery.setLimit(100);
        BmobQuery<NextItem> nextBmobQuery = new BmobQuery<>();
        nextBmobQuery.setLimit(100);
        newsQuery.findObjects(context, new FindListener<NewsItem>() {
            @Override
            public void onSuccess(List<NewsItem> list) {
                if (null != list && !list.isEmpty()) {
                    NewsItemDao newsItemDao = new NewsItemDao(context);
                    for (NewsItem item : list) {
                        newsItemDao.add(item);
                    }
                }
            }

            @Override
            public void onError(int i, String s) {
                ToastUtils.show(context, "新闻" + context.getString(R.string.sync_failed) + s);
            }
        });
        nextBmobQuery.findObjects(context, new FindListener<NextItem>() {
            @Override
            public void onSuccess(List<NextItem> list) {
                if (null != list && !list.isEmpty()) {
                    NextItemDao nextItemDao = new NextItemDao(context);
                    for (NextItem item : list) {
                        nextItemDao.add(item);
                    }
                }
            }

            @Override
            public void onError(int i, String s) {
                ToastUtils.show(context, "NEXT" + context.getString(R.string.sync_failed) + s);
            }
        });
    }

    /**
     * 是否登录
     * 判断是否本地磁盘中有一个缓存的用户对象
     *
     * @param context
     * @return
     */
    public static Boolean isLogin(Context context) {
        User user = BmobUser.getCurrentUser(context, User.class);
        return user != null;
    }

    /**
     * 获取当前用户
     *
     * @param context
     * @return
     */
    public static User getCurrentUser(Context context) {
        User user = BmobUser.getCurrentUser(context, User.class);
        if (null != user) {
            return user;
        }
        return null;
    }

    /**
     * 更新用户头像
     *
     * @param context
     * @param user
     * @param avatarFile
     * @param userUpdateListener
     */
    public static void updateUserAvatar(Context context, User user, BmobFile avatarFile, final UserUpdateListener userUpdateListener) {
        if (null != user) {
            if (null != avatarFile) user.setAvatar(avatarFile);
            user.update(context, new UpdateListener() {
                @Override
                public void onSuccess() {
                    if (null != userUpdateListener) userUpdateListener.onSuccess();
                }

                @Override
                public void onFailure(int code, String msg) {
                    if (null != userUpdateListener) userUpdateListener.onFailure(msg);
                }
            });
        }
    }

    /**
     * 更新用户昵称、性别、个性签名
     *
     * @param context
     * @param user
     * @param nickname
     * @param userUpdateListener
     */
    public static void updateUserInfo(Context context, User user, String nickname, String sex, String signature,
                                      final UserUpdateListener userUpdateListener) {
        if (null != user) {
            if (null != nickname) user.setNickname(nickname);
            if (null != sex) user.setSex(sex);
            if (null != signature) user.setSignature(signature);
            user.update(context, new UpdateListener() {
                @Override
                public void onSuccess() {
                    if (null != userUpdateListener) userUpdateListener.onSuccess();
                }

                @Override
                public void onFailure(int code, String msg) {
                    if (null != userUpdateListener) userUpdateListener.onFailure(msg);
                }
            });
        }
    }

    /**
     * 退出登录
     *
     * @param context
     */
    public static void logout(Context context) {
        BmobUser.logOut(context);
        clearDataBase(context);
    }

    /**
     * 清空数据库
     * @param context
     */
    private static void clearDataBase(Context context) {
        NewsItemDao newsItemDao = new NewsItemDao(context);
        NextItemDao nextItemDao = new NextItemDao(context);
        List<NewsItem> news = newsItemDao.getAll();
        List<NextItem> next = nextItemDao.getAll();

        if (null != news && !news.isEmpty()) {
            newsItemDao.deleteBatch(news);
        }
        if (null != next && !next.isEmpty()) {
            nextItemDao.deleteBatch(next);
        }
    }
}
