package com.example.fertilizercrm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BeanArrayListActivity;
import com.example.fertilizercrm.bean.VisitInfo;
import com.example.fertilizercrm.http.Params;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.utils.FerUtil;
import com.example.fertilizercrm.view.SalesmanSearchHeadView;
import org.apache.http.Header;
import java.util.List;
import butterknife.Bind;
import com.example.fertilizercrm.common.utils.JsonUtils;

/**
 * 拜访记录
 */
public class VisitRecordsListActivity extends BeanArrayListActivity<VisitRecordsListActivity.ViewHolder,VisitInfo> {
    @Bind(R.id.head_view) SalesmanSearchHeadView head_view;

    @Override
    protected int getCellLayoutResId() {
        return R.layout.list_item_call_records_list;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_visit_records_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), CustomerVisitAddActivity.class);
                intent.putExtra(KEY_OPEN_TYPE, CustomerVisitAddActivity.OPEN_TYPE_SHOW_DETIL);
                intent.putExtra(CustomerVisitAddActivity.KEY_VISIT_INFO, adapter.getItem(position));
                startActivity(intent);
            }
        });
        head_view.enableSubmit().setOnSearchListener(new SalesmanSearchHeadView.OnSearchListener() {
            @Override
            public void onSearch() {
                loadData();
            }
        });
        if (currentRole().isSalesman()) {
            loadData();
        }
    }

    @Override
    protected void bindData(int position, ViewHolder holder, VisitInfo obj) {
        holder.tv_dest.setText(obj.getFusername());
        holder.tv_name.setText(obj.getUsername());
        holder.tv_time.setText(FerUtil.formatCreateDate(obj.getCreateDate()));
    }

    private void loadData() {
        if (TextUtils.isEmpty(head_view.getBsoid())) {
            showLongToast("请先选择业务员");
            return;
        }
        Params params = new Params();
        params.put("sign", "searchvisit");
        params.put("start", "1");
        params.put("begindate", head_view.getFormatStartDate());
        params.put("enddate", head_view.getFormatEndDate());
        params.put("limit", "100");
        if (!currentRole().isSalesman()) {
            //厂家、一零售商查看拜访记录需要id
            params.put("sbsoid", head_view.getBsoid());
        }

        new Req().url(Req.dailyWorkDeal)
                .params(params)
                .get(new DefaultCallback<List<VisitInfo>>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, List<VisitInfo> response) {
                if (response.size() > 0) {
                    head_view.animHide();
                }
                adapter.setData(response);
            }
        });
    }

    public static final class ViewHolder {
        @Bind(R.id.tv_dest)     TextView tv_dest;
        @Bind(R.id.tv_name)     TextView tv_name;
        @Bind(R.id.tv_time)     TextView tv_time;
    }
}
