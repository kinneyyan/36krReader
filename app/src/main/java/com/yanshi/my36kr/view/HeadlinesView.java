package com.yanshi.my36kr.view;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.antonyt.infiniteviewpager.InfinitePagerAdapter;
import com.antonyt.infiniteviewpager.InfiniteViewPager;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.adapter.index.ImagePagerAdapter;
import com.yanshi.my36kr.bean.NewsItem;

import java.util.List;

/**
 * auto scroll infinite ViewPager with a title
 * include an open source project: InfiniteViewPager
 * Created by kinneyYan on 2015/07/01.
 */
public class HeadlinesView extends FrameLayout {

    private Handler mHandler;
    private int autoScrollDelay = 3000;//自动滑动的间隔时间
    private boolean isAutoScroll = true;//是否自动滑动

    private InfiniteViewPager mViewPager;
    private TextView titleTv;
    private TextView numberTv;

    private List<NewsItem> list;

    // 初始化数据
    public void initData(List<NewsItem> feedList) {
        if (null == feedList || feedList.isEmpty()) return;
        this.list = feedList;

        PagerAdapter wrappedAdapter = new InfinitePagerAdapter(new ImagePagerAdapter(getContext(), feedList));
        mViewPager.setAdapter(wrappedAdapter);
        mViewPager.addOnPageChangeListener(new MyOnPageChangeListener());
        titleTv.setText(list.get(0 % list.size()).getTitle());
        numberTv.setText(new StringBuilder().append((0) % list.size() + 1).append("/")
                .append(list.size()));

        this.setVisibility(VISIBLE);
        mHandler = new Handler();
//        ImageLoader.getInstance().loadImage(imgUrl, MyApplication.getInstance().getOptions(),
//                new SimpleImageLoadingListener() {
//                    @Override
//                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                        //此处为了使ImageView的宽高比和原图的宽高比一致（ImageView宽度占满屏幕）
//                        int imageWidth = ScreenUtils.getScreenWidth(getContext());
//                        int bitmapWidth = loadedImage.getWidth();
//                        int bitmapHeight = loadedImage.getHeight();
//
//                        int imageHeight = bitmapHeight * imageWidth / bitmapWidth;
//                        ViewGroup.LayoutParams lp = headlineIv.getLayoutParams();
//                        lp.width = imageWidth;
//                        lp.height = imageHeight;
//                        headlineIv.requestLayout();
//                        headlineIv.setImageBitmap(loadedImage);
//                    }
//                });
    }

    // 启动自动滑动 used after initData()
    public void startAutoScroll() {
        if (null != mHandler) mHandler.postDelayed(autoScrollTask, autoScrollDelay);
    }

    // 关闭自动滑动 used in onPause()
    public void stopAutoScroll() {
        if (null != mHandler) mHandler.removeCallbacks(autoScrollTask);
    }

    // 设置是否自动滑动
    public void setAutoScroll(boolean isAutoScroll) {
        this.isAutoScroll = isAutoScroll;
    }

    // 设置自动滑动的间隔时间
    public void setAutoScrollDelay(int autoScrollDelay) {
        this.autoScrollDelay = autoScrollDelay;
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.headlines_view, this);
        findViews();
        this.setVisibility(GONE);
    }

    private void findViews() {
        mViewPager = (InfiniteViewPager) findViewById(R.id.headlines_view_vp);
        titleTv = (TextView) findViewById(R.id.headlines_view_title_tv);
        numberTv = (TextView) findViewById(R.id.headlines_view_number_tv);
    }

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


    private Runnable autoScrollTask = new Runnable() {
        @Override
        public void run() {
            if(isAutoScroll && null != mViewPager){
                int currentItem = mViewPager.getSuperCurrentItem();
                currentItem++;
                if (Integer.MAX_VALUE == currentItem) currentItem = 0;
                mViewPager.setSuperCurrentItem(currentItem);
                startAutoScroll();
            }
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                stopAutoScroll();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                startAutoScroll();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            titleTv.setText(list.get(position % list.size()).getTitle());
            numberTv.setText(new StringBuilder().append((position) % list.size() + 1).append("/")
                    .append(list.size()));
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

}
