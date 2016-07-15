package com.example.fertilizercrm.common.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;

/**
 * Created by tong on 9/17/15.
 */
public class SmartBeanArrayAdapter<BEAN> extends BeanArrayAdapter<BEAN> {
    protected Integer layoutResource;
    protected ViewBinder mViewBinder;
    protected Class<?> viewHolderClazz;

    public SmartBeanArrayAdapter(Context context,Integer layoutResource,Class<?> viewHolderClazz) {
        super(context);
        this.layoutResource = layoutResource;
        this.viewHolderClazz = viewHolderClazz;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mContext, this.layoutResource,null);
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

    protected Object getViewHolder() {
        try {
            return viewHolderClazz.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void setViewBinder(ViewBinder binder) {
        this.mViewBinder = binder;
    }

    public void bindData(int position, Object h,BEAN obj) {

    }

    public interface ViewBinder {
        void bindData(int position, Object holder,Object obj);
    }
}
