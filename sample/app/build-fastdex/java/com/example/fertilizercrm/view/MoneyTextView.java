package com.example.fertilizercrm.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import java.text.DecimalFormat;

/**
 * Created by tong on 15/12/31.
 */
public class MoneyTextView extends TextView {
    public MoneyTextView(Context context) {
        super(context);
    }

    public MoneyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MoneyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (text == null) {
            text = "0.0";
        }
        String str = String.valueOf(text);
        super.setText("￥" + new DecimalFormat("0.0").format(str), type);
    }
}
