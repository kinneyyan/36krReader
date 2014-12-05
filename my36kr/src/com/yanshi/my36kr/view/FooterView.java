package com.yanshi.my36kr.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.yanshi.my36kr.R;

/**
 * Created by kingars on 2014/11/1.
 */
public class FooterView extends FrameLayout {

    private TextView textView;
    private LinearLayout loadingLl;
    private OnLoadMoreListener listener;

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.loading_more_view, this);
        textView = (TextView) this.findViewById(R.id.loading_more_tv);
        loadingLl = (LinearLayout) this.findViewById(R.id.loading_more_ll);

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoadingState();

                if(null != listener) {
                    listener.onLoadMore();
                }
            }
        });
    }

    public void setLoadMoreState() {
        textView.setVisibility(VISIBLE);
        loadingLl.setVisibility(INVISIBLE);
    }

    public void setLoadingState() {
        textView.setVisibility(INVISIBLE);
        loadingLl.setVisibility(VISIBLE);
    }

    public FooterView(Context context) {
        super(context);
        init(context);
    }

    public FooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FooterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setListener(OnLoadMoreListener listener) {
        this.listener = listener;
    }

    public interface OnLoadMoreListener {
        public void onLoadMore();
    }
}
