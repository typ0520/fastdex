package com.example.fertilizercrm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BeanArrayListActivity;
import com.example.fertilizercrm.bean.Receivable;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.utils.FerUtil;

import org.apache.http.Header;
import java.util.List;
import butterknife.Bind;
import com.example.fertilizercrm.common.utils.DimensionPixelUtil;

/**
 * 收款查询列表
 */
public class ReceivablesListActivity extends BeanArrayListActivity<ReceivablesListActivity.ViewHolder,Receivable> {
    @Override
    protected int getCellLayoutResId() {
        return R.layout.list_item_receivables_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getTitleView().setTitle("收款查询");
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(),ReceivablesActivity.class);
                intent.putExtra(KEY_DATA,adapter.getItem(position));
                startActivity(intent);
            }
        });
        loadData();
    }

    @Override
    protected void bindData(int position, ViewHolder holder, Receivable obj) {
//        {
//            "amount": 1,
//                "bsoid": 1,
//                "createDate": "2016-01-02 15:01:04.0",
//                "creator": "fuck1",
//                "cuserid": 1,
//                "cusername": "李老二",
//                "cusertype": 7,
//                "memo": "",
//                "modifier": "",
//                "modifyDate": "",
//                "muse": "提货",
//                "paytype": "现金",
//                "sbsoid": 0,
//                "sname": "",
//                "status": 0,
//                "userid": 1,
//                "usertype": 5
//        }
        holder.tv_amount.setText("￥" + obj.getAmount());
        holder.tv_time.setText(FerUtil.formatCreateDate(obj.getCreateDate()));
        holder.tv_name.setText(obj.getPaytype());
        holder.tv_content.setTextSize(13.f);
        if (TextUtils.isEmpty(obj.getSname())) {
            holder.tv_content.setText("客户: " + obj.getCusername());
        }
        else {
            holder.tv_content.setText("客户: " + obj.getCusername() + " 发起人: " + obj.getSname());
        }
    }

    private void loadData() {
        new Req().url(Req.inmoneyDeal)
                .addParam("sign", "search")
                .addParam("start","1")
                .addParam("limit","10000")
                .get(new DefaultCallback<List<Receivable>>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, List<Receivable> response) {
                        adapter.setData(response);
                    }
                });
    }

    public static final class ViewHolder {
        @Bind(R.id.tv_content)        TextView tv_content;
        @Bind(R.id.tv_apply_name)     TextView tv_apply_name;
        @Bind(R.id.tv_name)           TextView tv_name;
        @Bind(R.id.tv_amount)         TextView tv_amount;
        @Bind(R.id.tv_time)           TextView tv_time;
    }
}
