package com.yanshi.my36kr.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.yanshi.my36kr.MyApplication;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.ui.ItemDetailActivity;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.bean.NewsItem;
import com.yanshi.my36kr.utils.ScreenUtils;

import java.util.List;

/**
 * 头条内容的view
 * Created by kingars on 2014/10/26.
 * 2015-03-29：去掉焦点图下方4个头条
 */
public class HeadlinesView extends LinearLayout {

    private FrameLayout headlineFl;
    private TextView headlineTv;
    private ImageView headlineIv;

    private List<NewsItem> headlinesList;

    /**
     * 将读取到的数据赋给每个tv
     *
     * @param headlinesList
     */
    public void initData(List<NewsItem> headlinesList) {
        this.headlinesList = headlinesList;
        if (null != headlinesList && headlinesList.size() > 0) {
            headlineTv.setVisibility(VISIBLE);
            headlineTv.setText(headlinesList.get(0).getTitle());

            String imgUrl = headlinesList.get(0).getImgUrl();
            ImageLoader.getInstance().loadImage(imgUrl, MyApplication.getInstance().getOptions(),
                    new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    //此处为了使ImageView的宽高比和原图的宽高比一致（ImageView宽度占满屏幕）
                    int imageWidth = ScreenUtils.getScreenWidth(getContext());
                    int bitmapWidth = loadedImage.getWidth();
                    int bitmapHeight = loadedImage.getHeight();

                    int imageHeight = bitmapHeight * imageWidth / bitmapWidth;
                    ViewGroup.LayoutParams lp = headlineIv.getLayoutParams();
                    lp.width = imageWidth;
                    lp.height = imageHeight;
                    headlineIv.requestLayout();
                    headlineIv.setImageBitmap(loadedImage);
                }
            });
        }
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.headlines_view, this);
        findViews();
        setListener();
    }

    private void setListener() {
        headlineFl.setOnClickListener(mOnClickListener);
    }

    private void findViews() {
        headlineFl = (FrameLayout) findViewById(R.id.headlines_one_fl);
        headlineTv = (TextView) findViewById(R.id.headlines_one);
        headlineIv = (ImageView) findViewById(R.id.headlines_one_iv);
    }

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            NewsItem item;
            Intent intent = new Intent(getContext(), ItemDetailActivity.class);
            switch (view.getId()) {
                case R.id.headlines_one_fl:
                    if ((item = headlinesList.get(0)) != null) {
                        intent.putExtra(Constant.NEWS_ITEM, item);
                    }
                    break;
            }
            getContext().startActivity(intent);
        }
    };

    public HeadlinesView(Context context) {
        super(context);
        init(context);
    }

    public HeadlinesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HeadlinesView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
}
