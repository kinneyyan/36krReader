package com.yanshi.my36kr.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.adapter.SmartFragmentStatePagerAdapter;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.view.slidingTab.SlidingTabLayout;

/**
 * 热门标签
 * 作者：yanshi
 * 时间：2014-10-29 10:57
 */
public class TopicFragment extends Fragment {

    private static final String[] TOPICS = new String[]{"创业公司", "产品", "应用软件", "科技",
            "网站", "人物", "品牌", "硬件"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.topic_fragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view) {
        ViewPager mViewPager = (ViewPager) view.findViewById(R.id.topic_vp);
        TopicFragmentPagerAdapter mAdapter = new TopicFragmentPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mAdapter);

//        TabPageIndicator indicator = (TabPageIndicator) view.findViewById(R.id.topic_indicator);
//        indicator.setViewPager(viewPager);

        SlidingTabLayout mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.topic_sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, R.id.tab_indicator_tv);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.primary_color));
        mSlidingTabLayout.setViewPager(mViewPager);
    }

    private class TopicFragmentPagerAdapter extends SmartFragmentStatePagerAdapter {

        public TopicFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (this.getRegisteredFragment(position) != null) {
                return getRegisteredFragment(position);
            } else {
                TopicItemFragment fragment = new TopicItemFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(Constant.POSITION, position);
                fragment.setArguments(bundle);
                return fragment;
            }
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