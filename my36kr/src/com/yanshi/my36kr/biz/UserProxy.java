package com.yanshi.my36kr.biz;

import android.content.Context;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import com.yanshi.my36kr.bean.bmob.User;

/**
 * 用户操作代理类
 * 作者：yanshi
 * 时间：2014-12-04 17:29
 */
public class UserProxy {

    public interface LoginListener {
        public void onSuccess();

        public void onFailure(String msg);
    }
    public interface UserUpdateListener {
        public void onSuccess();

        public void onFailure(String msg);
    }
    /**
     * 登录
     *
     * @param context
     * @param username
     * @param password
     */
    public static void login(Context context, String username, String password, final LoginListener loginListener) {
        final User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.login(context, new SaveListener() {
            @Override
            public void onSuccess() {
                if (null != loginListener) loginListener.onSuccess();
            }

            @Override
            public void onFailure(int code, String msg) {
                if (null != loginListener) loginListener.onFailure(msg);
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
     * 更新用户昵称、性别、个性签名
     * @param context
     * @param user
     * @param nickname
     * @param userUpdateListener
     */
    public static void updateUserInfo(Context context, User user, String nickname, String sex, String signature,
                                      final UserUpdateListener userUpdateListener) {
        if(null != user) {
            if(null != nickname) user.setNickname(nickname);
            if(null != sex) user.setSex(sex);
            if(null != signature) user.setSignature(signature);
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
     * @param context
     */
    public static void logout(Context context) {
        BmobUser.logOut(context);
    }
}
