package com.example.fertilizercrm.common.utils;

import android.text.TextUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by tong on 15/1/15.
 */
public class EventEmitter {
    private Map<String,Set<OnEventListener>> mListenerMap = new HashMap<String, Set<OnEventListener>>();

    public void register(String event, OnEventListener listener){
        if (TextUtils.isEmpty(event))
            throw new IllegalArgumentException("event can not be null or empty!");
        if (listener == null)
            return;
        Set<OnEventListener> listenerSet = mListenerMap.get(event);
        if (listenerSet == null) {
            listenerSet = new HashSet<OnEventListener>();
            mListenerMap.put(event, listenerSet);
        }
        listenerSet.add(listener);
    }

    public void unregister(String event){
        if (TextUtils.isEmpty(event))
            return;
        Set<OnEventListener> listenerSet = mListenerMap.get(event);
        if (listenerSet == null)
            return;
        listenerSet.clear();
    }

    public void unregister(String event, OnEventListener listener){
        if (TextUtils.isEmpty(event))
            return;
        if (listener == null)
            return;
        Set<OnEventListener> listenerSet = mListenerMap.get(event);
        if (listenerSet == null)
            return;
        listenerSet.remove(listener);
    }

    public void unregisterAll() {
        mListenerMap.clear();
    }

    public void emit(String event) {
        emit(event,null);
    }

    public void emit(String event,Object data) {
        if (TextUtils.isEmpty(event))
            throw new IllegalArgumentException("event can not be null or empty!");
        Set<OnEventListener> listenerSet = mListenerMap.get(event);
        if (listenerSet == null)
            return;
        for (OnEventListener lis : listenerSet) {
            lis.onEvent(event,data);
        }
    }

    public static abstract class OnEventListener {
        public abstract void onEvent(String event, Object data);
    }
}
