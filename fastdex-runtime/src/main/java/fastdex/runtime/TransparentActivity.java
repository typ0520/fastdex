package fastdex.runtime;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by tong on 17/9/6.
 */
public class TransparentActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        finish();
    }
}
