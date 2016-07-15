package com.example.fertilizercrm.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.JSONArrayListActivity;
import com.example.fertilizercrm.http.Params;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.view.PerformanceRankingsHeadView;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
import butterknife.Bind;

/**
 * 业绩排名
 */
public class PerformanceRankingsActivity extends JSONArrayListActivity<PerformanceRankingsActivity.ViewHolder> {
    @Bind(R.id.head_view) PerformanceRankingsHeadView headView;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_performance_rankings;
    }

    @Override
    protected int getCellLayoutResId() {
        return R.layout.list_item_performance_rankings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lv.setHeaderDividersEnabled(false);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        getTitleView().setRightText("筛选").setRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData(headView.getQueryType());
            }
        });
        loadData(headView.getQueryType());
    }

    @Override
    protected boolean onBack() {
        if (headView.getAlertView() != null && headView.getAlertView().isShowing()) {
            headView.getAlertView().dismiss();
            return true;
        }
        return super.onBack();
    }

    private void loadData(int type) {
        Params params = new Params();
        params.put("bdate",headView.getFormatStartTime());
        params.put("edate", headView.getFormatEndTime());

        if (type == headView.TYPE_PRODUCT_RANKING) {
            params.put("sign", "product");
        }
        else if (type == headView.TYPE_SALESMAN_RANKING) {
            params.put("sign", "salesperson");
        }
        else if (type == headView.TYPE_CUSTOMER_RANKING) {
            params.put("sign", "salesperson");
        }

        new Req().url(Req.rankingDeal)
                .params(params)
                .get(new DefaultCallback<JSONArray>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        adapter.setData(response);
                    }
                });
    }

    @Override
    protected void bindData(int position, ViewHolder holder, JSONObject obj) {
        if (headView.getQueryType() == headView.TYPE_PRODUCT_RANKING) {
            holder.tv_name.setText(obj.optString("pname"));
        }
        else if (headView.getQueryType() == headView.TYPE_SALESMAN_RANKING) {
            holder.tv_name.setText(obj.optString("sname"));
        }
        else if (headView.getQueryType() == headView.TYPE_CUSTOMER_RANKING) {
            holder.tv_name.setText(obj.optString("sname"));
        }
        holder.tv_quantity.setText(obj.optString("brand") + "吨");
    }

    public static final class ViewHolder {
        @Bind(R.id.tv_name)     TextView tv_name;
        @Bind(R.id.tv_quantity) TextView tv_quantity;
    }
}
