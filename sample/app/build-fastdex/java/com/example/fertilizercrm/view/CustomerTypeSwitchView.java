package com.example.fertilizercrm.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.role.Role;

import butterknife.Bind;
import butterknife.ButterKnife;
import com.example.fertilizercrm.common.utils.Logger;

/**
 * Created by tong on 15/12/11.
 * @see com.example.fertilizercrm.activity.MessageExpandActivity
 */
public class CustomerTypeSwitchView extends LinearLayout {
    /**
     * 批发商
     */
    public static final int TYPE_YIJI = 0;

    /**
     * 零售商
     */
    public static final int TYPE_ERJI = 1;

    /**
     * 农户
     */
    public static final int TYPE_FARMER = 2;

    private LinearLayout container;
    private OnItemClickListener mOnItemClickListener;
    private int currentType;
    private Role currentRole;

    @Bind(R.id.rl_yiji) View rl_yiji;
    @Bind(R.id.rl_erji) View rl_erji;
    @Bind(R.id.rl_farmer) View rl_farmer;

    public CustomerTypeSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_customer_type_switch, this);
        ButterKnife.bind(this);
        initRole();

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

    protected void initRole() {
        if (Role.currentRole() == Role.factory
                || Role.currentRole() == Role.factory_salesman) {
            this.currentType = TYPE_YIJI;
            this.currentRole = Role.yiji;
        }
        else if (Role.currentRole() == Role.yiji
                || Role.currentRole() == Role.yiji_salesman) {
            rl_yiji.setVisibility(View.GONE);
            this.currentType = TYPE_ERJI;
            this.currentRole = Role.erji;
        }
        else if (Role.currentRole() == Role.erji
                || Role.currentRole() == Role.erji_salesman) {
            //零售商客户只有农户，隐藏掉自身
            this.setVisibility(GONE);
            this.currentType = TYPE_FARMER;
            this.currentRole = Role.farmer;
        }
    }

    private void fireItemClick(int position) {
        Logger.d(">> position: " + position);
        for (int i = 0;i < container.getChildCount();i++) {
            ViewGroup root = (ViewGroup) container.getChildAt(i);
            View marker = root.getChildAt(1);
            marker.setVisibility(i == position ? VISIBLE : INVISIBLE);
        }
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(getTypeByPosition(position),getRoleByPosition(position));
        }
        this.currentType = getTypeByPosition(position);
        this.currentRole = getRoleByPosition(position);
    }

    private Role getRoleByPosition(int position) {
        return position == TYPE_YIJI ? Role.yiji :
                position == TYPE_ERJI ? Role.erji : Role.farmer;
    }

    public int getTypeByPosition(int position) {
        return position == TYPE_YIJI ? TYPE_YIJI :
                    position == TYPE_ERJI ? TYPE_ERJI : TYPE_FARMER;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int type,Role role);
    }

    public int getCurrentType() {
        return currentType;
    }

    public Role getCurrentRole() {
        return currentRole;
    }

    public void setCurrentRole(Role role) {
        if (role == Role.yiji) {
            currentType = TYPE_YIJI;
        }
        else if (role == Role.erji) {
            currentType = TYPE_ERJI;
        }
        else if (role == Role.farmer) {
            currentType = TYPE_FARMER;
        }
        currentRole = role;
        for (int i = 0;i < container.getChildCount();i++) {
            ViewGroup root = (ViewGroup) container.getChildAt(i);
            View marker = root.getChildAt(1);
            marker.setVisibility(i == currentType ? VISIBLE : INVISIBLE);
        }
    }
}
