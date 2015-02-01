package com.yanshi.my36kr.ui;

import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yanshi.my36kr.MyApplication;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.adapter.CommonAdapter;
import com.yanshi.my36kr.adapter.ViewHolder;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.bean.NewsItem;
import com.yanshi.my36kr.biz.NewsItemBiz;
import com.yanshi.my36kr.dao.NewsItemDao;
import com.yanshi.my36kr.utils.ACache;
import com.yanshi.my36kr.utils.HttpUtils;
import com.yanshi.my36kr.utils.NetUtils;
import com.yanshi.my36kr.utils.ToastFactory;
import com.yanshi.my36kr.view.FooterView;
import com.yanshi.my36kr.view.dialog.ListDialogFragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 热门标签中的每个栏目
 * Created by kingars on 2014/11/1.
 */
public class TopicItemFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String[] TYPE = {"startups", "products", "apps", "technology",
            "websites", "people", "brands", "devices"};
    private int typePosition;
    private String typeUrl = Constant.TOPIC_URL + "/" + TYPE[typePosition];

    private static final int LOAD_COMPLETE = 0X110;
    private static final int LOAD_MORE_COMPLETE = 0X111;
    private MainActivity activity;
    private ACache mCache;

    private SwipeRefreshLayout mSwipeLayout;
    private ListView mListView;
    private FooterView footerView;
    private CommonAdapter<NewsItem> mAdapter;
    private Button reloadBtn;

    private List<NewsItem> newsItemList = new ArrayList<NewsItem>();
    private List<NewsItem> loadingNewsItemList;//加载时候的list
    private NewsItemBiz newsItemBiz = new NewsItemBiz();

    private boolean needLoadMore = true;
    private int currentPage = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (null != bundle) {
            typePosition = bundle.getInt(Constant.POSITION);
            typeUrl = Constant.TOPIC_URL + "/" + TYPE[typePosition];
        }

        activity = (MainActivity) this.getActivity();
        mCache = ACache.get(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.topic_item_fragment, null);
        initView(view);
        initListener();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (null == savedInstanceState) {
            activity.refreshOptionsMenu(true);
            loadCache();
            loadData(currentPage);
        } else {
            if(!loadCache()) {
                loadData(currentPage);
            }
        }

    }

    private void initView(View view) {
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.topic_item_sl);
        mSwipeLayout.setColorSchemeResources(R.color.secondary_color, R.color.primary_color, R.color.next_product_title_color, R.color.next_product_count_bg);
        mSwipeLayout.setOnRefreshListener(this);
        mListView = (ListView) view.findViewById(R.id.topic_item_lv);
        reloadBtn = (Button) view.findViewById(R.id.topic_item_reload_btn);
        footerView = new FooterView(activity);
        mListView.addFooterView(footerView);
        mListView.setAdapter(mAdapter = new CommonAdapter<NewsItem>(activity, newsItemList, R.layout.index_timeline_item) {
            @Override
            public void convert(ViewHolder helper, NewsItem item) {
                helper.setText(R.id.index_timeline_item_title_tv, item.getTitle());
                helper.setText(R.id.index_timeline_item_content_tv, item.getContent());
                helper.setText(R.id.index_timeline_item_type_tv, item.getNewsType());
                ImageView imageView = helper.getView(R.id.index_timeline_item_iv);
                ImageLoader.getInstance().displayImage(item.getImgUrl(), imageView, MyApplication.getInstance().getOptions());

                //去除时间文本中的多余信息
                String info = item.getDate();
                if (info.contains("+08:00")) {
                    int start = info.indexOf("•") + 1;
                    int end = start + 11;
                    String removeStr = info.substring(start, end);
                    info = info.replace(removeStr, "").replace(" ", "").replace("+08:00", "");
                }
                helper.setText(R.id.index_timeline_item_info_tv, info);

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
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int size = newsItemList.size();
                int realPosition = position - mListView.getHeaderViewsCount();
                NewsItem item;
                if (size > 0 && (item = newsItemList.get(realPosition % size)) != null) {
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
                final NewsItem newsItem = newsItemList.get(position);
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
                listDialogFragment.show(getFragmentManager(), "TopicItem");

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

    /**
     * 从缓存加载数据
     * @return 是否有缓存
     */
    private boolean loadCache() {
        if (getJsonToDataList()) {
            if (null != mAdapter) mAdapter.notifyDataSetChanged();
            mListView.setVisibility(View.VISIBLE);
//            mSwipeLayout.setRefreshing(true);
            return true;
        }
        return false;
    }

    /**
     * 从网络加载数据
     */
    private void loadData(int page) {
        if (!NetUtils.isConnected(activity)) {
            ToastFactory.getToast(activity, activity.getResources().getString(R.string.network_not_access)).show();

            if (mSwipeLayout.isRefreshing()) mSwipeLayout.setRefreshing(false);
            activity.refreshOptionsMenu(false);
            if (newsItemList.isEmpty()) reloadBtn.setVisibility(View.VISIBLE);
            return;
        }

        HttpUtils.doGetAsyn(initUrl(page), new HttpUtils.CallBack() {
            @Override
            public void onRequestComplete(String result) {
                if (null != result) {
                    loadingNewsItemList = newsItemBiz.getNewsItems(result);

                    mCache.put(Constant.TOPIC_ITEM_CACHE + "_" + TYPE[typePosition],
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
        if (!NetUtils.isConnected(activity)) {
            ToastFactory.getToast(activity, activity.getResources().getString(R.string.network_not_access)).show();

            footerView.setLoadMoreState();
            needLoadMore = true;
            return;
        }

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
        String urlStr = typeUrl + "?page=" + currentPage;
        return urlStr;
    }

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
                    loadingNewsItemList = null;

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

    /**
     * 读取缓存文件转换成json，放置于list中
     *
     * @return 返回是否有缓存
     */
    public boolean getJsonToDataList() {
        JSONObject jsonObject = mCache.getAsJSONObject(Constant.TOPIC_ITEM_CACHE + "_" + TYPE[typePosition]);
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

    @Override
    public void onRefresh() {
        currentPage = 1;
        loadData(currentPage);
    }

}