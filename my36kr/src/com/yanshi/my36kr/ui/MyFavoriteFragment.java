package com.yanshi.my36kr.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.adapter.SmartFragmentStatePagerAdapter;
import com.yanshi.my36kr.bean.FragmentInterface;
import com.yanshi.my36kr.utils.ScreenUtils;
import com.yanshi.my36kr.view.slidingTab.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 我的收藏页
 * Created by kingars on 2014/12/3.
 */
public class MyFavoriteFragment extends Fragment {

    private Activity activity;
    private static final String[] TYPES = new String[]{"文章", "NEXT"};
    private List<Fragment> fragmentList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        fragmentList = new ArrayList<Fragment>();
        fragmentList.add(null);
        fragmentList.add(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.my_favorite, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view) {
        ViewPager mViewPager = (ViewPager) view.findViewById(R.id.my_collection_vp);
        MyCollectionFragmentPagerAdapter mAdapter = new MyCollectionFragmentPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mAdapter);

        SlidingTabLayout mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.my_collection_sliding_tabs);
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
        mSlidingTabLayout.setViewPager(mViewPager, ScreenUtils.getScreenWidth(activity));
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

    @Override
    public void onHiddenChanged(boolean hidden) {
        //当fragment显示时，收藏的数据再加载一遍
        if (!hidden) {
            for (Fragment fragment : fragmentList) {
                if(null != fragment && fragment instanceof FragmentInterface) {
                    ((FragmentInterface)fragment).callBack();
                }
            }
        }
    }
}