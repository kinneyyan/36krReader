package com.yanshi.my36kr.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yanshi.my36kr.R;
import com.yanshi.my36kr.adapter.SmartFragmentStatePagerAdapter;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.view.slidingTab.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 热门标签
 * Created by kingars on 2014/10/29.
 */
public class TopicFragment extends Fragment {

    private final String[] TOPICS = new String[]{"国内公司", "国外公司", "国内资讯", "国外资讯",
            "专栏", "存档"};

    private List<TopicItemFragment> fragmentList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentList = new ArrayList<>();
        for (int i = 0; i < TOPICS.length; i++) {
            fragmentList.add(null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.topic_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
    }

    private void findViews(View view) {
        ViewPager mViewPager = (ViewPager) view.findViewById(R.id.topic_vp);
        TopicFragmentPagerAdapter mAdapter = new TopicFragmentPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mAdapter);

        SlidingTabLayout mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.topic_sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, R.id.tab_indicator_tv);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.primary_color));
        mSlidingTabLayout.setViewPager(mViewPager);
    }

    private class TopicFragmentPagerAdapter extends FragmentStatePagerAdapter {

        public TopicFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            TopicItemFragment fragment = fragmentList.get(position);
            if (null == fragment) {
                fragment = new TopicItemFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(Constant.POSITION, position);
                fragment.setArguments(bundle);

                fragmentList.set(position, fragment);
            }
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TOPICS[position % TOPICS.length];
        }

        @Override
        public int getCount() {
            return TOPICS.length;
        }
    }

}