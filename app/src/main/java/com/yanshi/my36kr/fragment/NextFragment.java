package com.yanshi.my36kr.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.yanshi.my36kr.MyApplication;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.activity.DetailActivity;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.bean.NextItem;
import com.yanshi.my36kr.biz.NextItemBiz;
import com.yanshi.my36kr.biz.OnParseListener;
import com.yanshi.my36kr.common.utils.ACache;
import com.yanshi.my36kr.common.utils.ToastUtils;
import com.yanshi.my36kr.fragment.base.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * NEXT
 * 作者：yanshi
 * 时间：2014-10-30 14:48
 */
public class NextFragment extends BaseFragment {

    private static final String TAG = "NextFragment";
    private ACache mACache;

    private SwipeRefreshLayout mSrl;
    private StickyListHeadersListView mListView;
    private MyAdapter mAdapter;
    private Button reloadBtn;

    private List<NextItem> nextItemList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mACache = ACache.get(getActivity());
    }

    @Override
    public View onViewInit(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_next_product, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        findViews(view);
        setListener();

        loadCache();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setViewsVisible(true, true, false);
                loadData();
            }
        }, 300);
    }

    private void findViews(View view) {
        mSrl = (SwipeRefreshLayout) view.findViewById(R.id.next_product_content_sl);
        mSrl.setColorSchemeResources(R.color.secondary_color, R.color.primary_color, R.color.next_product_title_color, R.color.next_product_count_bg);
        mListView = (StickyListHeadersListView) view.findViewById(R.id.next_product_lv);
        reloadBtn = (Button) view.findViewById(R.id.next_product_reload_btn);

        mAdapter = new MyAdapter(mActivity, nextItemList);
        mListView.setAdapter(mAdapter);
    }

    private void setListener() {
        mSrl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setViewsVisible(true, true, false);
                loadData();
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int size = nextItemList.size();
                NextItem item;
                if (size > 0 && (item = nextItemList.get(position % size)) != null) {
                    Intent intent = new Intent(mActivity, DetailActivity.class);
                    intent.putExtra(Constant.NEXT_ITEM, item);
                    startActivity(intent);
                }
            }
        });
    }

    //加载缓存
    private void loadCache() {
        if (convertToList()) {
            if (null != mAdapter) mAdapter.notifyDataSetChanged();
        }
    }

    //加载网络
    private void loadData() {
        StringRequest stringRequest = new StringRequest(Constant.NEXT_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!TextUtils.isEmpty(response)) {
                    NextItemBiz.getFeed(response, new OnParseListener<NextItem>() {
                        @Override
                        public void onParseSuccess(List<NextItem> list) {
                            if (null != list && !list.isEmpty()) {
                                nextItemList.clear();
                                nextItemList.addAll(list);

                                if (null != mAdapter) mAdapter.notifyDataSetChanged();
                                setViewsVisible(false, true, false);

                                mACache.put(TAG, convertToJson(nextItemList));
                            } else {
                                setViewsVisible(false, false, true);
                                ToastUtils.show(mActivity, "parse html failed");
                            }
                        }

                        @Override
                        public void onParseFailed() {
                            setViewsVisible(false, false, true);
                            ToastUtils.show(mActivity, "parse html failed");
                        }
                    });
                } else {
                    setViewsVisible(false, false, true);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (null != nextItemList && nextItemList.isEmpty()) {
                    setViewsVisible(false, false, true);
                } else {
                    setViewsVisible(false, true, false);
                }
                ToastUtils.show(mActivity, error.getMessage());
            }
        });
        stringRequest.setTag(TAG);
        MyApplication.getRequestQueue().add(stringRequest);
    }

    //读取缓存，赋给list
    public boolean convertToList() {
        JSONObject jsonObject = mACache.getAsJSONObject(TAG);
        if (null != jsonObject) {
            try {
                JSONArray nextAr = jsonObject.getJSONArray("next_list");
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

    //转换数据为json
    private JSONObject convertToJson(List<NextItem> nextItemList) {
        JSONObject outerJsonObj = new JSONObject();
        JSONArray nextAr = new JSONArray();
        for (NextItem nextItem : nextItemList) {
            JSONObject jsonObject = nextItem.toJSONObj();
            nextAr.put(jsonObject);
        }
        try {
            outerJsonObj.put("next_list", nextAr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return outerJsonObj;
    }

    //设置各种View的显示状态
    private void setViewsVisible(boolean mSrl, boolean mListView, boolean reloadBtn) {
        if (mSrl) {
            if (null != this.mSrl) this.mSrl.setRefreshing(true);
        } else {
            if (null != this.mSrl) this.mSrl.setRefreshing(false);
        }
        if (mListView) {
            if (null != this.mListView) this.mListView.setVisibility(View.VISIBLE);
        } else {
            if (null != this.mListView) this.mListView.setVisibility(View.GONE);
        }
        if (reloadBtn) {
            if (null != this.reloadBtn) this.reloadBtn.setVisibility(View.VISIBLE);
        } else {
            if (null != this.reloadBtn) this.reloadBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MyApplication.getRequestQueue().cancelAll(TAG);
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
                convertView = inflater.inflate(R.layout.view_next_product_item, parent, false);
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
                    holder.voteCount.setText(nextItem.getVoteCount() + "");
                    holder.commentCount.setText("评论:" + nextItem.getCommentCount());
                }
            }


            return convertView;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            if (convertView == null) {
                holder = new HeaderViewHolder();
                convertView = inflater.inflate(R.layout.view_next_product_item_header, parent, false);
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