package com.example.fertilizercrm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import butterknife.Bind;
import com.example.fertilizercrm.common.utils.Logger;

import com.example.fertilizercrm.R;
import com.example.fertilizercrm.adapter.LinkmanAdapter;
import com.example.fertilizercrm.basic.ListViewActivity;
import com.example.fertilizercrm.bean.Agent;
import com.example.fertilizercrm.bean.AgentLevel2;
import com.example.fertilizercrm.bean.Farmer;
import com.example.fertilizercrm.bean.RoleBean;
import com.example.fertilizercrm.bean.Salesman;
import com.example.fertilizercrm.http.Params;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.role.Role;
import com.example.fertilizercrm.view.LinkSelectView;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 联系人选择
 */
public class LinkmanSelectActivity extends ListViewActivity<LinkmanAdapter> {
    public static final String KEY_SHOW_FARMER = "show_farmer";
    public static final String KEY_SIZE = "size";
    public static final String KEY_ROLEBEAN = "rolebean";

    @Bind(R.id.link_select) LinkSelectView link_select;

    private List selectedList = new ArrayList();
    private HashMap<Role,List> dataMap = new HashMap<>();
    private Role currentRole;
    private boolean showFarmer;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_linkman_select;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showFarmer = getIntent().getBooleanExtra(KEY_SHOW_FARMER,true);
        link_select.showFarmer(showFarmer);
        adapter.setSelectedList(selectedList);
        currentRole = link_select.getCurrentRole();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.selectOrUnselectItem(position);
                if (selectedList.isEmpty()) {
                    getTitleView().setRightText("");
                }
                else {
                    getTitleView().setRightText("完成");
                }
            }
        });

        link_select.setOnItemClickListener(new LinkSelectView.OnItemClickListener() {
            @Override
            public void onItemClick(Role role) {
                currentRole = role;
                Logger.e(">> currentRole: " + role);

                loadData();
            }
        });

        getTitleView().setRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra(KEY_SIZE, selectedList.size());
                for (int i = 0;i < selectedList.size();i++) {
                    data.putExtra(KEY_ROLEBEAN + (i + 1), (RoleBean) selectedList.get(i));
                }
                setResult(RESULT_OK,data);
                finish();
            }
        });
        loadData();
    }

    private void loadData() {
        List<RoleBean> dataList = dataMap.get(currentRole);
        if (dataList == null) {
            loadFromServer();
        } else {
            adapter.setData(dataList);
        }
    }

    private void loadFromServer() {
        Params params = new Params();
        params.put("start","1");
        params.put("limit","1000");
        params.put("sign","search");

        if (currentRole == Role.yiji) {
            params.put("level", "1");
            new Req().url(Req.agentDeal)
                    .params(params).get(new DefaultCallback<List<Agent>>() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, List<Agent> response) {
                    dataMap.put(currentRole, response);
                    adapter.setData(response);
                }
            });
        }
        if (currentRole == Role.erji) {
            params.put("level","2");
            new Req().url(Req.agentDeal)
                    .params(params).get(new DefaultCallback<List<AgentLevel2>>() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, List<AgentLevel2> response) {
                    dataMap.put(currentRole,response);
                    adapter.setData(response);
                }
            });
        }
        if (currentRole == Role.farmer) {
            new Req().url(Req.customerDeal)
                    .params(params).post(new DefaultCallback<List<Farmer>>() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, List<Farmer> response) {
                    dataMap.put(currentRole,response);
                    adapter.setData(response);
                }
            });
        }

        if (currentRole.isSalesman()) {
            new Req().url(Req.salespersonDeal)
                    .addParam("sign", "search")
                    .addParam("start", "1")
                    .addParam("limit", "100")
                    .get(new DefaultCallback<List<Salesman>>() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, List<Salesman> response) {
                            dataMap.put(currentRole,response);
                            adapter.setData(response);
                        }
                    });
        }
    }
}
