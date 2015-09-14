package com.yanshi.my36kr.common.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * 解决Toast重复出现多次，保持全局只有一个Toast实例
 * 
 * @author adison
 * 
 */
public class ToastFactory {
	private static Context context = null;
	private static Toast toast = null;

	public static Toast getToast(Context context, String text) {
		if (ToastFactory.context == context) {
			// toast.cancel();
			toast.setText(text);
			toast.setDuration(Toast.LENGTH_SHORT);

		} else {

			ToastFactory.context = context;
			toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		}
		return toast;
	}

	public static void cancelToast() {
		if (toast != null) {
			toast.cancel();
		}
	}

}
