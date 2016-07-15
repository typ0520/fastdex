package com.example.fertilizercrm.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.basic.BaseActivity;

/**
 * 文本框修改
 */
public class TextModifyActivity extends BaseActivity {
    public static final String EXTRA_KEY_LABEL = "label";
    public static final String EXTRA_KEY_INPUTTYPE = "input_type";
    public static TextView target;

    @Bind(R.id.et)     EditText et;
    @Bind(R.id.ib_del) ImageButton ib_del;
    private String originText;
    private int inputType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_modify);
        ButterKnife.bind(this);
        String label = getIntent().getStringExtra(EXTRA_KEY_LABEL);
        if (label == null) {
            label = "";
        }
        label = label.trim();
        inputType = getIntent().getIntExtra(EXTRA_KEY_INPUTTYPE,-1);
        if (inputType != -1) {
            et.setInputType(inputType);
        }

        getTitleView().setTitle(label).setRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!et.getText().toString().equals(originText)) {
                    if (target != null) {
                        target.setText(et.getText());
                    }
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });

        this.originText = target == null ? "" : target.getText().toString().trim();

        et.setText(originText);
        et.setSelection(originText.length());
        ib_del.setVisibility(originText.length() > 0 ? View.VISIBLE : View.GONE);
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getTitleView().rightEnable(!s.toString().equals(originText));
                ib_del.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ib_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et.setText("");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        target = null;
    }

    public static Intent createIntent(Context context,String label,TextView target) {
        Intent intent = new Intent(context,TextModifyActivity.class);
        intent.putExtra(EXTRA_KEY_LABEL, label);
        TextModifyActivity.target = target;
        return intent;
    }

    public static Intent createIntent(Context context,String label,TextView target,int inputType) {
        Intent intent = new Intent(context,TextModifyActivity.class);
        intent.putExtra(EXTRA_KEY_LABEL,label);
        intent.putExtra(EXTRA_KEY_INPUTTYPE,inputType);
        TextModifyActivity.target = target;
        return intent;
    }
}
