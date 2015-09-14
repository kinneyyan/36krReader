package com.yanshi.my36kr.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;

import com.yanshi.my36kr.R;
import com.yanshi.my36kr.adapter.SmartFragmentStatePagerAdapter;
import com.yanshi.my36kr.activity.base.BaseActivity;
import com.yanshi.my36kr.fragment.FavoriteNewsFragment;
import com.yanshi.my36kr.fragment.FavoriteNextFragment;
import com.yanshi.my36kr.common.utils.ScreenUtils;
import com.yanshi.my36kr.common.view.slidingTab.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * desc: 我的收藏页
 * author: shiyan
 * date: 2015/7/7
 */
public class FavoriteActivity extends BaseActivity {

    private Toolbar mToolbar;
    private final String[] TYPES = new String[]{"文章", "NEXT"};
    private List<Fragment> fragmentList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);// 取消透明导航栏
        }
        setContentView(R.layout.activity_my_favorite);
        setSlidr();
        findViews();

        fragmentList = new ArrayList<>();
        fragmentList.add(null);
        fragmentList.add(null);
    }

    private void findViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        if (null != ab) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        ViewPager mViewPager = (ViewPager) findViewById(R.id.my_collection_vp);
        MyCollectionFragmentPagerAdapter mAdapter = new MyCollectionFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);

        //ViewPager导航器
        SlidingTabLayout mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.my_collection_sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, R.id.tab_indicator_tv);
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                if (position == 0) {
                    return getResources().getColor(R.color.primary_color);
                } else if (position == 1) {
                    return getResources().getColor(R.color.next_product_title_color);
                }
                return getResources().getColor(R.color.white);
            }

            @Override
            public int getDividerColor(int position) {
                return getResources().getColor(R.color.half_transparent);
            }
        });
        //设置其宽度为屏幕宽度，官方默认的效果未提供占满屏幕的功能
        mSlidingTabLayout.setViewPager(mViewPager, ScreenUtils.getScreenWidth(this));
    }

    private class MyCollectionFragmentPagerAdapter extends SmartFragmentStatePagerAdapter {

        public MyCollectionFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (this.getRegisteredFragment(position) != null) {
                return getRegisteredFragment(position);
            } else {
                Fragment fragment = fragmentList.get(position);
                if (null == fragment) {
                    switch (position) {
                        case 0:
                            fragment = new FavoriteNewsFragment();
                            break;
                        case 1:
                            fragment = new FavoriteNextFragment();
                            break;
                    }
                    fragmentList.set(position, fragment);
                }
                return fragment;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TYPES[position % TYPES.length];
        }

        @Override
        public int getCount() {
            return TYPES.length;
        }
    }
}
