package com.example.fertilizercrm.common.view;

import android.content.Context;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;
import com.example.fertilizercrm.common.utils.Logger;

/**
 * Created by tong on 9/17/15.
 */
public abstract class BeanArrayAdapter<T> extends BaseAdapter {
    protected Context mContext;
    protected List<T> data;

    public BeanArrayAdapter(Context context) {
        this.mContext = context;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
        Logger.d(">>>>数据: " + data.toString());
        notifyDataSetChanged();
    }


    public void addData(List<T> data) {
        if (getData() != null) {
            this.data.addAll(data);
            Logger.d(">>>>添加数据: " + data.toString());
            notifyDataSetChanged();
        } else {
            setData(data);
        }
    }

    public void addData(T data) {
        if (getData() != null) {
            this.data.add(data);
            Logger.d(">>>>添加数据: " + data.toString());
            notifyDataSetChanged();
        } else {
            List<T> dataList = new ArrayList<>();
            dataList.add(data);
            setData(dataList);
        }
    }

    @Override
    public int getCount() {
        return this.data != null ? this.data.size() : 0;
    }

    @Override
    public T getItem(int position) {
        return this.data != null ? this.data.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void delete(int position) {
        getData().remove(position);
        notifyDataSetChanged();
    }
}
