package com.example.fertilizercrm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.JSONArrayListActivity;
import com.example.fertilizercrm.bean.OutsideSystemChannel;
import com.example.fertilizercrm.http.Req;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 系统外进货渠道选择
 */
public class OutsideSystemChannelActivity extends JSONArrayListActivity<OutsideSystemChannelActivity.ViewHolder> {
    public static final int REQUEST_CODE_DEFAULT = 1;

    @Override
    protected int getCellLayoutResId() {
        return R.layout.list_item_factory_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleView().setTitle("系统外进货渠道");
        ButterKnife.bind(this);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //{"body":[{"bsoid":3,"contacter":"联系人","createDate":"2016-02-20 20:20:04.0","creator":"testej",
// "memo":"","mobile":"18956786340","modifier":"testej","modifyDate":"2016-02-20 20:20:04.0","name":"他只是"
// ,"status":0,"userid":6,"usertype":2}],"code":200,"message":"null","total":1,"start":1,"limit":1000}

                JSONObject obj = adapter.getItem(position);
                OutsideSystemChannel channel = new OutsideSystemChannel();
                channel.setSelect(true);
                channel.setName(obj.optString("name"));
                channel.setContacter(obj.optString("contacter"));
                channel.setMobile(obj.optString("mobile"));
                channel.setBsoid(obj.optInt("bsoid"));
                Intent data = new Intent();
                data.putExtra(KEY_DATA,channel);
                setResult(RESULT_OK, data);
                finish();
            }
        });
        getTitleView()
                .setRightText("添加")
                .setRightClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivityForResult(new Intent(getActivity(),OutsideSystemChannelActivityAddActivity.class),REQUEST_CODE_DEFAULT);
                    }
                });
        loadData();
    }

    @Override
    protected void bindData(int position, ViewHolder holder, JSONObject obj) {
        holder.tv_name.setText(obj.optString("contacter"));
        holder.tv_mobile.setText(obj.optString("mobile"));
    }

    private void loadData() {
        new Req()
                .url(Req.purchaseDeal)
                .addParam("start", 1)
                .addParam("limit",1000)
                .addParam("sign", "outchannel")
                .get(new DefaultCallback<JSONArray>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        adapter.setData(response);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_DEFAULT
                && resultCode == RESULT_OK) {
            setResult(RESULT_OK,data);
            finish();
        }
    }

    public static final class ViewHolder {
        @Bind(R.id.tv_name)     TextView tv_name;
        @Bind(R.id.tv_mobile)   TextView tv_mobile;
    }
}
