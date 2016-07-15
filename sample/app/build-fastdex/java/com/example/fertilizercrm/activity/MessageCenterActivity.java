package com.example.fertilizercrm.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import butterknife.Bind;
import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.JSONArrayListActivity;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.role.Role;
import com.example.fertilizercrm.utils.FerUtil;
import com.example.fertilizercrm.view.MessageTypeSwitchView;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 消息中心
 */
public class MessageCenterActivity extends JSONArrayListActivity<MessageCenterActivity.ViewHolder> {
    /**
     * 站内信
     */
    private static final int TYPE_STATION = 0;
    /**
     * 手机短信
     */
    private static final int TYPE_MOBILE_MESSAGE = 1;

    @Bind(R.id.message_type) MessageTypeSwitchView message_type;
    private int types;//0收到的，1发出的--默认0

    @Override
    protected int getCellLayoutResId() {
        return R.layout.list_item_message_center;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_message_center;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String[] strings = new String[]{"收到的消息", "发出的消息"};
        if (Role.currentRole().isFactory()) {
            types = 1;
            getTitleView().setRightText(strings[types]).rightEnable(false);
        }
        else {
            getTitleView().setRightText(strings[types]).setRightClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    types = types == 0 ? 1 : 0;
                    getTitleView().setRightText(strings[types]);
                    loadData();
                }
            });
        }

        message_type.setOnItemClickListener(new MessageTypeSwitchView.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                loadData();
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject obj = adapter.getItem(position);
                String gname = obj.optString("gname");
                String pname = obj.optString("pname");

                String content = "";
                if (types == 1) {
                    content = "\n收信人: " + gname + ";\n  " + obj.optString("content");
                }
                else {
                    content = "\n发信人: " + pname + ";\n  " + obj.optString("content");
                }

                alertView = new AlertView(null, content, null, new String[]{"确定"}, null, getActivity(),
                        AlertView.Style.Alert, null);
                alertView.show();
            }
        });
        loadData();
    }

    private void loadData() {
        if (message_type.getCurrentIndex() == TYPE_STATION) {
            new Req()
                    .url(Req.pushMessageDeal)
                    .addParam("sign", "searchpush")
                    .addParam("start","1")
                    .addParam("limit","1000")
                    .addParam("types",types)
                    .get(new DefaultCallback<JSONArray>() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            adapter.setData(response);
                        }
                    });
        }
        else if (message_type.getCurrentIndex() == TYPE_MOBILE_MESSAGE) {
            new Req()
                    .url(Req.pushMessageDeal)
                    .addParam("sign", "searchsend")
                    .addParam("start","1")
                    .addParam("limit","1000")
                    .addParam("types",types)
                    .get(new DefaultCallback<JSONArray>() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            adapter.setData(response);
                        }
                    });
        }
    }

    @Override
    protected void bindData(int position, ViewHolder holder, JSONObject obj) {
        String gname = obj.optString("gname");
        String pname = obj.optString("pname");
        String createDate = FerUtil.formatCreateDate(obj.optString("createDate"));
        if (createDate.length() >= 5) {
            createDate = createDate.substring(5,createDate.length() - 1);
        }
        holder.tv_date.setText(createDate);
        if (types == 1) {
            holder.tv_content.setText("收信人: " + gname + ";  " + obj.optString("content"));
        }
        else {
            holder.tv_content.setText("发信人: " + pname + ";  " + obj.optString("content"));
        }
        holder.view_bottom.setVisibility(position == adapter.getCount() - 1 ? View.GONE : View.VISIBLE);
    }

    public static class ViewHolder {
        @Bind(R.id.tv_date)
        TextView tv_date;
        @Bind(R.id.view_bottom)
        View view_bottom;
        @Bind(R.id.tv_content)
        TextView tv_content;
    }
}
