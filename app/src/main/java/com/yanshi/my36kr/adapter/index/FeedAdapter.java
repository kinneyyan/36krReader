package com.yanshi.my36kr.adapter.index;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.yanshi.my36kr.MyApplication;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.bean.NewsItem;

import java.util.List;

/**
 * desc: 首页资讯列表的adapter(带headerView)
 * author: kinneyYan
 * email: kingars@foxmail.com
 * date: 16/4/15
 */
public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.MyViewHolder> {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_NORMAL = 1;

    private List<NewsItem> list;

    private View headerView;

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setHeaderView(View headerView) {
        this.headerView = headerView;
        notifyItemInserted(0);
    }

    public View getHeaderView() {
        return headerView;
    }

    public void addDatas(List<NewsItem> datas) {
        if (null != list) list.addAll(datas);
        notifyDataSetChanged();
    }

    public FeedAdapter(List<NewsItem> list) {
        this.list = list;
    }

    @Override
    public int getItemViewType(int position) {
        if (headerView == null) return TYPE_NORMAL;
        if (position == 0) return TYPE_HEADER;
        return TYPE_NORMAL;
    }

    @Override
    public FeedAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (headerView != null && viewType == TYPE_HEADER) return new MyViewHolder(headerView);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_index_timeline_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FeedAdapter.MyViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER) return;

        final int realPos = getRealPosition(holder);
        setItemData(holder, realPos);
        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(realPos);
                }
            });
        }
    }

    private void setItemData(MyViewHolder holder, int realPos) {
        NewsItem item = list.get(realPos);
        ImageLoader.getInstance().displayImage(item.getImgUrl(), holder.picIv, MyApplication.getInstance().getOptionsWithRoundedCorner());
        holder.titleTv.setText(item.getTitle());
        holder.contentTv.setText(item.getContent());
        holder.dateTv.setText(item.getDate());
    }

    public int getRealPosition(RecyclerView.ViewHolder holder) {
        int position = holder.getLayoutPosition();
        return headerView == null ? position : position - 1;
    }

    @Override
    public int getItemCount() {
        return headerView == null ? list.size() : list.size() + 1;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView picIv;
        TextView titleTv;
        TextView contentTv;
        TextView dateTv;

        public MyViewHolder(View itemView) {
            super(itemView);
            if (itemView == headerView) return;
            picIv = (ImageView) itemView.findViewById(R.id.index_timeline_item_iv);
            titleTv = (TextView) itemView.findViewById(R.id.index_timeline_item_title_tv);
            contentTv = (TextView) itemView.findViewById(R.id.index_timeline_item_content_tv);
            dateTv = (TextView) itemView.findViewById(R.id.index_timeline_item_info_tv);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
