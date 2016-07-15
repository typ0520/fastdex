package com.example.fertilizercrm.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import com.example.fertilizercrm.common.utils.Logger;

import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;
import com.example.fertilizercrm.bean.Agent;
import com.example.fertilizercrm.bean.AgentLevel2;
import com.example.fertilizercrm.bean.AreaInfo;
import com.example.fertilizercrm.bean.RoleBean;
import com.example.fertilizercrm.http.Params;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.utils.FerUtil;

import org.apache.http.Header;
import org.json.JSONObject;

/**
 * 代理商添加、更新
 */
public class AgentActivity extends BaseActivity {
    /**
     * 是否是批发商
     */
    public static final String KEY_IS_LEVEL1 = "is_level1";

    /**
     * 修改文本信息请求码
     */
    private static final int REQUEST_CODE_TEXT_MODIFY = 1;
    /**
     * 选择地区请求码
     */
    private static final int REQUEST_CODE_AREA = 2;

    @Bind(R.id.tv_name) TextView tv_name;
    @Bind(R.id.tv_mobile)  TextView tv_mobile;
    @Bind(R.id.tv_contacter)  TextView tv_contacter;
    @Bind(R.id.tv_level)  TextView tv_level;
    @Bind(R.id.tv_area1)   TextView tv_area1;
    @Bind(R.id.tv_area2)   TextView tv_area2;
    @Bind(R.id.tv_area3)   TextView tv_area3;
    @Bind(R.id.view_area3) View view_area3;
    @Bind(R.id.ll_area3)   View ll_area3;

    private RoleBean roleBean;

    private boolean isModify;
    private boolean isLevel1Agent = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLevel1Agent = getIntent().getBooleanExtra(KEY_IS_LEVEL1,true);
        roleBean = (RoleBean) getIntent().getSerializableExtra(KEY_DATA);
        setContentView(R.layout.activity_agent);
        ButterKnife.bind(this);

        if (isLevel1Agent) {
            tv_level.setText("批发商");
        }
        else {
            tv_level.setText("零售商");
        }

        initView();

        if (openType == OPEN_TYPE_DEFAULT) {
            getTitleView().setRightText("保存");
        }
        else if (openType == OPEN_TYPE_EDIT) {
            if (isLevel1Agent) {
                Agent agent = (Agent) roleBean;
                tv_name.setText(agent.getAname());
                tv_mobile.setText(agent.getMobile());
                tv_contacter.setText(agent.getContacter());
                tv_area1.setText(agent.getProvince());
                tv_area2.setText(agent.getCity());
                tv_area3.setText(agent.getDistrict());
            }
            else {
                AgentLevel2 agent = (AgentLevel2) roleBean;
                tv_name.setText(agent.getAname());
                tv_mobile.setText(agent.getMobile());
                tv_contacter.setText(agent.getContacter());
                tv_area1.setText(agent.getProvince());
                tv_area2.setText(agent.getCity());
                tv_area3.setText(agent.getDistrict());
            }
            getTitleView().setTitle("修改代理商").setRightText("修改");
        }

        getTitleView().setRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveOrUpdate();
            }
        });
    }

    private void initView() {
        ViewGroup ll_container = (ViewGroup) findViewById(R.id.ll_container);
        int idx = 0;
        for (int i = 0;i < ll_container.getChildCount();i++) {
            final View item = ll_container.getChildAt(i);
            if (item instanceof RelativeLayout) {
                final int position = idx;

                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClick(position);
                    }
                });
                idx ++;
            }
        }
    }

    private void saveOrUpdate() {
        String name = tv_name.getText().toString().trim();
        String mobile = tv_mobile.getText().toString().trim();
        String contacter = tv_contacter.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            showLongToast("请输入代理商姓名");
            return;
        }

        try {
            FerUtil.checkMobile(mobile);
        } catch (Throwable e) {
            showLongToast(e.getMessage());
            return;
        }

        if (TextUtils.isEmpty(contacter)) {
            showLongToast("请输入联系人姓名");
            return;
        }

        if (TextUtils.isEmpty(tv_area1.getText().toString().trim())) {
            showLongToast("请输入省市区信息");
            return;
        }

        String area1 = tv_area1.getText().toString().trim();
        String area2 = tv_area2.getText().toString().trim();
        String area3 = tv_area3.getText().toString().trim();

        Params params = new Params();
        params.put("aname", name);
        params.put("level",isLevel1Agent ? "1" : "2");
        params.put("contacter",contacter);
        params.put("mobile",mobile);
        params.put("province",area1);
        params.put("city",area2);
        params.put("district",area3);

        if (openType == OPEN_TYPE_DEFAULT) {
            params.put("sign","save");
            new Req().url(Req.agentDeal)
                    .params(params)
                    .post(new DefaultCallback<JSONObject>() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            showLongToast("添加成功!");
                            setResult(RESULT_OK);
                            finish();
                        }
                    });
        }
        else if (openType == OPEN_TYPE_EDIT) {
            params.put("sign","update");
            params.put("bsoid",roleBean.getBsoid());
            new Req().url(Req.agentDeal)
                    .params(params)
                    .get(new DefaultCallback<JSONObject>() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            showLongToast("修改成功!");
                            setResult(RESULT_OK);
                            finish();
                        }
                    });
        }
    }

    private void onItemClick(int position) {
        Logger.d(">>position: " + position);

        if (position == 0) {
//            this.isLevel1Agent = !this.isLevel1Agent;
//            if (isLevel1Agent) {
//                tv_level.setText("批发商");
//            }
//            else {
//                tv_level.setText("零售商");
//            }
        }
        else if (position == 1) {
            //姓名
            startActivityForResult(TextModifyActivity.createIntent(this,"代理商姓名",tv_name),REQUEST_CODE_TEXT_MODIFY);
        }
        else  if (position == 2) {
            //手机
            startActivityForResult(TextModifyActivity.createIntent(this,"手机号",tv_mobile),REQUEST_CODE_TEXT_MODIFY);
        }
        else  if (position == 3) {
            //联系人名称
            startActivityForResult(TextModifyActivity.createIntent(this, "联系人名称", tv_contacter),REQUEST_CODE_TEXT_MODIFY);
        }
        else  if (position == 4) {
            //所在省
            startActivityForResult(AreaActivity.createIntent(this,"省市区",tv_area1),REQUEST_CODE_AREA);
        }
        else  if (position == 5) {
            //所在市
            startActivityForResult(AreaActivity.createIntent(this,"省市区",tv_area2),REQUEST_CODE_AREA);
        }
        else  if (position == 6) {
            //所在区
            startActivityForResult(AreaActivity.createIntent(this,"省市区",tv_area3),REQUEST_CODE_AREA);
        }
    }

    @Override
    protected boolean onBack() {
        if (isModify) {
            new AlertDialog.Builder(this)
                    .setTitle("信息未保存")
                    .setMessage("是否退出?")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("保存信息", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveOrUpdate();
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
            isModify = true;
            Logger.d(">> isModify: " + isModify);
        }
        if (requestCode == REQUEST_CODE_AREA
                && resultCode == RESULT_OK) {
            AreaInfo areaInfo = (AreaInfo) data.getSerializableExtra(AreaActivity.KEY_AREA_INFO);
            tv_area1.setText(areaInfo.getProvinceName());
            tv_area2.setText(areaInfo.getCityName());
            if (TextUtils.isEmpty(areaInfo.getAreaName())) {
                view_area3.setVisibility(View.GONE);
                ll_area3.setVisibility(View.GONE);
                tv_area3.setText("");
            }
            else {
                view_area3.setVisibility(View.VISIBLE);
                ll_area3.setVisibility(View.VISIBLE);
                tv_area3.setText(areaInfo.getAreaName());
            }
            isModify = true;
            Logger.d(">> isModify: " + isModify);
        }
    }
}
