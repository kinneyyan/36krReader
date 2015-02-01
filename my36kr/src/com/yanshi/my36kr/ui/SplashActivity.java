package com.yanshi.my36kr.ui;

import android.os.Bundle;
import android.os.Handler;
import android.view.animation.*;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.bmob.v3.Bmob;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.ui.base.BaseActivity;

/**
 * APP启动页
 * Created by kingars on 2014/10/28.
 */
public class SplashActivity extends BaseActivity {

    private static final long DELAY_TIME = 1000L;

    TextView textView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        Bmob.initialize(this, Constant.BMOB_APPLICATION_ID);
        if (savedInstanceState == null) {
            initView();

            Animation am = AnimationUtils.loadAnimation(this, R.anim.scale_fade_in);
            am.setInterpolator(new BounceInterpolator());
            am.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            jumpToActivity(SplashActivity.this, MainActivity.class, null);
                            SplashActivity.this.finish();
                        }
                    }, DELAY_TIME);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            textView.setAnimation(am);
            am.start();

        }

    }

    private void initView() {
        textView = (TextView) this.findViewById(R.id.splash_tv);
    }
}