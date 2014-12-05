package com.yanshi.my36kr.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.bean.bmob.User;
import com.yanshi.my36kr.biz.UserProxy;
import com.yanshi.my36kr.ui.base.BaseActivity;
import com.yanshi.my36kr.utils.StringUtils;
import com.yanshi.my36kr.utils.ToastFactory;
import com.yanshi.my36kr.view.dialog.ConfirmDialogFragment;
import com.yanshi.my36kr.view.dialog.EditTextDialogFragment;
import com.yanshi.my36kr.view.dialog.ListDialogFragment;
import com.yanshi.my36kr.view.dialog.LoadingDialogFragment;

/**
 * 个人中心页面
 * 作者：yanshi
 * 时间：2014-11-28 15:43
 */
public class PersonalActivity extends BaseActivity {

    private static final int REQUEST_CODE = 0x1000;

    ImageView userAvatarIv;//用户头像
    TextView userNicknameTv, userSexTv, userSignatureTv;//昵称、性别、个性签名
    Button userAvatarBtn, userNicknameBtn, userSexBtn, userSignatureBtn, myFavoriteBtn;
    Button userLogoutBtn;//退出账号按钮

    User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal);
        initView();
        initListener();

        if (!UserProxy.isLogin(this)) {
            ToastFactory.getToast(this, getResources().getString(R.string.personal_login_first)).show();
            jumpToActivityForResult(this, LoginActivity.class, REQUEST_CODE, null);
            return;
        }
        user = UserProxy.getCurrentUser(this);
        if (null != user) {
            setUserInfo(user);
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final LoadingDialogFragment loadingDialogFragment = new LoadingDialogFragment();
            loadingDialogFragment.setParams(getResources().getString(R.string.loading_dialog_title));
            EditTextDialogFragment editTextDialogFragment = new EditTextDialogFragment();
            switch (v.getId()) {
                case R.id.personal_my_favorite_btn://我的收藏
                    jumpToActivity(PersonalActivity.this, MyFavoriteActivity.class, null);
                    break;
                case R.id.personal_user_logout_btn://退出登录
                    String title = getResources().getString(R.string.confirm_dialog_title, "退出登录");
                    ConfirmDialogFragment confirmDialogFragment = new ConfirmDialogFragment();
                    confirmDialogFragment.setParams(title, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            UserProxy.logout(PersonalActivity.this);
                            PersonalActivity.this.finish();
                        }
                    }, null);
                    confirmDialogFragment.show(PersonalActivity.this.getFragmentManager(), "personal_confirm_dialog");
                    break;
                case R.id.personal_user_avatar_btn://头像
                    break;
                case R.id.personal_user_nickname_btn://昵称
                    editTextDialogFragment.setEditTextParams(userNicknameTv.getText().toString(), true, 20);
                    editTextDialogFragment.setMyOnClickListener(new EditTextDialogFragment.MyOnClickListener() {
                        @Override
                        public void onClick(String str) {
                            if(null == str || StringUtils.isBlank(str)) return;
                            loadingDialogFragment.show(getFragmentManager(), "set_nickname_loading_dialog");
                            UserProxy.updateUserInfo(mContext, user, str, null, null, new UserProxy.UserUpdateListener() {
                                @Override
                                public void onSuccess() {
                                    loadingDialogFragment.dismiss();
                                    ToastFactory.getToast(mContext, getResources().getString(R.string.personal_update_success)).show();
                                    userNicknameTv.setText(user.getNickname());
                                }
                                @Override
                                public void onFailure(String msg) {
                                    loadingDialogFragment.dismiss();
                                    ToastFactory.getToast(mContext, getResources().getString(R.string.personal_update_failed) + msg).show();
                                }
                            });
                        }
                    });
                    editTextDialogFragment.show(getFragmentManager(), "set_nickname_list_dialog");
                    break;
                case R.id.personal_user_sex_btn://性别
                    final String[] str = {"男", "女"};
                    ListDialogFragment listDialogFragment = new ListDialogFragment();
                    listDialogFragment.setParams(null, str, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            loadingDialogFragment.show(getFragmentManager(), "set_sex_loading_dialog");
                            UserProxy.updateUserInfo(mContext, user, null, str[which], null, new UserProxy.UserUpdateListener() {
                                @Override
                                public void onSuccess() {
                                    loadingDialogFragment.dismiss();
                                    ToastFactory.getToast(mContext, getResources().getString(R.string.personal_update_success)).show();
                                    userSexTv.setText(user.getSex());
                                }
                                @Override
                                public void onFailure(String msg) {
                                    loadingDialogFragment.dismiss();
                                    ToastFactory.getToast(mContext, getResources().getString(R.string.personal_update_failed) + msg).show();
                                }
                            });
                        }
                    });
                    listDialogFragment.show(getSupportFragmentManager(), "set_sex_list_dialog");
                    break;
                case R.id.personal_user_signature_btn://个性签名
                    editTextDialogFragment.setEditTextParams(userSignatureTv.getText().toString(), false, 100);
                    editTextDialogFragment.setMyOnClickListener(new EditTextDialogFragment.MyOnClickListener() {
                        @Override
                        public void onClick(String str) {
                            if(null == str || StringUtils.isBlank(str)) return;
                            loadingDialogFragment.show(getFragmentManager(), "set_signature_loading_dialog");
                            UserProxy.updateUserInfo(mContext, user, null, null, str, new UserProxy.UserUpdateListener() {
                                @Override
                                public void onSuccess() {
                                    loadingDialogFragment.dismiss();
                                    ToastFactory.getToast(mContext, getResources().getString(R.string.personal_update_success)).show();
                                    userSignatureTv.setText(user.getSignature());
                                }
                                @Override
                                public void onFailure(String msg) {
                                    loadingDialogFragment.dismiss();
                                    ToastFactory.getToast(mContext, getResources().getString(R.string.personal_update_failed) + msg).show();
                                }
                            });
                        }
                    });
                    editTextDialogFragment.show(getFragmentManager(), "set_signature_list_dialog");
                    break;
            }
        }
    };

    private void initListener() {
        myFavoriteBtn.setOnClickListener(mOnClickListener);
        userLogoutBtn.setOnClickListener(mOnClickListener);
        userAvatarBtn.setOnClickListener(mOnClickListener);
        userNicknameBtn.setOnClickListener(mOnClickListener);
        userSexBtn.setOnClickListener(mOnClickListener);
        userSignatureBtn.setOnClickListener(mOnClickListener);
    }

    private void initView() {
        userAvatarIv = (ImageView) findViewById(R.id.personal_user_avatar_iv);
        userNicknameTv = (TextView) findViewById(R.id.personal_user_nickname_tv);
        userSexTv = (TextView) findViewById(R.id.personal_user_sex_tv);
        userSignatureTv = (TextView) findViewById(R.id.personal_user_signature_tv);
        userAvatarBtn = (Button) findViewById(R.id.personal_user_avatar_btn);
        userNicknameBtn = (Button) findViewById(R.id.personal_user_nickname_btn);
        userSexBtn = (Button) findViewById(R.id.personal_user_sex_btn);
        userSignatureBtn = (Button) findViewById(R.id.personal_user_signature_btn);
        myFavoriteBtn = (Button) findViewById(R.id.personal_my_favorite_btn);
        userLogoutBtn = (Button) findViewById(R.id.personal_user_logout_btn);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE == requestCode && RESULT_OK == resultCode) {
            user = UserProxy.getCurrentUser(this);
            if (null != user) setUserInfo(user);
        } else {
            this.finish();
        }
    }

    /**
     * 设置登录用户的信息
     */
    private void setUserInfo(User user) {
        //头像
        String imgUrl;
        if (null != user.getAvatar() && null != (imgUrl = user.getAvatar().getFileUrl())) {
            ImageLoader.getInstance().displayImage(imgUrl, userAvatarIv, mMyApplication.getOptions(R.drawable.ic_user_avatar));
        }
        //昵称
        userNicknameTv.setText(user.getNickname());
        //性别
        userSexTv.setText(user.getSex());
        //个性签名
        userSignatureTv.setText(user.getSignature());
    }
}