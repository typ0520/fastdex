package com.example.fertilizercrm.activity;

import android.os.Bundle;
import android.widget.TextView;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.adapter.SendOutDetailAdapter;
import com.example.fertilizercrm.basic.ListViewActivity;
import com.example.fertilizercrm.bean.Product;
import com.example.fertilizercrm.bean.SendOutOrder;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.view.SendOutOrInHeadView;
import org.apache.http.Header;

import java.math.BigDecimal;
import java.util.List;

import butterknife.Bind;

/**
 * 发货单
 */
public class SendOutDetailActivity extends ListViewActivity<SendOutDetailAdapter> {
    public static final String KEY_OUTNUMBERS = "outnumbers";

    @Bind(R.id.tv_customer)    TextView tv_customer;//客户名称
    @Bind(R.id.tv_quantity)    TextView tv_quantity;//数量
    @Bind(R.id.tv_total_sales) TextView tv_total_sales;//销售总金额
    @Bind(R.id.tv_total_should)TextView tv_total_should;//应收总计
    @Bind(R.id.head_view)      SendOutOrInHeadView head_view;
    private String outnumbers;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_send_out_detail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        outnumbers = getIntent().getStringExtra(KEY_OUTNUMBERS);

        head_view.setEnabled(false);
        head_view.setDateString("");
        loadData();
    }

    private void loadData() {
        //加载发货单详情
        new Req().url(Req.salesDeal)
                .addParam("sign","searchdetail")
                .addParam("outnumbers", outnumbers)
                .get(new DefaultCallback<SendOutOrder>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, SendOutOrder response) {
                        head_view.setDateString(response.getPdate());
                        tv_customer.setText(response.getTusername());
                        adapter.setData(response.getProductdata());

                        head_view.setIncomeamount(response.getIncomeamount() + "");
                        head_view.setPricerole(response.getPricerole());
                        tv_quantity.setText("数量： " + response.getCountnumber());
                        tv_total_sales.setText("销售总计: " + response.getIncomeamount());
                        tv_total_should.setText("应收总计: 0");
                    }
                });
    }
}
