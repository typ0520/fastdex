package com.dx168.fastdex.sample;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by tong on 17/10/3.
 */
public class MainActivity extends Activity {
    public static void aa() {

    }

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new CustomView(this,null);
        //new CustomView2(this,null);

        Button btn = (Button) findViewById(R.id.btn);
        //btn.setOnClickListener(view -> Toast.makeText(this,"哈哈",Toast.LENGTH_LONG).show());

        String s1 = getString(R.string.s1);
        String s3 = getString(R.string.s3);
        //Toast.makeText(this,"haha " + " | " + s1 +  " | " + s3,Toast.LENGTH_LONG).show();


        String s2 = getString(R.string.s2);
        Toast.makeText(this,"1" + " | " + s1 + " | " + s2 + " | " + s3,Toast.LENGTH_LONG).show();

        new Runnable(){
            @Override
            public void run() {

            }
        };

        SampleApplication realApp = (SampleApplication)getApplication();

        new Runnable(){
            @Override
            public void run() {
                //Toast.makeText(getApplicationContext(),"1223",Toast.LENGTH_LONG).show();
            }
        }.run();
        new T1();
        new T2();
    }

    private class T1 {

    }

    private static class T2 {

    }
}
