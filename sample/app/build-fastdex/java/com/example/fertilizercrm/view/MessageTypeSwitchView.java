package com.example.fertilizercrm.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.role.Role;

import com.example.fertilizercrm.common.utils.Logger;

/**
 * Created by tong on 15/12/11.
 * @see com.example.fertilizercrm.activity.MessageExpandActivity
 */
public class MessageTypeSwitchView extends LinearLayout {
    private LinearLayout container;
    private OnItemClickListener mOnItemClickListener;
    private View rl_site_msg;
    private int currentIndex;

    public MessageTypeSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_message_type_switch, this);

        container = (LinearLayout) findViewById(R.id.ll_container);

        for (int i = 0;i < container.getChildCount();i++) {
            ViewGroup root = (ViewGroup) container.getChildAt(i);
            final int position = i;
            root.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    fireItemClick(position);
                }
            });
        }
        rl_site_msg = findViewById(R.id.rl_site_msg);
        if (Role.currentRole() == Role.erji_salesman && getTag() == null) {
            rl_site_msg.setVisibility(View.GONE);
            fireItemClick(1);
        }
    }

    private void fireItemClick(int position) {
        Logger.d(">> position: " + position);
        for (int i = 0;i < container.getChildCount();i++) {
            ViewGroup root = (ViewGroup) container.getChildAt(i);
            ImageView marker = (ImageView) root.getChildAt(0);
            marker.setImageResource(position == i ? R.drawable.rect_bg_checked : R.drawable.rect_bg_default);
        }
        this.currentIndex = position;
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

    public int getCurrentIndex() {
        return currentIndex;
    }
}
