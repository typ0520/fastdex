package com.example.fertilizercrm.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.bean.PriceType;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by tong on 15/12/23.
 */
public class SendOutOrInHeadView extends LinearLayout {
    @Bind(R.id.tv_time)
    TextView tv_time;

    @Bind(R.id.tv_provision)
    TextView tv_provision;

    @Bind(R.id.tv_cost)
    TextView tv_cost;

    @Bind(R.id.tv_amount)
    TextView tv_amount;

    @Bind(R.id.rl_cost)
    View rl_cost;

    private String dateString;
    private String freight = "0.0";//单吨运输成本
    private String incomeamount = "0.0";//实收金额
    private AlertView alertView;
    private PriceType priceType = PriceType.defaultType();
    private OnStatusChangeListener mOnStatusChangeListener;

    public SendOutOrInHeadView(Context context) {
        super(context, null);
    }

    public SendOutOrInHeadView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_send_out_head, this);
        ButterKnife.bind(this);
        dateString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        tv_time.setText(dateString);
        tv_provision.setText(priceType.getDescription());
        if (getTag() != null) {
            String str = (String)getTag();
            if (str.equals("sendin")) {
                setSendOut(false);
            }
        }
    }

    @OnClick(R.id.rl_provision) void selectProvision() {
        if (!isEnabled()) {
            return;
        }
        //价格条目
        final String[] strings = new String[]{PriceType.defaultType().getDescription(),
                PriceType.defaultType().otherType().getDescription()};
        alertView = new AlertView("请选择价格条目", null, null, null, strings, getContext(), AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                priceType = position == 0 ? PriceType.defaultType() : PriceType.defaultType().otherType();
                tv_provision.setText(priceType.getDescription());
                if (mOnStatusChangeListener != null) {
                    mOnStatusChangeListener.onStatusChange(priceType);
                }
            }
        });
        alertView.show();
    }

    @OnClick(R.id.rl_cost) void clickCost() {
//        if (!isEnabled()) {
//            return;
//        }
//        //单吨运输成本
//        alertView = new AlertView("单吨运输成本", null, null, null,
//                new String[]{"确定", "取消"},
//                getContext(), AlertView.Style.PlainTextInput, new OnItemClickListener(){
//            public void onItemClick(Object o,int position){
//                if (position != 0)return;
//                freight = ((AlertView)o).getEditText().getText().toString();
//                BigDecimal bigDecimal = null;
//                try {
//                    bigDecimal = new BigDecimal(freight);
//                } catch (Throwable e) {
//                    bigDecimal = BigDecimal.ZERO;
//                }
//                freight = new DecimalFormat("0.0").format(bigDecimal);
//                tv_cost.setText(freight);
//            }
//        });
//        alertView.show();
//        EditText editText = alertView.getEditText();
//        editText.setHint("请输入单吨运输成本");
//        if (!"0.0".equals(freight)) {
//            editText.setText(freight);
//            editText.setSelection(freight.length());
//        }
    }

    @OnClick(R.id.rl_amount) void clickAmount() {
//        if (!isEnabled()) {
//            return;
//        }
//        //实收金额
//        EditText editText = null;
//         alertView = new AlertView("实收金额", null, null, null,
//                new String[]{"确定", "取消"},
//                getContext(), AlertView.Style.PlainTextInput, new OnItemClickListener(){
//            public void onItemClick(Object o,int position){
//                if (position != 0)return;
//                incomeamount = ((AlertView)o).getEditText().getText().toString();
//                BigDecimal bigDecimal = null;
//                try {
//                    bigDecimal = new BigDecimal(incomeamount);
//                } catch (Throwable e) {
//                    bigDecimal = BigDecimal.ZERO;
//                }
//                incomeamount = new DecimalFormat("0.0").format(bigDecimal);
//                tv_amount.setText(incomeamount);
//            }
//        });
//        alertView.show();
//        editText = alertView.getEditText();
//        editText.setHint("请输入实收金额");
//        if (!"0.0".equals(incomeamount)) {
//            editText.setText(incomeamount);
//            editText.setSelection(incomeamount.length());
//        }
    }

    public void setSendOut(boolean sendOut) {
        if (sendOut) {
            rl_cost.setVisibility(View.VISIBLE);
        } else {
            rl_cost.setVisibility(View.GONE);
        }
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
        tv_time.setText(dateString);
    }

    public String getDateString() {
        return dateString;
    }

    public PriceType getPriceType() {
        return priceType;
    }

    public String getPricerole() {
        return priceType.getDescription();
    }

    public String getFreight() {
        return freight;
    }

    public String getIncomeamount() {
        return incomeamount;
    }

    public void setIncomeamount(String incomeamount) {
        this.incomeamount = incomeamount;
        tv_amount.setText(incomeamount);
    }

    public void setFreight(String freight) {
        this.freight = freight;
    }

    public void dismiss() {
        if (alertView != null) {
            alertView.dismiss();
        }
    }

    public boolean isShowing() {
        return alertView != null && alertView.isShowing();
    }

    public void setOnStatusChangeListener(OnStatusChangeListener listener) {
        mOnStatusChangeListener = listener;
    }

    public void setPricerole(String pricerole) {
        tv_provision.setText(pricerole);
    }

    public interface OnStatusChangeListener {
        void onStatusChange(PriceType priceType);
    }
}
