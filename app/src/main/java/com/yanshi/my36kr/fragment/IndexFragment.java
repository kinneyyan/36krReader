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
    private ACache aCache;

    private HeadlinesView headlinesView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ExRecyclerView exRecyclerView;
    private RvAdapter rvAdapter;
    private Button reloadBtn;

    private List<NewsItem> headlinesList = new ArrayList<>();
    private List<NewsItem> feedList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aCache = ACache.get(getActivity());
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
        swipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                setLoadingState();
                loadData();
            }
        }, 200);
    }

    private void findViews(View view) {
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.index_content_sl);
        swipeRefreshLayout.setColorSchemeResources(R.color.secondary_color, R.color.primary_color, R.color.next_product_title_color, R.color.next_product_count_bg);
        exRecyclerView = (ExRecyclerView) view.findViewById(R.id.index_feed_rv);
        reloadBtn = (Button) view.findViewById(R.id.index_reload_btn);

        headlinesView = new HeadlinesView(activity);
        exRecyclerView.addHeaderView(headlinesView);
        exRecyclerView.setLayoutManager(new LinearLayoutManager(activity));// 线性布局
        exRecyclerView.setItemAnimator(new DefaultItemAnimator());// 设置item动画
        exRecyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(activity, DetailActivity.class);
                intent.putExtra(Constant.NEWS_ITEM, feedList.get(position));
                startActivity(intent);
            }
        });
        exRecyclerView.addOnScrollListener(new OnRecyclerViewScrollListener() {
            @Override
            public void onScrollUp() {

            }

            @Override
            public void onScrollDown() {

            }

            @Override
            public void onBottom() {
                ToastUtils.show(activity, "36氪网站改版获取不了分页数据/(ㄒoㄒ)/~~");
            }

            @Override
            public void onMoved(int i, int i1) {

            }
        });
        exRecyclerView.setAdapter(rvAdapter = new RvAdapter(feedList));
    }

    private void setListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoadingState();
                loadData();
            }
        });
    }

    //从缓存加载数据
    private void loadCache() {
        if (convertToList()) {
            headlinesView.refreshData(headlinesList);
            if (null != rvAdapter) rvAdapter.notifyDataSetChanged();

            fadeInAnim(exRecyclerView);
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
                            headlinesList.clear();
                            headlinesList.addAll(list.subList(0, 4));//从网页解析到的头条一共有4条
                            headlinesView.refreshData(headlinesList);
                            headlinesView.startAutoScroll();

                            list.removeAll(headlinesList);
                            feedList.clear();
                            feedList.addAll(list);
                            if (null != rvAdapter) rvAdapter.updateData(feedList);

                            setLoadSuccState();
                            // add cache
                            aCache.put(TAG, convertToJson(headlinesList, feedList));
                        }

                        @Override
                        public void onParseFailed() {
                            setLoadFailedState();
                            ToastUtils.show(activity, "parse html failed");
                        }
                    });
                } else {
                    setLoadFailedState();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setLoadFailedState();
                ToastUtils.show(activity, error.getMessage());
            }
        });
        stringRequest.setTag(TAG);
        MyApplication.getRequestQueue().add(stringRequest);
    }

    //读取缓存，赋给list
    public boolean convertToList() {
        JSONObject jsonObject = aCache.getAsJSONObject(TAG);
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

    // control view's state
    private void setLoadingState() {
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(true);
        reloadBtn.setVisibility(View.GONE);
    }

    private void setLoadSuccState() {
        swipeRefreshLayout.setRefreshing(false);
        fadeInAnim(exRecyclerView);
    }

    private void setLoadFailedState() {
        swipeRefreshLayout.setRefreshing(false);
        if (feedList != null && feedList.isEmpty()) {
            reloadBtn.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);
            exRecyclerView.setVisibility(View.GONE);
        }
    }
}