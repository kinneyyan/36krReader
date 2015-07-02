package com.yanshi.my36kr.ui.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 可保存View状态的Fragment
 * Created by kingars on 2015/3/28.
 */
public abstract class BaseFragment extends Fragment {

    protected View rootView;

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
    }

    public abstract View onViewInit(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

}
