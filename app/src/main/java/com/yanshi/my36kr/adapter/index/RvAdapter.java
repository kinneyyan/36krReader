package com.yanshi.my36kr.adapter.index;

import com.yanshi.my36kr.bean.NewsItem;

import java.util.List;

import kale.adapter.AdapterItem;
import kale.recycler.ExCommonRcvAdapter;

/**
 * desc: 首页RecyclerView的adapter
 * author: kinneyYan
 * date: 2015/7/2
 */
public class RvAdapter extends ExCommonRcvAdapter<NewsItem> {

    public RvAdapter(List<NewsItem> data) {
        super(data);
    }

    @Override
    protected AdapterItem<NewsItem> initItemView(Object o) {
        return new FeedAdapterItem();
    }
}
