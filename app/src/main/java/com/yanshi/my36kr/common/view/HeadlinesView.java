package com.yanshi.my36kr.common.view;

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

import java.util.ArrayList;
import java.util.List;

/**
 * auto scroll infinite ViewPager with a title
 * include an open source project: InfiniteViewPager
 * Created by kinneyYan on 2015/07/01.
 */
public class HeadlinesView extends FrameLayout {

    private int scrollDelayTime = 3000;//自动滑动的间隔时间
    private boolean isAutoScroll = true;//是否自动滑动
    private boolean isScrolling = false;//是否正在自动滑动

    private List<NewsItem> list = new ArrayList<>();//数据集
    private InfiniteViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private TextView titleTv;
    private TextView numberTv;

    private Handler mHandler;

    /**
     * 初始化数据
     *
     * @param feedList
     */
    public void refreshData(List<NewsItem> feedList) {
        if (null == feedList || feedList.isEmpty()) return;
        list.clear();
        list.addAll(feedList);

        mPagerAdapter = new InfinitePagerAdapter(new ImagePagerAdapter(getContext(), list));
        mViewPager.setAdapter(mPagerAdapter);

        titleTv.setText(list.get(0 % list.size()).getTitle());
        numberTv.setText(new StringBuilder().append((0) % list.size() + 1).append("/")
                .append(list.size()));

        this.setVisibility(VISIBLE);
    }

    /**
     * 开始自动滑动 used in onResume()
     */
    public void startAutoScroll() {
        if (isAutoScroll && !isScrolling && null != mHandler) {
            mHandler.postDelayed(autoScrollTask, scrollDelayTime);
            isScrolling = true;
        }
    }

    /**
     * 停止自动滑动 used in onPause()
     */
    public void stopAutoScroll() {
        if (null != mHandler) {
            mHandler.removeCallbacks(autoScrollTask);
            isScrolling = false;
        }
    }

    /**
     * 设置是否自动滑动
     *
     * @param isAutoScroll
     */
    public void setAutoScroll(boolean isAutoScroll) {
        this.isAutoScroll = isAutoScroll;
    }

    /**
     * 设置自动滑动的间隔时间
     *
     * @param time_scroll_delay
     */
    public void setAutoScrollDelay(int time_scroll_delay) {
        this.scrollDelayTime = time_scroll_delay;
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

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.view_headlines, this);
        findViews();
        mHandler = new Handler();

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                titleTv.setText(list.get(position % list.size()).getTitle());
                numberTv.setText(new StringBuilder().append((position) % list.size() + 1).append("/")
                        .append(list.size()));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        this.setVisibility(GONE);
    }

    private void findViews() {
        mViewPager = (InfiniteViewPager) findViewById(R.id.headlines_view_vp);
        titleTv = (TextView) findViewById(R.id.headlines_view_title_tv);
        numberTv = (TextView) findViewById(R.id.headlines_view_number_tv);
    }

    private Runnable autoScrollTask = new Runnable() {
        @Override
        public void run() {
            int currentItem = mViewPager.getSuperCurrentItem();
            currentItem++;
            if (Integer.MAX_VALUE == currentItem) currentItem = 0;
            mViewPager.setSuperCurrentItem(currentItem);

            mHandler.postDelayed(this, scrollDelayTime);
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
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

}
