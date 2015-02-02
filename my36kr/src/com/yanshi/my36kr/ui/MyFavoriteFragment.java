package com.yanshi.my36kr.ui;

import android.app.Activity;
import android.content.Intent;
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
import com.yanshi.my36kr.biz.UserProxy;
import com.yanshi.my36kr.utils.ScreenUtils;
import com.yanshi.my36kr.utils.ToastFactory;
import com.yanshi.my36kr.view.slidingTab.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 我的收藏页
 * Created by kingars on 2014/12/3.
 */
public class MyFavoriteFragment extends Fragment {

    private static final int REQUEST_CODE = 0x100;
    private MainActivity activity;
    private static final String[] TYPES = new String[]{"文章", "NEXT"};
    private List<Fragment> fragmentList;

//    private User user;
//    private LoadingDialogFragment loadingDialogFragment;
//    private boolean syncNewsOver = false;
//    private boolean syncNextOver = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);
        activity = (MainActivity) getActivity();
        fragmentList = new ArrayList<Fragment>();
        fragmentList.add(null);
        fragmentList.add(null);

//        user = UserProxy.getCurrentUser(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.my_favorite, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);

        if (!UserProxy.isLogin(activity)) {
            ToastFactory.getToast(activity, getString(R.string.personal_login_first)).show();
            startActivityForResult(new Intent(activity, LoginActivity.class), REQUEST_CODE);
        }
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

//        loadingDialogFragment = new LoadingDialogFragment();
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
        if (!hidden) {
            if (UserProxy.isLogin(activity)) {
                for (Fragment fragment : fragmentList) {
                    if (null != fragment && fragment instanceof FragmentInterface) {
                        ((FragmentInterface) fragment).callBack();
                    }
                }
            } else {
                for (Fragment fragment : fragmentList) {
                    if (null != fragment && fragment instanceof FragmentInterface) {
                        ((FragmentInterface) fragment).setNotLoginStr();
                    }
                }
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE == requestCode && Activity.RESULT_OK == resultCode) {
            for (Fragment fragment : fragmentList) {
                if (null != fragment && fragment instanceof FragmentInterface) {
                    ((FragmentInterface) fragment).callBack2();
                }
            }
        }
    }

    //    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.my_favorite_fragment_actions, menu);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (R.id.action_sync == item.getItemId()) {
//            if (!UserProxy.isLogin(activity) || null == user) {
//                ToastFactory.getToast(activity, getString(R.string.personal_login_first)).show();
//                activity.jumpToActivity(activity, LoginActivity.class, null);
//            } else {
//                syncWithBmob();
//            }
//
//        }
//        return super.onOptionsItemSelected(item);
//    }

    /**
     * 与线上Bmob的数据进行同步
     */
//    private void syncWithBmob() {
//        if (null != fragmentList) {
//            loadingDialogFragment.setParams(getString(R.string.syncing));
//            loadingDialogFragment.show(activity.getFragmentManager(), "sync_loading_dialog");
//
//            List<NewsItem> localNews = null;
//            List<NextItem> localNext = null;
//            //获取本地新闻、NEXT收藏的列表
//            for (Fragment fragment : fragmentList) {
//                if (null != fragment && fragment instanceof FavoriteNewsIntf) {
//                    localNews = ((FavoriteNewsIntf) fragment).getNewsList();
//                } else if (null != fragment && fragment instanceof FavoriteNextIntf) {
//                    localNext = ((FavoriteNextIntf) fragment).getNextList();
//                }
//            }
//
//            insertBatchNews(localNews);
//            insertBatchNext(localNext);
//
//        } else {
//            loadingDialogFragment.dismiss();
//        }
//
//    }

    /**
     * 批量插入数据到Bmob的FavoriteNext表中（NEXT）
     *
     * @param localNext
     */
//    private void insertBatchNext(List<NextItem> localNext) {
//        if (null != localNext && !localNext.isEmpty()) {
//            List<BmobObject> batchNext = new ArrayList<BmobObject>();
//            for (NextItem nextItem : localNext) {
//                FavoriteNext fNext = new FavoriteNext();
//                fNext.setTitle(nextItem.getTitle());
//                fNext.setUrl(nextItem.getUrl());
//                fNext.setContent(nextItem.getContent());
//                fNext.setUserId(user.getObjectId());
//
//                batchNext.add(fNext);
//            }
//            new BmobObject().insertBatch(activity, batchNext, new SaveListener() {
//                @Override
//                public void onSuccess() {
//                    ToastFactory.getToast(activity, "NEXT" + getString(R.string.sync_success)).show();
//                    toggleSyncStatus(false);
//                }
//
//                @Override
//                public void onFailure(int i, String s) {
//                    ToastFactory.getToast(activity, "NEXT" + getString(R.string.sync_failed) + s).show();
//                    toggleSyncStatus(false);
//                }
//            });
//
//        } else {
//            ToastFactory.getToast(activity, "NEXT" + getString(R.string.sync_no_data)).show();
//            toggleSyncStatus(false);
//        }
//
//    }

    /**
     * 批量插入数据到Bmob的FavoriteNews表中
     */
//    private void insertBatchNews(List<NewsItem> localNews) {
//        if (null != localNews && !localNews.isEmpty()) {
//            List<BmobObject> batchNews = new ArrayList<BmobObject>();
//            for (NewsItem newsItem : localNews) {
//                FavoriteNews fNews = new FavoriteNews();
//                fNews.setContent(newsItem.getContent());
//                fNews.setImgUrl(newsItem.getImgUrl());
//                fNews.setNewsType(newsItem.getNewsType());
//                fNews.setTitle(newsItem.getTitle());
//                fNews.setUrl(newsItem.getUrl());
//                fNews.setUserId(user.getObjectId());
//
//                batchNews.add(fNews);
//            }
//            new BmobObject().insertBatch(activity, batchNews, new SaveListener() {
//                @Override
//                public void onSuccess() {
//                    ToastFactory.getToast(activity, "新闻" + getString(R.string.sync_success)).show();
//                    toggleSyncStatus(true);
//                }
//
//                @Override
//                public void onFailure(int code, String msg) {
//                    ToastFactory.getToast(activity, "新闻" + getString(R.string.sync_failed) + msg).show();
//                    toggleSyncStatus(true);
//                }
//            });
//        } else {
//            ToastFactory.getToast(activity, "新闻" + getString(R.string.sync_no_data)).show();
//            toggleSyncStatus(true);
//        }
//    }

    /**
     * 控制新闻、NEXT同步的状态
     */
//    public void toggleSyncStatus(boolean isNews) {
//        if (isNews) {
//            syncNewsOver = true;
//            if (syncNextOver && null != loadingDialogFragment) {
//                loadingDialogFragment.dismiss();
//            }
//        } else {
//            syncNextOver = true;
//            if (syncNewsOver && null != loadingDialogFragment) {
//                loadingDialogFragment.dismiss();
//            }
//        }
//    }
}