package com.yanshi.my36kr.fragment.base;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * BaseFragment:当配合使用FragmentPagerAdapter时,能够保存view的状态
 * Created by Kinney on 2015/3/28.
 */
public abstract class BaseFragment extends Fragment {

    protected Activity mActivity;
    protected Handler mHandler;

    protected View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mHandler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null == rootView) {
            return onViewInit(inflater, container, savedInstanceState);
        }
        else {
            return rootView;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != rootView) {
            ViewGroup viewGroup = (ViewGroup) rootView.getParent();
            if (null != viewGroup) {
                viewGroup.removeView(rootView);
            }
        }
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    public abstract View onViewInit(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

}
