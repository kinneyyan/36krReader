package com.yanshi.my36kr.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.activity.base.BaseActivity;
import com.yanshi.my36kr.common.utils.ScreenUtils;
import com.yanshi.my36kr.common.utils.ToastUtils;

import uk.co.senab.photoview.PhotoView;

/**
 * 图片终端页
 * Created by kingars on 2014/11/7.
 */
public class ImageActivity extends BaseActivity {

    private ProgressBar progressBar;
    private PhotoView photoView;
    private String imgUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_terminal);

        if (savedInstanceState == null) {
            Bundle bundle = getIntent().getExtras();
            if (null != bundle) {
                imgUrl = bundle.getString(Constant.URL);
            }
            findViews();

            if (imgUrl != null) {
                ImageLoader.getInstance().displayImage(imgUrl, photoView, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        super.onLoadingStarted(imageUri, view);
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        super.onLoadingFailed(imageUri, view, failReason);
                        progressBar.setVisibility(View.GONE);
                        ToastUtils.show(ImageActivity.this, getString(R.string.image_terminal_img_failed));
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        progressBar.setVisibility(View.GONE);
                    }
                });
            } else {
                progressBar.setVisibility(View.GONE);
                ToastUtils.show(this, getString(R.string.image_terminal_img_failed));
            }
        }

    }

    private void findViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ((LinearLayout.LayoutParams) toolbar.getLayoutParams()).setMargins(0, ScreenUtils.getStatusBarHeight(this), 0, 0);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressBar = (ProgressBar) this.findViewById(R.id.image_terminal_pb);
        photoView = (PhotoView) this.findViewById(R.id.image_terminal_photo_view);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}