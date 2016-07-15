package com.example.fertilizercrm.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 业务员审核
 */
public class SalesmanAuditActivity extends BaseActivity {
    private static final String KEY_ICON = "icon";
    private static final String KEY_TITLE = "title";

    @Bind(R.id.grid_view) GridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salesman_audit);
        ButterKnife.bind(this);

        List<Map<String,Object>> list = getDataSource();
        String[] from = {KEY_ICON,KEY_TITLE};
        int[] to = {R.id.iv, R.id.tv_title};
        SimpleAdapter adapter = new SimpleAdapter(getActivity(),list,R.layout.list_item_salesman_audit,from,to);
        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (data.equals(R.drawable.tab_01) || data.equals("")) {
                    view.setVisibility(View.INVISIBLE);
                }
                return false;
            }
        });
        mGridView.setAdapter(adapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    startActivity(SignInQueryListActivity.class);
                }
                else if (position == 1) {
                    startActivity(WorkDailyReportListActivity.class);
                }
                else if (position == 2) {
                    startActivity(BusinessApplyListActivity.class);
                }
                else if (position == 3) {
                    startActivity(VisitRecordsListActivity.class);
                }
                else if (position == 4) {
                    startActivity(LocusActivity.class);
                }
            }
        });
    }

    protected List<Map<String, Object>> getDataSource() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        list.add(new HashMap<String, Object>(){{
            put(KEY_ICON,R.drawable.signin);
            put(KEY_TITLE,"签到查询");
        }});
        list.add(new HashMap<String, Object>(){{
            put(KEY_ICON,R.drawable.work_day_report);
            put(KEY_TITLE,"工作日报");
        }});
        list.add(new HashMap<String, Object>(){{
            put(KEY_ICON,R.drawable.business_audit);
            put(KEY_TITLE,"业务申请");
        }});
        list.add(new HashMap<String, Object>() {{
            put(KEY_ICON, R.drawable.call_records);
            put(KEY_TITLE, "拜访记录");
        }});
        list.add(new HashMap<String, Object>() {{
            put(KEY_ICON, R.drawable.track_view);
            put(KEY_TITLE, "轨迹查看");
        }});
        list.add(new HashMap<String, Object>() {{
            put(KEY_ICON, R.drawable.tab_01);
            put(KEY_TITLE, "");
        }});
        return list;
    }
}
