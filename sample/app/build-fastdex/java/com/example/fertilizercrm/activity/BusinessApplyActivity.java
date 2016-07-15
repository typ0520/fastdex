package com.example.fertilizercrm.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import butterknife.ButterKnife;
import com.example.fertilizercrm.common.utils.Logger;

import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;
import com.example.fertilizercrm.fragment.AskForLeaveFragment;
import com.example.fertilizercrm.fragment.CostFragment;
import com.example.fertilizercrm.fragment.TravelFragment;
import com.example.fertilizercrm.view.TitleView;

/**
 * 业务员业务申请(请假、出差、费用)
 */
public class BusinessApplyActivity extends BaseActivity {
    /**
     * 类型
     */
    public static final String KEY_TYPE = "type";

    public static final String TYPE_ASK_FOR_LEAVE = "请假";

    public static final String TYPE_TRAVEL = "出差";

    public static final String TYPE_COST = "费用";

    private AskForLeaveFragment askForLeaveFragment;
    private TravelFragment travelFragment;
    private CostFragment costFragment;
    private Fragment[] fragments;
    private int currentPosition;
    private String type;
    private String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = getIntent().getStringExtra(KEY_DATA);
        type = getIntent().getStringExtra(KEY_TYPE);

        Logger.e("type: " + type + " ,content: " + content);
        setContentView(R.layout.activity_business_apply);
        ButterKnife.bind(this);

        getTitleView().setOnTitleItemClickListener(new TitleView.OnTitleItemClickListener() {
            @Override
            public void onTitleItemClick(int position) {
                showTargetView(position);
            }
        });

        askForLeaveFragment = new AskForLeaveFragment();
        travelFragment = new TravelFragment();
        costFragment = new CostFragment();

        if (!TextUtils.isEmpty(type)) {
            getTitleView().setTitle(type);
            if (type.equals(TYPE_ASK_FOR_LEAVE)) {
                askForLeaveFragment.setContent(content);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fl_content, askForLeaveFragment)
                        .show(askForLeaveFragment).commit();
            }
            else if (type.equals(TYPE_TRAVEL)) {
                travelFragment.setContent(content);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fl_content, travelFragment)
                        .show(travelFragment).commit();
            }
            else if (type.equals(TYPE_COST)) {
                costFragment.setContent(content);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fl_content, costFragment)
                        .show(costFragment).commit();
            }
        }
        else {

            fragments = new Fragment[]{askForLeaveFragment,travelFragment,costFragment};
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fl_content, askForLeaveFragment)
                    .add(R.id.fl_content, travelFragment)
                    .add(R.id.fl_content, costFragment)
                    .hide(travelFragment)
                    .hide(costFragment)
                    .show(askForLeaveFragment).commit();

            showTargetView(currentPosition);
        }
    }

    private void showTargetView(int position) {
        FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
        trx.hide(fragments[currentPosition]);

        trx.show(fragments[position]).commit();

        this.currentPosition = position;
    }
}
