package com.yanshi.my36kr.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * 通用列表项的dialog
 * 作者：yanshi
 * 时间：2014-11-03 18:16
 */
public class ListDialogFragment extends DialogFragment {

    private String title;
    private String[] items;
    private DialogInterface.OnClickListener chooserOnClickListener;

    public void setParams(String title, String[] items, DialogInterface.OnClickListener chooserOnClickListener) {
        this.title = title;
        this.items = items;
        this.chooserOnClickListener = chooserOnClickListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if(null != title) builder.setTitle(title);
        if(null != items && items.length > 0) builder.setItems(items, chooserOnClickListener);
        builder.setCancelable(true);

        return builder.create();
    }


}
