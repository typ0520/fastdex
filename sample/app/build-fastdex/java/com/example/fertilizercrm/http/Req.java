package com.example.fertilizercrm.http;

import java.io.File;
import java.io.FileNotFoundException;

import com.example.fertilizercrm.common.httpclient.AsyncHttpResponseHandler;

/**
 * Created by tong on 15/12/19.
 */
public class Req implements URLText {
    private String url;
    private boolean enableLog = true;
    private Params params = new Params();
    private Object tag;

    public Req url(String url) {
        this.url = url;
        return this;
    }

    public Req addParam(String key, String value) {
        params.put(key,value);
        return this;
    }
    public Req addParam(String key, File file) {
        try {
            params.put(key,file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return this;
    }

    public Req addParam(String key, int value) {
        params.put(key,value);
        return this;
    }

    public Req addParam(String key, double value) {
        params.put(key,value);
        return this;
    }

    public Req params(Params params) {
        this.params = params;
        return this;
    }

    public Req enableLog(boolean enableLog) {
        this.enableLog = enableLog;
        return this;
    }

    public void get(AsyncHttpResponseHandler responseHandler) {
        if (responseHandler != null) {
            responseHandler.setEnableLog(enableLog);
        }
        H.getInstance().get(url, params, responseHandler);
    }

    public void post(AsyncHttpResponseHandler responseHandler) {
        if (responseHandler != null) {
            responseHandler.setEnableLog(enableLog);
        }
        H.getInstance().post(url, params, responseHandler);
    }

    public Req tag(Object tag) {
        this.tag = tag;
        return this;
    }

    public Object tag() {
        return tag;
    }
}
