package com.antonyt.infiniteviewpager;


import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;

/**
 * A {@link ViewPager} that allows pseudo-infinite paging with a wrap-around effect. Should be used with an {@link
 * InfinitePagerAdapter}.
 *
 * 添加了根据滑动方向拦截父控件touch事件
 * Updated by kinneyYan on 2015/07/02.
 */
public class InfiniteViewPager extends ViewPager {

    public InfiniteViewPager(Context context) {
        super(context);
    }

    public InfiniteViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        super.setAdapter(adapter);
        // offset first element so that we can scroll to the left
        setCurrentItem(0);
    }

    @Override
    public void setCurrentItem(int item) {
        // offset the current item to ensure there is space to scroll
        setCurrentItem(item, false);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        if (getAdapter().getCount() == 0) {
            super.setCurrentItem(item, smoothScroll);
            return;
        }
        item = getOffsetAmount() + (item % getAdapter().getCount());
        super.setCurrentItem(item, smoothScroll);
    }

    // 提供直接调用父类的方法
    public void setSuperCurrentItem(int item) {
        super.setCurrentItem(item);
    }

    // 提供直接调用父类的方法
    public int getSuperCurrentItem() {
        return super.getCurrentItem();
    }

    @Override
    public int getCurrentItem() {
        if (getAdapter().getCount() == 0) {
            return super.getCurrentItem();
        }
        int position = super.getCurrentItem();
        if (getAdapter() instanceof InfinitePagerAdapter) {
            InfinitePagerAdapter infAdapter = (InfinitePagerAdapter) getAdapter();
            // Return the actual item position in the data backing InfinitePagerAdapter
            return (position % infAdapter.getRealCount());
        } else {
            return super.getCurrentItem();
        }
    }

    private int getOffsetAmount() {
        if (getAdapter().getCount() == 0) {
            return 0;
        }
        if (getAdapter() instanceof InfinitePagerAdapter) {
            InfinitePagerAdapter infAdapter = (InfinitePagerAdapter) getAdapter();
            // allow for 100 back cycles from the beginning
            // should be enough to create an illusion of infinity
            // warning: scrolling to very high values (1,000,000+) results in
            // strange drawing behaviour
            return infAdapter.getRealCount() * 100;
        } else {
            return 0;
        }
    }

    // 滑动距离、坐标
    private float xDistance, yDistance, xLast, yLast;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDistance = yDistance = 0f;
                xLast = ev.getX();
                yLast = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float xCur = ev.getX();
                float yCur = ev.getY();
                // 计算在X和Y方向的偏移量
                xDistance += Math.abs(xCur - xLast);
                yDistance += Math.abs(yCur - yLast);
                xLast = xCur;
                yLast = yCur;

                ViewParent viewParent = getParent();
                if (null != viewParent) {
                    // 横向滑动小于纵向滑动时不截断事件
                    if (xDistance < yDistance) {
                        viewParent.requestDisallowInterceptTouchEvent(false);
                    } else {
                        viewParent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

}
