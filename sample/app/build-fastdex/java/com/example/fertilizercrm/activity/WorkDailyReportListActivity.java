package com.example.fertilizercrm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BeanArrayListActivity;
import com.example.fertilizercrm.bean.WorkDailyInfo;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.view.WorkDailyReportHeadView;
import org.apache.http.Header;
import java.util.ArrayList;
import java.util.List;
import butterknife.Bind;

/**
 * 工作日报列表
 */
public class WorkDailyReportListActivity extends BeanArrayListActivity<WorkDailyReportListActivity.ViewHolder,WorkDailyInfo> {
    @Bind(R.id.head_view) WorkDailyReportHeadView head_view;
    private List<WorkDailyInfo> jsonArray;

    @Override
    protected int getCellLayoutResId() {
        return R.layout.list_item_work_daily_report_list;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_work_daily_report_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        head_view.setOnSearchListener(new WorkDailyReportHeadView.OnSearchListener() {
            @Override
            public void onSearch(String keyword) {
                if (jsonArray == null) {
                    return;
                }
                if (TextUtils.isEmpty(keyword)) {
                    adapter.setData(jsonArray);
                    return;
                }
                List<WorkDailyInfo> array = new ArrayList<WorkDailyInfo>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    WorkDailyInfo obj = jsonArray.get(i);
                    String content = obj.getContent();
                    if (content == null) {
                        content = "";
                    }
                    if (content.contains(keyword)) {
                        array.add(obj);
                    }
                }
                adapter.setData(array);
            }
        });

        getTitleView().setRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(),WorkDailyReportActivity.class);
                intent.putExtra(KEY_DATA,adapter.getItem(position));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void bindData(int position, ViewHolder holder, WorkDailyInfo obj) {
        holder.tv_name.setText(obj.getSname());
        holder.tv_content.setText(obj.getContent());
    }

    private void loadData() {
        if (TextUtils.isEmpty(head_view.getBsoid())) {
            showLongToast("请先选择业务员");
            return;
        }
        new Req().url(Req.dailyWorkDeal)
                .addParam("sign", "searchdailyreport")
                .addParam("start", "1")
                .addParam("sbsoid",head_view.getBsoid())
                .addParam("begindate", head_view.getFormatStartDate())
                .addParam("enddate",head_view.getFormatEndDate())
                .addParam("limit", "100").get(new DefaultCallback<List<WorkDailyInfo>>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, List<WorkDailyInfo> response) {
                jsonArray = response;
                adapter.setData(response);
            }
        });
    }

    @Override
    protected boolean onBack() {
        if (head_view.isShowing()) {
            head_view.dismiss();
            return true;
        }
        return super.onBack();
    }

    public static final class ViewHolder {
        @Bind(R.id.tv_name)     TextView tv_name;
        @Bind(R.id.tv_content)  TextView tv_content;
    }
}
