package com.example.fertilizercrm.activity;

import android.os.Bundle;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.example.fertilizercrm.common.utils.DeviceUtil;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;

/**
 * 关于我们
 */
public class AboutUsActivity extends BaseActivity {
    @Bind(R.id.tv_version) TextView tv_version;
    @Bind(R.id.tv_content) TextView tv_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        ButterKnife.bind(this);

        tv_version.setText("v" + DeviceUtil.getCurrentVersionName(this));
        tv_content.setText("        " + tv_content.getText().toString());
    }
}
