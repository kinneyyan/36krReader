package com.yanshi.my36kr.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.adapter.SmartFragmentStatePagerAdapter;
import com.yanshi.my36kr.ui.base.BaseActivity;
import com.yanshi.my36kr.utils.ScreenUtils;
import com.yanshi.my36kr.view.slidingTab.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 我的收藏页
 * Created by kingars on 2014/12/3.
 */
public class MyFavoriteActivity extends BaseActivity {

    private static final String[] TYPES = new String[]{"文章", "NEXT"};
    private List<Fragment> fragmentList;
    private SlidingTabLayout mSlidingTabLayout;
    private MyCollectionFragmentPagerAdapter mAdapter;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentList = new ArrayList<Fragment>();
        fragmentList.add(null);
        fragmentList.add(null);
        setContentView(R.layout.my_favorite);

        initView();
    }

    private void initView() {
        ViewPager mViewPager = (ViewPager) findViewById(R.id.my_collection_vp);
        mAdapter = new MyCollectionFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.my_collection_sliding_tabs);
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
                return getResources().getColor(R.color.darker_gray);
            }
        });
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
                            fragment = new MyFavoriteNewsFragment();
                            break;
                        case 1:
                            fragment = new MyFavoriteNextFragment();
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