package com.example.fertilizercrm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.JSONArrayListActivity;
import com.example.fertilizercrm.bean.Salesman;
import com.example.fertilizercrm.http.Req;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
import butterknife.Bind;
import com.example.fertilizercrm.common.utils.JsonUtils;

/**
 * 我的业务员
 */
public class SalesmanListActivity extends JSONArrayListActivity<SalesmanListActivity.ViewHolder> {
    /**
     * 打开模式-获取业务员
     */
    public static final int OPEN_GET_GET_SALESMAN = 1;

    public static final String KEY_BSOID = "bsoid";
    public static final String KEY_SNAME = "sname";

    private static final int REQUEST_CODE_ADD_SALESMAN = 1;

    private static final int ACTION_PRODUCT = 0;
    private static final int ACTION_CUSTOMER = 1;
    private static final int ACTION_MODIFY = 2;
    private static final int ACTION_DELETE = 3;

    private static final int MENU_STATE_NORMAL = -1;
    private int checkedPosition = MENU_STATE_NORMAL;

    private View.OnClickListener menuClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int action = (Integer)v.getTag();
            onItemMenuClickListener(checkedPosition,action);
        }
    };

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_salesman_list;
    }

    @Override
    protected int getCellLayoutResId() {
        return R.layout.list_item_salesman_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleView().setRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(SalesmanSaveOrEditActivity.class, REQUEST_CODE_ADD_SALESMAN);
            }
        });
        loadData();
    }

    private void onItemMenuClickListener(int position, int action) {
        JSONObject obj = adapter.getItem(position);

        if (action == ACTION_PRODUCT) {
            //查看产品
            Intent intent = new Intent(getActivity(), SalesmanProductListActivity.class);
            intent.putExtra(SalesmanProductListActivity.KEY_BSOID, obj.optString("bsoid"));
            startActivity(intent);
        }
        else if (action == ACTION_CUSTOMER) {
            //查看客户
            Intent intent = new Intent(getActivity(), SalesmanCustomerListActivity.class);
            intent.putExtra(SalesmanProductListActivity.KEY_BSOID, obj.optString("bsoid"));
            startActivity(intent);
        }
        else if (action == ACTION_MODIFY) {
            Salesman salesman = JsonUtils.jsonToObject(obj.toString(),Salesman.class);
            //修改
            Intent intent = new Intent(getActivity(), SalesmanSaveOrEditActivity.class);
            intent.putExtra(SalesmanSaveOrEditActivity.KEY_OPEN_TYPE, SalesmanSaveOrEditActivity.OPEN_TYPE_EDIT);
            intent.putExtra(SalesmanSaveOrEditActivity.KEY_SALESMAN, salesman);

            startActivityForResult(intent, REQUEST_CODE_ADD_SALESMAN);
        }
        else if (action == ACTION_DELETE) {
            //删除
            new AlertView("提示", "确定删除选中的业务员吗?", null, new String[]{"确定", "取消"}, null, getActivity(),
                    AlertView.Style.Alert, new OnItemClickListener() {
                @Override
                public void onItemClick(Object o, int position) {
                    if (position == 0) {
                        deleteSalesman(position);
                    }
                }
            }).show();
        }
    }

    //删除成功
    private void deleteSalesman(final int position) {
        JSONObject obj = adapter.getItem(position);
        new Req().url(Req.salespersonDeal)
                .addParam("sign", "delete")
                .addParam("start","1")
                .addParam("limit","100")
                .addParam("bsoid",obj.optString("bsoid"))
                .get(new DefaultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        adapter.delete(position);
                        showLongToast("业务员删除成功!");
                    }
                });
    }

    private void loadData() {
        new Req().url(Req.salespersonDeal)
                .addParam("sign", "search")
                .addParam("start","1")
                .addParam("limit","100")
                .get(new DefaultCallback<JSONArray>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        adapter.setData(response);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ADD_SALESMAN
                && resultCode == RESULT_OK) {
            checkedPosition = MENU_STATE_NORMAL;
            loadData();
        }
    }

    @Override
    protected void bindData(final int position, ViewHolder holder, JSONObject obj) {
        holder.tv_name.setText(obj.optString("sname"));
        holder.tv_mobile.setText(obj.optString("phone"));
        if (openType == OPEN_TYPE_DEFAULT) {
            if (checkedPosition == position) {
                holder.ll_container.setVisibility(View.VISIBLE);
                holder.ib_menu.setImageResource(R.drawable.arrow_checked);
            } else {
                holder.ll_container.setVisibility(View.GONE);
                holder.ib_menu.setImageResource(R.drawable.arrow_default);
            }
            holder.rl_item_click.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkedPosition == position) {
                        checkedPosition = MENU_STATE_NORMAL;
                    } else {
                        checkedPosition = position;
                    }
                    adapter.notifyDataSetChanged();
                }
            });
            holder.ll_tab_01.setTag(ACTION_PRODUCT);
            holder.ll_tab_02.setTag(ACTION_CUSTOMER);
            holder.ll_tab_03.setTag(ACTION_MODIFY);
            holder.ll_tab_04.setTag(ACTION_DELETE);
            holder.ll_tab_01.setOnClickListener(menuClickListener);
            holder.ll_tab_02.setOnClickListener(menuClickListener);
            holder.ll_tab_03.setOnClickListener(menuClickListener);
            holder.ll_tab_04.setOnClickListener(menuClickListener);
        }
        else if (openType == OPEN_GET_GET_SALESMAN) {
            holder.ib_menu.setVisibility(View.GONE);
            holder.rl_item_click.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject obj = adapter.getItem(position);
                    Intent data = new Intent();
                    data.putExtra(KEY_BSOID,obj.optString("bsoid"));
                    data.putExtra(KEY_SNAME,obj.optString("sname"));
                    setResult(RESULT_OK,data);
                    finish();
                }
            });
        }
    }

    public static class ViewHolder {
        @Bind(R.id.tv_name)     TextView tv_name;
        @Bind(R.id.tv_mobile)   TextView tv_mobile;
        @Bind(R.id.ib_menu)     ImageButton ib_menu;
        @Bind(R.id.rl_item_click) View rl_item_click;
        @Bind(R.id.ll_container) View ll_container;
        @Bind(R.id.ll_tab_01)   View ll_tab_01;
        @Bind(R.id.ll_tab_02)   View ll_tab_02;
        @Bind(R.id.ll_tab_03)   View ll_tab_03;
        @Bind(R.id.ll_tab_04)   View ll_tab_04;
    }
}


