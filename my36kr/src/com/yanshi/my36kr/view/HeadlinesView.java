package com.yanshi.my36kr.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yanshi.my36kr.MyApplication;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.ui.NewsDetailActivity;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.bean.NewsItem;

import java.util.List;

/**
 * 5个头条内容的view
 * Created by kingars on 2014/10/26.
 */
public class HeadlinesView extends LinearLayout {

    private FrameLayout headline1Fl;
    private TextView headline1Tv;
    private ImageView headline1Iv;
    private TextView headline2Tv;
    private TextView headline3Tv;
    private TextView headline4Tv;
    private TextView headline5Tv;

    private List<NewsItem> headlinesList;

    /**
     * 将读取到的数据赋给每个tv
     *
     * @param headlinesList
     */
    public void initData(List<NewsItem> headlinesList) {
        this.headlinesList = headlinesList;
        if (null != headlinesList && headlinesList.size() == 5) {
            headline1Tv.setText(headlinesList.get(0).getTitle());
            String imgUrl = headlinesList.get(0).getImgUrl();
            ImageLoader.getInstance().displayImage(imgUrl, headline1Iv, MyApplication.getInstance().getOptions());
            headline2Tv.setText(headlinesList.get(1).getTitle());
            headline3Tv.setText(headlinesList.get(2).getTitle());
            headline4Tv.setText(headlinesList.get(3).getTitle());
            headline5Tv.setText(headlinesList.get(4).getTitle());

        }
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.headlines_view, this);
        initView();
        initListener();
    }

    private void initListener() {
        headline1Fl.setOnClickListener(mOnClickListener);
        headline2Tv.setOnClickListener(mOnClickListener);
        headline3Tv.setOnClickListener(mOnClickListener);
        headline4Tv.setOnClickListener(mOnClickListener);
        headline5Tv.setOnClickListener(mOnClickListener);
    }

    private void initView() {
        headline1Fl = (FrameLayout) findViewById(R.id.headlines_one_fl);
        headline1Tv = (TextView) findViewById(R.id.headlines_one);
        headline1Iv = (ImageView) findViewById(R.id.headlines_one_iv);
        headline2Tv = (TextView) findViewById(R.id.headlines_two);
        headline3Tv = (TextView) findViewById(R.id.headlines_three);
        headline4Tv = (TextView) findViewById(R.id.headlines_four);
        headline5Tv = (TextView) findViewById(R.id.headlines_five);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            NewsItem item;
            Intent intent = new Intent(getContext(), NewsDetailActivity.class);
            switch (view.getId()) {
                case R.id.headlines_one_fl:
                    if ((item = headlinesList.get(0)) != null) {
                        intent.putExtra(Constant.OBJECT_1, item);
//                        intent.putExtra(Constant.TITLE, item.getTitle());
//                        intent.putExtra(Constant.URL, item.getUrl());
                    }
                    break;
                case R.id.headlines_two:
                    if ((item = headlinesList.get(1)) != null) {
                        intent.putExtra(Constant.OBJECT_1, item);
//                        intent.putExtra(Constant.TITLE, item.getTitle());
//                        intent.putExtra(Constant.URL, item.getUrl());
                    }
                    break;
                case R.id.headlines_three:
                    if ((item = headlinesList.get(2)) != null) {
                        intent.putExtra(Constant.OBJECT_1, item);
//                        intent.putExtra(Constant.TITLE, item.getTitle());
//                        intent.putExtra(Constant.URL, item.getUrl());
                    }
                    break;
                case R.id.headlines_four:
                    if ((item = headlinesList.get(3)) != null) {
                        intent.putExtra(Constant.OBJECT_1, item);
//                        intent.putExtra(Constant.TITLE, item.getTitle());
//                        intent.putExtra(Constant.URL, item.getUrl());
                    }
                    break;
                case R.id.headlines_five:
                    if ((item = headlinesList.get(4)) != null) {
                        intent.putExtra(Constant.OBJECT_1, item);
//                        intent.putExtra(Constant.TITLE, item.getTitle());
//                        intent.putExtra(Constant.URL, item.getUrl());
                    }
                    break;
                default:
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
