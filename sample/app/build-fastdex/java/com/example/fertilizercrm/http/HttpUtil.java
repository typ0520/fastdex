package com.example.fertilizercrm.http;

import android.util.Log;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tong on 14/11/12.
 */
public class HttpUtil {
    public static String getCookie(Header[] headers) {
        if (headers == null)
            return "";
        for (Header header : headers) {
            if (header != null && header.getName() != null && "Set-Cookie".equals(header.getName().trim())) {
                Log.d(HttpUtil.class.getSimpleName(),"cookie: " + header.getValue());
                return header.getValue();
            }
        }
        return "";
    }

    public static Map<String,String> getPublicHeaders() {
        Map<String,String> headerMap = new HashMap<String, String>();

        return headerMap;
    }

    public static Header[] map2headerArray(Map<String, String> headerMap) {
        Header[] headers = null;
        if (headerMap != null) {
            headers = new Header[headerMap.size()];
            int i = 0;
            for (Map.Entry<String, String> headerKeyValue : headerMap.entrySet()) {
                headers[i++] = new BasicHeader(headerKeyValue.getKey(), headerKeyValue.getValue());
            }
        }
        return headers;
    }
}
