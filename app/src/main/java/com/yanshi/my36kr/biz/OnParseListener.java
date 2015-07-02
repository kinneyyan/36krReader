package com.yanshi.my36kr.biz;

import java.util.List;

/**
 * desc: 解析html的回调接口
 * author: shiyan
 * date: 2015/7/1
 */
public interface OnParseListener<T> {

    void onParseSuccess(List<T> list);
    void onParseFailed();

}
