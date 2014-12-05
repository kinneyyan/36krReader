package com.yanshi.my36kr.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * 通用确认的dialog
 * 作者：yanshi
 * 时间：2014-11-28 16:24
 */
public class ConfirmDialogFragment extends DialogFragment {

    private String title;
    private String[] items = {"确认", "取消"};
    private DialogInterface.OnClickListener okOnClickListener;
    private DialogInterface.OnClickListener cancelOnClickListener;

    public void setParams(String title, DialogInterface.OnClickListener okOnClickListener, DialogInterface.OnClickListener cancelOnClickListener) {
        setParams(title, items, okOnClickListener, cancelOnClickListener);
    }

    public void setParams(String title, String[] items, DialogInterface.OnClickListener okOnClickListener, DialogInterface.OnClickListener cancelOnClickListener) {
        this.title = title;
        this.items = items;
        this.okOnClickListener = okOnClickListener;
        this.cancelOnClickListener = cancelOnClickListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (null != title) builder.setTitle(title);
        if (null != items && items.length > 1) {
            builder.setPositiveButton(items[0], okOnClickListener)
                    .setNegativeButton(items[1], cancelOnClickListener);
        }
        builder.setCancelable(true);

        return builder.create();
    }
}
