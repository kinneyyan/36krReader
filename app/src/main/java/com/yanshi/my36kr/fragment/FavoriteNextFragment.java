package com.yanshi.my36kr.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.yanshi.my36kr.R;
import com.yanshi.my36kr.activity.DetailActivity;
import com.yanshi.my36kr.adapter.CommonAdapter;
import com.yanshi.my36kr.adapter.ViewHolder;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.bean.FragmentInterface;
import com.yanshi.my36kr.bean.NextItem;
import com.yanshi.my36kr.bean.bmob.User;
import com.yanshi.my36kr.biz.UserProxy;
import com.yanshi.my36kr.common.utils.NetUtils;
import com.yanshi.my36kr.dao.NextItemDao;
import com.yanshi.my36kr.fragment.base.BaseFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

/**
 * 我的收藏-NEXT
 * 作者：yanshi
 * 时间：2014-11-04 12:55
 */
public class FavoriteNextFragment extends BaseFragment implements FragmentInterface {

    private static final int REQUEST_CODE = 0X100;

    private ListView mListView;
    private CommonAdapter<NextItem> mAdapter;
    private TextView tipTv;//数据为空or未登录时 的提示TextView

    private List<NextItem> nextItemList = new ArrayList<>();

    @Override
    public View onViewInit(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_favorite, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        findViews(view);
        setListener();

        if (!UserProxy.isLogin(mActivity)) {
            setTipTvNotLogin();
            return;
        }
        if (NetUtils.isConnected(mActivity)) {
            setTipTvloading();
            loadDataByNet();
        }
        //无网络时读取本地数据库
        else {
            loadLocalData();
        }
    }

    private void findViews(View view) {
        mListView = (ListView) view.findViewById(R.id.my_collection_item_lv);
        mListView.setAdapter(mAdapter = new CommonAdapter<NextItem>(mActivity, nextItemList, R.layout.view_next_product_item) {
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
                    Intent intent = new Intent(mActivity, DetailActivity.class);
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
        nextItemList.clear();
        nextItemList.addAll(NextItemDao.getAll());
        Collections.reverse(nextItemList);
        if (!nextItemList.isEmpty()) {
            if (null != mAdapter) {
                mAdapter.notifyDataSetChanged();
            }
            tipTv.setVisibility(View.GONE);
        } else {
            //无数据
            setTipTvEmptyData();
        }
    }

    /**
     * 读取网络数据
     */
    private void loadDataByNet() {
        User user = UserProxy.getCurrentUser(mActivity);
        if (null == user) {
            setTipTvNotLogin();
            return;
        }
        BmobQuery<NextItem> query = new BmobQuery<>();
        query.setLimit(100);//设置单次查询返回的条目数
        query.addWhereEqualTo("userId", user.getObjectId());
        query.findObjects(mActivity, new FindListener<NextItem>() {
            @Override
            public void onSuccess(List<NextItem> list) {
                if (null != list && !list.isEmpty()) {
                    nextItemList.clear();
                    for(int i = 0; i < list.size(); i++) {
                        NextItem nextItem = list.get(i);
                        nextItem.setBmobId(nextItem.getObjectId());
                        nextItemList.add(nextItem);
                    }
                    Collections.reverse(nextItemList);
                    if (null != mAdapter) {
                        mAdapter.notifyDataSetChanged();
                    }
                    tipTv.setVisibility(View.GONE);

                    updateDataBase(list);
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

    //更新本地数据库中的数据
    private void updateDataBase(List<NextItem> list) {
        NextItemDao.clear();
        for (int i = 0; i < list.size(); i++) {
            NextItemDao.add(list.get(i));
        }
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
        if (UserProxy.isLogin(mActivity)) {
            if (nextItemList.isEmpty()) {
                setTipTvloading();
                loadDataByNet();
            } else {
                loadLocalData();
            }
        } else {
            setTipTvNotLogin();
            nextItemList.clear();
            if (null != mAdapter) mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE == requestCode && Activity.RESULT_OK == resultCode) {
            loadLocalData();
        }
    }

}
