package com.example.fertilizercrm.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.fertilizercrm.R;

import com.example.fertilizercrm.common.utils.Logger;

/**
 * Created by tong on 14/11/8.
 * @see com.example.fertilizercrm.activity.MainActivity
 */
public class TabGroupView extends LinearLayout implements OnClickListener{
    private View ll_tab_01,ll_tab_02,ll_tab_03,ll_tab_04;
    private ImageView iv_tab_01;
    private ImageView iv_tab_02;
    private ImageView iv_tab_03;
    private ImageView iv_tab_04;

    private TextView tv_tab_01;
    private TextView tv_tab_02;
    private TextView tv_tab_03;
    private TextView tv_tab_04;

    private OnCheckedChangeListener mChangeListener;
    private int mCurrentIndex = 1;
	private TextView tv_new_num;

    private TextView unread_msg_number;
    private TextView unread_address_number;

	public TabGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.view_tab_group, this);
        unread_msg_number = (TextView) findViewById(R.id.unread_msg_number);
        unread_address_number = (TextView) findViewById(R.id.unread_address_number);
        (ll_tab_01 = findViewById(R.id.ll_tab_01)).setOnClickListener(this);
        (ll_tab_02 = findViewById(R.id.ll_tab_02)).setOnClickListener(this);
        (ll_tab_03 = findViewById(R.id.ll_tab_03)).setOnClickListener(this);
        (ll_tab_04 = findViewById(R.id.ll_tab_04)).setOnClickListener(this);

        iv_tab_01 = (ImageView) findViewById(R.id.iv_tab_01);
        iv_tab_02 = (ImageView) findViewById(R.id.iv_tab_02);
        iv_tab_03 = (ImageView) findViewById(R.id.iv_tab_03);
        iv_tab_04 = (ImageView) findViewById(R.id.iv_tab_04);

        tv_tab_01 = (TextView) findViewById(R.id.tv_tab_01);
        tv_tab_02 = (TextView) findViewById(R.id.tv_tab_02);
        tv_tab_03 = (TextView) findViewById(R.id.tv_tab_03);
        tv_tab_04 = (TextView) findViewById(R.id.tv_tab_04);
        tv_new_num = (TextView) findViewById(R.id.tv_new_num);
    }


    @Override
    public void onClick(View v) {
        int index = v.getId() == R.id.ll_tab_01 ? 0 :
        			v.getId() == R.id.ll_tab_02 ? 1 :
                    v.getId() == R.id.ll_tab_03 ? 2 :
                            v.getId() == R.id.ll_tab_04 ? 3 : 4;

        //Logger.d("<< tab index click: " + index);
        if (index == mCurrentIndex)
            return;
        setCheckedItem(index);
        if (mChangeListener != null) {
            mChangeListener.onCheckedChanged(index);
        }
    }

    public void setCheckedItem(int index) {
        mCurrentIndex = index;
        ll_tab_01.setBackgroundColor(getResources().getColor(R.color.tab_bg_default));
        ll_tab_02.setBackgroundColor(getResources().getColor(R.color.tab_bg_default));
        ll_tab_03.setBackgroundColor(getResources().getColor(R.color.tab_bg_default));
        ll_tab_04.setBackgroundColor(getResources().getColor(R.color.tab_bg_default));

        iv_tab_01.setImageResource(R.drawable.tab_01);
        iv_tab_02.setImageResource(R.drawable.tab_02);
        iv_tab_03.setImageResource(R.drawable.tab_03);
        iv_tab_04.setImageResource(R.drawable.tab_04);

        tv_tab_01.setTextColor(getResources().getColor(R.color.tab_text_default));
        tv_tab_02.setTextColor(getResources().getColor(R.color.tab_text_default));
        tv_tab_03.setTextColor(getResources().getColor(R.color.tab_text_default));
        tv_tab_04.setTextColor(getResources().getColor(R.color.tab_text_default));

        if (index == 0) {
            ll_tab_01.setBackgroundColor(getResources().getColor(R.color.tab_bg_checked));
            iv_tab_01.setImageResource(R.drawable.tab_1);
            tv_tab_01.setTextColor(getContext().getResources().getColor(R.color.tab_text_checked));
        } else if (index == 1) {
            ll_tab_02.setBackgroundColor(getResources().getColor(R.color.tab_bg_checked));
            iv_tab_02.setImageResource(R.drawable.tab_2);
            tv_tab_02.setTextColor(getContext().getResources().getColor(R.color.tab_text_checked));
        } else if (index == 2) {
            ll_tab_03.setBackgroundColor(getResources().getColor(R.color.tab_bg_checked));
            iv_tab_03.setImageResource(R.drawable.tab_3);
            tv_tab_03.setTextColor(getContext().getResources().getColor(R.color.tab_text_checked));
        } else if (index == 3) {
            ll_tab_04.setBackgroundColor(getResources().getColor(R.color.tab_bg_checked));
            iv_tab_04.setImageResource(R.drawable.tab_4);
            tv_tab_04.setTextColor(getContext().getResources().getColor(R.color.tab_text_checked));
        }
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener changeListener) {
        this.mChangeListener = changeListener;
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(int index);
    }

 	public void setMessageCount(int count) {
        tv_new_num.setVisibility(count <= 0 ? View.GONE : View.VISIBLE);
        tv_new_num.setText(count + "");
    }

    public TextView getUnread_msg_number() {
        return unread_msg_number;
    }

    public TextView getUnread_address_number() {
        return unread_address_number;
    }
}
