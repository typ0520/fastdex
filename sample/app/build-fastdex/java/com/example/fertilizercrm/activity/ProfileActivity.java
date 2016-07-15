package com.example.fertilizercrm.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.example.fertilizercrm.easemob.applib.controller.HXSDKHelper;
import com.example.fertilizercrm.easemob.chatuidemo.DemoHXSDKHelper;
import com.example.fertilizercrm.FertilizerApplication;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;
import com.example.fertilizercrm.basic.DataManager;
import com.example.fertilizercrm.bean.AreaInfo;
import com.example.fertilizercrm.bean.LoginResponse;
import com.example.fertilizercrm.http.Callback;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.role.Role;
import com.example.fertilizercrm.utils.FerUtil;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import com.example.fertilizercrm.common.utils.ImageCropHelper;
import com.example.fertilizercrm.common.utils.Logger;

/**
 * 个人资料
 */
public class ProfileActivity extends BaseActivity {
    /**
     * 修改密码请求码
     */
    public static final int REQUEST_CODE_MODIFY_PWD = 1;

    /**
     * 修改文本信息请求码
     */
    private static final int REQUEST_CODE_TEXT_MODIFY = 2;

    /**
     * 选择地区请求码
     */
    private static final int REQUEST_CODE_AREA = 3;

    /**
     * 退出登录的result_code
     */
    public static final int RESULT_CODE_GO_TO_LOGIN = 1;

    /**
     * 厂家信息请求码
     */
    public static final int REQUEST_CODE_FACTORY_NFO = 4;

    /**
     * 修改头像
     */
    private static final String ACTION_HEAD = "head";
    /**
     * 修改密码
     */
    private static final String ACTION_MODIFY_PWD = "repwd";
    /**
     * 退出登录
     */
    private static final String ACTION_LOGOUT = "logout";
    /**
     * 姓名(厂家名称、代理商名称、业务员姓名)
     */
    private static final String ACTION_NAME = "name";
    /**
     * 手机号(手机号、联系电话)
     */
    private static final String ACTION_MOBILE = "mobile";
    /**
     * 联系人姓名
     */
    private static final String ACTION_CONTACTER = "contacter";
    /**
     * 所在地区
     */
    private static final String ACTION_ADDRESS = "address";
    /**
     * 厂家信息
     */
    private static final String ACTION_FACTORY_INFO = "factory_info";

    /**
     * 更新了头像的事件
     */
    public static final String EVENT_GET_NEW_HICON = "get_new_hicon";

    @Bind(R.id.ll_container) ViewGroup ll_container;
    @Bind(R.id.iv_head) ImageView iv_head;
    @Bind(R.id.tv_name) TextView tv_name;
    @Bind(R.id.tv_mobile) TextView tv_mobile;
    @Bind(R.id.tv_factory_info_label) TextView tv_factory_info_label;

    @Nullable
    @Bind(R.id.tv_contacter)
    TextView tv_contacter;
    @Nullable
    @Bind(R.id.tv_address)
    TextView tv_address;

    private ImageCropHelper imageCropHelper;
    private boolean isModify;
    private AreaInfo areaInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(
                Role.currentRole().isSalesman() ?
                        R.layout.activity_profile : R.layout.activity_profile_factory_agent);
        ButterKnife.bind(this);

        if (currentRole().isFactory()) {
            findViewById(R.id.rl_factory_info).setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.rl_factory_info).setVisibility(View.GONE);
        }


        for (int i = 0;i < ll_container.getChildCount();i++) {
            final View item = ll_container.getChildAt(i);
            if (item instanceof RelativeLayout) {
                final ViewGroup container = (ViewGroup)item;
                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClick((String)container.getTag());
                    }
                });
            }
        }

        imageCropHelper = new ImageCropHelper(this,iv_head){
            @Override
            public void start() {
                new AlertView("上传头像", null, "取消", null,
                        new String[]{"拍照", "从相册中选择"},
                        ProfileActivity.this, AlertView.Style.ActionSheet, new OnItemClickListener() {
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
            public void onGetImage(final File imgFile, final Bitmap bitmap) {
                new Req().url(Req.uploadHicon)
                        .addParam("sign", "update")
                        .addParam("picture", imgFile)
                        .enableLog(false)
                        .post(new DefaultCallback<JSONObject>() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    uploadUserAvatar(bitmap2Bytes(bitmap));
                                } catch (Throwable e) {

                                }
                                eventEmitter().emit(EVENT_GET_NEW_HICON, imgFile);
                            }
                        });
            }
        });
        //loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (tv_factory_info_label != null && currentRole().isFactory()) {
            tv_factory_info_label.setText(getFactoryInfo() == null ? "**请完善厂家资料" : "厂家资料");
            tv_factory_info_label.setTextColor(getFactoryInfo() == null ?
                    getResources().getColor(R.color.red) :
                    getResources().getColor(R.color.black));
        }
    }

    public JSONObject getFactoryInfo() {
        try {
            return loginResponse().getUserinfo().optJSONArray("supplierspetinfo").optJSONObject(0);
        } catch (Throwable e) {

        }
        return null;
    }

    private void uploadUserAvatar(final byte[] data) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                final String avatarUrl = ((DemoHXSDKHelper)HXSDKHelper.getInstance()).getUserProfileManager().uploadUserAvatar(data);
                DataManager.getInstance().getLoginResponse().setMImageURL(avatarUrl);
                Logger.d("avatarUrl: " + avatarUrl);
            }
        }).start();
    }


    public byte[] bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    private void loadData() {
        //加载数据
        String hiconUrl = DataManager.getInstance().getLoginResponse().getMImageURL();
        if (!TextUtils.isEmpty(FerUtil.getImageUrl(hiconUrl))) {
            try {
                Picasso.with(getActivity()).load(FerUtil.getImageUrl(hiconUrl)).placeholder(R.drawable.default_img).into(iv_head);
            } catch (Throwable e) {

            }
        }
        JSONObject userinfo = DataManager.getInstance().getLoginResponse().getUserinfo();
        if (currentRole().isFactory() || currentRole().isAgent()) {
            if (currentRole().isFactory()) {
                tv_name.setText(userinfo.optString("sname"));
            }
            else {
                tv_name.setText(userinfo.optString("aname"));
            }
            tv_mobile.setText(userinfo.optString("mobile"));
            tv_contacter.setText(userinfo.optString("contacter"));
            areaInfo = new AreaInfo();
            areaInfo.setProvinceName(userinfo.optString("province"));
            areaInfo.setCityName(userinfo.optString("city"));
            areaInfo.setAreaName(userinfo.optString("district"));
            tv_address.setText(areaInfo.getProvinceName() + "-" + areaInfo.getCityName() + "-" + areaInfo.getAreaName());
        }
        else if (currentRole().isSalesman()) {
            tv_name.setText(userinfo.optString("sname"));
            tv_mobile.setText(userinfo.optString("phone"));
        }
    }

    //修改资料
    private void modifyProfile() {
        final String name = tv_name.getText().toString();
        final String mobile = tv_mobile.getText().toString();
        String contacter = "";
        if (TextUtils.isEmpty(name)) {
            showLongToast("请输入姓名");
            return;
        }

        try {
            FerUtil.checkMobile(mobile);
        } catch (Throwable e) {
            showLongToast(e.getMessage());
            return;
        }

        if (currentRole().isFactory() || currentRole().isAgent()) {
            contacter = tv_contacter.getText().toString();
            if (TextUtils.isEmpty(contacter)) {
                showLongToast("请输入联系人名称");
                return;
            }
            if (areaInfo == null) {
                showLongToast("请选择所在地区");
                return;
            }
        }

        final String finalContacter = contacter;

        if (currentRole().isFactory()) {
            new Req()
                    .url(Req.suppliersDeal)
                    .addParam("sign", "modify")
                    .addParam("sname",name)
                    .addParam("mobile", mobile)
                    .addParam("contacter",contacter)
                    .addParam("province",areaInfo.getProvinceName())
                    .addParam("city",areaInfo.getCityName())
                    .addParam("district",areaInfo.getAreaName())
                    .get(new Callback<JSONObject>() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            LoginResponse loginResponse = DataManager.getInstance().getLoginResponse();
                            JSONObject userinfo = loginResponse.getUserinfo();
                            try {
                                userinfo.put("sname", name);
                                userinfo.put("mobile", mobile);
                                userinfo.put("contacter", finalContacter);
                                userinfo.put("province", areaInfo.getProvinceName());
                                userinfo.put("city", areaInfo.getCityName());
                                userinfo.put("district", areaInfo.getAreaName());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            loginResponse.setMUserName(name);
                            loginResponse.setMCompany(finalContacter);
                            loginResponse.setMPhone(mobile);

                            showLongToast("恭喜您，操作成功!");
                            isModify = false;
                            getTitleView().setRightText("");
                        }
                    });
        }
        else if (currentRole().isAgent()) {
            new Req()
                    .url(Req.agentDeal)
                    .addParam("sign", "modify")
                    .addParam("aname",name)
                    .addParam("mobile", mobile)
                    .addParam("contacter",contacter)
                    .addParam("province",areaInfo.getProvinceName())
                    .addParam("city",areaInfo.getCityName())
                    .addParam("district",areaInfo.getAreaName())
                    .get(new Callback<JSONObject>() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            LoginResponse loginResponse = DataManager.getInstance().getLoginResponse();
                            JSONObject userinfo = loginResponse.getUserinfo();
                            try {
                                userinfo.put("aname", name);
                                userinfo.put("mobile", mobile);
                                userinfo.put("contacter", finalContacter);
                                userinfo.put("province", areaInfo.getProvinceName());
                                userinfo.put("city", areaInfo.getCityName());
                                userinfo.put("district", areaInfo.getAreaName());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            loginResponse.setMUserName(name);
                            loginResponse.setMCompany(finalContacter);
                            loginResponse.setMPhone(mobile);

                            showLongToast("恭喜您，操作成功!");
                            isModify = false;
                            getTitleView().setRightText("");
                            updateRemoteNick(name);
                        }
                    });
        }
        else if (currentRole().isSalesman()) {
            new Req()
                    .url(Req.salespersonDeal)
                    .addParam("sign", "modify")
                    .addParam("sname",name)
                    .addParam("phone", mobile)
                    .get(new Callback<JSONObject>() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            LoginResponse loginResponse = DataManager.getInstance().getLoginResponse();
                            JSONObject userinfo = loginResponse.getUserinfo();
                            try {
                                userinfo.put("sname",name);
                                userinfo.put("phone",mobile);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            loginResponse.setMUserName(name);
                            loginResponse.setMPhone(mobile);

                            showLongToast("恭喜您，操作成功!");
                            isModify = false;
                            getTitleView().setRightText("");

                            updateRemoteNick(name);
                        }
                    });
        }
    }

    private void updateRemoteNick(final String nickName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ((DemoHXSDKHelper) HXSDKHelper.getInstance()).getUserProfileManager().updateParseNickName(nickName);
            }
        }).start();
    }

    //当点击时
    private void onItemClick(String action) {
        if (action == null) {
            return;
        }
        if (action.equals(ACTION_HEAD)) {
            //换头像
            imageCropHelper.start();
        }
        else if (action.equals(ACTION_NAME)) {
            //名字
            startActivityForResult(TextModifyActivity.createIntent(this,"姓名",tv_name),REQUEST_CODE_TEXT_MODIFY);
        }
        else if (action.equals(ACTION_MOBILE)) {
            //手机号
            startActivityForResult(TextModifyActivity.createIntent(this,"手机号",tv_mobile),REQUEST_CODE_TEXT_MODIFY);
        }
        else if (action.equals(ACTION_CONTACTER)) {
            //联系人名称
            startActivityForResult(TextModifyActivity.createIntent(this,"联系人名称",tv_contacter),REQUEST_CODE_TEXT_MODIFY);
        }
        else if (action.equals(ACTION_ADDRESS)) {
            //所在地区
            startActivityForResult(AreaActivity.createIntent(this,"所在地区",tv_address),REQUEST_CODE_AREA);
        }
        else if (action.equals(ACTION_FACTORY_INFO)) {
            //修改厂家个人信息
            startActivityForResult(FactoryProfileActivity.class, REQUEST_CODE_FACTORY_NFO);
        }
        else if (action.equals(ACTION_MODIFY_PWD)) {
            //修改密码
            startActivityForResult(new Intent(this, PWDModifyActivity.class), REQUEST_CODE_MODIFY_PWD);
        }
        else if (action.equals(ACTION_LOGOUT)) {
            //退出登录
            new AlertDialog.Builder(getActivity())
                    .setTitle("提示")
                    .setMessage("你确定要退出退出登录吗")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            setResult(RESULT_CODE_GO_TO_LOGIN);
                            finish();
                            FertilizerApplication.getInstance().logout();
                        }
                    })
                    .setNegativeButton("取消",null)
                    .create().show();
        }
    }

    @Override
    protected boolean onBack() {
        if (isModify) {
            new AlertDialog.Builder(this)
                    .setTitle("个人信息未保存")
                    .setMessage("是否退出?")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("保存个人信息", new DialogInterface.OnClickListener() {
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

        Logger.e(">>requestCode: " + requestCode + " ,resultCode: " + resultCode);
        imageCropHelper.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_TEXT_MODIFY
                && resultCode == RESULT_OK) {
            ProfileActivity.this.isModify = true;
            //修改完成
            getTitleView().setRightText("保存").setRightClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isModify) {
                        return;
                    }
                    ProfileActivity.this.modifyProfile();
                }
            });
        }

        if (requestCode == REQUEST_CODE_MODIFY_PWD
                && resultCode == RESULT_OK) {
            setResult(RESULT_CODE_GO_TO_LOGIN);
            finish();
        }

        if (requestCode == REQUEST_CODE_AREA
                && resultCode == RESULT_OK) {
            areaInfo = (AreaInfo) data.getSerializableExtra(AreaActivity.KEY_AREA_INFO);
            ProfileActivity.this.isModify = true;
            //修改完成
            getTitleView().setRightText("保存").setRightClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isModify) {
                        return;
                    }
                    ProfileActivity.this.modifyProfile();
                }
            });
            Logger.d(">> isModify: " + isModify);
        }
    }
}
