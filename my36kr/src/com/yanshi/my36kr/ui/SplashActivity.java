package com.yanshi.my36kr.ui;

import android.os.Bundle;
import android.os.Handler;
import android.widget.RelativeLayout;
import cn.bmob.v3.Bmob;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.ui.base.BaseActivity;

/**
 * APP启动页
 * Created by kingars on 2014/10/28.
 */
public class SplashActivity extends BaseActivity {

    private static final long DELAY_TIME = 2000L;

    private RelativeLayout contentRl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        Bmob.initialize(this, Constant.BMOB_APPLICATION_ID);
        if (savedInstanceState == null) {
            initView();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    jumpToActivity(SplashActivity.this, MainActivity.class, null);
                    SplashActivity.this.finish();
                }
            }, DELAY_TIME);

        }

    }

    private void initView() {
        contentRl = (RelativeLayout) this.findViewById(R.id.splash_content_rl);
    }
}