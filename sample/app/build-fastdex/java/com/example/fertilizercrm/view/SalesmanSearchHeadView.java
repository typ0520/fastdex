package com.example.fertilizercrm.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.bigkoo.pickerview.TimePickerView;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.activity.SalesmanListActivity;
import com.example.fertilizercrm.basic.BaseActivity;
import com.example.fertilizercrm.basic.DataManager;
import com.example.fertilizercrm.role.Role;

import java.text.SimpleDateFormat;
import java.util.Date;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.example.fertilizercrm.common.utils.Logger;

/**
 * Created by tong on 16/1/8.
 * 厂家和代理商查询业务员日常工作信息时参数选择view
 */
public class SalesmanSearchHeadView extends LinearLayout {
    private static final int REQUESTCODE_SALESMAN = 100;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    @Bind(R.id.tv_salesman) TextView tv_salesman;
    @Bind(R.id.tv_start_date) TextView tv_start_date;
    @Bind(R.id.tv_end_date)   TextView tv_end_date;
    @Bind(R.id.tv_apply_type) TextView tv_apply_type;
    @Bind(R.id.rl_apply_type) View rl_apply_type;
    @Bind(R.id.rl_salesman) View rl_salesman;
    @Bind(R.id.btn_submit) View btn_submit;

    private Date startDate;
    private Date endDate;
    private String bsoid;
    private AlertView alertView;
    private OnSearchListener mOnSearchListener;
    private Animation showAnim;
    private Animation hideAnim;
    private boolean showAnimShowing;
    private boolean hideAnimShowing;

    public SalesmanSearchHeadView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_salesman_search_head, this);
        ButterKnife.bind(this);
        initAnim();
        startDate = new Date();
        endDate = new Date();
        tv_start_date.setText(getFormatStartDate());
        tv_end_date.setText(getFormatEndDate());

        final BaseActivity activity = (BaseActivity)context;
        activity.registerActivityResultListener(new BaseActivity.OnActivityResultListener() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                if (requestCode == REQUESTCODE_SALESMAN
                        && resultCode == Activity.RESULT_OK) {
                    SalesmanSearchHeadView.this.bsoid = data.getStringExtra(SalesmanListActivity.KEY_BSOID);
                    String sname = data.getStringExtra(SalesmanListActivity.KEY_SNAME);
                    tv_salesman.setText(sname);
                }
            }
        });
        if (Role.currentRole().isSalesman()) {
            //如果是业务员收起掉业务员选择ui
            rl_salesman.setVisibility(GONE);
            this.bsoid = DataManager.getInstance().getLoginResponse().getMID();
        }
    }

    public SalesmanSearchHeadView enableSubmit() {
        btn_submit.setVisibility(VISIBLE);
        getActivity().registerActivityResultListener(new BaseActivity.OnActivityResultListener() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                if (requestCode == REQUESTCODE_SALESMAN
                        && resultCode == Activity.RESULT_OK) {
                    SalesmanSearchHeadView.this.bsoid = data.getStringExtra(SalesmanListActivity.KEY_BSOID);
                    String sname = data.getStringExtra(SalesmanListActivity.KEY_SNAME);
                    tv_salesman.setText(sname);
                }
            }
        });
        getActivity().getTitleView().setRightText("收起").setRightClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getVisibility() == VISIBLE) {
                    animHide();
                    getActivity().getTitleView().setRightText("筛选");
                } else {
                    animShow();
                    getActivity().getTitleView().setRightText("收起");
                }
            }
        });
        return this;
    }

    public BaseActivity getActivity() {
        return (BaseActivity) getContext();
    }

    private void initAnim() {
        showAnim = AnimationUtils.loadAnimation(getContext(), R.anim.scale_in_from_top);
        showAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                setVisibility(View.VISIBLE);
                showAnimShowing = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showAnimShowing = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        hideAnim =  AnimationUtils.loadAnimation(getContext(), R.anim.scale_out_from_bottom);
        hideAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Logger.d(">> animation start");
                hideAnimShowing = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Logger.d(">> animation end");
                hideAnimShowing = false;
                setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @OnClick(R.id.rl_salesman) void selectSalesman() {
        Intent intent = new Intent(getContext(),SalesmanListActivity.class);
        intent.putExtra(SalesmanListActivity.KEY_OPEN_TYPE, SalesmanListActivity.OPEN_GET_GET_SALESMAN);
        ((Activity)getContext()).startActivityForResult(intent, REQUESTCODE_SALESMAN);
    }

    @OnClick(R.id.rl_start_date) void selectStartDate() {
        TimePickerView timePickerView = new TimePickerView(getContext(), TimePickerView.Type.YEAR_MONTH_DAY);
        timePickerView.setTitle("开始时间");
        timePickerView.setTime(startDate);
        timePickerView.setOnTimeSelectListener(new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date) {
                startDate = date;
                tv_start_date.setText(getFormatStartDate());
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
                tv_end_date.setText(getFormatEndDate());
            }
        });
        timePickerView.show();
    }

    @OnClick(R.id.rl_apply_type) void selectApplyType() {
        //选择申请类别
        final String[] strings = new String[]{"请假","出差","费用"};
        alertView = new AlertView("请选择申请类别", null, null, null, strings, getContext(), AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                tv_apply_type.setText(strings[position]);
            }
        });
        alertView.show();
    }

    @OnClick(R.id.btn_submit) void submit() {
        if (!Role.currentRole().isSalesman()) {
            if (TextUtils.isEmpty(getBsoid())) {
                Toast.makeText(getContext(),"请先选择业务员",Toast.LENGTH_LONG).show();
                return;
            }
        }
        if (mOnSearchListener != null) {
            this.mOnSearchListener.onSearch();
        }
    }

    public void showApplyTypeView() {
        rl_apply_type.setVisibility(View.VISIBLE);
    }

    public String getApplyType() {
        return tv_apply_type.getText().toString().trim();
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getFormatStartDate() {
        return dateFormat.format(startDate);
    }

    public String getFormatEndDate() {
        return dateFormat.format(endDate);
    }

    public String getBsoid() {
        return bsoid;
    }

    public void dismiss() {
        if (alertView != null) {
            alertView.dismiss();
        }
    }

    public boolean isShowing() {
        return alertView != null && alertView.isShowing();
    }

    public void animShow() {
//        if (showAnimShowing) {
//            return;
//        }
//        showAnim.start();
        setVisibility(View.VISIBLE);
    }

    public void animHide() {
//        if (hideAnimShowing) {
//            return;
//        }
//        hideAnim.start();
        setVisibility(View.GONE);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

        if (visibility == GONE) {
            getActivity().getTitleView().setRightText("筛选");
        }
        else if (visibility == VISIBLE) {
            getActivity().getTitleView().setRightText("收起");
        }
    }

    public void setOnSearchListener(OnSearchListener listener) {
        this.mOnSearchListener = listener;
    }

    public interface OnSearchListener {
        void onSearch();
    }
}
