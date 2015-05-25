package com.yanshi.my36kr.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.r0adkll.slidr.Slidr;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.biz.UserProxy;
import com.yanshi.my36kr.ui.base.BaseActivity;
import com.yanshi.my36kr.utils.StringUtils;
import com.yanshi.my36kr.utils.ToastFactory;
import com.yanshi.my36kr.view.DeletableEditText;
import com.yanshi.my36kr.view.dialog.LoadingDialogFragment;

/**
 * 登录页
 * Created by kingars on 2014/12/04.
 */
public class LoginActivity extends BaseActivity {

    private DeletableEditText usernameEt, passwordEt;
    private DeletableEditText emailEt;
    private Button loginBtn, registerBtn, forgetPassword;
    private LoadingDialogFragment loadingDialogFragment;

    private String username, password, email;

    private enum UserOperation{
        LOGIN, REGISTER, RESET_PASSWORD
    }

    private UserOperation userOperation = UserOperation.LOGIN;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        findViews();
        setListener();
    }

    /**
     * 判断用户信息填写是否完整
     * @return
     */
    private boolean isUserInfoComplete() {
        username = usernameEt.getText().toString();
        password = passwordEt.getText().toString();
        email = emailEt.getText().toString();

        //登录情况下
        if (StringUtils.isBlank(username)) {
            ToastFactory.getToast(mContext, getString(R.string.login_empty_username)).show();
            usernameEt.setShakeAnimation();
            return false;
        }
        if (StringUtils.isBlank(password)) {
            ToastFactory.getToast(mContext, getString(R.string.login_empty_password)).show();
            passwordEt.setShakeAnimation();
            return false;
        }
        //注册情况下
        if(userOperation == UserOperation.REGISTER) {
            if (StringUtils.isBlank(email)) {
                ToastFactory.getToast(mContext, getString(R.string.login_register_empty_email)).show();
                emailEt.setShakeAnimation();
                return false;
            }
        }
        return true;
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.login_login_btn://登录或注册按钮
                    if(userOperation == UserOperation.LOGIN) {
                        if (!isUserInfoComplete()) return;
                        loadingDialogFragment.show(LoginActivity.this.getFragmentManager(), "login_loading_dialog");
                        UserProxy.login(getApplicationContext(), username, password, new UserProxy.LoginListener() {
                            @Override
                            public void onSuccess() {
                                loadingDialogFragment.dismiss();
                                ToastFactory.getToast(mContext, getString(R.string.login_success)).show();
                                setResult(RESULT_OK);
                                LoginActivity.this.finish();
                            }

                            @Override
                            public void onFailure(String msg) {
                                loadingDialogFragment.dismiss();
                                ToastFactory.getToast(mContext, getString(R.string.login_failed) + msg).show();
                            }
                        });
                    }
                    else if(userOperation == UserOperation.REGISTER) {
                        if (!isUserInfoComplete()) return;
                        loadingDialogFragment.show(LoginActivity.this.getFragmentManager(), "login_register_loading_dialog");
                        UserProxy.register(mContext, username, password, email, new UserProxy.RegisterListener() {
                            @Override
                            public void onSuccess() {
                                loadingDialogFragment.dismiss();
                                ToastFactory.getToast(mContext, getString(R.string.login_register_success)).show();
                                setResult(RESULT_OK);
                                LoginActivity.this.finish();
                            }

                            @Override
                            public void onFailure(String msg) {
                                loadingDialogFragment.dismiss();
                                ToastFactory.getToast(mContext, getString(R.string.login_register_failed) + msg).show();
                            }
                        });
                    }
                    break;
                case R.id.login_register_btn://注册
                    if(userOperation == UserOperation.LOGIN) {
                        userOperation = UserOperation.REGISTER;
                        registerBtn.setText(getString(R.string.login_login));
                    }
                    else if(userOperation == UserOperation.REGISTER) {
                        userOperation = UserOperation.LOGIN;
                        registerBtn.setText(getString(R.string.login_register_now));
                    }
                    updateLayout(userOperation);
                    break;
                case R.id.login_forget_password_btn://忘记密码
                    break;
            }
        }
    };

    private void updateLayout(UserOperation userOperation) {
        if(userOperation == UserOperation.LOGIN) {
            loginBtn.setText(getString(R.string.login_login));
            emailEt.setVisibility(View.GONE);
        }
        else if(userOperation == UserOperation.REGISTER) {
            loginBtn.setText(getString(R.string.login_register));
            emailEt.setVisibility(View.VISIBLE);
        }
    }

    private void setListener() {
        loginBtn.setOnClickListener(mOnClickListener);
        registerBtn.setOnClickListener(mOnClickListener);
        forgetPassword.setOnClickListener(mOnClickListener);
    }

    private void findViews() {
        usernameEt = (DeletableEditText) findViewById(R.id.login_username_et);
        passwordEt = (DeletableEditText) findViewById(R.id.login_password_et);
        emailEt = (DeletableEditText) findViewById(R.id.login_register_email_et);
        loginBtn = (Button) findViewById(R.id.login_login_btn);
        registerBtn = (Button) findViewById(R.id.login_register_btn);
        forgetPassword = (Button) findViewById(R.id.login_forget_password_btn);

        loadingDialogFragment = new LoadingDialogFragment();
        loadingDialogFragment.setParams(getString(R.string.loading_dialog_title));
    }

}