package com.yanshi.my36kr.ui;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.listener.FindListener;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.adapter.CommonAdapter;
import com.yanshi.my36kr.adapter.ViewHolder;
import com.yanshi.my36kr.bean.*;
import com.yanshi.my36kr.bean.bmob.FavoriteNews;
import com.yanshi.my36kr.bean.bmob.FavoriteNext;
import com.yanshi.my36kr.bean.bmob.User;
import com.yanshi.my36kr.biz.UserProxy;
import com.yanshi.my36kr.dao.NextItemDao;
import com.yanshi.my36kr.utils.NetUtils;
import com.yanshi.my36kr.utils.ToastFactory;
import com.yanshi.my36kr.view.dialog.ListDialogFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 我的收藏-NEXT
 * 作者：yanshi
 * 时间：2014-11-04 12:55
 */
public class MyFavoriteNextFragment extends Fragment implements FragmentInterface, FavoriteNextIntf {

    private static final int REQUEST_CODE = 0X100;

    private MainActivity activity;
    private ListView mListView;
    private CommonAdapter<NextItem> mAdapter;
    private TextView emptyTv;

    private List<NextItem> nextItemList = new ArrayList<NextItem>();
    private List<NextItem> loadingNextItemList = new ArrayList<NextItem>();//加载时候的list

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
        mListView.setAdapter(mAdapter = new CommonAdapter<NextItem>(activity, nextItemList, R.layout.next_product_item) {
            @Override
            public void convert(ViewHolder helper, NextItem item) {
                helper.setText(R.id.next_product_item_title_tv, item.getTitle());
                helper.setText(R.id.next_product_item_content_tv, item.getContent());

                TextView tv1 = helper.getView(R.id.next_product_item_vote_count_tv);
                TextView tv2 = helper.getView(R.id.next_product_item_comment_count_tv);
                if (null != tv1) tv1.setVisibility(View.GONE);
                if (null != tv2) tv2.setVisibility(View.GONE);
            }
        });
        emptyTv = (TextView) view.findViewById(R.id.my_collection_item_empty_tv);
    }

    private void initListener() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int size = nextItemList.size();
                NextItem item;
                if (size > 0 && (item = nextItemList.get(position % size)) != null) {
                    Intent intent = new Intent(activity, ItemDetailActivity.class);
                    intent.putExtra(Constant.NEXT_ITEM, item);
                    startActivityForResult(intent, REQUEST_CODE);
                }
            }
        });
/*        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final NextItem next = nextItemList.get(position);
                if (null == next) return false;
                String[] chooserItems = getResources().getStringArray(R.array.chooser_dialog_items_collect);
                ListDialogFragment listDialogFragment = new ListDialogFragment();
                listDialogFragment.setParams(null, chooserItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: //转发
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, next.getTitle() + "【" + getResources().getString(R.string.app_name) + "】" + " - " + next.getUrl());
                                sendIntent.setType("text/plain");
                                startActivity(Intent.createChooser(sendIntent, "分享到"));
                                break;
                            case 1: //复制链接
                                ClipboardManager clipboardManager = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, next.getUrl()));
                                ToastFactory.getToast(activity, getResources().getString(R.string.has_copied_tip)).show();
                                break;
                            case 2: //删除
                                if (new NextItemDao(activity).deleteById(next.getId())) {
                                    nextItemList.remove(next);
                                    if (null != mAdapter) {
                                        mAdapter.notifyDataSetChanged();
                                    }
                                    if (nextItemList.isEmpty()) {
                                        emptyTv.setVisibility(View.VISIBLE);
                                    }
                                }
                                break;
                        }
                    }
                });
                listDialogFragment.show(getFragmentManager(), "NextItemCollection");

                return true;
            }
        });*/
    }

    /**
     * 读取本地数据库数据
     */
    private void loadData() {
        NextItemDao nextItemdao = new NextItemDao(activity);
        loadingNextItemList.clear();
        loadingNextItemList.addAll(nextItemdao.getAll());
        if (!loadingNextItemList.isEmpty()) {
            Collections.reverse(loadingNextItemList);
            nextItemList.clear();
            nextItemList.addAll(loadingNextItemList);

            if (null != mAdapter) {
                mAdapter.notifyDataSetChanged();
            }
            emptyTv.setVisibility(View.GONE);
        } else {
            emptyTv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 读取网络数据
     */
    private void loadDataByNet() {
        activity.refreshOptionsMenu(true);//actionbar的进度圈

        BmobQuery<FavoriteNext> query = new BmobQuery<FavoriteNext>();
        query.setLimit(100);//设置单次查询返回的条目数
        query.addWhereEqualTo("userId", user.getObjectId());
        query.findObjects(activity, new FindListener<FavoriteNext>() {
            @Override
            public void onSuccess(List<FavoriteNext> list) {
                activity.refreshOptionsMenu(false);
                if (null != list && !list.isEmpty()) {
                    nextItemList.clear();
                    for (FavoriteNext fNext : list) {
                        NextItem nextItem = new NextItem();
                        nextItem.setTitle(fNext.getTitle());
                        nextItem.setContent(fNext.getContent());
                        nextItem.setUrl(fNext.getUrl());
                        nextItemList.add(nextItem);
                    }
                    Collections.reverse(nextItemList);
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
    public List<NextItem> getNextList() {
        return nextItemList;
    }
}
