package com.dx168.fastdex.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import com.dx168.fastdex.sample.common.CommonUtils;
import com.dx168.fastdex.sample.common2.Common2Utils;
import com.dx168.fastdex.sample.common3.Common3Utils;
import com.dx168.fastdex.sample.javalib.JavaLib;
import java.lang.reflect.Field;

/**
 * Created by tong on 17/10/3.
 */
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

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

        JavaLib javaLib = new JavaLib();

        System.out.println("==app: " + javaLib.str);

        String s2 = getString(R.string.s2);
        int common3str = 0;
        try {
            Class common3RClass = Class.forName("com.dx168.fastdex.sample.common3.R$string");
            Field field = common3RClass.getField("common3str");
            common3str = (Integer) field.get(null);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(JavaLib.str);
        sb.append("\n");
        sb.append(CommonUtils.str);
        sb.append("\n");
        sb.append(Common2Utils.str);
        sb.append("\n");
        sb.append(Common3Utils.str);
        sb.append("\n");
        sb.append(getResources().getString(common3str));
        sb.append("\n");
        sb.append("haha");

        Log.d(TAG,"sb: \n" + sb.toString());
        Toast.makeText(this, sb.toString(),Toast.LENGTH_LONG).show();

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
