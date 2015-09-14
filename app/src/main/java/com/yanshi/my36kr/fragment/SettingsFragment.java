package com.yanshi.my36kr.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.common.utils.ACache;
import com.yanshi.my36kr.common.utils.DataCleanManager;
import com.yanshi.my36kr.common.utils.SDCardUtils;
import com.yanshi.my36kr.common.utils.ToastFactory;
import com.yanshi.my36kr.common.view.MyWebView;
import com.yanshi.my36kr.common.view.dialog.ConfirmDialogFragment;
import com.yanshi.my36kr.common.view.dialog.LoadingDialogFragment;

import java.io.File;
import java.text.DecimalFormat;

/**
 * 设置页
 * Created by kingars on 2014/12/3.
 */
public class SettingsFragment extends Fragment {

    private Activity activity;
    private DialogHandler dialogHandler;

    private Button offlineDownloadBtn;//离线下载
    private Button clearCacheBtn;//清除缓存
    private TextView cacheSizeTv;//缓存大小

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        setListener();
        initCacheSize();
    }

    /**
     * 初始化缓存大小并显示
     */
    private void initCacheSize() {
        String WEBVIEW_CACHE_PATH = "/data/data/com.yanshi.my36kr/app_webview/Cache";//WebView的缓存路径

        //内置存储cache
        double internalCacheSize = SDCardUtils.getDirSize(activity.getCacheDir());
        //外置存储cache
        double externalCacheSize = 0;
        if (SDCardUtils.isSDCardEnable())
            externalCacheSize = SDCardUtils.getDirSize(activity.getExternalCacheDir());
        //WebView的cache
        double webViewCacheSize = SDCardUtils.getDirSize(new File(WEBVIEW_CACHE_PATH));
        String totalCacheSize = new DecimalFormat("#.00").format(internalCacheSize + externalCacheSize + webViewCacheSize);
        if (totalCacheSize.startsWith(".")) totalCacheSize = "0" + totalCacheSize;
        //若总大小小于0.1MB，直接显示0.00MB
        if (Float.parseFloat(totalCacheSize) < 0.10f) totalCacheSize = "0.00";
        String str = getString(R.string.settings_cache_size, totalCacheSize);

        cacheSizeTv.setText(str);
        Log.d("yslog", "internalCacheSize--->" + internalCacheSize);
        Log.d("yslog", "externalCacheSize--->" + externalCacheSize);
        Log.d("yslog", "webViewCacheSize--->" + webViewCacheSize);
    }

    private void findViews(View view) {
        offlineDownloadBtn = (Button) view.findViewById(R.id.settings_offline_download_btn);
        clearCacheBtn = (Button) view.findViewById(R.id.settings_clear_cache_btn);
        cacheSizeTv = (TextView) view.findViewById(R.id.settings_cache_size_tv);
    }

    private void setListener() {
        offlineDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastFactory.getToast(activity, "开发中~").show();
            }
        });
        clearCacheBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = getResources().getString(R.string.confirm_dialog_title, "清除");
                ConfirmDialogFragment confirmDialogFragment = new ConfirmDialogFragment();
                confirmDialogFragment.setParams(title, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LoadingDialogFragment loadingDialogFragment = new LoadingDialogFragment();
                        loadingDialogFragment.setParams(getResources().getString(R.string.loading_dialog_title));
                        loadingDialogFragment.show(activity.getFragmentManager(), "settings_loading_dialog");

                        clearAppCache();

                        dialogHandler = new DialogHandler(loadingDialogFragment);
                        dialogHandler.sendEmptyMessageDelayed(0, 1000);
                    }
                }, null);
                confirmDialogFragment.show(activity.getFragmentManager(), "settings_confirm_dialog");
            }
        });
    }

    /**
     * 清除app所有的缓存
     */
    private void clearAppCache() {
        ACache.get(activity).clear();
        ImageLoader.getInstance().clearDiskCache();
        DataCleanManager.cleanInternalCache(activity);
        DataCleanManager.cleanExternalCache(activity);
        new MyWebView(activity).clearCache();
//        DataCleanManager.cleanCustomCache(WEBVIEW_CACHE_PATH);//root时可用
    }

    private class DialogHandler extends Handler {

        LoadingDialogFragment dialogFragment;

        public DialogHandler(LoadingDialogFragment dialogFragment) {
            this.dialogFragment = dialogFragment;
        }

        @Override
        public void handleMessage(Message msg) {
            if (null != dialogFragment && dialogFragment.getDialog().isShowing()) {
                dialogFragment.dismiss();

                ToastFactory.getToast(activity, getResources().getString(R.string.settings_clear_cache_success)).show();
//                initCacheSize();
                String str = getString(R.string.settings_cache_size, "0.00");
                cacheSizeTv.setText(str);
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        //当fragment显示时
        if (!hidden) {
            initCacheSize();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != dialogHandler) {
            dialogHandler.removeCallbacksAndMessages(null);
        }
    }
}
