package com.yanshi.my36kr.ui;

import android.app.ActionBar;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yanshi.my36kr.MyApplication;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.adapter.CommonAdapter;
import com.yanshi.my36kr.adapter.ViewHolder;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.bean.NewsItem;
import com.yanshi.my36kr.biz.NewsItemBiz;
import com.yanshi.my36kr.dao.NewsItemDao;
import com.yanshi.my36kr.utils.*;
import com.yanshi.my36kr.view.FooterView;
import com.yanshi.my36kr.view.HeadlinesView;
import com.yanshi.my36kr.view.dialog.ListDialogFragment;
import com.yanshi.my36kr.view.observableScrollview.ObservableListView;
import com.yanshi.my36kr.view.observableScrollview.ObservableScrollViewCallbacks;
import com.yanshi.my36kr.view.observableScrollview.ScrollState;
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

    private static final int LOAD_COMPLETE = 0X110;
    private static final int LOAD_MORE_COMPLETE = 0X111;
    private MainActivity activity;
//    private ActionBar actionBar;
    private ACache mCache;

    private HeadlinesView headlinesView;
    private SwipeRefreshLayout mSwipeLayout;
    private ObservableListView mListView;
    private FooterView footerView;
    private CommonAdapter<NewsItem> mAdapter;
    private Button reloadBtn;

    private List<NewsItem> headlinesList = new ArrayList<NewsItem>();
    private List<NewsItem> timelinesList = new ArrayList<NewsItem>();
    private List<NewsItem> loadingHeadlinesList;//加载时候的list
    private List<NewsItem> loadingTimelinesList;//加载时候的list
    private NewsItemBiz newsItemBiz = new NewsItemBiz();

    private boolean needLoadMore = true;
    private int currentPage = 1;

//    @Override
//    public void onHiddenChanged(boolean hidden) {
//        //fragment隐藏时显示ActionBar
//        if (hidden) {
//            if (null != actionBar && !actionBar.isShowing()) {
//                actionBar.show();
//            }
//        }
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (MainActivity) this.getActivity();
//        actionBar = activity.getActionBar();
        mCache = ACache.get(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.index_fragment, null);
        initView(view);
        initListener();

        return view;
    }

    private void initView(View view) {
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.index_content_sl);
        mSwipeLayout.setColorSchemeResources(R.color.secondary_color, R.color.primary_color, R.color.next_product_title_color, R.color.next_product_count_bg);
        mSwipeLayout.setOnRefreshListener(this);
        mListView = (ObservableListView) view.findViewById(R.id.index_timeline_lv);
        reloadBtn = (Button) view.findViewById(R.id.index_reload_btn);
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
                ImageView imageView = helper.getView(R.id.index_timeline_item_iv);
                ImageLoader.getInstance().displayImage(item.getImgUrl(), imageView, MyApplication.getInstance().getOptionsWithNoFade());

                //去除时间文本中的多余信息
                String info = item.getDate();
                if (null != info && info.contains("+08:00")) {
                    int start = info.indexOf("•") + 1;
                    int end = start + 11;
                    String removeStr = info.substring(start, end);
                    info = info.replace(removeStr, "").replace(" ", "").replace("+08:00", "");
                }
                helper.setText(R.id.index_timeline_item_info_tv, info);

                TextView tv = helper.getView(R.id.index_timeline_item_title_tv);
                ImageView iv = helper.getView(R.id.index_timeline_item_iv);
                if (StringUtils.isBlank(item.getDate())) {
                    //8点一氪晚间版
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                    iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }
            }
        });

    }

    private void initListener() {
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
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int size = timelinesList.size();
                int realPosition = position - mListView.getHeaderViewsCount();
                NewsItem item;
                if (size > 0 && (item = timelinesList.get(realPosition % size)) != null) {
                    Intent intent = new Intent(activity, ItemDetailActivity.class);
                    intent.putExtra(Constant.NEWS_ITEM, item);
//                    intent.putExtra(Constant.TITLE, item.getTitle());
//                    intent.putExtra(Constant.URL, item.getUrl());
                    startActivity(intent);
                }
            }
        });
/*        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int size = timelinesList.size();
                int realPosition = position - mListView.getHeaderViewsCount();
                final NewsItem newsItem = timelinesList.get(realPosition % size);
                if (null == newsItem) return false;
                String[] chooserItems = getResources().getStringArray(R.array.chooser_dialog_items_list);
                ListDialogFragment listDialogFragment = new ListDialogFragment();
                listDialogFragment.setParams(null, chooserItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: //收藏
                                        if (new NewsItemDao(activity).add(newsItem)) {
                                            ToastFactory.getToast(activity, getResources().getString(R.string.collect_success)).show();
                                        } else {
                                            ToastFactory.getToast(activity, getResources().getString(R.string.collect_failed)).show();
                                        }
                                        break;
                                    case 1: //转发
                                        Intent sendIntent = new Intent();
                                        sendIntent.setAction(Intent.ACTION_SEND);
                                        sendIntent.putExtra(Intent.EXTRA_TEXT, newsItem.getTitle() + "【" + getResources().getString(R.string.app_name) + "】" + " - " + newsItem.getUrl());
                                        sendIntent.setType("text/plain");
                                        startActivity(Intent.createChooser(sendIntent, "分享到"));
                                        break;
                                    case 2: //复制链接
                                        ClipboardManager clipboardManager = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                                        clipboardManager.setPrimaryClip(ClipData.newPlainText(null, newsItem.getUrl()));
                                        ToastFactory.getToast(activity, getResources().getString(R.string.has_copied_tip)).show();
                                        break;
                                }
                            }
                        });
                listDialogFragment.show(getFragmentManager(), "index_listDialogFragment");

                return true;
            }
        });*/
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity.refreshOptionsMenu(true);
        loadCache();
        loadData(currentPage);
    }

    /**
     * 从缓存加载数据
     */
    private void loadCache() {
        if (getJsonToDataList()) {
            headlinesView.initData(headlinesList);
            if (null != mAdapter) {
                mAdapter.notifyDataSetChanged();
            }
            mListView.setVisibility(View.VISIBLE);
//            mSwipeLayout.setRefreshing(true);
        }
    }

    /**
     * 从网络加载数据
     */
    private void loadData(int page) {
        if (!NetUtils.isConnected(activity)) {
            ToastFactory.getToast(activity, activity.getResources().getString(R.string.network_not_access)).show();

            if (mSwipeLayout.isRefreshing()) mSwipeLayout.setRefreshing(false);
            activity.refreshOptionsMenu(false);
            if(timelinesList.isEmpty()) reloadBtn.setVisibility(View.VISIBLE);
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
                    mListView.setVisibility(View.VISIBLE);
                    activity.refreshOptionsMenu(false);
                    reloadBtn.setVisibility(View.GONE);
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

    private String initUrl(int currentPage) {
        currentPage = currentPage > 0 ? currentPage : 1;
        String urlStr = Constant.INDEX_URL + "/?page=" + currentPage + "#lastest";
        return urlStr;
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


}