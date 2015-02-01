package com.yanshi.my36kr.ui;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.TypedValue;
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
import com.yanshi.my36kr.utils.*;
import com.yanshi.my36kr.view.dialog.ListDialogFragment;
import com.yanshi.my36kr.view.dialog.LoadingDialogFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 我的收藏-新闻
 * 作者：yanshi
 * 时间：2014-11-04 12:08
 */
public class MyFavoriteNewsFragment extends Fragment implements FragmentInterface, FavoriteNewsIntf {

    private static final int REQUEST_CODE = 0X100;

    private MainActivity activity;
    private ListView mListView;
    private CommonAdapter<NewsItem> mAdapter;
    private TextView emptyTv;

    private List<NewsItem> newsItemList = new ArrayList<NewsItem>();
    private List<NewsItem> loadingNewsItemList = new ArrayList<NewsItem>();//加载时候的list

    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (MainActivity) this.getActivity();
        if (UserProxy.isLogin(activity)) user = UserProxy.getCurrentUser(activity);
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
        if (!UserProxy.isLogin(activity) || null == user) return;
        if (NetUtils.isConnected(activity)) {
            loadDataByNet();
        } else {
            loadData();
        }
    }

    private void initView(View view) {
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
                if(null != tv) tv.setVisibility(View.GONE);
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
/*        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
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
        });*/
    }

    /**
     * 读取本地数据库数据
     */
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
            //无数据
            emptyTv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 读取网络数据
     */
    private void loadDataByNet() {
        activity.refreshOptionsMenu(true);//actionbar的进度圈

        BmobQuery<FavoriteNews> query = new BmobQuery<FavoriteNews>();
        query.setLimit(100);//设置单次查询返回的条目数
        query.addWhereEqualTo("userId", user.getObjectId());
        query.findObjects(activity, new FindListener<FavoriteNews>() {
            @Override
            public void onSuccess(List<FavoriteNews> list) {
                activity.refreshOptionsMenu(false);
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
                activity.refreshOptionsMenu(false);
                loadData();
            }
        });

    }

    @Override
    public void callBack() {
        loadData();
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
            loadData();
        }
    }

    @Override
    public List<NewsItem> getNewsList() {
        return newsItemList;
    }
}
