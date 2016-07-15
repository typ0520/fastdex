package com.example.fertilizercrm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.example.fertilizercrm.common.utils.Logger;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;
import com.example.fertilizercrm.bean.RoleBean;
import com.example.fertilizercrm.http.Params;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.http.URLText;
import com.example.fertilizercrm.view.MessageTypeSwitchView;
import org.apache.http.Header;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息推广
 */
public class MessageExpandActivity extends BaseActivity {
    /**
     * 站内消息
     */
    private static final int TYPE_STATION = 0;
    /**
     * 手机短信
     */
    private static final int TYPE_MOBILE_MESSAGE = 1;

    private static final int REQUEST_CODE_LINKMAN_SELECT = 1;

    @Bind(R.id.message_type)         MessageTypeSwitchView message_type;
    @Bind(R.id.et)                   EditText et;
    @Bind(R.id.hslv)                 View hslv;
    @Bind(R.id.ll_linkman_container) LinearLayout ll_linkman_container;
    private List<RoleBean> roleBeans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_expand);
        ButterKnife.bind(this);

        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 60) {
                    et.setText(s.subSequence(0,59));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @OnClick(R.id.ib_plus) void clickSendLink() {
        Intent intent = new Intent(this,LinkmanSelectActivity.class);
        if (message_type.getCurrentIndex() == TYPE_STATION) {
            intent.putExtra(LinkmanSelectActivity.KEY_SHOW_FARMER,false);
        } else {
            intent.putExtra(LinkmanSelectActivity.KEY_SHOW_FARMER,true);
        }
        startActivityForResult(intent, REQUEST_CODE_LINKMAN_SELECT);
    }

    @OnClick(R.id.btn_submit) void clickSubmit() {
        if (roleBeans == null || roleBeans.isEmpty()) {
            showLongToast("请添加联系人");
            return;
        }
        String content = et.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            showLongToast("请输入内容");
            return;
        }
        Params params = new Params();
        params.put("pushcount",roleBeans.size());
        params.put("msgcontent",content);

        for (int i = 0;i < roleBeans.size();i++) {
            params.put("guserid" + (i + 1), roleBeans.get(i).getBsoid());
            params.put("gusertype" + (i + 1),roleBeans.get(i).getRoleTypeStr());
        }

        DefaultCallback<JSONObject> callback = new DefaultCallback<JSONObject>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                showLongToast("恭喜您,发送成功!");
                finish();
            }
        };
        if (message_type.getCurrentIndex() == TYPE_STATION) {
            params.put("sign", "pushmessage");
        }
        else if (message_type.getCurrentIndex() == TYPE_MOBILE_MESSAGE) {
            params.put("sign", "sendmessage");
        }
        new Req()
                .url(URLText.pushMessageDeal)
                .params(params)
                .get(callback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_LINKMAN_SELECT
                && resultCode == RESULT_OK) {
            int size = data.getIntExtra(LinkmanSelectActivity.KEY_SIZE,0);
            roleBeans = new ArrayList<>();
            for (int i = 0;i < size;i++) {
                RoleBean roleBean = (RoleBean)data.getSerializableExtra(LinkmanSelectActivity.KEY_ROLEBEAN + (i + 1));
                roleBeans.add(roleBean);
                Logger.e("<< rolebean: " + roleBean);
            }
            addLinkmanView();
        }
    }

    private void addLinkmanView() {
        if (roleBeans == null) {
            return;
        }
        ll_linkman_container.removeAllViews();
        for (int i = 0;i < roleBeans.size();i++) {
            RoleBean roleBean = roleBeans.get(i);
            View view = View.inflate(this, R.layout.view_linkman_item, null);
            TextView tv = (TextView) view.findViewById(R.id.tv);
            tv.setText(roleBean.getContacter());
            ll_linkman_container.addView(view);
        }
        hslv.setVisibility(View.VISIBLE);
    }
}
