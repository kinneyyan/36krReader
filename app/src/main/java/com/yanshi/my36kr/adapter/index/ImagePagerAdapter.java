package com.yanshi.my36kr.adapter.index;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jakewharton.salvage.RecyclingPagerAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yanshi.my36kr.MyApplication;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.bean.NewsItem;
import com.yanshi.my36kr.activity.DetailActivity;

import java.util.List;

/**
 * desc: 纯图片PagerAdapter
 * author: kinneyYan
 * date: 2015/7/1
 */
public class ImagePagerAdapter extends RecyclingPagerAdapter {

    private Context context;
    private List<NewsItem> list;

    public ImagePagerAdapter(Context context, List<NewsItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            convertView = holder.imageView = imageView;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final NewsItem item = list.get(position);
        if (null != item) {
            ImageLoader.getInstance().displayImage(item.getImgUrl(), holder.imageView, MyApplication.getInstance().getOptions());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DetailActivity.class);
                    intent.putExtra(Constant.NEWS_ITEM, item);
                    context.startActivity(intent);
                }
            });
        }

        return convertView;
    }

    private static class ViewHolder {
        ImageView imageView;
    }

}
