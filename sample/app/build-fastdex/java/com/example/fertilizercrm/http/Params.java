package com.example.fertilizercrm.http;

import com.example.fertilizercrm.basic.DataManager;

import java.util.Map;

import com.example.fertilizercrm.common.httpclient.RequestParams;

/**
 * Created by tong on 15/10/10.
 */
public class Params extends RequestParams {
    public Params() {
        addInitInfo();
    }

    public Params(Map<String, String> source) {
        super(source);
        addInitInfo();
    }

    public Params(String key, String value) {
        super(key, value);
        addInitInfo();
    }

    public Params(Object... keysAndValues) {
        super(keysAndValues);
        addInitInfo();
    }

    public void put(String key, int value) {
        super.put(key, String.valueOf(value));
    }

    public void put(String key, long value) {
        super.put(key, String.valueOf(value));
    }

    public void put(String key, float value) {
        super.put(key, String.valueOf(value));
    }

    public void put(String key, double value) {
        super.put(key, String.valueOf(value));
    }

    private void addInitInfo() {
        if (DataManager.getInstance().getLoginResponse() != null) {
            put("token", DataManager.getInstance().getLoginResponse().getToken());
            put("userid", DataManager.getInstance().getLoginResponse().getMID() + "");
            put("usertype",DataManager.getInstance().getLoginResponse().getMUerType() + "");
        }
    }
}
