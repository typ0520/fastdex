package com.example.fertilizercrm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.example.fertilizercrm.common.utils.Logger;
import com.bigkoo.pickerview.TimePickerView;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;
import com.example.fertilizercrm.basic.DataManager;
import com.example.fertilizercrm.bean.RoleBean;
import com.example.fertilizercrm.bean.VisitInfo;
import com.example.fertilizercrm.http.Params;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.view.ImageLinearLayout;
import org.apache.http.Header;
import org.json.JSONObject;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 客户拜访添加
 */
public class CustomerVisitAddActivity extends BaseActivity {
    /**
     * 打开方式-展示详情
     */
    public static final int OPEN_TYPE_SHOW_DETIL = 1;

    private static final int REQUEST_CODE_GET_CUSTOMER = 1;
    public static final String KEY_VISIT_INFO = "visit_info";
    @Bind(R.id.image_ll) ImageLinearLayout image_ll;
    @Bind(R.id.tv_visit_date) TextView tv_visit_date;
    @Bind(R.id.tv_visit_name) TextView tv_visit_name;
    @Bind(R.id.et_content)    TextView et_content;
    @Bind(R.id.btn_submit)    Button btn_submit;
    private Date visitDate;
    private RoleBean roleBean;
    private VisitInfo visitInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_visit_add);
        ButterKnife.bind(this);
        visitInfo = (VisitInfo)getIntent().getSerializableExtra(KEY_VISIT_INFO);

        Logger.d(">>visitInfo:  " + visitInfo);
        if (visitInfo != null) {
            Logger.e(">> visitInfo: " + visitInfo.toString());
            openType = OPEN_TYPE_SHOW_DETIL;
        }
        visitDate = new Date();
        tv_visit_date.setText(getFormatVisitDate());
        image_ll.setMax(5);

        if (visitInfo != null) {
            btn_submit.setEnabled(false);
            findViewById(R.id.rl_visit_date).setClickable(false);
            findViewById(R.id.rl_people_visit).setClickable(false);
            tv_visit_date.setText(visitInfo.getVisitDate());
            tv_visit_name.setText(visitInfo.getFusername());
            image_ll.setEnabled(false);
            String content = visitInfo.getContent();
            if (content == null) {
                content = "";
            }

            content = new String(Base64.decode(content.getBytes(),Base64.DEFAULT));
//            try {
//                content = new String(content.getBytes("ISO-8859-1"),"UTF-8");
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
            et_content.setText(content);
            et_content.setEnabled(false);
            image_ll.setEnabled(false);
            image_ll.setImageUrls(visitInfo.getImageUrls());
        }
    }

    @OnClick(R.id.rl_visit_date) void selectVisitDate() {
        TimePickerView timePickerView = new TimePickerView(getActivity(), TimePickerView.Type.YEAR_MONTH_DAY);
        timePickerView.setTitle("开始时间");
        timePickerView.setTime(visitDate);
        timePickerView.setOnTimeSelectListener(new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date) {
                visitDate = date;
                tv_visit_date.setText(getFormatVisitDate());
            }
        });
        timePickerView.show();
    }

    @OnClick(R.id.rl_people_visit) void selectPeople() {
        Intent intent = new Intent(this,CustomerListActivity.class);
        intent.putExtra(CustomerListActivity.KEY_OPEN_TYPE, CustomerListActivity.OPEN_TYPE_GET_CUSTOMER);
        startActivityForResult(intent, REQUEST_CODE_GET_CUSTOMER);
    }

    @OnClick(R.id.btn_submit) void clickSubmit() {
        if (this.roleBean == null) {
            showLongToast("请选择拜访人");
            return;
        }
        String content = et_content.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            showLongToast("请添加内容");
            return;
        }
        content = content.replace("\n", "");

        Params params = new Params();
        params.put("sign","save");
        params.put("fuserid",this.roleBean.getBsoid());
        params.put("fusertype",this.roleBean.getRole().getCodeStr());
        params.put("content", Base64.encodeToString(content.getBytes(), Base64.DEFAULT).replace("\n", ""));
//        try {
//            params.put("content",URLEncoder.encode(content,"UTF-8"));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        params.put("longitude", DataManager.getInstance().getCurrentLatLng().longitude);
        params.put("latitude", DataManager.getInstance().getCurrentLatLng().latitude);
        params.put("visitDate", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        List<File> imageList = image_ll.getImageFiles();
        try {
            for (int i = 0;i < imageList.size();i++) {
                params.put("picture" + (i + 1), imageList.get(i));
            }
        } catch (Exception e) {
           e.printStackTrace();
        }
        new Req().url(Req.uploadUrl)
                .params(params).post(new DefaultCallback<JSONObject>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                showLongToast("恭喜您，操作成功!");
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        image_ll.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_GET_CUSTOMER
                && resultCode == RESULT_OK) {
            this.roleBean = (RoleBean)data.getSerializableExtra(CustomerListActivity.KEY_DATA);
            tv_visit_name.setText(roleBean.getContacter());

            Logger.d("<< roleBean: " + roleBean);
        }
    }

    public String getFormatVisitDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(visitDate);
    }
}
