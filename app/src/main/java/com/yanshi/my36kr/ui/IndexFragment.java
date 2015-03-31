package com.yanshi.my36kr.ui;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yanshi.my36kr.MyApplication;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.adapter.CommonAdapter;
import com.yanshi.my36kr.adapter.ViewHolder;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.bean.NewsItem;
import com.yanshi.my36kr.biz.NewsItemBiz;
import com.yanshi.my36kr.utils.*;
import com.yanshi.my36kr.view.FooterView;
import com.yanshi.my36kr.view.HeadlinesView;
import com.yanshi.my36kr.view.observableScrollview.ObservableListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页
 * Created by kingars on 2014/10/25.
 */
public class IndexFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private final int LOAD_COMPLETE = 0X110;
    private final int LOAD_MORE_COMPLETE = 0X111;
    private Activity activity;
    private ACache mCache;

    private HeadlinesView headlinesView;
    private SwipeRefreshLayout mSwipeLayout;
    private ObservableListView mListView;
    private FooterView footerView;
    private CommonAdapter<NewsItem> mAdapter;
    private Button reloadBtn;
    private TextView loadingTv;

    private List<NewsItem> headlinesList = new ArrayList<NewsItem>();
    private List<NewsItem> timelinesList = new ArrayList<NewsItem>();
    private List<NewsItem> loadingHeadlinesList;//加载时候的list
    private List<NewsItem> loadingTimelinesList;//加载时候的list
    private NewsItemBiz newsItemBiz = new NewsItemBiz();

    private boolean needLoadMore = true;
    private int currentPage = 1;//当前页数

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this.getActivity();
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

        loadCache();
        setLoadingTvIn();
        loadData(currentPage);
    }

    //设置加载Tv的进入动画
    private void setLoadingTvIn() {
        if (loadingTv.getVisibility() == View.GONE) {
            loadingTv.setVisibility(View.VISIBLE);
            Animation am = AnimationUtils.loadAnimation(activity, R.anim.translate_top_in);
            loadingTv.setAnimation(am);
            am.start();
        }
    }

    //设置加载Tv的退出动画
    private void setLoadingTvOut() {
        if (loadingTv.getVisibility() == View.VISIBLE) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadingTv.setVisibility(View.GONE);
                    Animation am = AnimationUtils.loadAnimation(activity, R.anim.translate_top_out);
                    loadingTv.setAnimation(am);
                    am.start();
                }
            }, 1000);
        }
    }

    private void findViews(View view) {
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.index_content_sl);
        mSwipeLayout.setColorSchemeResources(R.color.secondary_color, R.color.primary_color, R.color.next_product_title_color, R.color.next_product_count_bg);
        mSwipeLayout.setOnRefreshListener(this);
        mListView = (ObservableListView) view.findViewById(R.id.index_timeline_lv);
        reloadBtn = (Button) view.findViewById(R.id.index_reload_btn);
        loadingTv = (TextView) view.findViewById(R.id.index_loading_tv);
        footerView = new FooterView(activity);
        mListView.addFooterView(footerView);
        headlinesView = new HeadlinesView(activity);
        mListView.addHeaderView(headlinesView);
        mListView.setAdapter(mAdapter = new CommonAdapter<NewsItem>(activity, timelinesList, R.layout.index_timeline_item) {
            @Override
            public void convert(ViewHolder helper, NewsItem item) {
                helper.setText(R.id.index_timeline_item_title_tv, item.getTitle());
                helper.setText(R.id.index_timeline_item_content_tv, item.getContent());
                helper.setText(R.id.index_timeline_item_type_tv, item.getNewsType());
                helper.setText(R.id.index_timeline_item_info_tv, item.getDate());
                ImageView imageView = helper.getView(R.id.index_timeline_item_iv);
                ImageLoader.getInstance().displayImage(item.getImgUrl(), imageView, MyApplication.getInstance().getOptionsWithNoFade());
            }
        });

    }

    private void setListener() {
        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData(currentPage);
            }
        });
        footerView.setListener(new FooterView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (needLoadMore) {
                    needLoadMore = false;

                    loadMoreData();
                }
            }
        });
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
//        mListView.setScrollViewCallbacks(new ObservableScrollViewCallbacks() {
//            @Override
//            public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
//            }
//            @Override
//            public void onDownMotionEvent() {
//            }
//            @Override
//            public void onUpOrCancelMotionEvent(ScrollState scrollState) {
//                if (scrollState == ScrollState.UP) {
//                    if (null != actionBar && actionBar.isShowing()) {
//                        actionBar.hide();
//                    }
//                } else if (scrollState == ScrollState.DOWN) {
//                    if (null != actionBar && !actionBar.isShowing()) {
//                        actionBar.show();
//                    }
//                }
//            }
//        });

/*        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    if (view.getLastVisiblePosition() == view.getCount() - 1) {
                            footerView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });*/
    }

    /**
     * 从缓存加载数据
     */
    private void loadCache() {
        if (getJsonToDataList()) {
            headlinesView.initData(headlinesList);
            if (null != mAdapter) mAdapter.notifyDataSetChanged();
            mListView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 从网络加载数据
     */
    private void loadData(int page) {
        if (!NetUtils.isConnected(activity)) {
            ToastFactory.getToast(activity, activity.getResources().getString(R.string.network_not_access)).show();

            if (mSwipeLayout.isRefreshing()) mSwipeLayout.setRefreshing(false);
            if (timelinesList.isEmpty()) reloadBtn.setVisibility(View.VISIBLE);
            setLoadingTvOut();
            return;
        }

        HttpUtils.doGetAsyn(initUrl(page), new HttpUtils.CallBack() {
            @Override
            public void onRequestComplete(String result) {
                if (null != result) {
                    loadingHeadlinesList = newsItemBiz.getHeadLines(result);
                    loadingTimelinesList = newsItemBiz.getNewsItems(result);

                    mCache.put(Constant.INDEX_CACHE, saveToJSONObj(loadingHeadlinesList, loadingTimelinesList), ACache.TIME_DAY);
                    mHandler.sendEmptyMessage(LOAD_COMPLETE);
                }
            }
        });
    }

    /**
     * 加载更多
     */
    private void loadMoreData() {
        if (!NetUtils.isConnected(activity)) {
            ToastFactory.getToast(activity, getResources().getString(R.string.network_not_access)).show();

            footerView.setLoadMoreState();
            needLoadMore = true;
            return;
        }

        currentPage += 1;
        HttpUtils.doGetAsyn(initUrl(currentPage), new HttpUtils.CallBack() {
            @Override
            public void onRequestComplete(String result) {
                if (null != result) {
                    timelinesList.addAll(newsItemBiz.getNewsItems(result));

                    mHandler.sendEmptyMessage(LOAD_MORE_COMPLETE);
                }
            }
        });
    }

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
                    loadingHeadlinesList = null;
                    loadingTimelinesList = null;

                    if (null != mAdapter) mAdapter.notifyDataSetChanged();

                    if (mSwipeLayout.isRefreshing()) mSwipeLayout.setRefreshing(false);
                    setLoadingTvOut();
                    mListView.setVisibility(View.VISIBLE);
                    reloadBtn.setVisibility(View.GONE);
                    break;
                case LOAD_MORE_COMPLETE:
                    if (null != mAdapter) mAdapter.notifyDataSetChanged();

                    footerView.setLoadMoreState();

                    needLoadMore = true;
                    break;
            }
        }
    };

    private String initUrl(int currentPage) {
        currentPage = currentPage > 0 ? currentPage : 1;
        return Constant.INDEX_URL + "/?page=" + currentPage + "#lastest";
    }

    /**
     * 读取缓存文件转换成json，放置于list中
     *
     * @return 返回是否有缓存
     */
    public boolean getJsonToDataList() {
        JSONObject jsonObject = mCache.getAsJSONObject(Constant.INDEX_CACHE);
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

    @Override
    public void onRefresh() {
        currentPage = 1;
        loadData(currentPage);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}