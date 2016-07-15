package com.example.fertilizercrm.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bigkoo.alertview.AlertView;
import com.bigkoo.pickerview.TimePickerView;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.role.Role;

import java.text.SimpleDateFormat;
import java.util.Date;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by tong on 15/12/13.
 * 业绩排名head view
 *
 * @see com.example.fertilizercrm.activity.PerformanceRankingsActivity
 */
public class PerformanceRankingsHeadView extends LinearLayout {
    /**
     * 产品销售排名
     */
    public static final int TYPE_PRODUCT_RANKING = 0;

    /**
     * 业务员销售排名
     */
    public static final int TYPE_SALESMAN_RANKING = 1;

    /**
     * 客户进货排名
     */
    public static final int TYPE_CUSTOMER_RANKING = 2;

    @Bind(R.id.btn_type)
    Button btn_type;

    @Bind(R.id.btn_location)
    Button btn_location;

    @Bind(R.id.tv_start_date)
    TextView tv_start_date;

    @Bind(R.id.tv_end_date)
    TextView tv_end_date;

    @Bind(R.id.btn_product)
    Button btn_product;

    @Bind(R.id.btn_customer)
    Button btn_customer;

    private int type;

    private AlertView alertView;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private Date startTime;
    private Date endTime;
    private Role currentCustomerRole;

    public PerformanceRankingsHeadView(Context context,AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_performance_rankings, this);
        ButterKnife.bind(this);

        btn_type.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] strings = new String[]{"产品销售排名", "业务员销售排名", "客户进货排名"};
                alertView = new AlertView("请选择查询类型", null, null, null, strings, getContext(), AlertView.Style.Alert, new com.bigkoo.alertview.OnItemClickListener() {
                    @Override
                    public void onItemClick(Object o, int position) {
                        setQueryType(position);
                        btn_type.setText(strings[position]);
                    }
                });
                alertView.show();
            }
        });

        btn_location.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btn_product.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        setQueryType(TYPE_PRODUCT_RANKING);
        setCurrentCustomerRole(getSupportRoles()[0]);
        startTime = new Date();
        endTime = new Date();

        tv_start_date.setText(getFormatStartTime());
        tv_end_date.setText(getFormatEndTime());
    }

    private void setCurrentCustomerRole(Role role) {
        currentCustomerRole = role;
        btn_customer.setText(role.getDescription());
    }

    @OnClick(R.id.ll_start_time) void selectStartTime() {
        TimePickerView timePickerView = new TimePickerView(getContext(), TimePickerView.Type.YEAR_MONTH_DAY);
        timePickerView.setTitle("开始时间");
        timePickerView.setTime(startTime);
        timePickerView.setOnTimeSelectListener(new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date) {
                startTime = date;
                tv_start_date.setText(getFormatStartTime());
            }
        });
        timePickerView.show();
    }

    @OnClick(R.id.ll_end_time) void selectEndTime() {
        TimePickerView timePickerView = new TimePickerView(getContext(), TimePickerView.Type.YEAR_MONTH_DAY);
        timePickerView.setTitle("结束时间");
        timePickerView.setTime(endTime);
        timePickerView.setOnTimeSelectListener(new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date) {
                endTime = date;
                tv_end_date.setText(getFormatEndTime());
            }
        });
        timePickerView.show();
    }

    @OnClick(R.id.btn_customer) void selectCustomer() {
        final Role[] roles = getSupportRoles();
        final String[] strings = new String[roles.length];
        for (int i = 0;i < roles.length;i++) {
            strings[i] = roles[i].getDescription();
        }
        alertView = new AlertView("请选择客户类型", null, null, null, strings, getContext(), AlertView.Style.Alert, new com.bigkoo.alertview.OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                setCurrentCustomerRole(roles[position]);
            }
        });
        alertView.show();
    }

    private Role[] getSupportRoles() {
        Role[] roles = null;
        if (Role.currentRole() == Role.factory
                || Role.currentRole() == Role.factory_salesman) {
            roles = new Role[]{Role.yiji,Role.erji,Role.farmer};
        }
        else if (Role.currentRole() == Role.yiji
                || Role.currentRole() == Role.yiji_salesman) {
            roles = new Role[]{Role.erji,Role.farmer};
        }
        else if (Role.currentRole() == Role.erji
                || Role.currentRole() == Role.erji_salesman) {
            roles = new Role[]{Role.farmer};
        }
        return roles;
    }

    private void setQueryType(int type) {
        btn_product.setVisibility(View.GONE);
        btn_location.setVisibility(View.GONE);
        btn_customer.setVisibility(View.GONE);

        if (type == TYPE_PRODUCT_RANKING) {

        }
        else if (type == TYPE_SALESMAN_RANKING) {

        }
        else if (type == TYPE_CUSTOMER_RANKING) {
            btn_customer.setVisibility(View.VISIBLE);
        }
        else {
            throw new RuntimeException("不存在的类型");
        }
        this.type = type;
    }

    public AlertView getAlertView() {
        return alertView;
    }

    public String getFormatStartTime() {
        return dateFormat.format(startTime);
    }

    public String getFormatEndTime() {
        return dateFormat.format(endTime);
    }

    public int getQueryType() {
        return type;
    }

    public int getCurrentCustomerType() {
        return currentCustomerRole.getCode();
    }
}
