package com.example.fertilizercrm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.JSONArrayListActivity;
import com.example.fertilizercrm.bean.Agent;
import com.example.fertilizercrm.bean.AgentLevel2;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.http.URLText;
import com.google.gson.Gson;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;

import butterknife.Bind;
import com.example.fertilizercrm.common.utils.Logger;

/**
 * 代理商列表
 */
public class AgentListActivity extends JSONArrayListActivity<AgentListActivity.ViewHolder> {
    public static final String DATA_AGENT = "agent";
    /**
     * 是否是批发商
     */
    public static final String KEY_IS_LEVEL1 = "is_level1";

    private boolean isLevel1Agent = true;
    private Gson gson = new Gson();

    @Override
    protected int getCellLayoutResId() {
        return R.layout.list_item_agent_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isLevel1Agent = getIntent().getBooleanExtra(KEY_IS_LEVEL1,true);

        getTitleView().setTitle("代理商");
        loadData();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject obj = adapter.getItem(position);
                Serializable object = (Serializable) gson.fromJson(obj.toString(), isLevel1Agent ? Agent.class : AgentLevel2.class);
                Intent data = new Intent();
                data.putExtra(DATA_AGENT, object);

                Logger.d("<< data: " + data);
                setResult(RESULT_OK, data);
                finish();
            }
        });
        if (isLevel1Agent) {
            getTitleView().setTitle("批发商");
        } else {
            getTitleView().setTitle("零售商");
        }
    }

    @Override
    protected void bindData(int position, ViewHolder holder, JSONObject obj) {
        holder.tv_name.setText(obj.optString("aname"));
        holder.tv_mobile.setText(obj.optString("mobile"));
    }

    private void loadData() {
        if (isLevel1Agent) {
            new Req().url(URLText.agentDeal)
                    .addParam("sign", "search")
                    .addParam("start","1")
                    .addParam("limit", "10000")
                    .addParam("level", "1")
                    .get(new DefaultCallback<JSONArray>() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers,JSONArray response) {
                            adapter.setData(response);
                        }
                    });
        }
        else {
            new Req().url(URLText.agentDeal)
                    .addParam("sign", "search")
                    .addParam("start","1")
                    .addParam("limit", "10000")
                    .addParam("level", "2")
                    .get(new DefaultCallback<JSONArray>() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            adapter.setData(response);
                        }
                    });
        }
    }

    public static final class ViewHolder {
        @Bind(R.id.tv_name)     TextView tv_name;
        @Bind(R.id.tv_mobile)   TextView tv_mobile;
    }
}
