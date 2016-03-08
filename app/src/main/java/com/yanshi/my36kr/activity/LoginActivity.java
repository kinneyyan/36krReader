package com.yanshi.my36kr.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.yanshi.my36kr.R;
import com.yanshi.my36kr.activity.base.BaseActivity;
import com.yanshi.my36kr.biz.UserProxy;
import com.yanshi.my36kr.common.utils.StringUtils;
import com.yanshi.my36kr.common.utils.ToastUtils;
import com.yanshi.my36kr.common.view.DeletableEditText;
import com.yanshi.my36kr.common.view.dialog.ConfirmDialogFragment;
import com.yanshi.my36kr.common.view.dialog.LoadingDialogFragment;

/**
 * 登录页
 * Created by kingars on 2014/12/04.
 */
public class LoginActivity extends BaseActivity {

    private final int REQUEST_CODE_LOGIN = 100;
    private final int REQUEST_CODE_REGISTER = 101;

    private DeletableEditText usernameEt, passwordEt;
    private DeletableEditText emailEt;
    private Button loginBtn, registerBtn, forgetPassword;
    private LoadingDialogFragment loadingDialogFragment;

    private String username, password, email;

    private enum UserOperation {
        LOGIN, REGISTER, RESET_PASSWORD
    }

    private UserOperation userOperation = UserOperation.LOGIN;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);// 取消透明导航栏
        }
        setContentView(R.layout.activity_login);
        findViews();
        setListener();
    }

    /**
     * 判断用户信息填写是否完整
     *
     * @return
     */
    private boolean isUserInfoComplete() {
        username = usernameEt.getText().toString();
        password = passwordEt.getText().toString();
        email = emailEt.getText().toString();

        //登录情况下
        if (StringUtils.isBlank(username)) {
            ToastUtils.show(mContext, getString(R.string.login_empty_username));
            usernameEt.setShakeAnimation();
            return false;
        }
        if (StringUtils.isBlank(password)) {
            ToastUtils.show(mContext, getString(R.string.login_empty_password));
            passwordEt.setShakeAnimation();
            return false;
        }
        //注册情况下
        if (userOperation == UserOperation.REGISTER) {
            if (StringUtils.isBlank(email)) {
                ToastUtils.show(mContext, getString(R.string.login_register_empty_email));
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
                    if (userOperation == UserOperation.LOGIN) {
                        if (!isUserInfoComplete()) return;
                        requestLogin();
                    } else if (userOperation == UserOperation.REGISTER) {
                        if (!isUserInfoComplete()) return;
                        requestRegister();
                    }
                    break;
                case R.id.login_register_btn://注册
                    if (userOperation == UserOperation.LOGIN) {
                        userOperation = UserOperation.REGISTER;
                        registerBtn.setText(getString(R.string.login_login));
                    } else if (userOperation == UserOperation.REGISTER) {
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

    private void requestRegister() {
        int hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                ConfirmDialogFragment dialog = new ConfirmDialogFragment();
                dialog.setParams("需要获取电话权限来注册", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_REGISTER);
                    }
                }, null);
                dialog.show(getFragmentManager(), "register_confirm_dialog");
                return;
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_REGISTER);
            return;
        }
        register();
    }

    private void register() {
        loadingDialogFragment.show(LoginActivity.this.getFragmentManager(), "login_register_loading_dialog");
        UserProxy.getInstance().register(mContext, username, password, email, new UserProxy.UserProxyListener() {
            @Override
            public void onSuccess() {
                loadingDialogFragment.dismiss();
                ToastUtils.show(mContext, getString(R.string.login_register_success));
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(String msg) {
                loadingDialogFragment.dismiss();
                ToastUtils.show(mContext, getString(R.string.login_register_failed) + msg);
            }
        });
    }

    private void requestLogin() {
        int hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            //该方法只有在用户在上一次已经拒绝过你的这个权限申请返回true;勾选了"不再显示"时返回false
            //你需要给用户一个解释，为什么要授权
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                ConfirmDialogFragment dialog = new ConfirmDialogFragment();
                dialog.setParams("需要获取电话权限来登录", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_LOGIN);
                    }
                }, null);
                dialog.show(getFragmentManager(), "login_confirm_dialog");
                return;
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_LOGIN);
            return;
        }
        login();
    }

    private void login() {
        loadingDialogFragment.show(LoginActivity.this.getFragmentManager(), "login_loading_dialog");
        UserProxy.getInstance().login(getApplicationContext(), username, password, new UserProxy.UserProxyListener() {
            @Override
            public void onSuccess() {
                loadingDialogFragment.dismiss();
                ToastUtils.show(mContext, getString(R.string.login_success));
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(String msg) {
                loadingDialogFragment.dismiss();
                ToastUtils.show(mContext, getString(R.string.login_failed) + msg);
            }
        });
    }

    private void updateLayout(UserOperation userOperation) {
        if (userOperation == UserOperation.LOGIN) {
            loginBtn.setText(getString(R.string.login_login));
            emailEt.setVisibility(View.GONE);
        } else if (userOperation == UserOperation.REGISTER) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOGIN:
                // Permission Granted
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    login();
                }
                // Permission Denied
                else {
                    handlePermissionDenied();
                }
                break;
            case REQUEST_CODE_REGISTER:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    register();
                } else {
                    handlePermissionDenied();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void handlePermissionDenied() {
        //若用户在拒绝权限时勾选了"不再显示",显示对话框提示用户
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
            ConfirmDialogFragment dialog = new ConfirmDialogFragment();
            dialog.setParams("获取电话信息权限被拒绝，请在设置-应用-权限管理中开启。", null, null);
            dialog.show(getFragmentManager(), "login_confirm_dialog");
            return;
        }
        Toast.makeText(this, "获取电话信息权限被拒绝", Toast.LENGTH_SHORT).show();
    }
}