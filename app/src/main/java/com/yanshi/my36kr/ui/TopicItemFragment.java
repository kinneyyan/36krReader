package com.yanshi.my36kr.ui;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.yanshi.my36kr.ui.base.BaseFragment;
import com.yanshi.my36kr.utils.ACache;
import com.yanshi.my36kr.utils.HttpUtils;
import com.yanshi.my36kr.utils.NetUtils;
import com.yanshi.my36kr.utils.ToastFactory;
import com.yanshi.my36kr.view.FooterView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 热门标签中的每个栏目
 * Created by kingars on 2014/11/1.
 */
public class TopicItemFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private Activity activity;
    private ACache mCache;
    private final String[] URL_TYPE = {"cn-startups", "us-startups", "cn-news", "breaking", "column", "archives"};
    private int typePosition = 0;
    private String url;
    private boolean needLoadMore = true;
    private int currentPage = 1;

    private SwipeRefreshLayout mSwipeLayout;
    private ListView mListView;
    private FooterView footerView;
    private CommonAdapter<NewsItem> mAdapter;
    private Button reloadBtn;
    private TextView loadingTv;

    private List<NewsItem> newsItemList = new ArrayList<>();
    private List<NewsItem> loadingNewsItemList;//加载时的list
    private NewsItemBiz newsItemBiz = new NewsItemBiz();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (null != bundle) {
            typePosition = bundle.getInt(Constant.POSITION);
        }
        activity = getActivity();
        mCache = ACache.get(activity);
        url = Constant.COLUMNS_URL + "/" + URL_TYPE[typePosition];
    }

    @Override
    public View onViewInit(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.topic_item_fragment, container, false);
        findViews(rootView);
        setListener();
        loadCache();
        loadData(currentPage);
        return rootView;
    }

    private void findViews(View view) {
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.topic_item_sl);
        mSwipeLayout.setColorSchemeResources(R.color.secondary_color, R.color.primary_color, R.color.next_product_title_color, R.color.next_product_count_bg);
        mSwipeLayout.setOnRefreshListener(this);
        mListView = (ListView) view.findViewById(R.id.topic_item_lv);
        reloadBtn = (Button) view.findViewById(R.id.topic_item_reload_btn);
        loadingTv = (TextView) view.findViewById(R.id.topic_item_loading_tv);
        footerView = new FooterView(activity);
        mListView.addFooterView(footerView);
        mListView.setAdapter(mAdapter = new CommonAdapter<NewsItem>(activity, newsItemList, R.layout.index_timeline_item) {
            @Override
            public void convert(ViewHolder helper, NewsItem item) {
                helper.setText(R.id.index_timeline_item_title_tv, item.getTitle());
                helper.setText(R.id.index_timeline_item_content_tv, item.getContent());
                helper.setText(R.id.index_timeline_item_type_tv, item.getNewsType());
                helper.setText(R.id.index_timeline_item_info_tv, item.getDate());
                ImageView imageView = helper.getView(R.id.index_timeline_item_iv);
                ImageLoader.getInstance().displayImage(item.getImgUrl(), imageView, MyApplication.getInstance().getOptions());
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
                int size = newsItemList.size();
                int realPosition = position - mListView.getHeaderViewsCount();
                NewsItem item;
                if (size > 0 && (item = newsItemList.get(realPosition % size)) != null) {
                    Intent intent = new Intent(activity, ItemDetailActivity.class);
                    intent.putExtra(Constant.NEWS_ITEM, item);
                    startActivity(intent);
                }
            }
        });
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
     *
     * @return 是否有缓存
     */
    private void loadCache() {
        if (getJsonToDataList()) {
            if (null != mAdapter) mAdapter.notifyDataSetChanged();
            setViewsVisible(true, true, false);
        } else {
            setViewsVisible(true, false, false);
        }
    }

    /**
     * 从网络加载数据
     */
    private void loadData(int page) {
        HttpUtils.doGetAsyn(initUrl(page), new HttpUtils.CallBack() {
            @Override
            public void onRequestComplete(String result) {
                if (null != result) {
                    loadingNewsItemList = newsItemBiz.getNewsItems(result);

                    mCache.put(getClass().getSimpleName() + "_" + URL_TYPE[typePosition],
                            saveToJSONObj(loadingNewsItemList), ACache.TIME_DAY);
                    mHandler.sendEmptyMessage(LOAD_COMPLETE);
                }
            }
        });
    }

    /**
     * 加载更多
     */
    private void loadMoreData() {
        currentPage += 1;
        HttpUtils.doGetAsyn(initUrl(currentPage), new HttpUtils.CallBack() {
            @Override
            public void onRequestComplete(String result) {
                if (null != result) {
                    newsItemList.addAll(newsItemBiz.getNewsItems(result));

                    mHandler.sendEmptyMessage(LOAD_MORE_COMPLETE);
                }
            }
        });
    }

    private String initUrl(int currentPage) {
        currentPage = currentPage > 0 ? currentPage : 1;
        return url + "?page=" + currentPage;
    }

    private final int LOAD_COMPLETE = 0X110;
    private final int LOAD_MORE_COMPLETE = 0X111;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOAD_COMPLETE:
                    if (loadingNewsItemList != null && !loadingNewsItemList.isEmpty()) {
                        newsItemList.clear();
                        newsItemList.addAll(loadingNewsItemList);
                    }

                    if (null != mAdapter) mAdapter.notifyDataSetChanged();

                    if (null != mSwipeLayout && mSwipeLayout.isRefreshing()) mSwipeLayout.setRefreshing(false);
                    setViewsVisible(false, true, false);
                    break;
                case LOAD_MORE_COMPLETE:
                    if (null != mAdapter) mAdapter.notifyDataSetChanged();

                    footerView.setLoadMoreState();

                    needLoadMore = true;
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 读取缓存文件转换成json，放置于list中
     *
     * @return 返回是否有缓存
     */
    public boolean getJsonToDataList() {
        JSONObject jsonObject = mCache.getAsJSONObject(getClass().getSimpleName() + "_" + URL_TYPE[typePosition]);
        if (null != jsonObject) {
            try {
                JSONArray newsAr = jsonObject.getJSONArray("topics");
                newsItemList.clear();
                for (int i = 0; i < newsAr.length(); i++) {
                    JSONObject timeline = newsAr.getJSONObject(i);
                    NewsItem item = NewsItem.parse(timeline);

                    newsItemList.add(item);
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
     * @param newsItemList
     * @return
     */
    private JSONObject saveToJSONObj(List<NewsItem> newsItemList) {
        JSONObject outerJsonObj = new JSONObject();
        JSONArray newsAr = new JSONArray();
        for (NewsItem newsItem : newsItemList) {
            JSONObject jsonObject = newsItem.toJSONObj();
            newsAr.put(jsonObject);
        }
        try {
            outerJsonObj.put("topics", newsAr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return outerJsonObj;
    }

    //设置各种View的显示状态
    private void setViewsVisible(boolean loadingTv, boolean mListView, boolean reloadBtn) {
        if (loadingTv) {
            setLoadingTvIn();
        } else {
            setLoadingTvOut();
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