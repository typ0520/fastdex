package com.example.fertilizercrm.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
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
 * 业务申请-费用
 */
public class CostFragment extends BaseFragment {
    @Bind(R.id.et_subject)    EditText et_subject;
    @Bind(R.id.tv_type)       TextView tv_type;
    @Bind(R.id.et_amount)     EditText et_amount;
    @Bind(R.id.et_content)    TextView et_content;

    private String content;

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = obtainContentView(R.layout.fragment_cost, container);
        ButterKnife.bind(this, contentView);

        Logger.e("onCreateView: " + content);

        if (!TextUtils.isEmpty(content)) {
            contentView.findViewById(R.id.rl_type).setClickable(false);
            contentView.findViewById(R.id.btn_submit).setEnabled(false);
            et_subject.setEnabled(false);
            et_amount.setEnabled(false);
            et_content.setEnabled(false);

            String[] strings = content.split(FerUtil.APPLY_SPLIT);
            try {
                et_subject.setText(strings[0]);
                tv_type.setText(strings[1]);
                et_amount.setText(strings[2]);
                et_content.setText(strings[3]);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return contentView;
    }

    @OnClick(R.id.rl_type) void selectType() {
        final String[] strings = new String[]{"差旅","考察"};
        AlertView alertView = new AlertView("请选择费用类型", null, null, null, strings, getContext(), AlertView.Style.Alert, new com.bigkoo.alertview.OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                tv_type.setText(strings[position]);
            }
        });
        alertView.show();
    }

    @OnClick(R.id.btn_submit) void clickSubmit() {
        String subject = et_subject.getText().toString().trim();
        String type = tv_type.getText().toString().trim();
        String amount = et_amount.getText().toString().trim();
        String content = et_content.getText().toString().trim();

        if (TextUtils.isEmpty(subject)) {
            showLongToast("请输入主题");
            return;
        }
        if (TextUtils.isEmpty(amount)) {
            showLongToast("请输入金额");
            return;
        }
        if (TextUtils.isEmpty(content)) {
            showLongToast("请输入出差");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(subject + FerUtil.APPLY_SPLIT);
        sb.append(type + FerUtil.APPLY_SPLIT);
        sb.append(amount + FerUtil.APPLY_SPLIT);
        sb.append(content + FerUtil.APPLY_SPLIT);

        new Req().url(Req.dailyWorkDeal)
                .addParam("sign", "savesupply")
                .addParam("content",sb.toString())
                .addParam("date",new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                .addParam("stypes", "费用")
                .get(new DefaultCallback<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        showLongToast("恭喜您，费用申请已提交成功!");
                        getActivity().finish();
                    }
                });
    }
}
