package com.yanshi.my36kr.ui;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;

import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yanshi.my36kr.MyApplication;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.adapter.CommonAdapter;
import com.yanshi.my36kr.adapter.ViewHolder;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.bean.NewsItem;
import com.yanshi.my36kr.biz.NewsItemBiz;
import com.yanshi.my36kr.utils.*;
import com.yanshi.my36kr.view.HeadlinesView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 36氪网站改版，分页加载的代码已注释
 * Updated by Kinney on 2015/06/30
 * 首页
 * Created by kingars on 2014/10/25.
 */
public class IndexFragment extends Fragment {

    private Activity activity;
    private ACache mCache;
//    private boolean needLoadMore = true;//是否需要加载更多
//    private int currentPage = 1;//当前页数

    private HeadlinesView headlinesView;
    private SwipeRefreshLayout mSrl;
    private ObservableListView mListView;
    //    private FooterView footerView;
    private CommonAdapter<NewsItem> mAdapter;
    private Button reloadBtn;

    private List<NewsItem> headlinesList = new ArrayList<>();
    private List<NewsItem> timelinesList = new ArrayList<>();
    private List<NewsItem> loadingHeadlinesList;//加载时的list
    private List<NewsItem> loadingTimelinesList;//加载时的list

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        mCache = ACache.get(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.index_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        findViews(view);
        setListener();

        loadCache();//加载缓存
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setViewsVisible(true, true, false);
                loadData();//加载网络
            }
        }, 358);

    }

    private void findViews(View view) {
        mSrl = (SwipeRefreshLayout) view.findViewById(R.id.index_content_sl);
        mSrl.setColorSchemeResources(R.color.secondary_color, R.color.primary_color, R.color.next_product_title_color, R.color.next_product_count_bg);
        mListView = (ObservableListView) view.findViewById(R.id.index_timeline_lv);
        reloadBtn = (Button) view.findViewById(R.id.index_reload_btn);
//        footerView = new FooterView(activity);
//        mListView.addFooterView(footerView);
        headlinesView = new HeadlinesView(activity);
        mListView.addHeaderView(headlinesView);
        mListView.setAdapter(mAdapter = new CommonAdapter<NewsItem>(activity, timelinesList, R.layout.index_timeline_item) {
            @Override
            public void convert(ViewHolder helper, NewsItem item) {
                helper.setText(R.id.index_timeline_item_title_tv, item.getTitle());
                helper.setText(R.id.index_timeline_item_content_tv, item.getContent());
                helper.setText(R.id.index_timeline_item_info_tv, item.getDate());
                ImageView imageView = helper.getView(R.id.index_timeline_item_iv);
                ImageLoader.getInstance().displayImage(item.getImgUrl(), imageView, MyApplication.getInstance().getOptionsWithRoundedCorner());
            }
        });

    }

    private void setListener() {
        mSrl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                currentPage = 1;
                loadData();
            }
        });
        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });
//        footerView.setListener(new FooterView.OnLoadMoreListener() {
//            @Override
//            public void onLoadMore() {
//                if (needLoadMore) {
//                    needLoadMore = false;
//
//                    loadMoreData();
//                }
//            }
//        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int size = timelinesList.size();
                int realPosition = position - mListView.getHeaderViewsCount();
                NewsItem item;
                if (size > 0 && (item = timelinesList.get(realPosition % size)) != null) {
                    Intent intent = new Intent(activity, ItemDetailActivity.class);
                    intent.putExtra(Constant.NEWS_ITEM, item);
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * 从缓存加载数据
     */
    private void loadCache() {
        if (getJsonToDataList()) {
            headlinesView.initData(headlinesList);
            if (null != mAdapter) mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 从网络加载数据
     */
    private void loadData() {
        HttpUtils.doGetAsyn(Constant.INDEX_URL, new HttpUtils.CallBack() {
            @Override
            public void onRequestComplete(String result) {
                if (null != result) {
                    loadingHeadlinesList = NewsItemBiz.getHeadLines(result);
                    loadingTimelinesList = NewsItemBiz.getFeed(result);

                    mCache.put(getClass().getSimpleName(), saveToJSONObj(loadingHeadlinesList, loadingTimelinesList), ACache.TIME_DAY * 3);
                    mHandler.sendEmptyMessage(LOAD_COMPLETE);
                }
            }
        });
    }

    /**
     * 加载更多
     */
//    private void loadMoreData() {
//        currentPage += 1;
//        HttpUtils.doGetAsyn(initUrl(currentPage), new HttpUtils.CallBack() {
//            @Override
//            public void onRequestComplete(String result) {
//                if (null != result) {
//                    timelinesList.addAll(NewsItemBiz.getFeed(result));
//
//                    mHandler.sendEmptyMessage(LOAD_MORE_COMPLETE);
//                }
//            }
//        });
//    }

    private final int LOAD_COMPLETE = 0X110;
//    private final int LOAD_MORE_COMPLETE = 0X111;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOAD_COMPLETE:
                    if (loadingHeadlinesList != null && !loadingHeadlinesList.isEmpty()
                            && loadingTimelinesList != null && !loadingTimelinesList.isEmpty()) {
                        headlinesList.clear();
                        headlinesList.addAll(loadingHeadlinesList);
                        headlinesView.initData(headlinesList);
                        timelinesList.clear();
                        timelinesList.addAll(loadingTimelinesList);
                    }

                    if (null != mAdapter) mAdapter.notifyDataSetChanged();
                    setViewsVisible(false, true, false);
                    break;
//                case LOAD_MORE_COMPLETE:
//                    if (null != mAdapter) mAdapter.notifyDataSetChanged();
//
//                    footerView.setLoadMoreState();
//
//                    needLoadMore = true;
//                    break;
            }
        }
    };

//    private String initUrl(int currentPage) {
//        currentPage = currentPage > 0 ? currentPage : 1;
//        return Constant.INDEX_URL + "/?page=" + currentPage + "#lastest";
//    }

    /**
     * 读取缓存文件转换成json，放置于list中
     *
     * @return 返回是否有缓存
     */
    public boolean getJsonToDataList() {
        JSONObject jsonObject = mCache.getAsJSONObject(getClass().getSimpleName());
        if (null != jsonObject) {
            try {
                JSONArray headlinesAr = jsonObject.getJSONArray("headlines");
                headlinesList.clear();
                for (int i = 0; i < headlinesAr.length(); i++) {
                    JSONObject headline = headlinesAr.getJSONObject(i);
                    NewsItem item = NewsItem.parse(headline);

                    headlinesList.add(item);
                }
                JSONArray timelinesAr = jsonObject.getJSONArray("timelines");
                timelinesList.clear();
                for (int i = 0; i < timelinesAr.length(); i++) {
                    JSONObject timeline = timelinesAr.getJSONObject(i);
                    NewsItem item = NewsItem.parse(timeline);

                    timelinesList.add(item);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    /**
     * 转换list中的数据为json
     *
     * @param headlinesList
     * @param timelinesList
     * @return
     */
    private JSONObject saveToJSONObj(List<NewsItem> headlinesList, List<NewsItem> timelinesList) {
        JSONObject outerJsonObj = new JSONObject();
        JSONArray headlineJsonAr = new JSONArray();
        for (NewsItem headlinesItem : headlinesList) {
            JSONObject jsonObject = headlinesItem.toJSONObj();
            headlineJsonAr.put(jsonObject);
        }
        JSONArray timelineJsonAr = new JSONArray();
        for (NewsItem timelinesItem : timelinesList) {
            JSONObject jsonObject = timelinesItem.toJSONObj();
            timelineJsonAr.put(jsonObject);
        }
        try {
            outerJsonObj.put("headlines", headlineJsonAr);
            outerJsonObj.put("timelines", timelineJsonAr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return outerJsonObj;
    }

    //设置各种View的显示状态
    private void setViewsVisible(boolean mSrl, boolean mListView, boolean reloadBtn) {
        if (mSrl) {
            if (null != this.mSrl) this.mSrl.setRefreshing(true);
        } else {
            if (null != this.mSrl) this.mSrl.setRefreshing(false);
        }
        if (mListView) {
            if (null != this.mListView) this.mListView.setVisibility(View.VISIBLE);
        } else {
            if (null != this.mListView) this.mListView.setVisibility(View.GONE);
        }
        if (reloadBtn) {
            if (null != this.reloadBtn) this.reloadBtn.setVisibility(View.VISIBLE);
        } else {
            if (null != this.reloadBtn) this.reloadBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

}