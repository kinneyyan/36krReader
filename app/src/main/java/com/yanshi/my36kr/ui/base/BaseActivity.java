package com.yanshi.my36kr.ui.base;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.yanshi.my36kr.MyApplication;

/**
 * Activity基类
 * Created by kingars on 2014/12/02.
 */
public class BaseActivity extends AppCompatActivity {

    protected MyApplication mMyApplication;
    protected Resources mResources;
    protected Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initConfigure();
    }

    private void initConfigure() {
        mContext = this;
        if (null == mMyApplication) {
            mMyApplication = MyApplication.getInstance();
        }
        mResources = getResources();
    }

    /**
     * 开启滑动关闭activity的功能，供子类调用
     */
    public void setSlidr() {
        SlidrConfig config = new SlidrConfig.Builder()
                .sensitivity(0.2f)//默认的敏感度为1f容易误操作
                .build();
        Slidr.attach(this, config);
    }

    /**
     * Activity跳转
     *
     * @param context
     * @param targetActivity
     * @param bundle
     */
    public void jumpToActivity(Context context, Class<?> targetActivity, Bundle bundle) {
        Intent intent = new Intent(context, targetActivity);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    /**
     * startActivityForResult
     *
     * @param context
     * @param targetActivity
     * @param requestCode
     * @param bundle
     */
    public void jumpToActivityForResult(Context context, Class<?> targetActivity, int requestCode, Bundle bundle) {
        Intent intent = new Intent(context, targetActivity);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        startActivityForResult(intent, requestCode);
    }

}