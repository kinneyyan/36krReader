package com.yanshi.my36kr.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
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
import com.yanshi.my36kr.activity.DetailActivity;
import com.yanshi.my36kr.adapter.index.RvAdapter;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.bean.NewsItem;
import com.yanshi.my36kr.biz.NewsItemBiz;
import com.yanshi.my36kr.biz.OnParseListener;
import com.yanshi.my36kr.common.utils.ACache;
import com.yanshi.my36kr.common.utils.ToastUtils;
import com.yanshi.my36kr.common.view.HeadlinesView;
import com.yanshi.my36kr.fragment.base.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import kale.recycler.ExRecyclerView;
import kale.recycler.OnRecyclerViewScrollListener;

/**
 * 首页
 * 36氪网站改版，移除了分页加载
 * Created by Kinney on 2014/10/25.
 * Updated by Kinney on 2015/06/30.
 */
public class IndexFragment extends BaseFragment {

    private static final String TAG = "IndexFragment";
    private ACache mACache;

    private HeadlinesView headlinesView;
    private SwipeRefreshLayout mSrl;
    private ExRecyclerView mRecyclerView;
    private RvAdapter mRvAdapter;
    private Button reloadBtn;

    private List<NewsItem> headlinesList = new ArrayList<>();
    private List<NewsItem> feedList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mACache = ACache.get(getActivity());
    }

    @Override
    public View onViewInit(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_index, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        findViews(view);
        setListener();

        loadCache();
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

        headlinesView = new HeadlinesView(mActivity);
        mRecyclerView.addHeaderView(headlinesView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));// 线性布局
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());// 设置item动画
        mRecyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mActivity, DetailActivity.class);
                intent.putExtra(Constant.NEWS_ITEM, feedList.get(position));
                startActivity(intent);
            }
        });
        mRecyclerView.addOnScrollListener(new OnRecyclerViewScrollListener() {
            @Override
            public void onScrollUp() {

            }

            @Override
            public void onScrollDown() {

            }

            @Override
            public void onBottom() {
                ToastUtils.show(mActivity, "36氪网站改版获取不了分页数据/(ㄒoㄒ)/~~");
            }

            @Override
            public void onMoved(int i, int i1) {

            }
        });
        mRecyclerView.setAdapter(mRvAdapter = new RvAdapter(feedList));
    }

    private void setListener() {
        mSrl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
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
    private void loadCache() {
        if (convertToList()) {
            headlinesView.refreshData(headlinesList);
            if (null != mRvAdapter) mRvAdapter.notifyDataSetChanged();
        }
    }

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
                                headlinesView.refreshData(headlinesList);
                                headlinesView.startAutoScroll();

                                list.removeAll(headlinesList);
                                feedList.clear();
                                feedList.addAll(list);

                                if (null != mRvAdapter) mRvAdapter.updateData(feedList);
                                setViewsVisible(false, true, false);

                                mACache.put(TAG, convertToJson(headlinesList, feedList));
                            } else {
                                setViewsVisible(false, false, true);
                                ToastUtils.show(mActivity, "parse html failed");
                            }
                        }

                        @Override
                        public void onParseFailed() {
                            setViewsVisible(false, false, true);
                            ToastUtils.show(mActivity, "parse html failed");
                        }
                    });
                } else {
                    setViewsVisible(false, false, true);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (feedList.isEmpty()) {
                    setViewsVisible(false, false, true);
                } else {
                    setViewsVisible(false, true, false);
                }
                ToastUtils.show(mActivity, error.getMessage());
            }
        });
        stringRequest.setTag(TAG);
        MyApplication.getRequestQueue().add(stringRequest);
    }

    //读取缓存，赋给list
    public boolean convertToList() {
        JSONObject jsonObject = mACache.getAsJSONObject(TAG);
        if (null != jsonObject) {
            try {
                JSONArray headlinesAr = jsonObject.getJSONArray("index_headlines");
                headlinesList.clear();
                for (int i = 0; i < headlinesAr.length(); i++) {
                    JSONObject headline = headlinesAr.getJSONObject(i);
                    NewsItem item = NewsItem.parse(headline);

                    headlinesList.add(item);
                }
                JSONArray listJsonArray = jsonObject.getJSONArray("index_list");
                feedList.clear();
                for (int i = 0; i < listJsonArray.length(); i++) {
                    JSONObject timeline = listJsonArray.getJSONObject(i);
                    NewsItem item = NewsItem.parse(timeline);

                    feedList.add(item);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    //转换数据为json
    private JSONObject convertToJson(List<NewsItem> headlinesList, List<NewsItem> feedList) {
        JSONObject outerJsonObj = new JSONObject();
        JSONArray headlineJsonAr = new JSONArray();
        for (NewsItem headlinesItem : headlinesList) {
            JSONObject jsonObject = headlinesItem.toJSONObj();
            headlineJsonAr.put(jsonObject);
        }
        JSONArray feedJsonAr = new JSONArray();
        for (NewsItem timelinesItem : feedList) {
            JSONObject jsonObject = timelinesItem.toJSONObj();
            feedJsonAr.put(jsonObject);
        }
        try {
            outerJsonObj.put("index_headlines", headlineJsonAr);
            outerJsonObj.put("index_list", feedJsonAr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return outerJsonObj;
    }

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
        MyApplication.getRequestQueue().cancelAll(TAG);
    }

    @Override
    public void onResume() {
        super.onResume();
        headlinesView.startAutoScroll();
    }

    @Override
    public void onPause() {
        super.onPause();
        headlinesView.stopAutoScroll();
    }
}