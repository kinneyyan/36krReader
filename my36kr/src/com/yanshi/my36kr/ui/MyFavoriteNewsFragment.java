package com.yanshi.my36kr.ui;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yanshi.my36kr.MyApplication;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.adapter.CommonAdapter;
import com.yanshi.my36kr.adapter.ViewHolder;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.bean.FragmentInterface;
import com.yanshi.my36kr.bean.NewsItem;
import com.yanshi.my36kr.dao.NewsItemDao;
import com.yanshi.my36kr.utils.StringUtils;
import com.yanshi.my36kr.utils.ToastFactory;
import com.yanshi.my36kr.view.dialog.ListDialogFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 我的收藏-新闻
 * 作者：yanshi
 * 时间：2014-11-04 12:08
 */
public class MyFavoriteNewsFragment extends Fragment implements FragmentInterface, SwipeRefreshLayout.OnRefreshListener {

    private static final int REQUEST_CODE = 0X100;

    private Activity activity;
    private SwipeRefreshLayout mSwipeLayout;
    private ListView mListView;
    private CommonAdapter<NewsItem> mAdapter;
    private TextView emptyTv;

    private List<NewsItem> newsItemList = new ArrayList<NewsItem>();
    private List<NewsItem> loadingNewsItemList = new ArrayList<NewsItem>();//加载时候的list

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this.getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_favorite_item, null);
        initView(view);
        initListener();

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData();
    }

    private void initView(View view) {
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.my_collection_item_sl);
        mSwipeLayout.setColorSchemeResources(R.color.primary_color, R.color.secondary_color, R.color.next_product_title_color, R.color.next_product_count_bg);
        mSwipeLayout.setOnRefreshListener(this);
        mListView = (ListView) view.findViewById(R.id.my_collection_item_lv);
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
                if (null != info && info.contains("+08:00")) {
//                    int start = info.indexOf("•")+1;
//                    int end = start+11;
//                    String removeStr = info.substring(start, end);
                    info = info.replace("T", " ").replace("+08:00", "");
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
        emptyTv = (TextView) view.findViewById(R.id.my_collection_item_empty_tv);
    }

    private void initListener() {
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
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final NewsItem newsItem = newsItemList.get(position);
                if (null == newsItem) return false;
                String[] chooserItems = getResources().getStringArray(R.array.chooser_dialog_items_collect);
                ListDialogFragment listDialogFragment = new ListDialogFragment();
                listDialogFragment.setParams(null, chooserItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: //转发
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, newsItem.getTitle() + "【" + getResources().getString(R.string.app_name) + "】" + " - " + newsItem.getUrl());
                                sendIntent.setType("text/plain");
                                startActivity(Intent.createChooser(sendIntent, "分享到"));
                                break;
                            case 1: //复制链接
                                ClipboardManager clipboardManager = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, newsItem.getUrl()));
                                ToastFactory.getToast(activity, getResources().getString(R.string.has_copied_tip)).show();
                                break;
                            case 2: //删除
                                if (new NewsItemDao(activity).deleteById(newsItem.getId())) {
                                    newsItemList.remove(newsItem);
                                    if (null != mAdapter) {
                                        mAdapter.notifyDataSetChanged();
                                    }
                                    if (newsItemList.isEmpty()) {
                                        emptyTv.setVisibility(View.VISIBLE);
                                    }
                                }
                                break;
                        }
                    }
                });
                listDialogFragment.show(getFragmentManager(), "NewsItemCollection");

                return true;
            }
        });
    }

    private void loadData() {
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
            emptyTv.setVisibility(View.VISIBLE);
        }
        mSwipeLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        loadData();
    }


    @Override
    public void callBack() {
        loadData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE == requestCode && Activity.RESULT_OK == resultCode) {
            loadData();
        }
    }
}
