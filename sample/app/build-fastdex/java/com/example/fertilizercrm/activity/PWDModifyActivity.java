package com.example.fertilizercrm.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;
import com.example.fertilizercrm.basic.DataManager;
import com.example.fertilizercrm.http.Req;
import org.apache.http.Header;
import org.json.JSONObject;

/**
 * 修改密码
 */
public class PWDModifyActivity extends BaseActivity {
    @Bind(R.id.et_old_pwd)    EditText et_old_pwd;
    @Bind(R.id.et_pwd)        EditText et_pwd;
    @Bind(R.id.et_pwd_repeat) EditText et_pwd_repeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_pwd);
        ButterKnife.bind(this);

//        et_old_pwd.setText("111111");
//        et_pwd.setText("222222");
//        et_pwd_repeat.setText("222222");
    }

    @OnClick(R.id.btn_submit) void clickSubmit() {
        String oldPwd = et_old_pwd.getText().toString().trim();
        String pwd = et_pwd.getText().toString().trim();
        String repeat = et_pwd_repeat.getText().toString().trim();
        if (TextUtils.isEmpty(oldPwd)) {
            showLongToast("请输入当前密码");
            return;
        }

        if (TextUtils.isEmpty(pwd)) {
            showLongToast("请输入新密码");
            return;
        }

        if (!pwd.equals(repeat)) {
            showLongToast("两次输入不一致");
            return;
        }
        new Req().url(Req.editpw)
                .addParam("username", DataManager.getInstance().getLoginResponse().getToken())
                .addParam("password", oldPwd)
                .addParam("newpassword", pwd)
                .post(new DefaultCallback<JSONObject>() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        showLongToast("修改成功");
                        setResult(RESULT_OK);
                        finish();
                    }
                });
    }
}
