package com.example.fertilizercrm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;
import com.example.fertilizercrm.common.utils.Logger;

import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;
import com.example.fertilizercrm.http.Req;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 常用地址选择
 */
public class CommonAddressListActivity extends BaseActivity {
    /**
     * 地址数据key
     */
    public static final String DATA_ADDRESS = "address";

    @Bind(R.id.lv) ListView lv;
    List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_address_list);
        ButterKnife.bind(this);

        loadData();
    }

    private void loadData() {
        new Req().url(Req.commonaddDeal)
                .addParam("sign","search")
                .addParam("start","1")
                .addParam("limit","100")
                .get(new DefaultCallback<JSONArray>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        list.clear();
                        for (int i = 0;i < response.length();i++) {
                            final JSONObject obj = response.optJSONObject(i);
                            list.add(new HashMap<String, Object>(){{
                                put("address",obj.optString("address"));
                                put("bsoid",obj.optString("bsoid"));
                            }});
                        }
                        String[] from = new String[]{"address"};
                        int[] to = new int[]{R.id.tv};
                        SimpleAdapter adapter = new SimpleAdapter(getActivity(),list,R.layout.list_item_area,from,to);
                        lv.setAdapter(adapter);
                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Map<String,Object> map = list.get(position);
                                Intent data = new Intent();
                                String address = (String)map.get("address");
                                Logger.d("<< address: " + address);
                                data.putExtra(DATA_ADDRESS,address);
                                setResult(RESULT_OK,data);
                                finish();
                            }
                        });
                    }
                });
    }
}
