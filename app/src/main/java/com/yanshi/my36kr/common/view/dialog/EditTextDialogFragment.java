package com.yanshi.my36kr.common.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.yanshi.my36kr.R;

/**
 * 只有一个editText的dialog
 * 作者：yanshi
 * 时间：2014-12-05 11:58
 */
public class EditTextDialogFragment extends DialogFragment {

//    private String title;
    private String[] items = {"确认", "取消"};

    private EditText editText;
    private String editStr;
    private boolean singleLine;
    private int maxEms;

    private MyOnClickListener myOnClickListener;

    public void setMyOnClickListener(MyOnClickListener myOnClickListener) {
        this.myOnClickListener = myOnClickListener;
    }

    public void setEditTextParams(String editStr, boolean singleLine, int maxEms) {
        this.editStr = editStr;
        this.singleLine = singleLine;
        this.maxEms = maxEms;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        if (null != title) builder.setTitle(title);
        View view = getActivity().getLayoutInflater().inflate(R.layout.view_edit_dialog, null);
        editText = (EditText) view.findViewById(R.id.edit_dialog_view_et);
        editText.setText(editStr);
        editText.setSingleLine(singleLine);
        if(maxEms != 0) editText.setMaxEms(maxEms);
        builder.setView(view)
                .setPositiveButton(items[0], new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(null != myOnClickListener) myOnClickListener.onClick(editText.getText().toString());
                    }
                })
                .setNegativeButton(items[1], null)
                .setCancelable(true);

        return builder.create();
    }

    public interface MyOnClickListener {
        void onClick(String str);
    }
}
