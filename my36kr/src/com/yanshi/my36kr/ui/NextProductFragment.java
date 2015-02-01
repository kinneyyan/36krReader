package com.yanshi.my36kr.ui;

import android.app.ActionBar;
import android.content.*;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.bean.NextItem;
import com.yanshi.my36kr.biz.NextItemBiz;
import com.yanshi.my36kr.dao.NextItemDao;
import com.yanshi.my36kr.utils.ACache;
import com.yanshi.my36kr.utils.HttpUtils;
import com.yanshi.my36kr.utils.NetUtils;
import com.yanshi.my36kr.utils.ToastFactory;
import com.yanshi.my36kr.view.dialog.ListDialogFragment;
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

    private static final int LOAD_COMPLETE = 0X110;
    private MainActivity activity;
    private ActionBar actionBar;
    private ACache mCache;

    private SwipeRefreshLayout mSwipeLayout;
    private StickyListHeadersListView mListView;
    private MyAdapter mAdapter;
    private Button reloadBtn;

    private List<NextItem> nextItemList = new ArrayList<NextItem>();
    private List<NextItem> loadingNextItemList;//加载时的list
    private NextItemBiz nextItemBiz = new NextItemBiz();

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            if (null != actionBar) {
                actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.next_product_title_color)));
            }
        } else {
            if (null != actionBar) {
                actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.primary_color)));
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) this.getActivity();
        actionBar = activity.getActionBar();
        if (null != actionBar) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.next_product_title_color)));
        }
        mCache = ACache.get(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.next_product_fragment, null);
        initView(view);
        initListener();

        return view;
    }

    private void initView(View view) {
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.next_product_content_sl);
        mSwipeLayout.setColorSchemeResources(R.color.secondary_color, R.color.primary_color, R.color.next_product_title_color, R.color.next_product_count_bg);
        mListView = (StickyListHeadersListView) view.findViewById(R.id.next_product_lv);
        reloadBtn = (Button) view.findViewById(R.id.next_product_reload_btn);

        mAdapter = new MyAdapter(activity, nextItemList);
        mListView.setAdapter(mAdapter);
    }

    private void initListener() {
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
//                    intent.putExtra(Constant.TITLE, item.getTitle());
//                    intent.putExtra(Constant.URL, item.getUrl());
                    startActivity(intent);
                }
            }
        });
/*        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int size = nextItemList.size();
                final NextItem item = nextItemList.get(position % size);
                if (null == item) return false;
                String[] chooserItems = getResources().getStringArray(R.array.chooser_dialog_items_list);
                ListDialogFragment listDialogFragment = new ListDialogFragment();
                listDialogFragment.setParams(null, chooserItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: //收藏
                                if (new NextItemDao(activity).add(item)) {
                                    ToastFactory.getToast(activity, getResources().getString(R.string.collect_success)).show();
                                } else {
                                    ToastFactory.getToast(activity, getResources().getString(R.string.collect_failed)).show();
                                }
                                break;
                            case 1: //转发
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, item.getTitle() + "【" + getResources().getString(R.string.app_name) + "】" + " - " + item.getUrl());
                                sendIntent.setType("text/plain");
                                startActivity(Intent.createChooser(sendIntent, "分享到"));
                                break;
                            case 2: //复制链接
                                ClipboardManager clipboardManager = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, item.getUrl()));
                                ToastFactory.getToast(activity, getResources().getString(R.string.has_copied_tip)).show();
                                break;
                        }
                    }
                });
                listDialogFragment.show(getFragmentManager(), "Index");

                return true;
            }
        });*/
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity.refreshOptionsMenu(true);
        loadCache();
        loadData();
    }

    private void loadCache() {
        if (getJsonToDataList()) {
            if (null != mAdapter) mAdapter.notifyDataSetChanged();

            mListView.setVisibility(View.VISIBLE);
//            mSwipeLayout.setRefreshing(true);
        }
    }

    private void loadData() {
        if (!NetUtils.isConnected(activity)) {
            ToastFactory.getToast(activity, activity.getResources().getString(R.string.network_not_access)).show();

            if (mSwipeLayout.isRefreshing()) mSwipeLayout.setRefreshing(false);
            activity.refreshOptionsMenu(false);
            if(nextItemList.isEmpty()) reloadBtn.setVisibility(View.VISIBLE);
            return;
        }

        HttpUtils.doGetAsyn(Constant.NEXT_URL, new HttpUtils.CallBack() {
            @Override
            public void onRequestComplete(String result) {
                if (null != result) {
                    loadingNextItemList = nextItemBiz.getNextItems(result);

                    mCache.put(Constant.NEXT_PRODUCT_CACHE, saveToJSONObj(loadingNextItemList), ACache.TIME_DAY);
                    mHandler.sendEmptyMessage(LOAD_COMPLETE);
                }
            }
        });
    }

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
                    loadingNextItemList = null;

                    if (null != mAdapter) mAdapter.notifyDataSetChanged();

                    if (mSwipeLayout.isRefreshing()) mSwipeLayout.setRefreshing(false);
                    mListView.setVisibility(View.VISIBLE);
                    activity.refreshOptionsMenu(false);
                    reloadBtn.setVisibility(View.GONE);
                    break;
                default:
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
        JSONObject jsonObject = mCache.getAsJSONObject(Constant.NEXT_PRODUCT_CACHE);
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

    @Override
    public void onRefresh() {
        loadData();
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