package com.yanshi.my36kr.ui;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.yanshi.my36kr.MyApplication;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.adapter.index.RvAdapter;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.bean.NewsItem;
import com.yanshi.my36kr.biz.NewsItemBiz;
import com.yanshi.my36kr.biz.OnParseListener;
import com.yanshi.my36kr.utils.ToastFactory;
import com.yanshi.my36kr.view.HeadlinesView;

import java.util.ArrayList;
import java.util.List;

import kale.recycler.ExRecyclerView;
import kale.recycler.OnRecyclerViewScrollListener;

/**
 * 36氪网站改版，分页加载的代码已注释
 * Updated by Kinney on 2015/06/30
 * 首页
 * Created by kingars on 2014/10/25.
 */
public class IndexFragment extends Fragment {

    private Activity activity;
    private Handler mHandler;
//    private boolean needLoadMore = true;//是否需要加载更多
//    private int currentPage = 1;//当前页数

    private HeadlinesView headlinesView;
    private SwipeRefreshLayout mSrl;
    private ExRecyclerView mRecyclerView;
    private RvAdapter mRvAdapter;
//    private FooterView footerView;
    private Button reloadBtn;

    private List<NewsItem> headlinesList = new ArrayList<>();
    private List<NewsItem> feedList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        mHandler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.index_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        findViews(view);
        setListener();

//        loadCache();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setViewsVisible(true, true, false);
                loadData();
            }
        }, 300);//此处是为了自动显示Srl

    }

    private void findViews(View view) {
        mSrl = (SwipeRefreshLayout) view.findViewById(R.id.index_content_sl);
        mSrl.setColorSchemeResources(R.color.secondary_color, R.color.primary_color, R.color.next_product_title_color, R.color.next_product_count_bg);
        mRecyclerView = (ExRecyclerView) view.findViewById(R.id.index_feed_rv);
        reloadBtn = (Button) view.findViewById(R.id.index_reload_btn);

        headlinesView = new HeadlinesView(activity);
        mRecyclerView.addHeaderView(headlinesView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));// 线性布局
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());// 设置item动画
        mRecyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NewsItem item = feedList.get(position & feedList.size());
                Intent intent = new Intent(activity, FeedDetailActivity.class);
                intent.putExtra(Constant.NEWS_ITEM, item);
                startActivity(intent);
            }
        });
        mRecyclerView.setAdapter(mRvAdapter = new RvAdapter(feedList));
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
                setViewsVisible(true, true, false);
                loadData();
            }
        });
    }

    //从缓存加载数据
//    private void loadCache() {
//        if (getJsonToDataList()) {
//            headlinesView.initData(headlinesList);
//            if (null != mAdapter) mAdapter.notifyDataSetChanged();
//        }
//    }

    //从网络加载数据
    private void loadData() {
        StringRequest stringRequest = new StringRequest(Constant.INDEX_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!TextUtils.isEmpty(response)) {
                    NewsItemBiz.getFeed(response, new OnParseListener<NewsItem>() {
                        @Override
                        public void onParseSuccess(List<NewsItem> list) {
                            if (null != list && list.size() > 4) {
                                headlinesList.clear();
                                headlinesList.addAll(list.subList(0, 4));//从网页解析到的头条一共有4条
                                headlinesView.initData(headlinesList);
                                headlinesView.startAutoScroll();

                                list.removeAll(headlinesList);
                                feedList.clear();
                                feedList.addAll(list);

                                if (null != mRvAdapter) mRvAdapter.updateData(feedList);
                                setViewsVisible(false, true, false);
                            } else {
                                setViewsVisible(false, false, true);
                                ToastFactory.getToast(activity, "parse html failed").show();
                            }
                        }
                        @Override
                        public void onParseFailed() {
                            setViewsVisible(false, false, true);
                            ToastFactory.getToast(activity, "parse html failed").show();
                        }
                    });
                } else {
                    setViewsVisible(false, false, true);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (null != feedList && !feedList.isEmpty()) setViewsVisible(false, false, true);
                ToastFactory.getToast(activity, error.getMessage()).show();
            }
        });
        stringRequest.setTag(getClass().getSimpleName());
        MyApplication.getRequestQueue().add(stringRequest);
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
//                    feedList.addAll(NewsItemBiz.getFeed(result));
//
//                    mHandler.sendEmptyMessage(LOAD_MORE_COMPLETE);
//                }
//            }
//        });
//    }

//    private String initUrl(int currentPage) {
//        currentPage = currentPage > 0 ? currentPage : 1;
//        return Constant.INDEX_URL + "/?page=" + currentPage + "#lastest";
//    }

    /**
     * 读取缓存文件转换成json，放置于list中
     *
     * @return 返回是否有缓存
     */
//    public boolean getJsonToDataList() {
//        JSONObject jsonObject = mCache.getAsJSONObject(getClass().getSimpleName());
//        if (null != jsonObject) {
//            try {
//                JSONArray headlinesAr = jsonObject.getJSONArray("headlines");
//                headlinesList.clear();
//                for (int i = 0; i < headlinesAr.length(); i++) {
//                    JSONObject headline = headlinesAr.getJSONObject(i);
//                    NewsItem item = NewsItem.parse(headline);
//
//                    headlinesList.add(item);
//                }
//                JSONArray timelinesAr = jsonObject.getJSONArray("timelines");
//                feedList.clear();
//                for (int i = 0; i < timelinesAr.length(); i++) {
//                    JSONObject timeline = timelinesAr.getJSONObject(i);
//                    NewsItem item = NewsItem.parse(timeline);
//
//                    feedList.add(item);
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            return true;
//        }
//        return false;
//    }

    //转换list中的数据为json
//    private JSONObject saveToJSONObj(List<NewsItem> headlinesList, List<NewsItem> feedList) {
//        JSONObject outerJsonObj = new JSONObject();
//        JSONArray headlineJsonAr = new JSONArray();
//        for (NewsItem headlinesItem : headlinesList) {
//            JSONObject jsonObject = headlinesItem.toJSONObj();
//            headlineJsonAr.put(jsonObject);
//        }
//        JSONArray feedJsonAr = new JSONArray();
//        for (NewsItem timelinesItem : feedList) {
//            JSONObject jsonObject = timelinesItem.toJSONObj();
//            feedJsonAr.put(jsonObject);
//        }
//        try {
//            outerJsonObj.put("headlines", headlineJsonAr);
//            outerJsonObj.put("feed", feedJsonAr);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        return outerJsonObj;
//    }

    //设置各种View的显示状态
    private void setViewsVisible(boolean mSrl, boolean mRecyclerView, boolean reloadBtn) {
        if (mSrl) {
            if (null != this.mSrl) this.mSrl.setRefreshing(true);
        } else {
            if (null != this.mSrl) this.mSrl.setRefreshing(false);
        }
        if (mRecyclerView) {
            if (null != this.mRecyclerView) this.mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            if (null != this.mRecyclerView) this.mRecyclerView.setVisibility(View.GONE);
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
        if (null != mHandler) mHandler.removeCallbacksAndMessages(null);
        MyApplication.getRequestQueue().cancelAll(getClass().getSimpleName());
    }

    @Override
    public void onPause() {
        super.onPause();
        headlinesView.stopAutoScroll();
    }
}