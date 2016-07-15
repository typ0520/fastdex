package com.example.fertilizercrm.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.fertilizercrm.R;

import com.example.fertilizercrm.common.utils.Logger;

/**
 * Created by tong on 15/12/11.
 * @see com.example.fertilizercrm.activity.ProductActivity
 */
public class ProductInfoSwitchView extends LinearLayout {
    private LinearLayout container;
    private OnItemClickListener mOnItemClickListener;

    public ProductInfoSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_product_info_switch, this);

        container = (LinearLayout) findViewById(R.id.ll_root);

        for (int i = 0;i < container.getChildCount();i++) {
            ViewGroup root = (ViewGroup) container.getChildAt(i);
            final int position = i;
            root.getChildAt(0).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    fireItemClick(position);
                }
            });
        }
    }

    private void fireItemClick(int position) {
        Logger.d(">> position: " + position);
        for (int i = 0;i < container.getChildCount();i++) {
            ViewGroup root = (ViewGroup) container.getChildAt(i);
            View marker = root.getChildAt(1);
            marker.setVisibility(position == i ? VISIBLE : INVISIBLE);
        }
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(position);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
