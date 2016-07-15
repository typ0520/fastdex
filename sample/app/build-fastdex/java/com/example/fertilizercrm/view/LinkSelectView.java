package com.example.fertilizercrm.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.role.Role;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import com.example.fertilizercrm.common.utils.Logger;

/**
 * Created by tong on 15/12/11.
 * @see com.example.fertilizercrm.activity.LinkmanSelectActivity
 */
public class LinkSelectView extends LinearLayout {
    /**
     * 批发商
     */
    private static final int TYPE_YIJI = 0;

    /**
     * 零售商
     */
    private static final int TYPE_ERJI = 1;

    /**
     * 农户
     */
    private static final int TYPE_FARMER = 2;

    /**
     * 业务员
     */
    private static final int TYPE_SALESMAN = 3;

    private List<Button> buttons = new ArrayList<Button>();
    private OnItemClickListener mOnItemClickListener;

    @Bind(R.id.rl_yiji)     View rl_yiji;
    @Bind(R.id.rl_erji)     View rl_erji;
    @Bind(R.id.rl_farmer)   View rl_farmer;
    @Bind(R.id.rl_salesman) View rl_salesman;

    private Role currentRole;

    public LinkSelectView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_link_select, this);
        ButterKnife.bind(this);
        initRole();

        ViewGroup container = (ViewGroup) findViewById(R.id.ll_container);
        for (int i = 0;i < container.getChildCount();i++) {
            final int position = i;
            View v = container.getChildAt(i);
            if (v instanceof Button) {
                buttons.add((Button) v);
                v.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fireOnItemClick(position);
                    }
                });
            }
        }
    }

    protected void initRole() {
        if (Role.currentRole() == Role.factory
                || Role.currentRole() == Role.factory_salesman) {
            this.currentRole = Role.yiji;
            rl_yiji.setBackgroundDrawable(getResources().getDrawable(R.drawable.link_triangle));
        }
        else if (Role.currentRole() == Role.yiji
                || Role.currentRole() == Role.yiji_salesman) {
            rl_yiji.setVisibility(View.GONE);
            this.currentRole = Role.erji;
            rl_erji.setBackgroundDrawable(getResources().getDrawable(R.drawable.link_triangle));
        }
        else if (Role.currentRole() == Role.erji
                || Role.currentRole() == Role.erji_salesman) {
            //零售商客户只有农户
            //setVisibility(GONE);
            rl_yiji.setVisibility(View.GONE);
            rl_erji.setVisibility(View.GONE);
            this.currentRole = Role.farmer;
            rl_farmer.setBackgroundDrawable(getResources().getDrawable(R.drawable.link_triangle));
        }

        if (Role.currentRole().isSalesman()) {
            rl_salesman.setVisibility(GONE);
        }
    }

    private void fireOnItemClick(int position) {
        Logger.d(">> fireOnItemClick position: " + position);
        for (int i = 0;i < buttons.size();i++) {
            Button btn = buttons.get(i);

            if (i == position) {
                btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.link_triangle));
            } else {
                btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.link_select_bg_default));
            }
        }
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(getRoleByPosition(position));
        }
    }

    private Role getRoleByPosition(int position) {
        if (position == TYPE_YIJI) {
            return Role.yiji;
        }
        else if (position == TYPE_ERJI) {
            return Role.erji;
        }
        else if (position == TYPE_FARMER) {
            return Role.farmer;
        }
        return Role.currentRole().subRole();
    }

    public Role getCurrentRole() {
        return currentRole;
    }

    public void showFarmer(boolean show) {
        rl_farmer.setVisibility(show ? VISIBLE : GONE);

        if (show == false && Role.currentRole() == Role.erji_salesman) {
            rl_salesman.setBackgroundDrawable(getResources().getDrawable(R.drawable.link_triangle));
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Role role);
    }
}
