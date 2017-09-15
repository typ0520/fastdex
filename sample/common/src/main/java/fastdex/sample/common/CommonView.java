package fastdex.sample.common;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import fastdex.sample.common.R;

/**
 * Created by tong on 17/4/13.
 */
public class CommonView extends LinearLayout {
    public CommonView(Context context) {
        this(context,null);
    }

    public CommonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context,R.layout.common_view,this);
    }
}
