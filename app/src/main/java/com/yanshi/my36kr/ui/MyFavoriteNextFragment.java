package com.yanshi.my36kr.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.adapter.CommonAdapter;
import com.yanshi.my36kr.adapter.ViewHolder;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.bean.FavoriteNextIntf;
import com.yanshi.my36kr.bean.FragmentInterface;
import com.yanshi.my36kr.bean.NextItem;
import com.yanshi.my36kr.bean.bmob.FavoriteNext;
import com.yanshi.my36kr.bean.bmob.User;
import com.yanshi.my36kr.biz.UserProxy;
import com.yanshi.my36kr.dao.NextItemDao;
import com.yanshi.my36kr.utils.NetUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 我的收藏-NEXT
 * 作者：yanshi
 * 时间：2014-11-04 12:55
 */
public class MyFavoriteNextFragment extends Fragment implements FragmentInterface, FavoriteNextIntf {

    private final int REQUEST_CODE = 0X100;

    private Activity activity;
    private ListView mListView;
    private CommonAdapter<NextItem> mAdapter;
    private TextView tipTv;

    private List<NextItem> nextItemList = new ArrayList<NextItem>();
    private List<NextItem> loadingNextItemList = new ArrayList<NextItem>();//加载时候的list

    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this.getActivity();
        if (UserProxy.isLogin(activity)) user = UserProxy.getCurrentUser(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.my_favorite_item, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        findViews(view);
        setListener();

        if (!UserProxy.isLogin(activity) || null == user) {
            setTipTvNotLogin();
            return;
        }
        if (NetUtils.isConnected(activity)) {
            setTipTvloading();
            loadDataByNet();
        } else {
            loadLocalData();
        }
    }

    private void findViews(View view) {
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
        tipTv = (TextView) view.findViewById(R.id.my_collection_item_tip_tv);
    }

    private void setListener() {
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
    }

    /**
     * 读取本地数据库数据
     */
    private void loadLocalData() {
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
            tipTv.setVisibility(View.GONE);
        } else {
            setTipTvEmptyData();
        }
    }

    /**
     * 读取网络数据
     */
    private void loadDataByNet() {

        BmobQuery<FavoriteNext> query = new BmobQuery<FavoriteNext>();
        query.setLimit(100);//设置单次查询返回的条目数
        query.addWhereEqualTo("userId", user.getObjectId());
        query.findObjects(activity, new FindListener<FavoriteNext>() {
            @Override
            public void onSuccess(List<FavoriteNext> list) {
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
                    tipTv.setVisibility(View.GONE);
                } else {
                    //无数据
                    setTipTvEmptyData();
                }
            }

            @Override
            public void onError(int i, String s) {
                Log.e("yslog", s);
                loadLocalData();
            }
        });
    }

    //设置未登录时的提示
    private void setTipTvNotLogin() {
        if (null != tipTv) {
            tipTv.setVisibility(View.VISIBLE);
            tipTv.setText(getString(R.string.personal_login_first));
        }
    }

    //设置无数据时的提示
    private void setTipTvEmptyData() {
        if (null != tipTv) {
            tipTv.setVisibility(View.VISIBLE);
            tipTv.setText(getString(R.string.collect_empty_list));
        }
    }

    //设置正在加载的提示
    private void setTipTvloading() {
        if (null != tipTv) {
            tipTv.setVisibility(View.VISIBLE);
            tipTv.setText(getString(R.string.app_loading_tv_text));
        }
    }

    @Override
    public void callBack() {
        loadLocalData();
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
            loadLocalData();
        }
    }

    @Override
    public List<NextItem> getNextList() {
        return nextItemList;
    }

    @Override
    public void setNotLoginStr() {
        setTipTvNotLogin();

        nextItemList.clear();
        if (null != mAdapter) mAdapter.notifyDataSetChanged();
    }
}
