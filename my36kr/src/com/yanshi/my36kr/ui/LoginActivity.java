package com.yanshi.my36kr.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.biz.UserProxy;
import com.yanshi.my36kr.ui.base.BaseActivity;
import com.yanshi.my36kr.utils.StringUtils;
import com.yanshi.my36kr.utils.ToastFactory;
import com.yanshi.my36kr.view.DeletableEditText;
import com.yanshi.my36kr.view.dialog.LoadingDialogFragment;

/**
 * 登录页
 * 作者：yanshi
 * 时间：2014-12-04 17:25
 */
public class LoginActivity extends BaseActivity {

    DeletableEditText usernameEt, passwordEt;
    Button loginBtn, registerBtn, forgetPassword;
    String username, password;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        initView();
        initListener();

        usernameEt.setText("kingars");
        passwordEt.setText("123456");
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.login_login_btn:
                    username = usernameEt.getText().toString();
                    password = passwordEt.getText().toString();
                    if (StringUtils.isBlank(username)) {
                        ToastFactory.getToast(mContext, getResources().getString(R.string.login_empty_username)).show();
                        usernameEt.setShakeAnimation();
                        return;
                    }
                    if (StringUtils.isBlank(password)) {
                        ToastFactory.getToast(mContext, getResources().getString(R.string.login_empty_password)).show();
                        passwordEt.setShakeAnimation();
                        return;
                    }
                    final LoadingDialogFragment loadingDialogFragment = new LoadingDialogFragment();
                    loadingDialogFragment.setParams(getResources().getString(R.string.loading_dialog_title));
                    loadingDialogFragment.show(LoginActivity.this.getFragmentManager(), "login_loading_dialog");
                    UserProxy.login(mContext, username, password, new UserProxy.LoginListener() {
                        @Override
                        public void onSuccess() {
                            loadingDialogFragment.dismiss();
                            ToastFactory.getToast(mContext, getResources().getString(R.string.login_success)).show();
                            setResult(RESULT_OK);
                            LoginActivity.this.finish();
                        }

                        @Override
                        public void onFailure(String msg) {
                            loadingDialogFragment.dismiss();
                            ToastFactory.getToast(mContext, getResources().getString(R.string.login_failed) + msg).show();
                        }
                    });
                    break;
                case R.id.login_forget_password_btn:
                    break;
                case R.id.login_register_btn:
                    break;
            }
        }
    };

    private void initListener() {
        loginBtn.setOnClickListener(mOnClickListener);
        registerBtn.setOnClickListener(mOnClickListener);
        forgetPassword.setOnClickListener(mOnClickListener);
    }

    private void initView() {
        usernameEt = (DeletableEditText) findViewById(R.id.login_username_et);
        passwordEt = (DeletableEditText) findViewById(R.id.login_password_et);
        loginBtn = (Button) findViewById(R.id.login_login_btn);
        registerBtn = (Button) findViewById(R.id.login_forget_password_btn);
        forgetPassword = (Button) findViewById(R.id.login_register_btn);
    }

}