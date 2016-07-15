package com.example.fertilizercrm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.example.fertilizercrm.common.utils.Logger;
import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;
import com.example.fertilizercrm.bean.Receivable;
import com.example.fertilizercrm.bean.RoleBean;
import com.example.fertilizercrm.http.Params;
import com.example.fertilizercrm.http.Req;

import org.apache.http.Header;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 收款页面
 */
public class ReceivablesActivity extends BaseActivity {
    /**
     * 选择客户
     */
    private static final int REQUEST_CODE_GET_CUSTOMER = 1;

    @Bind(R.id.iv)          ImageView iv;
    @Bind(R.id.tv_customer_label) TextView tv_customer_label;
    @Bind(R.id.tv_company)  TextView tv_company;
    @Bind(R.id.tv_name)     TextView tv_name;
    @Bind(R.id.tv_mobile)   TextView tv_mobile;
    @Bind(R.id.tv_pay_type) TextView tv_pay_type;
    @Bind(R.id.iv_pay_type) ImageView iv_pay_type;
    @Bind(R.id.tv_use)      TextView tv_use;
    @Bind(R.id.iv_use)      ImageView iv_use;
    @Bind(R.id.tv_balance)  TextView tv_balance;
    @Bind(R.id.tv_amount)   TextView tv_amount;
    @Bind(R.id.et_remark)   EditText et_remark;

    @Bind(R.id.rl_customer) View rl_customer;
    @Bind(R.id.rl_receiver) View rl_receiver;
    private AlertView alertView;
    private RoleBean roleBean;
    private Receivable receivable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receivables);
        ButterKnife.bind(this);
        rl_receiver.setVisibility(View.GONE);

        receivable = (Receivable) getIntent().getSerializableExtra(KEY_DATA);
        if (receivable != null) {
            rl_customer.setClickable(false);
            findViewById(R.id.rl_pay_type).setClickable(false);
            findViewById(R.id.rl_use).setClickable(false);
            findViewById(R.id.btn_submit).setEnabled(false);
            findViewById(R.id.rl_amount).setVisibility(View.GONE);
            tv_amount.setEnabled(false);
            et_remark.setEnabled(false);

            tv_customer_label.setText("客户名字: " + receivable.getCusername());
            tv_pay_type.setText(receivable.getPaytype());
            tv_use.setText(receivable.getMuse());

            //TODO 设置账户余额
            tv_amount.setText(receivable.getAmount() + "");
            et_remark.setText(receivable.getMemo());
        }
    }

    @OnClick({R.id.rl_customer,R.id.rl_receiver}) void selectCustomer() {
        //选择收货人
        Intent intent = new Intent(this,CustomerListActivity.class);
        intent.putExtra(CustomerListActivity.KEY_OPEN_TYPE, CustomerListActivity.OPEN_TYPE_GET_CUSTOMER);
        startActivityForResult(intent, REQUEST_CODE_GET_CUSTOMER);
    }

    @OnClick(R.id.rl_pay_type) void selectPayType() {
//        打款方式：现金/打卡/账户/承兑/其它---类里配置好，直接保存名称
        //选择打款方式
        final String[] strings = new String[]{"现金", "打卡", "账户", "承兑"};
        alertView = new AlertView("请选择打款方式", null, null, null, strings, this, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                tv_pay_type.setText(strings[position]);
            }
        });
        alertView.show();
    }

    @OnClick(R.id.rl_use) void selectUse() {
//        款项用途：提货/预收款/还款/其它---类里配置好，直接保存名称
        //选择款项用途
        final String[] strings = new String[]{"提货", "预收款", "还款"};
        alertView = new AlertView("请选择款项用途", null, null, null, strings, this, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                tv_use.setText(strings[position]);
            }
        });
        alertView.show();
    }

    @OnClick(R.id.btn_submit) void clickSubmit() {
        //确定
        if (roleBean == null) {
            showLongToast("请先选择客户");
            return;
        }

        String amount = tv_amount.getText().toString();
        if (TextUtils.isEmpty(amount)) {
            showLongToast("请输入收款金额");
            return;
        }

        //token(唯一标识)、userid(当前用户id)、usertype(当前用户类型)、
        // sign(参数值固定为save)、cuserid (目标用户id)、cusertype (目标用户类型)、idate (收款日期)、
        // amount(收款金额)、paytype (打款方式-汉字)、muse (款项用途-汉字)


        Params params = new Params();
        params.put("sign","save");
        params.put("cuserid", roleBean.getBsoid() + "");
        params.put("cusertype", roleBean.getRoleTypeStr());
        params.put("idate",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        params.put("amount",amount);
        params.put("paytype",tv_pay_type.getText().toString());
        params.put("muse",tv_use.getText().toString());
        params.put("memo",et_remark.getText().toString());

        new Req().url(Req.inmoneyDeal).params(params).get(new DefaultCallback<JSONObject>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                showLongToast("恭喜您，操作成功!");
                finish();
            }
        });
    }

    @Override
    protected boolean onBack() {
        if (alertView != null && alertView.isShowing()) {
            alertView.dismiss();
            return true;
        }
        return super.onBack();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_GET_CUSTOMER
                && resultCode == RESULT_OK) {
            rl_customer.setVisibility(View.GONE);
            rl_receiver.setVisibility(View.VISIBLE);
            this.roleBean = (RoleBean)data.getSerializableExtra(CustomerListActivity.KEY_DATA);

            tv_balance.setText(roleBean.getBalance());
            tv_mobile.setText(roleBean.getMobile());
            tv_name.setText(roleBean.getContacter());
            tv_company.setText(roleBean.getCompany());
            Logger.d("<< roleBean: " + roleBean);
        }
    }
}
