package com.example.fertilizercrm.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.example.fertilizercrm.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.example.fertilizercrm.common.utils.Logger;

/**
 * Created by tong on 15/12/11.
 * 产品列表headView
 * @see com.example.fertilizercrm.activity.ProductListActivity
 */
public class ProductListHeadView extends LinearLayout {
    @Bind(R.id.et)         EditText et;
    @Bind(R.id.tv_all)     TextView tv_all;
    @Bind(R.id.tv_stock)   TextView tv_stock;
    @Bind(R.id.iv_all)     ImageView iv_all;
    @Bind(R.id.iv_stock)   ImageView iv_stock;
    private int ordertype = 0;//默认0-desc排序。1-asc排序
    private OnEventListener mOnEventListener;
    private String ptype = "";
    private List<String> ptnameList;
    private AlertView alertView;
    private String keyword;

    public ProductListHeadView(Context context) {
        this(context, null);
    }

    public ProductListHeadView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_product_list_head, this);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.rl_all) void clickAll() {
        if (ptnameList == null || ptnameList.isEmpty()) {
            return;
        }

        final String[] strings = new String[ptnameList.size()];
        for (int i = 0;i < ptnameList.size();i++) {
            strings[i] = ptnameList.get(i);
        }
        alertView = new AlertView("请选择产品类别", null, null, null, strings, getContext(), AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                ptype = strings[position];
                tv_all.setText(ptype);
                if (mOnEventListener != null) {
                    mOnEventListener.onEvent(null,ptype,ordertype);
                }
            }
        });
        alertView.show();
    }

    @OnClick(R.id.rl_stock) void clickStock() {
        if (ordertype == 0) {
            ordertype = 1;
            iv_stock.setImageResource(R.drawable.green_array_bottom);
        }
        else {
            ordertype = 0;
            iv_stock.setImageResource(R.drawable.green_array_top);
        }
        if (mOnEventListener != null) {
            mOnEventListener.onEvent(null,ptype,ordertype);
        }
    }

    @OnClick(R.id.iv_search) void clickSearch() {
        if (mOnEventListener != null) {
            keyword = et.getText().toString().trim();
            mOnEventListener.onEvent(keyword,ptype,ordertype);
        }
        Logger.d(">> content: " + et.getText().toString());
    }

    public void setOnEventListener(OnEventListener listener) {
        this.mOnEventListener = listener;
    }

    public int getOrdertype() {
        return ordertype;
    }

    public String getPtype() {
        return ptype;
    }

    public void setPtnameList(List<String> ptnameList) {
        this.ptnameList = ptnameList;
    }

    public List<String> getPtnameList() {
        return ptnameList;
    }

    public String getKeyword() {
        return keyword;
    }
    public interface OnEventListener {
        void onEvent(String keyword,String ptype,int ordertype);
    }

    public AlertView getAlertView() {
        return alertView;
    }
}
