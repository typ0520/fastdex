package com.example.fertilizercrm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.JSONArrayListActivity;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.view.SalesmanSearchHeadView;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
import butterknife.Bind;

/**
 * 业务申请查询列表
 */
public class BusinessApplyListActivity extends JSONArrayListActivity<BusinessApplyListActivity.ViewHolder> {
    @Bind(R.id.head_view) SalesmanSearchHeadView head_view;

    @Override
    protected int getCellLayoutResId() {
        return R.layout.list_item_business_apply_list;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_business_apply_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        head_view.showApplyTypeView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String content = adapter.getItem(position).optString("content");
                String type = adapter.getItem(position).optString("stypes");

                Intent intent = new Intent(getContext(),BusinessApplyActivity.class);
                intent.putExtra(BusinessApplyActivity.KEY_TYPE,type);
                intent.putExtra(BusinessApplyActivity.KEY_DATA,content);
                startActivity(intent);
            }
        });

        head_view.enableSubmit().setOnSearchListener(new SalesmanSearchHeadView.OnSearchListener() {
            @Override
            public void onSearch() {
                loadData();
            }
        });
    }

    private void loadData() {
        if (TextUtils.isEmpty(head_view.getBsoid())) {
            showLongToast("请先选择业务员");
            return;
        }
        new Req().url(Req.dailyWorkDeal)
                .addParam("sign", "searchsupply")
                .addParam("start", "1")
                .addParam("sbsoid",head_view.getBsoid())
                .addParam("begindate", head_view.getFormatStartDate())
                .addParam("enddate",head_view.getFormatEndDate())
                .addParam("stypes",head_view.getApplyType())
                .addParam("limit", "100").get(new DefaultCallback<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                if (response.length() > 0) {
                    head_view.animHide();
                }
                adapter.setData(response);
            }
        });
    }

    @Override
    protected boolean onBack() {
        if (head_view.isShowing()) {
            head_view.dismiss();
        }
        return super.onBack();
    }

    @Override
    protected void bindData(int position, ViewHolder holder, JSONObject obj) {
        holder.tv_content.setText(obj.optString("stypes"));
        holder.tv_name.setText(obj.optString("sname"));
        holder.tv_time.setText(obj.optString("sdate"));
    }

    public static final class ViewHolder {
        @Bind(R.id.tv_content)        TextView tv_content;//业务申请类型
        @Bind(R.id.tv_apply_name)     TextView tv_apply_name;//申请人label
        @Bind(R.id.tv_name)           TextView tv_name;//申请人名字
        @Bind(R.id.tv_time)           TextView tv_time;//时间
    }
}
