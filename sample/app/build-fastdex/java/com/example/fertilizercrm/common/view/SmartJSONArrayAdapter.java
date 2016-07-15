package com.example.fertilizercrm.common.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import org.json.JSONObject;
import butterknife.ButterKnife;

/**
 * Created by tong on 9/17/15.
 */
public class SmartJSONArrayAdapter extends JSONArrayAdapter {
    protected int layoutResource;
    protected ViewBinder mViewBinder;
    protected Class<?> viewHolderClazz;

    public SmartJSONArrayAdapter(Context context,int layoutResource,Class<?> viewHolderClazz) {
        super(context);
        this.layoutResource = layoutResource;
        this.viewHolderClazz = viewHolderClazz;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mContext, getLayoutResource(),null);
            Object holder = getViewHolder();
            ButterKnife.bind(holder, convertView);
            convertView.setTag(holder);
        }
        if (mViewBinder != null) {
            mViewBinder.bindData(position, convertView.getTag(), getItem(position));
        }
        bindData(position,convertView.getTag(), getItem(position));
        return convertView;
    }

    protected int getLayoutResource() {
        return this.layoutResource;
    }

    protected Class<?> getViewHolderClass() {
        return this.viewHolderClazz;
    }

    protected Object getViewHolder() {
        try {
            return getViewHolderClass().getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void setViewBinder(ViewBinder binder) {
        this.mViewBinder = binder;
    }

    public void bindData(int position, Object holder,JSONObject obj) {

    }

    public interface ViewBinder {
        void bindData(int position, Object holder,JSONObject obj);
    }
}
