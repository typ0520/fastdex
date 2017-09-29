package fastdex.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import fastdex.sample.common.CommonUtils;
import com.github.typ0520.fastdex.sample.R;
import butterknife.ButterKnife;
import fastdex.sample.common2.Common2Utils;
import fastdex.sample.javalib.JavaLib;
import java.lang.reflect.Field;
import butterknife.BindView;

/**
 * Created by tong on 17/10/3.
 */
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.tv)
    View view2;

    public static void aa() {

    }

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        new CustomView(this,null);
        //new CustomView2(this,null);
        view2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "view2",Toast.LENGTH_LONG).show();
            }
        });

        Button btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });
        btn.setOnClickListener(view -> Toast.makeText(this,"哈哈",Toast.LENGTH_LONG).show());

        String s1 = getString(R.string.s1);
        String s3 = getString(R.string.s3);
        //Toast.makeText(this,"haha " + " | " + s1 +  " | " + s3,Toast.LENGTH_LONG).show();

        JavaLib javaLib = new JavaLib();

        System.out.println("==app: " + javaLib.str);

        String s2 = getString(R.string.s2);
        int commonstr = 0;
        try {
            Class commonRClass = Class.forName("fastdex.sample.common.R$string");
            Field field = commonRClass.getField("st");
            commonstr = (Integer) field.get(null);
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
//        sb.append(new fastdex.sample.kotlinlib.KotlinHello().getName());
//        sb.append("\n");
        sb.append(getResources().getString(commonstr));
        sb.append("\n");
        sb.append("1");

        Log.d(TAG,"sb: \n" + sb.toString());
        Toast.makeText(this, sb.toString(),Toast.LENGTH_LONG).show();

        new Runnable(){
            @Override
            public void run() {

            }
        };

        //SampleApplication realApp = (SampleApplication)getApplication();

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
