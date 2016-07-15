package com.example.fertilizercrm.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

import com.example.fertilizercrm.common.utils.ImageCropHelper;
import com.example.fertilizercrm.common.utils.Logger;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;
import com.example.fertilizercrm.bean.Salesman;
import com.example.fertilizercrm.http.Params;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.http.URLText;
import com.example.fertilizercrm.utils.FerUtil;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.File;

/**
 * 业务员添加
 */
public class SalesmanSaveOrEditActivity extends BaseActivity {
    /**
     * 修改文本信息请求码
     */
    private static final int REQUEST_CODE_TEXT_MODIFY = 1;
    /**
     * 选择地区请求码
     */
    private static final int REQUEST_CODE_AREA = 2;

    public static final int OPEN_TYPE_EDIT = 1;
    public static final String KEY_SALESMAN = "salesman";

    @Bind(R.id.iv_head)    ImageView iv_head;
    @Bind(R.id.tv_name)    TextView tv_name;
    @Bind(R.id.tv_mobile)  TextView tv_mobile;
    @Bind(R.id.tv_area1)   TextView tv_area1;
    @Bind(R.id.tv_area2)   TextView tv_area2;
    @Bind(R.id.tv_area3)   TextView tv_area3;
    private Salesman salesman;
    private ImageCropHelper imageCropHelper;
    private boolean isModify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salesman_add);
        ButterKnife.bind(this);

        salesman = (Salesman) getIntent().getSerializableExtra(KEY_SALESMAN);
        initView();

        if (openType == OPEN_TYPE_DEFAULT) {
            getTitleView().setRightText("保存");
        }
        else if (openType == OPEN_TYPE_EDIT) {
            if (!TextUtils.isEmpty(salesman.getHicon())) {
                Picasso.with(getActivity()).load(URLText.imgUrl + salesman.getHicon()).into(iv_head);
            }
            tv_name.setText(salesman.getSname());
            tv_mobile.setText(salesman.getPhone());
            tv_area1.setText(salesman.getCoverrange1());
            tv_area2.setText(salesman.getCoverrange2());
            tv_area3.setText(salesman.getCoverrange3());
            iv_head.setTag(salesman.getHicon());
            getTitleView().setTitle("修改业务员").setRightText("修改");
        }

        getTitleView().setRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveOrUpdateSalesman();
            }
        });

        imageCropHelper = new ImageCropHelper(this,iv_head){
            @Override
            public void start() {
                new AlertView("上传头像", null, "取消", null,
                        new String[]{"拍照", "从相册中选择"},
                        getContext(), AlertView.Style.ActionSheet, new OnItemClickListener() {
                    @Override
                    public void onItemClick(Object o, int position) {
                        if (position == 0) {
                            imageCropHelper.takePicture();
                        }
                        else if (position == 1) {
                            imageCropHelper.openAlbum();
                        }
                    }
                }).show();
            }
        };
        imageCropHelper.setOnCropListener(new ImageCropHelper.OnCropListener() {
            @Override
            public void onGetImage(final File imgFile, Bitmap bitmap) {
//                new Req().url(Req.uploadHicon)
//                        .addParam("sign","update")
//                        .addParam("picture",imgFile)
//                        .enableLog(false)
//                        .post(new DefaultCallback<JSONObject>() {
//                            @Override
//                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//
//                            }
//                        });
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

    private void onItemClick(int position) {
        Logger.d(">>position: " + position);

        if (position == 0) {
            //头像
            imageCropHelper.start();
        }
        else  if (position == 1) {
            //姓名
            startActivityForResult(TextModifyActivity.createIntent(this,"姓名",tv_name),REQUEST_CODE_TEXT_MODIFY);
        }
        else  if (position == 2) {
            //手机
            startActivityForResult(TextModifyActivity.createIntent(this,"手机",tv_mobile),REQUEST_CODE_TEXT_MODIFY);
        }
        else  if (position == 3) {
            //负责地区1
            startActivityForResult(AreaActivity.createIntent(this,"负责地区1",tv_area1),REQUEST_CODE_AREA);
        }
        else  if (position == 4) {
            //负责地区2
            startActivityForResult(AreaActivity.createIntent(this,"负责地区2",tv_area2),REQUEST_CODE_AREA);
        }
        else  if (position == 5) {
            //负责地区3
            startActivityForResult(AreaActivity.createIntent(this,"负责地区3",tv_area3),REQUEST_CODE_AREA);
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
                            saveOrUpdateSalesman();
                        }
                    }).show();
            return true;
        }
        return super.onBack();
    }

    private void saveOrUpdateSalesman() {
        String name = tv_name.getText().toString().trim();
        String mobile = tv_mobile.getText().toString().trim();
        String hicon = (String) iv_head.getTag();

        if (TextUtils.isEmpty(name)) {
            showLongToast("请输入业务员姓名");
            return;
        }

        if (TextUtils.isEmpty(mobile)) {
            showLongToast("请输入联系电话");
            return;
        }

        try {
            FerUtil.checkMobile(mobile);
        } catch (Throwable e) {
            showLongToast("请输入正确的手机号");
            return;
        }

        String area1 = tv_area1.getText().toString().trim();
        String area2 = tv_area2.getText().toString().trim();
        String area3 = tv_area3.getText().toString().trim();

        Params params = new Params();
        if (!TextUtils.isEmpty(hicon)) {
            params.put("hicon",hicon);
        }
        params.put("sname", name);
        params.put("phone",mobile);
        params.put("coverrange1",area1);
        params.put("coverrange2",area2);
        params.put("coverrange3",area3);

        if (openType == OPEN_TYPE_DEFAULT) {
            params.put("sign","save");
            new Req().url(Req.salespersonDeal)
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
            params.put("bsoid",salesman.getBsoid());
            new Req().url(Req.salespersonDeal)
                    .params(params)
                    .post(new DefaultCallback<JSONObject>() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            showLongToast("修改成功!");
                            setResult(RESULT_OK);
                            finish();
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageCropHelper.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_TEXT_MODIFY
                && resultCode == RESULT_OK) {
            isModify = true;
        }
        if (requestCode == REQUEST_CODE_AREA
                && resultCode == RESULT_OK) {
            isModify = true;
        }
    }
}
