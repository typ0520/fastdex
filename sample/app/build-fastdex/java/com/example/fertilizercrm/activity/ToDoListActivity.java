package com.example.fertilizercrm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Bind;
import com.example.fertilizercrm.common.utils.Logger;

import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.JSONArrayListActivity;
import com.example.fertilizercrm.bean.Receivable;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.utils.FerUtil;
import com.google.gson.Gson;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

/**
 * 待办事项
 */
public class ToDoListActivity extends JSONArrayListActivity<ToDoListActivity.ViewHolder> {
    private Map<Integer,String> typeMap = new HashMap<Integer,String>(){{
        put(0,"进货申请确认");
        put(1,"进货申请审批");
        put(2,"收款操作审批");
        put(3,"业务申请审批");
        put(4,"发货申请审批");
        put(5,"收货确认");
    }};

    @Override
    protected int getCellLayoutResId() {
        return R.layout.list_item_to_do;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getTitleView().setTitle("待办事项");
        loadData();
    }

    private void loadData() {
        new Req().url(Req.todoDeal)
                .addParam("sign", "search")
                .addParam("start", "1")
                .addParam("limit", "100").get(new DefaultCallback<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                adapter.setData(response);
            }
        });
    }

    @Override
    protected void bindData(final int position, ViewHolder holder, final JSONObject obj) {
        //类别：0进货申请确认、1进货申请审批、2收款操作审批、3业务申请审批、4 发货申请审批 5收货确认
        String time = obj.optString("createDate");
        holder.tv_time.setText(FerUtil.formatCreateDate(time));
        final int type = obj.optInt("types", -1);
        holder.tv_type.setText(typeMap.get(type));
        holder.tv_title.setText(obj.optString("title"));

        String actionText = "确认";
        if (type >= 1 && type <= 4) {
            actionText =  "审批";
        }
        holder.rl_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(type,position,obj);
            }
        });
        holder.btn_examine.setText(actionText);
        holder.btn_examine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onExamineItemClick(type, position, obj, false);
            }
        });
    }

    private void onItemClick(int type, int position, JSONObject obj) {
        Logger.d("");
        if (type == 0) {
            //进货申请确认
            Intent intent = new Intent(getActivity(),SendOutDetailActivity.class);
            intent.putExtra(SendOutDetailActivity.KEY_OUTNUMBERS,obj.optString("numbers"));
            startActivity(intent);
        } else if (type == 1) {
            //进货申请审批
            Intent intent = new Intent(getActivity(),SendOutDetailActivity.class);
            intent.putExtra(SendOutDetailActivity.KEY_OUTNUMBERS,obj.optString("numbers"));
            startActivity(intent);
        } else if (type == 2) {
            String json = obj.optString("id");
            Receivable receivable = new Gson().fromJson(json,Receivable.class);
            Intent intent = new Intent(getContext(),ReceivablesActivity.class);
            intent.putExtra(KEY_DATA,receivable);
            startActivity(intent);
        } else if (type == 3) {
            //业务申请审批
            JSONObject sd = obj.optJSONObject("sd");
            Intent intent = new Intent(getContext(),BusinessApplyActivity.class);
            intent.putExtra(BusinessApplyActivity.KEY_TYPE,sd.optString("stypes"));
            intent.putExtra(BusinessApplyActivity.KEY_DATA,sd.optString("content"));
            startActivity(intent);
        } else if (type == 4) {
            //发货申请审批
            Intent intent = new Intent(getActivity(),SendOutDetailActivity.class);
            intent.putExtra(SendOutDetailActivity.KEY_OUTNUMBERS,obj.optString("numbers"));
            startActivity(intent);
        } else if (type == 5) {
            //收货确认
            Intent intent = new Intent(getActivity(), SendInDetailActivity.class);
            intent.putExtra(SendInDetailActivity.KEY_INNUMBERS,  obj.optString("numbers"));
            startActivity(intent);
        }
    }

    public void onExamineItemClick(int type,int position,JSONObject obj,boolean batch) {
        if (type == 0) {
            //进货申请确认
            confirmStockApply(obj);
        } else if (type == 1) {
            //进货申请审批
            approvalStockApply(obj);
        } else if (type == 2) {
            //收款审批
            approvalReceivables(obj);
        } else if (type == 3) {
            //业务申请审批
            approvalBusinessApply(obj);
        } else if (type == 4) {
            //发货申请审批
            approvalDeliverApply(obj);
        } else if (type == 5) {
            //收货确认
            confirmDeliverApply(obj);
        }
    }

    /**
     * 收款审批
     * @param obj
     */
    private void approvalReceivables(JSONObject obj) {
        new Req().url(Req.inmoneyDeal)
                .addParam("sign", "confirm")
                .addParam("bsoid", obj.optString("numbers"))
                .get(new DefaultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        showLongToast("恭喜您，收款记录审核完成! ");
                        loadData();
                    }
                });
    }

    /**
     * 业务申请审批
     * @param obj
     */
    private void approvalBusinessApply(JSONObject obj) {
        new Req().url(Req.todoDeal)
                .addParam("sign", "checksupply")
                .addParam("bsoid", obj.optString("numbers"))
                .get(new DefaultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        showLongToast("恭喜您，审核操作已完成! ");
                        loadData();
                    }
                });
    }

    /**
     * 进货单确认
     * @param obj
     */
    private void confirmStockApply(JSONObject obj) {
        new Req().url(Req.salesDeal)
                .addParam("sign", "confirm")
                .addParam("outnumbers", obj.optString("numbers"))
                .get(new DefaultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        showLongToast("订单已确认，本次交易已完成");
                        loadData();
                    }
                });
    }

    /**
     * 进货单审批
     * @param obj
     */
    private void approvalStockApply(JSONObject obj) {
        new Req().url(Req.salesDeal)
                .addParam("sign", "check")
                .addParam("outnumbers", obj.optString("numbers"))
                .get(new DefaultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        showLongToast("订单已审核，等待对方确认收货！");
                        loadData();
                    }
                });
    }

    /**
     * 发货申请审批
     * @param obj
     */
    public void approvalDeliverApply(JSONObject obj) {
        new Req().url(Req.salesDeal)
                .addParam("sign", "check")
                .addParam("outnumbers", obj.optString("numbers"))
                .get(new DefaultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        showLongToast("订单已审核，等待对方确认收货！");
                        loadData();
                    }
                });
    }

    /**
     * 收货确认
     * @param obj
     */
    public void confirmDeliverApply(JSONObject obj) {
        new Req().url(Req.purchaseDeal)
                .addParam("sign", "confirm")
                .addParam("innumbers", obj.optString("numbers"))
                .get(new DefaultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        showLongToast("订单已确认，等待上级审批！");
                        loadData();
                    }
                });
    }

    @Override
    public void finish() {
        if (adapter.getData() != null) {
            Intent data = new Intent();
            data.putExtra(KEY_DATA,adapter.getData().length());
            setResult(RESULT_OK,data);
        }
        super.finish();
    }

    public static final class ViewHolder {
        @Bind(R.id.rl_container) View rl_container;
        @Bind(R.id.tv_time)     TextView tv_time;
        @Bind(R.id.tv_type)     TextView tv_type;
        @Bind(R.id.tv_title)    TextView tv_title;
        @Bind(R.id.btn_examine) Button btn_examine;
    }
}
