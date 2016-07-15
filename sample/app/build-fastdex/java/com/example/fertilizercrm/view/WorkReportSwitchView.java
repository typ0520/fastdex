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
 * @see com.example.fertilizercrm.activity.MessageExpandActivity
 */
public class WorkReportSwitchView extends LinearLayout {

    private LinearLayout container;
    private OnItemClickListener mOnItemClickListener;
    private boolean enable = true;

    public WorkReportSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_work_report_switch, this);

        container = (LinearLayout) findViewById(R.id.ll_container);
        boolean showMarker = false;

        for (int i = 0;i < container.getChildCount();i++) {
            ViewGroup root = (ViewGroup) container.getChildAt(i);
            final int position = i;
            if (root.getVisibility() != GONE && !showMarker) {
                root.getChildAt(1).setVisibility(VISIBLE);
                showMarker = true;
            }
            root.getChildAt(0).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    fireItemClick(position);
                }
            });
        }
    }
    private void fireItemClick(int position) {
        if (!enable) {
            return;
        }
        Logger.d(">> position: " + position);
        for (int i = 0;i < container.getChildCount();i++) {
            ViewGroup root = (ViewGroup) container.getChildAt(i);
            View marker = root.getChildAt(1);
            marker.setVisibility(i == position ? VISIBLE : INVISIBLE);
        }
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(position);
        }
    }

    public void setCurrent(int position) {
        for (int i = 0;i < container.getChildCount();i++) {
            ViewGroup root = (ViewGroup) container.getChildAt(i);
            View marker = root.getChildAt(1);
            marker.setVisibility(i == position ? VISIBLE : INVISIBLE);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enable = enabled;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
