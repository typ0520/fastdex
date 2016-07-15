package com.example.fertilizercrm.utils;

import android.text.TextUtils;

import com.example.fertilizercrm.http.URLText;

/**
 * Created by tong on 16/1/22.
 */
public class FerUtil {
    /**
     * 业务申请业务分割线
     */
    public static final String APPLY_SPLIT = "@@@";

    public static String getImageUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        if (url.startsWith("http")) {
            return url;
        }
        if (!url.startsWith("/")) {
            url = "/" + url;
        }
        return URLText.imgUrl + url;
    }

    /**
     * 2016-01-23 10:00:12.0 => 2016-01-23
     * @param str
     * @return
     */
    public static String formatCreateDate(String createDate) {
        String result = "";
        if (!TextUtils.isEmpty(createDate) && createDate.length() >= 10) {
            result = createDate.substring(0,10);
        }
        return result;
    }

    /**
     * 检查手机号是否合法
     * @param mobile
     * @throws RuntimeException
     */
    public static void checkMobile(String mobile) throws RuntimeException {
        if (TextUtils.isEmpty(mobile)) {
            throw new RuntimeException("请输入手机号");
        }
        if (mobile.length() != 11 || !mobile.startsWith("1")) {
            throw new RuntimeException("请输入正确的手机号");
        }
    }
}
