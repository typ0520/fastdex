package com.example.fertilizercrm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;
import com.example.fertilizercrm.bean.OutsideSystemChannel;
import com.example.fertilizercrm.utils.FerUtil;

/**
 * 系统外进货渠道添加
 */
public class OutsideSystemChannelActivityAddActivity extends BaseActivity {
    @Bind(R.id.et_name)
    EditText et_name;

    @Bind(R.id.et_contacter)
    EditText et_contacter;

    @Bind(R.id.et_mobile)
    EditText et_mobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outside_system_channel_activity_add);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_submit) void submit() {
        String name = et_name.getText().toString();
        String contacter = et_contacter.getText().toString();
        String mobile = et_mobile.getText().toString();

        if (TextUtils.isEmpty(name)) {
            showLongToast("请输入姓名");
            return;
        }
        if (TextUtils.isEmpty(contacter)) {
            showLongToast("请输入联系人");
            return;
        }
        if (TextUtils.isEmpty(mobile)) {
            showLongToast("请输入手机号");
            return;
        }
        try {
            FerUtil.checkMobile(mobile);
        } catch (Throwable e) {
            showLongToast(e.getMessage());
            return;
        }

        OutsideSystemChannel channel = new OutsideSystemChannel();
        channel.setSelect(false);
        channel.setName(name);
        channel.setContacter(contacter);
        channel.setMobile(mobile);

        Intent data = new Intent();
        data.putExtra(KEY_DATA,channel);
        setResult(RESULT_OK,data);
        finish();
    }
}
