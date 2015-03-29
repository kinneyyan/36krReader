package com.yanshi.my36kr.view.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

/**
 * 通用加载的dialog
 * Created by kingars on 2014/11/29.
 */
public class LoadingDialogFragment extends DialogFragment {

    private String title;
    private String message;

    public void setParams(String message) {
        setParams(null, message);
    }

    public void setParams(String title, String message) {
        this.title = title;
        this.message = message;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        if(null != title) progressDialog.setTitle(title);
        if(null != message) progressDialog.setMessage(message);
        progressDialog.setCanceledOnTouchOutside(false);

        return progressDialog;
    }
}
