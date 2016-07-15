package com.example.fertilizercrm.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.pickerview.TimePickerView;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseFragment;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.utils.FerUtil;

import org.apache.http.Header;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.example.fertilizercrm.common.utils.Logger;

/**
 * Created by tong on 16/1/6.
 * 业务申请-出差
 */
public class TravelFragment extends BaseFragment {
    @Bind(R.id.et_subject)    EditText et_subject;
    @Bind(R.id.et_day)        EditText et_day;
    @Bind(R.id.tv_type)       TextView tv_type;
    @Bind(R.id.tv_start_date) TextView tv_start_date;
    @Bind(R.id.tv_end_date)   TextView tv_end_date;
    @Bind(R.id.et_city)       TextView et_city;
    @Bind(R.id.et_target_city)TextView et_target_city;
    @Bind(R.id.et_content)    TextView et_content;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private Date startDate;
    private Date endDate;
    private String content;

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = obtainContentView(R.layout.fragment_travel, container);
        ButterKnife.bind(this, contentView);

        Logger.e("onCreateView: " + content);

        if (!TextUtils.isEmpty(content)) {
//            String subject = et_subject.getText().toString().trim();
//            String day = et_day.getText().toString().trim();
//            String type = tv_type.getText().toString().trim();
//            String startDate = tv_start_date.getText().toString().trim();
//            String endDate = tv_end_date.getText().toString().trim();
//            String city = et_city.getText().toString().trim();
//            String targetCity = et_target_city.getText().toString().trim();
//            String content = et_content.getText().toString().trim();

            contentView.findViewById(R.id.rl_type).setClickable(false);
            contentView.findViewById(R.id.rl_start_date).setClickable(false);
            contentView.findViewById(R.id.rl_end_date).setClickable(false);
            contentView.findViewById(R.id.btn_submit).setEnabled(false);
            et_subject.setEnabled(false);
            et_day.setEnabled(false);
            et_city.setEnabled(false);
            et_target_city.setEnabled(false);
            et_content.setEnabled(false);
            String[] strings = content.split(FerUtil.APPLY_SPLIT);
            try {
                et_subject.setText(strings[0]);
                et_day.setText(strings[1]);
                tv_type.setText(strings[2]);
                tv_start_date.setText(strings[3]);
                tv_end_date.setText(strings[4]);
                et_city.setText(strings[5]);
                et_target_city.setText(strings[6]);
                et_content.setText(strings[7]);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return contentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        startDate = new Date();
        endDate = new Date();
        tv_start_date.setText(dateFormat.format(startDate));
        tv_end_date.setText(dateFormat.format(endDate));
    }

    @OnClick(R.id.rl_type) void selectType() {
        final String[] strings = new String[]{"会议","考察"};
        AlertView alertView = new AlertView("请选择出差类型", null, null, null, strings, getContext(), AlertView.Style.Alert, new com.bigkoo.alertview.OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                tv_type.setText(strings[position]);
            }
        });
        alertView.show();
    }

    @OnClick(R.id.rl_start_date) void selectStartDate() {
        TimePickerView timePickerView = new TimePickerView(getContext(), TimePickerView.Type.YEAR_MONTH_DAY);
        timePickerView.setTitle("开始时间");
        timePickerView.setTime(startDate);
        timePickerView.setOnTimeSelectListener(new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date) {
                startDate = date;
                tv_start_date.setText(dateFormat.format(startDate));
            }
        });
        timePickerView.show();
    }

    @OnClick(R.id.rl_end_date) void selectEndDate() {
        TimePickerView timePickerView = new TimePickerView(getContext(), TimePickerView.Type.YEAR_MONTH_DAY);
        timePickerView.setTitle("结束时间");
        timePickerView.setTime(endDate);
        timePickerView.setOnTimeSelectListener(new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date) {
                endDate = date;
                tv_end_date.setText(dateFormat.format(endDate));
            }
        });
        timePickerView.show();
    }

    @OnClick(R.id.btn_submit) void clickSubmit() {
        // 主题-天数-类型-开始时间-结束时间-小时-备注
        // 存到 content里
        String subject = et_subject.getText().toString().trim();
        String day = et_day.getText().toString().trim();
        String type = tv_type.getText().toString().trim();
        String startDate = tv_start_date.getText().toString().trim();
        String endDate = tv_end_date.getText().toString().trim();
        String city = et_city.getText().toString().trim();
        String targetCity = et_target_city.getText().toString().trim();
        String content = et_content.getText().toString().trim();

        if (TextUtils.isEmpty(subject)) {
            showLongToast("请输入主题");
            return;
        }
        if (TextUtils.isEmpty(day)) {
            showLongToast("请输入请假天数");
            return;
        }
        if (TextUtils.isEmpty(city)) {
            showLongToast("请输入出发城市");
            return;
        }
        if (TextUtils.isEmpty(targetCity)) {
            showLongToast("请输入到达城市");
            return;
        }
        if (TextUtils.isEmpty(content)) {
            showLongToast("请输入内容");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(subject + FerUtil.APPLY_SPLIT);
        sb.append(day + FerUtil.APPLY_SPLIT);
        sb.append(type + FerUtil.APPLY_SPLIT);
        sb.append(startDate + FerUtil.APPLY_SPLIT);
        sb.append(endDate + FerUtil.APPLY_SPLIT);
        sb.append(city + FerUtil.APPLY_SPLIT);
        sb.append(targetCity + FerUtil.APPLY_SPLIT);
        sb.append(content + FerUtil.APPLY_SPLIT);

        new Req().url(Req.dailyWorkDeal)
                .addParam("sign", "savesupply")
                .addParam("content",sb.toString())
                .addParam("date",new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                .addParam("stypes", "出差")
                .get(new DefaultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        showLongToast("恭喜您，出差申请已提交成功!");
                        getActivity().finish();
                    }
                });
    }
}
