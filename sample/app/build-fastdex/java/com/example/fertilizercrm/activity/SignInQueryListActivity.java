package com.example.fertilizercrm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import butterknife.Bind;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BeanArrayListActivity;
import com.example.fertilizercrm.bean.SignInfo;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.utils.FerUtil;
import com.example.fertilizercrm.view.SalesmanSearchHeadView;
import org.apache.http.Header;
import java.util.List;

/**
 * 签到查询
 */
public class SignInQueryListActivity extends BeanArrayListActivity<BusinessApplyListActivity.ViewHolder,SignInfo> {
    @Bind(R.id.head_view) SalesmanSearchHeadView head_view;

    @Override
    protected int getCellLayoutResId() {
        return R.layout.list_item_business_apply_list;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_sign_in_query_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        head_view.enableSubmit().setOnSearchListener(new SalesmanSearchHeadView.OnSearchListener() {
            @Override
            public void onSearch() {
                loadData();
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(),LocationSyncActivity.class);
                intent.putExtra(KEY_DATA,adapter.getItem(position));
                startActivity(intent);
            }
        });
    }

    private void loadData() {
        if (TextUtils.isEmpty(head_view.getBsoid())) {
            showLongToast("请先选择业务员");
            return;
        }
        new Req().url(Req.dailyWorkDeal)
                .addParam("sign", "searchsign")
                .addParam("start", "1")
                .addParam("sbsoid",head_view.getBsoid())
                .addParam("begindate", head_view.getFormatStartDate())
                .addParam("enddate",head_view.getFormatEndDate())
                .addParam("limit", "100").get(new DefaultCallback<List<SignInfo>>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, List<SignInfo> response) {
                if (response.size() > 0) {
                    head_view.animHide();
                }
                adapter.setData(response);
            }
        });
    }

    @Override
    protected void bindData(int position, BusinessApplyListActivity.ViewHolder holder, SignInfo obj) {
        holder.tv_content.setText(obj.getAddress());
        holder.tv_apply_name.setText("签到人");
        holder.tv_name.setText(obj.getSname());
        holder.tv_time.setText(FerUtil.formatCreateDate(obj.getCreateDate()));
    }

    public static final class ViewHolder {
        @Bind(R.id.tv_content)        TextView tv_content;
        @Bind(R.id.tv_apply_name)     TextView tv_apply_name;
        @Bind(R.id.tv_name)           TextView tv_name;
        @Bind(R.id.tv_time)           TextView tv_time;
    }
}
