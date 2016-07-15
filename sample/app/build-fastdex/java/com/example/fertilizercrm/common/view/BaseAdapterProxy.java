package com.example.fertilizercrm.common.view;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created by tong on 15/12/25.
 * adapter代理类
 */
public class BaseAdapterProxy extends BaseAdapter {

    private BaseAdapter base;

    public BaseAdapterProxy(BaseAdapter base) {
        this.base = base;
    }

    @Override
    public boolean hasStableIds() {
        return base.hasStableIds();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        base.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        base.unregisterDataSetObserver(observer);
    }

    @Override
    public void notifyDataSetChanged() {
        base.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated() {
        base.notifyDataSetInvalidated();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return base.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(int position) {
        return base.isEnabled(position);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return base.getDropDownView(position, convertView, parent);
    }

    @Override
    public int getItemViewType(int position) {
        return base.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return base.getViewTypeCount();
    }

    @Override
    public boolean isEmpty() {
        return base.isEmpty();
    }

    @Override
    public int getCount() {
        return base.getCount();
    }

    @Override
    public Object getItem(int position) {
        return base.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return base.getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return base.getView(position, convertView, parent);
    }
}
