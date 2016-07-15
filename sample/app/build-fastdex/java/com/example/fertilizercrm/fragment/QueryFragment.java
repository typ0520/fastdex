package com.example.fertilizercrm.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.activity.PerformanceRankingsActivity;
import com.example.fertilizercrm.activity.ReceivablesListActivity;
import com.example.fertilizercrm.activity.SalesmanAuditActivity;
import com.example.fertilizercrm.activity.SendInListActivity;
import com.example.fertilizercrm.activity.SendOutListActivity;
import com.example.fertilizercrm.activity.VisitRecordsListActivity;
import com.example.fertilizercrm.basic.BaseFragment;
import com.example.fertilizercrm.role.Role;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 查询页面
 */
public class QueryFragment extends BaseFragment {
    /**
     * 出货查询
     */
    public static final int ACTION_SEND_OUT_QUERY = 1;
    /**
     * 款项查询
     */
    public static final int ACTION_RECEIPT_QUERY = 2;
    /**
     * 业务员考核
     */
    public static final int ACTION_SALESMAN_AUDIT = 3;
    /**
     * 业绩排名
     */
    public static final int ACTION_PERFORMANCE_RANKINGS = 4;
    /**
     * 拜访轨迹
     */
    public static final int ACTION_VISIT_TRACK = 5;
    /**
     * 进货查询
     */
    public static final int ACTION_SEND_IN_QUERY = 6;

    //    查询
    //    厂家:
    //    出货查询 款项查询 业务员考核 业绩排名
    //
    //    厂家业务员:
    //    出货查询 款项查询 拜访轨迹
    //
    //    批发商
    //    出货查询 进货查询 款项查询 业务员考核 业绩排名
    //
    //    批发商业务员
    //    出货查询 款项查询 拜访轨迹
    //
    //    零售商
    //    出货查询 进货查询 款项查询 业务员考核 业绩排名
    //
    //    零售商业务员
    //    出货查询 款项查询 拜访轨迹

    @Bind(R.id.rl_container) ViewGroup rl_container;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int layoutId = R.layout.fragment_query;
        if (currentRole() == Role.factory) {
            layoutId = R.layout.fragment_query_factory;
        }
        else if (currentRole().isSalesman()) {
            layoutId = R.layout.fragment_query_salesman;
        }
        View contentView = obtainContentView(layoutId, container);
        ButterKnife.bind(this, contentView);
        return contentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        for (int i = 0;i < rl_container.getChildCount();i++) {
            if (rl_container.getChildAt(i) instanceof RelativeLayout) {
                final ViewGroup item = (ViewGroup) rl_container.getChildAt(i);
                if (item.getTag() != null) {
                    item.setClickable(true);
                    item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onItemClick(Integer.valueOf((String) item.getTag()));
                        }
                    });
                }
            }
        }
    }

    protected void onItemClick(int action) {
        if (action == ACTION_SEND_OUT_QUERY) {
            //出货查询
            startActivity(SendOutListActivity.class);
        }
        else if (action == ACTION_RECEIPT_QUERY) {
            //收款查询
            startActivity(ReceivablesListActivity.class);
        }
        else if (action == ACTION_SALESMAN_AUDIT) {
            //业务员考核
            startActivity(SalesmanAuditActivity.class);
        }
        else if (action == ACTION_PERFORMANCE_RANKINGS) {
            //业绩排名
            startActivity(PerformanceRankingsActivity.class);
        }
        else if (action == ACTION_VISIT_TRACK) {
            startActivity(VisitRecordsListActivity.class);
        }
        else if (action == ACTION_SEND_IN_QUERY) {
            //进货查询
            startActivity(SendInListActivity.class);
        }
    }
}
