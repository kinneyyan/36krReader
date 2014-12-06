package com.yanshi.my36kr.ui;

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
import com.yanshi.my36kr.utils.ACache;
import com.yanshi.my36kr.utils.DataCleanManager;
import com.yanshi.my36kr.utils.SDCardUtils;
import com.yanshi.my36kr.utils.ToastFactory;
import com.yanshi.my36kr.view.dialog.ConfirmDialogFragment;
import com.yanshi.my36kr.view.dialog.LoadingDialogFragment;

import java.io.File;
import java.text.DecimalFormat;

/**
 * 设置页
 * Created by kingars on 2014/12/3.
 */
public class SettingsFragment extends Fragment {

    Activity activity;
    Button offlineDownloadBtn;//离线下载
    Button clearCacheBtn;//清除缓存
    TextView cacheSizeTv;//缓存大小
    //WebView的缓存路径
    final String WEBVIEW_CACHE_PATH = "/data/data/com.yanshi.my36kr/app_webview/Cache";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initListener();
        initCacheSize();
    }

    /**
     * 初始化缓存大小并显示
     */
    private void initCacheSize() {
        //内置存储cache
        double internalCacheSize = SDCardUtils.getDirSize(activity.getCacheDir());
        //外置存储cache
        double externalCacheSize = 0;
        if (SDCardUtils.isSDCardEnable()) externalCacheSize = SDCardUtils.getDirSize(activity.getExternalCacheDir());
        //WebView的cache
        double webViewCacheSize = SDCardUtils.getDirSize(new File(WEBVIEW_CACHE_PATH));
        String totalCacheSize = new DecimalFormat("#.00").format(internalCacheSize + externalCacheSize + webViewCacheSize);
        if (totalCacheSize.startsWith(".")) totalCacheSize = "0" + totalCacheSize;
        //若总大小小于0.1MB，直接显示0.00MB
        if (Float.parseFloat(totalCacheSize) < 0.10f) totalCacheSize = "0.00";
        String str = this.getString(R.string.settings_cache_size, totalCacheSize);

        cacheSizeTv.setText(str);
        Log.d("yslog", "getFileDir--->" + activity.getFilesDir().getAbsolutePath());
        Log.d("yslog", "internalCacheSize--->" + internalCacheSize);
        Log.d("yslog", "externalCacheSize--->" + externalCacheSize);
        Log.d("yslog", "webViewCacheSize--->" + webViewCacheSize);
    }

    private void initView(View view) {
        offlineDownloadBtn = (Button) view.findViewById(R.id.settings_offline_download_btn);
        clearCacheBtn = (Button) view.findViewById(R.id.settings_clear_cache_btn);
        cacheSizeTv = (TextView) view.findViewById(R.id.settings_cache_size_tv);
    }

    private void initListener() {
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

                        ACache.get(activity).clear();
                        ImageLoader.getInstance().clearDiskCache();
                        DataCleanManager.cleanInternalCache(activity);
                        DataCleanManager.cleanExternalCache(activity);
                        DataCleanManager.cleanCustomCache(WEBVIEW_CACHE_PATH);

                        new DialogHandler(loadingDialogFragment).sendEmptyMessageDelayed(0, 1000);
                    }
                }, null);
                confirmDialogFragment.show(activity.getFragmentManager(), "settings_confirm_dialog");
            }
        });
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
                initCacheSize();
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
}
