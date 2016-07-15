package com.example.fertilizercrm.activity;

import android.os.Bundle;
import android.widget.TextView;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.adapter.SendOutDetailAdapter;
import com.example.fertilizercrm.basic.ListViewActivity;
import com.example.fertilizercrm.bean.SendOutOrder;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.view.SendOutOrInHeadView;
import org.apache.http.Header;
import butterknife.Bind;

/**
 * 进货单
 */
public class SendInDetailActivity extends ListViewActivity<SendOutDetailAdapter> {
    public static final String KEY_INNUMBERS = "innumbers";

    @Bind(R.id.tv_customer)    TextView tv_customer;//客户名称
    @Bind(R.id.tv_quantity)    TextView tv_quantity;//数量
    @Bind(R.id.tv_total_sales) TextView tv_total_sales;//销售总金额
    @Bind(R.id.tv_total_should)TextView tv_total_should;//应收总计
    @Bind(R.id.head_view)      SendOutOrInHeadView head_view;
    private String innumbers;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_send_out_detail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        innumbers = getIntent().getStringExtra(KEY_INNUMBERS);

        getTitleView().setTitle("进货单");
        head_view.setEnabled(false);
        head_view.setSendOut(false);
        head_view.setDateString("");
        loadData();
    }

    private void loadData() {
        //加载发货单详情
        new Req().url(Req.purchaseDeal)
                .addParam("sign","searchdetail")
                .addParam("innumbers", innumbers)
                .get(new DefaultCallback<SendOutOrder>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, SendOutOrder response) {
                        head_view.setDateString(response.getPdate());
                        tv_customer.setText(response.getTusername());
                        adapter.setData(response.getProductdata());

                        head_view.setPricerole(response.getPricerole());
                        head_view.setIncomeamount(response.getIncomeamount() + "");
                        tv_quantity.setText("数量： " + response.getCountnumber());
                        tv_total_sales.setText("销售总计: " + response.getIncomeamount());
                        tv_total_should.setText("应收总计: 0");
                    }
                });
    }
}
