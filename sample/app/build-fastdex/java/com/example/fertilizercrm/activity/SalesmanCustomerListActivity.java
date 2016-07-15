package com.example.fertilizercrm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import butterknife.Bind;
import com.example.fertilizercrm.common.utils.Logger;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.adapter.CustomerListAdapter;
import com.example.fertilizercrm.basic.ListViewActivity;
import com.example.fertilizercrm.bean.RoleBean;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.role.Role;
import com.example.fertilizercrm.view.CustomerTypeSwitchView;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 业务员的客户页面
 */
public class SalesmanCustomerListActivity extends ListViewActivity<CustomerListAdapter> {
    /**
     * 选择客户
     */
    private static final int REQUEST_CODE_GET_CUSTOMER = 1;

    /**
     * 业务员id
     */
    public static final String KEY_BSOID = "bsoid";

    @Bind(R.id.customer_switch) CustomerTypeSwitchView customer_switch;

    private String bsoid;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_salesman_customer_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bsoid = getIntent().getStringExtra(KEY_BSOID);
        Logger.d("<<业务员Id: " + bsoid);

        if (currentRole() == Role.erji) {
            setEmptyText("此业务员未分配农户");
        }

        getTitleView().setRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),CustomerListActivity.class);
                intent.putExtra(CustomerListActivity.KEY_OPEN_TYPE, CustomerListActivity.OPEN_TYPE_GET_CUSTOMER);
                startActivityForResult(intent, REQUEST_CODE_GET_CUSTOMER);
            }
        });
        customer_switch.setOnItemClickListener(new CustomerTypeSwitchView.OnItemClickListener() {
            @Override
            public void onItemClick(int type, Role role) {
                loadData(role);
            }
        });
        loadData(customer_switch.getCurrentRole());
    }

    private void loadData(Role role) {
        new Req().url(Req.salespersonDeal)
                .addParam("sign", "searchcustomer")
                .addParam("start","1")
                .addParam("limit","100")
                .addParam("bsoid",bsoid)
                .addParam("types", role.getCodeStr())
                .get(new DefaultCallback<JSONArray>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        adapter.setType(customer_switch.getCurrentType());
                        adapter.setData(response);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_GET_CUSTOMER
                && resultCode == RESULT_OK) {
            RoleBean roleBean = (RoleBean)data.getSerializableExtra(CustomerListActivity.KEY_DATA);
            Logger.d("<< roleBean: " + roleBean);
            assignCustomer(roleBean);
        }
    }

    //分配客户
    private void assignCustomer(final RoleBean roleBean) {
        new Req().url(Req.salespersonDeal)
                .addParam("sign", "savecustomer")
                .addParam("bsoid",bsoid)
                .addParam("count",1)
                .addParam("cid1",roleBean.getBsoid())
                .addParam("ctype1",roleBean.getRole().getCodeStr())
                .get(new DefaultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        customer_switch.setCurrentRole(roleBean.getRole());
                        loadData(roleBean.getRole());
                    }
                });
    }
}
