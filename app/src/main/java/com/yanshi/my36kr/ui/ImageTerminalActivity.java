package com.yanshi.my36kr.ui;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.ui.base.BaseActivity;
import uk.co.senab.photoview.PhotoView;

/**
 * 图片终端页
 * Created by kingars on 2014/11/7.
 */
public class ImageTerminalActivity extends BaseActivity {

    private PhotoView photoView;
    private String imgUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_terminal);

        if (savedInstanceState == null) {
            Bundle bundle = getIntent().getExtras();
            if (null != bundle) {
                imgUrl = bundle.getString(Constant.URL);
            }
            findViews();

            if (imgUrl != null) {
                ImageLoader.getInstance().displayImage(imgUrl, photoView, mMyApplication.getOptions());
            } else {
                photoView.setImageResource(R.drawable.ic_app_logo);
            }
        }

    }

    private void findViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        photoView = (PhotoView) this.findViewById(R.id.image_terminal_photo_view);
    }

}