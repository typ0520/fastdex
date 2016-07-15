package com.example.fertilizercrm.common.view;

import android.content.Context;
import android.os.Build;
import android.widget.BaseAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.List;

import com.example.fertilizercrm.common.utils.Logger;

/**
 * Created by tong on 9/17/15.
 */
public abstract class JSONArrayAdapter extends BaseAdapter {
    protected Context mContext;
    protected JSONArray data;

    public JSONArrayAdapter(Context context) {
        this.mContext = context;
    }

    public JSONArray getData() {
        return data;
    }

    public void setData(JSONArray data) {
        this.data = data;
        Logger.d(">>>>数据: " + data.toString());
        notifyDataSetChanged();
    }


    public void addData(JSONArray data) {
        if (getData() != null) {
            if (data.length() > 0) {
                for (int i = 0;i < data.length();i++) {
                    getData().put(data.opt(i));
                }
            }
            Logger.d(">>>>添加数据: " + data.toString());
        } else {
            setData(data);
            Logger.d(">>>>初始化数据: " + data.toString());
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.data != null ? this.data.length() : 0;
    }

    @Override
    public JSONObject getItem(int i) {
        return this.data != null ? this.data.optJSONObject(i) : null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void delete(int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            data.remove(position);
        } else {
            try {
                Field field = data.getClass().getDeclaredField("values");
                field.setAccessible(true);
                List<Object> values = (List<Object>)field.get(data);
                values.remove(position);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        notifyDataSetChanged();
    }
}
