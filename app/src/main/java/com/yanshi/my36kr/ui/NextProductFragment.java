package com.yanshi.my36kr.ui;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import com.yanshi.my36kr.R;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.bean.NextItem;
import com.yanshi.my36kr.biz.NextItemBiz;
import com.yanshi.my36kr.utils.ACache;
import com.yanshi.my36kr.utils.HttpUtils;
import com.yanshi.my36kr.utils.NetUtils;
import com.yanshi.my36kr.utils.ToastFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import java.util.ArrayList;
import java.util.List;

/**
 * NEXT
 * 作者：yanshi
 * 时间：2014-10-30 14:48
 */
public class NextProductFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private Activity activity;
    private ACache mCache;

    private SwipeRefreshLayout mSwipeLayout;
    private StickyListHeadersListView mListView;
    private MyAdapter mAdapter;
    private Button reloadBtn;
    private TextView loadingTv;

    private List<NextItem> nextItemList = new ArrayList<>();
    private List<NextItem> loadingNextItemList;//加载时的list
    private NextItemBiz nextItemBiz = new NextItemBiz();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        mCache = ACache.get(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.next_product_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        findViews(view);
        setListener();

        loadCache();
        setLoadingTvIn();
        loadData();
    }

    private void findViews(View view) {
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.next_product_content_sl);
        mSwipeLayout.setColorSchemeResources(R.color.secondary_color, R.color.primary_color, R.color.next_product_title_color, R.color.next_product_count_bg);
        mListView = (StickyListHeadersListView) view.findViewById(R.id.next_product_lv);
        reloadBtn = (Button) view.findViewById(R.id.next_product_reload_btn);
        loadingTv = (TextView) view.findViewById(R.id.next_product_loading_tv);

        mAdapter = new MyAdapter(activity, nextItemList);
        mListView.setAdapter(mAdapter);
    }

    private void setListener() {
        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });
        mSwipeLayout.setOnRefreshListener(this);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int size = nextItemList.size();
                NextItem item;
                if (size > 0 && (item = nextItemList.get(position % size)) != null) {
                    Intent intent = new Intent(activity, ItemDetailActivity.class);
                    intent.putExtra(Constant.NEXT_ITEM, item);
                    startActivity(intent);
                }
            }
        });
    }

    //加载缓存
    private void loadCache() {
        if (getJsonToDataList()) {
            if (null != mAdapter) mAdapter.notifyDataSetChanged();
        }
    }

    //加载网络
    private void loadData() {
        HttpUtils.doGetAsyn(Constant.NEXT_URL, new HttpUtils.CallBack() {
            @Override
            public void onRequestComplete(String result) {
                if (null != result) {
                    loadingNextItemList = nextItemBiz.getNextItems(result);

                    mCache.put(getClass().getSimpleName(), saveToJSONObj(loadingNextItemList), ACache.TIME_DAY*3);
                    mHandler.sendEmptyMessage(LOAD_COMPLETE);
                }
            }
        });
    }

    private final int LOAD_COMPLETE = 0X110;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOAD_COMPLETE:
                    if (loadingNextItemList != null && !loadingNextItemList.isEmpty()) {
                        nextItemList.clear();
                        nextItemList.addAll(loadingNextItemList);
                    }

                    if (null != mAdapter) mAdapter.notifyDataSetChanged();

                    setViewsVisible(true);
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
        JSONObject jsonObject = mCache.getAsJSONObject(getClass().getSimpleName());
        if (null != jsonObject) {
            try {
                JSONArray nextAr = jsonObject.getJSONArray("nextProducts");
                nextItemList.clear();
                for (int i = 0; i < nextAr.length(); i++) {
                    JSONObject headline = nextAr.getJSONObject(i);
                    NextItem item = NextItem.parse(headline);

                    nextItemList.add(item);
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
     * @param nextItemList
     * @return
     */
    private JSONObject saveToJSONObj(List<NextItem> nextItemList) {
        JSONObject outerJsonObj = new JSONObject();
        JSONArray nextAr = new JSONArray();
        for (NextItem nextItem : nextItemList) {
            JSONObject jsonObject = nextItem.toJSONObj();
            nextAr.put(jsonObject);
        }
        try {
            outerJsonObj.put("nextProducts", nextAr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return outerJsonObj;
    }

    //设置加载成功与否View的显示状态
    private void setViewsVisible(boolean loadSuccess) {
        setLoadingTvOut();
        if (null != mSwipeLayout && mSwipeLayout.isRefreshing()) mSwipeLayout.setRefreshing(false);
        if (loadSuccess) {
            if (null != mListView) mListView.setVisibility(View.VISIBLE);
            if (null != reloadBtn) reloadBtn.setVisibility(View.GONE);
        } else {
            if (null != mListView) mListView.setVisibility(View.GONE);
            if (null != reloadBtn) reloadBtn.setVisibility(View.VISIBLE);
        }
    }

    //设置加载Tv的进入动画
    private void setLoadingTvIn() {
        if (loadingTv.getVisibility() == View.GONE) {
            loadingTv.setVisibility(View.VISIBLE);
            Animation am = AnimationUtils.loadAnimation(activity, R.anim.translate_top_in);
            loadingTv.setAnimation(am);
            am.start();
        }
    }

    //设置加载Tv的退出动画
    private void setLoadingTvOut() {
        if (loadingTv.getVisibility() == View.VISIBLE) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadingTv.setVisibility(View.GONE);
                    Animation am = AnimationUtils.loadAnimation(activity, R.anim.translate_top_out);
                    loadingTv.setAnimation(am);
                    am.start();
                }
            }, 1000);
        }
    }

    @Override
    public void onRefresh() {
        loadData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    public class MyAdapter extends BaseAdapter implements StickyListHeadersAdapter {

        private List<NextItem> nextItemList;
        private LayoutInflater inflater;

        public MyAdapter(Context context, List<NextItem> nextItemList) {
            inflater = LayoutInflater.from(context);
            this.nextItemList = nextItemList;
        }

        @Override
        public int getCount() {
            return nextItemList != null ? nextItemList.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            if (nextItemList != null && nextItemList.size() > 0) {
                return nextItemList.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.next_product_item, parent, false);
                holder.title = (TextView) convertView.findViewById(R.id.next_product_item_title_tv);
                holder.content = (TextView) convertView.findViewById(R.id.next_product_item_content_tv);
                holder.voteCount = (TextView) convertView.findViewById(R.id.next_product_item_vote_count_tv);
                holder.commentCount = (TextView) convertView.findViewById(R.id.next_product_item_comment_count_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (nextItemList.size() > 0) {
                NextItem nextItem = nextItemList.get(position);
                if (null != nextItem) {
                    holder.title.setText(nextItem.getTitle());
                    holder.content.setText(nextItem.getContent());
                    holder.voteCount.setText("票数\n" + nextItem.getVoteCount());
                    holder.commentCount.setText(nextItem.getCommentCount() + "");
                }
            }


            return convertView;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            if (convertView == null) {
                holder = new HeaderViewHolder();
                convertView = inflater.inflate(R.layout.next_product_item_header, parent, false);
                holder.text = (TextView) convertView.findViewById(R.id.next_product_itemt_header_tv);
                convertView.setTag(holder);
            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }
            //set header text as first char in name
            if (nextItemList.size() > 0) {
                NextItem nextItem = nextItemList.get(position);
                if (null != nextItem) {
                    holder.text.setText(nextItem.getDate());
                }
            }

            return convertView;
        }

        @Override
        public long getHeaderId(int position) {
            //return the first character of the country as ID because this is what headers are based upon
            if (nextItemList.size() > 0) {
                return nextItemList.get(position).getDate().hashCode();
            }
            return 0;
        }

        class HeaderViewHolder {
            TextView text;
        }

        class ViewHolder {
            TextView title;
            TextView content;
            TextView voteCount;
            TextView commentCount;
        }

    }
}