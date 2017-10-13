package fastdex.runtime;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

/**
 * Created by tong on 17/9/6.
 */
public class TransparentActivity extends Activity {
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    Runnable finishRunnable = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HANDLER.removeCallbacks(finishRunnable);
        HANDLER.postDelayed(finishRunnable,200L);
    }

    @Override
    protected void onDestroy() {
        HANDLER.removeCallbacks(finishRunnable);
        super.onDestroy();
    }
}
