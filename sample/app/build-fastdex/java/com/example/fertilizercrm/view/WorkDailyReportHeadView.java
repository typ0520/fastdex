package com.example.fertilizercrm.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.activity.SalesmanListActivity;
import com.example.fertilizercrm.basic.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.example.fertilizercrm.common.utils.Logger;

/**
 * 工作日报ListView head view
 * @see com.example.fertilizercrm.activity.WorkDailyReportListActivity
 */
public class WorkDailyReportHeadView extends LinearLayout {
    private static final int REQUESTCODE_SALESMAN = 100;

    @Bind(R.id.btn)       Button btn;
    @Bind(R.id.et)        EditText et;
    @Bind(R.id.ib_search) ImageButton ib_search;
    @Bind(R.id.tv_salesman) TextView tv_salesman;
    private OnSearchListener mOnSearchListener;
    private AlertView alertView;

    private int currentMonth;
    private int selectMonth;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private String bsoid;

    public WorkDailyReportHeadView(Context context) {
        this(context, null);
    }

    public WorkDailyReportHeadView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_work_daily_report_head, this);
        ButterKnife.bind(this);
        ib_search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSearchListener != null) {
                    mOnSearchListener.onSearch(et.getText().toString().trim());
                }
            }
        });
        ((BaseActivity)getContext()).registerActivityResultListener(new BaseActivity.OnActivityResultListener() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                if (requestCode == REQUESTCODE_SALESMAN
                        && resultCode == Activity.RESULT_OK) {
                    WorkDailyReportHeadView.this.bsoid = data.getStringExtra(SalesmanListActivity.KEY_BSOID);
                    String sname = data.getStringExtra(SalesmanListActivity.KEY_SNAME);
                    tv_salesman.setText(sname);
                }
            }
        });

        Calendar calendar = getCalendar();
        currentMonth = calendar.get(Calendar.MONTH) + 1;
        selectMonth = currentMonth;

        btn.setText(selectMonth + "月");
    }

    @OnClick(R.id.btn) void selectMonth() {
        final String[] strings = new String[currentMonth];
        for (int i = 0;i < strings.length;i++) {
            strings[i] = i + 1 + "月";
        }

        alertView = new AlertView("请选择月份", null, null, null, strings, getContext(), AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                selectMonth = position + 1;
            }
        });
        alertView.show();
    }

    @OnClick(R.id.rl_salesman) void selectSalesman() {
        Intent intent = new Intent(getContext(),SalesmanListActivity.class);
        intent.putExtra(SalesmanListActivity.KEY_OPEN_TYPE, SalesmanListActivity.OPEN_GET_GET_SALESMAN);
        ((Activity)getContext()).startActivityForResult(intent, REQUESTCODE_SALESMAN);
    }

    public String getFormatStartDate() {
        Calendar calendar = getCalendar();
        calendar.set(Calendar.MONTH, selectMonth - 1);
        calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DATE));
        String endDateStr =  dateFormat.format(calendar.getTime());
        Logger.e("<< startDateStr: " + endDateStr);
        return endDateStr;
    }

    public String getFormatEndDate() {
        Calendar calendar = getCalendar();
        calendar.set(Calendar.MONTH, selectMonth - 1);
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
        String endDateStr =  dateFormat.format(calendar.getTime());
        Logger.e("<< endDateStr: " + endDateStr);
        return endDateStr;
    }

    private Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();
        return calendar;
    }

    public void dismiss() {
        if (alertView != null) {
            alertView.dismiss();
        }
    }

    public String getBsoid() {
        return bsoid;
    }

    public boolean isShowing() {
        return alertView != null && alertView.isShowing();
    }

    public void setOnSearchListener(OnSearchListener listener) {
        this.mOnSearchListener = listener;
    }

    public interface OnSearchListener {
        void onSearch(String keyword);
    }
}
