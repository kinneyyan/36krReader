package com.yanshi.my36kr.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yanshi.my36kr.MyApplication;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.adapter.CommonAdapter;
import com.yanshi.my36kr.adapter.ViewHolder;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.bean.FavoriteNewsIntf;
import com.yanshi.my36kr.bean.FragmentInterface;
import com.yanshi.my36kr.bean.NewsItem;
import com.yanshi.my36kr.bean.bmob.FavoriteNews;
import com.yanshi.my36kr.bean.bmob.User;
import com.yanshi.my36kr.biz.UserProxy;
import com.yanshi.my36kr.dao.NewsItemDao;
import com.yanshi.my36kr.utils.NetUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 我的收藏-新闻
 * 作者：yanshi
 * 时间：2014-11-04 12:08
 */
public class MyFavoriteNewsFragment extends Fragment implements FragmentInterface, FavoriteNewsIntf {

    private final int REQUEST_CODE = 0X100;

    private Activity activity;
    private ListView mListView;
    private CommonAdapter<NewsItem> mAdapter;
    private TextView emptyTv;

    private List<NewsItem> newsItemList = new ArrayList<NewsItem>();
    private List<NewsItem> loadingNewsItemList = new ArrayList<NewsItem>();//加载时候的list

    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this.getActivity();
        if (UserProxy.isLogin(activity)) user = UserProxy.getCurrentUser(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.my_favorite_item, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        findViews(view);
        setListener();

        if (!UserProxy.isLogin(activity) || null == user) {
            if (null != emptyTv) {
                emptyTv.setVisibility(View.VISIBLE);
                emptyTv.setText(getString(R.string.personal_login_first));
            }
            return;
        }
        if (NetUtils.isConnected(activity)) {
            loadDataByNet();
        } else {
            loadLocalData();
        }
    }

    private void findViews(View view) {
        mListView = (ListView) view.findViewById(R.id.my_collection_item_lv);
        mListView.setAdapter(mAdapter = new CommonAdapter<NewsItem>(activity, newsItemList, R.layout.index_timeline_item) {
            @Override
            public void convert(ViewHolder helper, NewsItem item) {
                helper.setText(R.id.index_timeline_item_title_tv, item.getTitle());
                helper.setText(R.id.index_timeline_item_content_tv, item.getContent());
                helper.setText(R.id.index_timeline_item_type_tv, item.getNewsType());
                ImageView imageView = helper.getView(R.id.index_timeline_item_iv);
                ImageLoader.getInstance().displayImage(item.getImgUrl(), imageView, MyApplication.getInstance().getOptions());

                TextView newsTypeTv = helper.getView(R.id.index_timeline_item_type_tv);
                if (null != newsTypeTv) {
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    newsTypeTv.setLayoutParams(layoutParams);
                }
                //隐藏时间的TextView
                TextView tv = helper.getView(R.id.index_timeline_item_info_tv);
                if (null != tv) tv.setVisibility(View.GONE);
            }
        });
        emptyTv = (TextView) view.findViewById(R.id.my_collection_item_empty_tv);
    }

    private void setListener() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int size = newsItemList.size();
                int realPosition = position - mListView.getHeaderViewsCount();
                NewsItem item;
                if (size > 0 && (item = newsItemList.get(realPosition % size)) != null) {
                    Intent intent = new Intent(activity, ItemDetailActivity.class);
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
        NewsItemDao newsItemDao = new NewsItemDao(activity);
        loadingNewsItemList.clear();
        loadingNewsItemList.addAll(newsItemDao.getAll());
        if (!loadingNewsItemList.isEmpty()) {
            Collections.reverse(loadingNewsItemList);
            newsItemList.clear();
            newsItemList.addAll(loadingNewsItemList);

            if (null != mAdapter) {
                mAdapter.notifyDataSetChanged();
            }
            emptyTv.setVisibility(View.GONE);
        } else {
            //无数据
            emptyTv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 读取网络数据
     */
    private void loadDataByNet() {

        BmobQuery<FavoriteNews> query = new BmobQuery<FavoriteNews>();
        query.setLimit(100);//设置单次查询返回的条目数
        query.addWhereEqualTo("userId", user.getObjectId());
        query.findObjects(activity, new FindListener<FavoriteNews>() {
            @Override
            public void onSuccess(List<FavoriteNews> list) {
                if (null != list && !list.isEmpty()) {
                    newsItemList.clear();
                    for (FavoriteNews fNews : list) {
                        NewsItem newsItem = new NewsItem();
                        newsItem.setTitle(fNews.getTitle());
                        newsItem.setContent(fNews.getContent());
                        newsItem.setUrl(fNews.getUrl());
                        newsItem.setImgUrl(fNews.getImgUrl());
                        newsItem.setNewsType(fNews.getNewsType());
                        newsItemList.add(newsItem);
                    }
                    Collections.reverse(newsItemList);
                    if (null != mAdapter) {
                        mAdapter.notifyDataSetChanged();
                    }
                    emptyTv.setVisibility(View.GONE);
                } else {
                    //无数据
                    emptyTv.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(int i, String s) {
                Log.e("yslog", s);
                loadLocalData();
            }
        });

    }

    @Override
    public void callBack() {
        loadLocalData();
    }

    @Override
    public void callBack2() {
        user = UserProxy.getCurrentUser(activity);
        if (null != user) {
            loadDataByNet();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE == requestCode && Activity.RESULT_OK == resultCode) {
            loadLocalData();
        }
    }

    @Override
    public List<NewsItem> getNewsList() {
        return newsItemList;
    }

    @Override
    public void setNotLoginStr() {
        if (null != emptyTv) {
            emptyTv.setVisibility(View.VISIBLE);
            emptyTv.setText(getString(R.string.personal_login_first));
        }
        newsItemList.clear();
        if (null != mAdapter) mAdapter.notifyDataSetChanged();
    }
}
