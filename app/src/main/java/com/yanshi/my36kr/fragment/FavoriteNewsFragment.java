package com.yanshi.my36kr.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.yanshi.my36kr.MyApplication;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.activity.DetailActivity;
import com.yanshi.my36kr.adapter.CommonAdapter;
import com.yanshi.my36kr.adapter.ViewHolder;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.bean.FragmentInterface;
import com.yanshi.my36kr.bean.NewsItem;
import com.yanshi.my36kr.bean.bmob.User;
import com.yanshi.my36kr.biz.UserProxy;
import com.yanshi.my36kr.common.utils.NetUtils;
import com.yanshi.my36kr.dao.NewsItemDao;
import com.yanshi.my36kr.fragment.base.BaseFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

/**
 * 我的收藏-新闻
 * 作者：Kinney
 * 时间：2014-11-04 12:08
 */
public class FavoriteNewsFragment extends BaseFragment implements FragmentInterface {

    private static final int REQUEST_CODE = 0X100;

    private ListView mListView;
    private CommonAdapter<NewsItem> mAdapter;
    private TextView tipTv;//数据为空or未登录时 的提示TextView

    private List<NewsItem> newsItemList = new ArrayList<>();

    @Override
    public View onViewInit(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_favorite, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        findViews(view);
        setListener();

        if (!UserProxy.getInstance().isLogin(activity)) {
            setTipTvNotLogin();
            return;
        }
        if (NetUtils.isConnected(activity)) {
            setTipTvloading();
            loadDataByNet();
        }
        //无网络时读取本地数据库
        else {
            loadLocalData();
        }
    }

    private void findViews(View view) {
        mListView = (ListView) view.findViewById(R.id.my_collection_item_lv);
        mListView.setAdapter(mAdapter = new CommonAdapter<NewsItem>(activity, newsItemList, R.layout.view_favourite_news_item) {
            @Override
            public void convert(ViewHolder helper, NewsItem item) {
                helper.setText(R.id.index_timeline_item_title_tv, item.getTitle());
                helper.setText(R.id.index_timeline_item_content_tv, item.getContent());
                ImageView imageView = helper.getView(R.id.index_timeline_item_iv);
                ImageLoader.getInstance().displayImage(item.getImgUrl(), imageView, MyApplication.getInstance().getOptionsWithRoundedCorner());
            }
        });
        tipTv = (TextView) view.findViewById(R.id.my_collection_item_tip_tv);
    }

    private void setListener() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int size = newsItemList.size();
                int realPosition = position - mListView.getHeaderViewsCount();
                NewsItem item;
                if (size > 0 && (item = newsItemList.get(realPosition % size)) != null) {
                    Intent intent = new Intent(activity, DetailActivity.class);
                    intent.putExtra(Constant.NEWS_ITEM, item);
                    startActivityForResult(intent, REQUEST_CODE);
                }
            }
        });
    }

    /**
     * 读取本地数据库数据
     */
    private void loadLocalData() {
        newsItemList.clear();
        newsItemList.addAll(NewsItemDao.getAll());
        Collections.reverse(newsItemList);
        if (!newsItemList.isEmpty()) {
            if (null != mAdapter) {
                mAdapter.notifyDataSetChanged();
            }
            tipTv.setVisibility(View.GONE);
        } else {
            //无数据
            setTipTvEmptyData();
        }
    }

    /**
     * 读取网络数据
     */
    private void loadDataByNet() {
        User user = UserProxy.getInstance().getCurrentUser(activity);
        if (null == user) {
            setTipTvNotLogin();
            return;
        }
        BmobQuery<NewsItem> query = new BmobQuery<>();
        query.setLimit(100);//设置单次查询返回的条目数
        query.addWhereEqualTo("userId", user.getObjectId());
        query.findObjects(activity, new FindListener<NewsItem>() {
            @Override
            public void onSuccess(List<NewsItem> list) {
                if (null != list && !list.isEmpty()) {
                    newsItemList.clear();
                    for (int i = 0; i < list.size(); i++) {
                        NewsItem newsItem = list.get(i);
                        newsItem.setBmobId(newsItem.getObjectId());
                        newsItemList.add(newsItem);
                    }
                    Collections.reverse(newsItemList);
                    if (null != mAdapter) {
                        mAdapter.notifyDataSetChanged();
                    }
                    tipTv.setVisibility(View.GONE);

                    updateDataBase(list);
                } else {
                    //无数据
                    setTipTvEmptyData();
                }
            }

            @Override
            public void onError(int i, String s) {
                loadLocalData();
            }
        });

    }

    //更新本地数据库中的数据
    private void updateDataBase(List<NewsItem> list) {
        NewsItemDao.clear();
        for (int i = 0; i < list.size(); i++) {
            NewsItemDao.add(list.get(i));
        }
    }

    //设置未登录时的提示
    private void setTipTvNotLogin() {
        if (null != tipTv) {
            tipTv.setVisibility(View.VISIBLE);
            tipTv.setText(getString(R.string.personal_login_first));
        }
    }

    //设置无数据时的提示
    private void setTipTvEmptyData() {
        if (null != tipTv) {
            tipTv.setVisibility(View.VISIBLE);
            tipTv.setText(getString(R.string.collect_empty_list));
        }
    }

    //设置正在加载的提示
    private void setTipTvloading() {
        if (null != tipTv) {
            tipTv.setVisibility(View.VISIBLE);
            tipTv.setText(getString(R.string.app_loading_tv_text));
        }
    }

    @Override
    public void callBack() {
        if (UserProxy.getInstance().isLogin(activity)) {
            if (newsItemList.isEmpty()) {
                setTipTvloading();
                loadDataByNet();
            } else {
                loadLocalData();
            }
        } else {
            setTipTvNotLogin();
            newsItemList.clear();
            if (null != mAdapter) mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE == requestCode && Activity.RESULT_OK == resultCode) {
            loadLocalData();
        }
    }

}
