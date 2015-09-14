package com.yanshi.my36kr.adapter.index;

import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.yanshi.my36kr.MyApplication;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.bean.NewsItem;

import kale.adapter.AdapterItem;
import kale.adapter.ViewHolder;

/**
 * desc:
 * author: Kinney
 * date: 2015/7/2
 */
public class FeedAdapterItem implements AdapterItem<NewsItem> {

    private ImageView picIv;
    private TextView titleTv;
    private TextView contentTv;
    private TextView dateTv;

    @Override
    public int getLayoutResId() {
        return R.layout.view_index_timeline_item;
    }

    @Override
    public void initViews(ViewHolder viewHolder, NewsItem newsItem, int i) {
        picIv = viewHolder.getView(R.id.index_timeline_item_iv);
        titleTv = viewHolder.getView(R.id.index_timeline_item_title_tv);
        contentTv = viewHolder.getView(R.id.index_timeline_item_content_tv);
        dateTv = viewHolder.getView(R.id.index_timeline_item_info_tv);
        setViews(newsItem);
    }

    private void setViews(NewsItem item) {
        ImageLoader.getInstance().displayImage(item.getImgUrl(), picIv, MyApplication.getInstance().getOptionsWithRoundedCorner());
        titleTv.setText(item.getTitle());
        contentTv.setText(item.getContent());
        dateTv.setText(item.getDate());
    }
}
