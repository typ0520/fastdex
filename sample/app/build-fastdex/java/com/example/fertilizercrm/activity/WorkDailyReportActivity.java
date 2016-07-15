package com.example.fertilizercrm.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bigkoo.pickerview.TimePickerView;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;
import com.example.fertilizercrm.bean.WorkDailyInfo;
import com.example.fertilizercrm.http.Req;
import com.example.fertilizercrm.utils.FerUtil;
import com.example.fertilizercrm.view.WorkReportSwitchView;
import org.apache.http.Header;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WorkDailyReportActivity extends BaseActivity {
    @Bind(R.id.tv_time) TextView tv_time;
    @Bind(R.id.et)      TextView et;
    @Bind(R.id.work_switch) WorkReportSwitchView work_switch;

    private Date date = new Date();
    private int currentTab;//0: 今日总结 1: 明日计划

    private WorkDailyInfo workDailyInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_daily_report);
        ButterKnife.bind(this);

        workDailyInfo = (WorkDailyInfo) getIntent().getSerializableExtra(KEY_DATA);
        tv_time.setText(getFormatTime());
        work_switch.setOnItemClickListener(new WorkReportSwitchView.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                currentTab = position;
            }
        });

        if (workDailyInfo != null) {
            findViewById(R.id.rl_time).setClickable(false);
            work_switch.setEnabled(false);
            int position = 0;
            try {
                position = Integer.valueOf(workDailyInfo.getMemo());
            } catch (Throwable e) {

            }
            work_switch.setCurrent(position);
            et.setEnabled(false);
            tv_time.setText(FerUtil.formatCreateDate(workDailyInfo.getCreateDate()));
            et.setText(workDailyInfo.getContent());
            findViewById(R.id.btn_submit).setEnabled(false);
        }
    }

    @OnClick(R.id.rl_time) void selectTime() {
        TimePickerView timePickerView = new TimePickerView(this, TimePickerView.Type.YEAR_MONTH_DAY);
        timePickerView.setTitle("开始时间");
        timePickerView.setTime(date);
        timePickerView.setOnTimeSelectListener(new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date) {
                WorkDailyReportActivity.this.date = date;
                tv_time.setText(getFormatTime());
            }
        });
        timePickerView.show();
    }

    private String getFormatTime() {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    @OnClick(R.id.btn_submit) void clickSubmit() {
        String content = et.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            showLongToast("请输入内容");
            return;
        }
        new Req().url(Req.dailyWorkDeal)
                .addParam("sign", "savedailyreport")
                .addParam("content", content)
                .addParam("date",tv_time.getText().toString())
                .addParam("memo",currentTab + "")
                .get(new DefaultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        showLongToast("恭喜您，日报已提交成功!");
                    }
                });
    }
}
