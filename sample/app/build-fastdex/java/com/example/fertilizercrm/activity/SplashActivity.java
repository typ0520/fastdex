package com.example.fertilizercrm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(intent.getFlags() | LoginActivity.FLAG_TO_MAIN_PAGE);
                startActivity(intent);
                finish();
            }
        },2000);
    }
}
