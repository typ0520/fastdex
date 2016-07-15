package com.example.fertilizercrm.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.example.fertilizercrm.common.utils.JsonUtils;
import com.example.fertilizercrm.common.utils.Logger;
import com.example.fertilizercrm.common.utils.SharedPreferenceUtil;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;
import com.example.fertilizercrm.basic.DataManager;
import com.example.fertilizercrm.bean.LoginResponse;
import com.example.fertilizercrm.http.BizError;
import com.example.fertilizercrm.http.Callback;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.http.URLText;
import com.example.fertilizercrm.view.QuantityPriceEditDialog;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 厂家资料
 */
public class FactoryProfileActivity extends BaseActivity {
    /**
     * 修改文本信息请求码
     */
    private static final int REQUEST_CODE_TEXT_MODIFY = 1;

    @Bind(R.id.tv_ptype) TextView tv_ptype;
    @Bind(R.id.tv_tech) TextView tv_tech;
    @Bind(R.id.tv_capacity) TextView tv_capacity;
    @Bind(R.id.tv_output) TextView tv_output;
    private int capacity;
    private int output;
    private boolean isModify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factory_profile);
        ButterKnife.bind(this);

        getTitleView().setRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isModify) {
                    return;
                }
                FactoryProfileActivity.this.modifyProfile();
            }
        });

        JSONObject factoryInfo = getFactoryInfo();
        if (factoryInfo != null) {
            //如果有厂家信息，展示信息
            tv_ptype.setText(factoryInfo.optString("ptype"));
            tv_tech.setText(factoryInfo.optString("tech"));

            capacity = factoryInfo.optInt("capacity");
            output = factoryInfo.optInt("output");

            tv_capacity.setText(capacity + "吨");
            tv_output.setText(output + "吨");
        }
    }

    //获取厂家信息
    private JSONObject getFactoryInfo() {
        try {
            return loginResponse().getUserinfo().optJSONArray("supplierspetinfo").optJSONObject(0);
        } catch (Throwable e) {

        }
        return null;
    }

    @OnClick(R.id.rl_ptype) void rl_ptype() {
        //生产类型
        startActivityForResult(TextModifyActivity.createIntent(this, "生产类型", tv_ptype), REQUEST_CODE_TEXT_MODIFY);
    }

    @OnClick(R.id.rl_tech) void rl_tech() {
        //工艺
        startActivityForResult(TextModifyActivity.createIntent(this,"工艺",tv_tech),REQUEST_CODE_TEXT_MODIFY);
    }

    @OnClick(R.id.rl_capacity) void rl_capacity() {
        //(产能-万吨)

        final int originStock = capacity;
        //修改库存
        QuantityPriceEditDialog quantityPriceEditDialog = new QuantityPriceEditDialog(this);
        quantityPriceEditDialog.setMode(QuantityPriceEditDialog.MODE_QUANTITY);
        quantityPriceEditDialog.setProductName("产能");
        quantityPriceEditDialog.setQuantit(originStock);
        quantityPriceEditDialog.setSendOutEventListener(new QuantityPriceEditDialog.SendOutEventListener() {
            @Override
            public boolean onDetermine(String quantit, String price) {
                Logger.d(">> quantit: " + quantit);
                int stock = Integer.valueOf(quantit);
                if (originStock != stock) {
                    capacity = stock;
                    tv_capacity.setText(stock + "吨");

                    getTitleView().setRightText("保存");
                    isModify = true;
                }
                return false;
            }
        });
        //设置初始化数据
        quantityPriceEditDialog.show();
    }

    @OnClick(R.id.rl_output) void rl_output() {
        //(产能-万吨)
        final int originStock = output;
        //修改库存
        QuantityPriceEditDialog quantityPriceEditDialog = new QuantityPriceEditDialog(this);
        quantityPriceEditDialog.setMode(QuantityPriceEditDialog.MODE_QUANTITY);
        quantityPriceEditDialog.setProductName("产能");
        quantityPriceEditDialog.setQuantit(originStock);
        quantityPriceEditDialog.setSendOutEventListener(new QuantityPriceEditDialog.SendOutEventListener() {
            @Override
            public boolean onDetermine(String quantit, String price) {
                Logger.d(">> quantit: " + quantit);
                int stock = Integer.valueOf(quantit);
                if (originStock != stock) {
                    output = stock;
                    tv_output.setText(stock + "吨");

                    getTitleView().setRightText("保存");
                    isModify = true;
                }
                return false;
            }
        });
        //设置初始化数据
        quantityPriceEditDialog.show();
    }

    @Override
    protected boolean onBack() {
        if (isModify) {
            new AlertDialog.Builder(this)
                    .setTitle("资料未保存")
                    .setMessage("是否退出?")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("保存资料", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            modifyProfile();
                        }
                    }).show();
            return true;
        }
        return super.onBack();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_TEXT_MODIFY
                && resultCode == RESULT_OK) {
            FactoryProfileActivity.this.isModify = true;
            //修改完成
            getTitleView().setRightText("保存");
        }
    }

    private void modifyProfile() {
        final String ptype = tv_ptype.getText().toString();
        final String tech = tv_tech.getText().toString();

        if (TextUtils.isEmpty(ptype)) {
            showLongToast("请输入生产类型");
            return;
        }
        if (TextUtils.isEmpty(tech)) {
            showLongToast("请输入工艺");
            return;
        }
        if (getFactoryInfo() == null) {
            saveFactoryInfo(ptype,tech);
        }
        else {
            updateFactoryInfo(ptype,tech);
        }
    }

    //更新厂家信息
    private void updateFactoryInfo(final String ptype, final String tech) {
        new Req()
                .url(Req.suppliersDeal)
                .addParam("bsoid", getFactoryInfo().optInt("bsoid"))
                .addParam("sign", "updatepetinfo")
                .addParam("ptype",ptype)
                .addParam("tech",tech)
                .addParam("capacity",capacity)
                .addParam("output", output)
                .get(new DefaultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        showLongToast("恭喜您，操作成功!");
                        JSONObject obj = getFactoryInfo();
                        try {
                            obj.put("ptype", ptype);
                            obj.put("tech", tech);
                            obj.put("capacity", capacity);
                            obj.put("output", output);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        setResult(RESULT_OK);
                        finish();
                    }
                });
    }

    //保存厂家信息
    private void saveFactoryInfo(String ptype, String tech) {
        new Req()
                .url(Req.suppliersDeal)
                .addParam("sign", "savepetinfo")
                .addParam("ptype",ptype)
                .addParam("tech",tech)
                .addParam("capacity",capacity)
                .addParam("output",output)
                .get(new Callback<JSONObject>() {
                    @Override
                    public void onStart() {
                        showProgress();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        reloadUserInfo();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);

                        hideProgress();
                    }

                    @Override
                    public void onError(int statusCode, Header[] headers, BizError error) {
                        super.onError(statusCode, headers, error);

                        hideProgress();
                    }
                });
    }

    //重新加载用户信息
    private void reloadUserInfo() {
        String username = SharedPreferenceUtil.getStringValueFromSP(getPackageName(), LoginActivity.KEY_USERNAME);
        String pwd = SharedPreferenceUtil.getStringValueFromSP(getPackageName(), LoginActivity.KEY_PWD);

        new Req().url(URLText.login)
                .addParam("username", username)
                .addParam("password", pwd)
                .get(new DefaultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject obj) {
                        LoginResponse response = JsonUtils.jsonToObject(obj.toString(), LoginResponse.class);
                        response.setUserinfo(obj.optJSONObject("message"));
                        Logger.d("login response: " + response.toString());

                        DataManager.getInstance().setLoginResponse(response);
                        showLongToast("恭喜您，操作成功!");
                        setResult(RESULT_OK);
                        finish();
                    }
                });
    }
}
