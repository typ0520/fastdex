package com.example.fertilizercrm.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.example.fertilizercrm.R;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tong on 14/11/8.
 */
public class TitleView extends LinearLayout {
    private static final String TAG = TitleView.class.getSimpleName();
    private View rl_left;
    private TextView tv_left;
    private TextView tv_title;
    private View rl_right;
    private TextView tv_right;
    private ImageView iv_right;
    private ImageView iv_title_arrow;
    private OnTitleItemClickListener mOnTitleItemClickListener;
    private String[] mTitleArray;
    private View ll_title;
    private int mTitlePosition;
    private AlertView alertView;

    public TitleView(Context context) {
        this(context, null);
    }

    public TitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setId(R.id.title_view);
        View.inflate(context, R.layout.view_title, this);
        rl_left = findViewById(R.id.rl_left);
        tv_left = (TextView) findViewById(R.id.tv_left);
        tv_title = (TextView)findViewById(R.id.tv_title);
        rl_right = findViewById(R.id.rl_right);
        tv_right = (TextView)findViewById(R.id.tv_right);
        iv_right = (ImageView) findViewById(R.id.iv_right);
        rl_left.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getContext() instanceof Activity) {
                    ((Activity) getContext()).finish();
                }
            }
        });
        iv_title_arrow = (ImageView) findViewById(R.id.iv_title_arrow);
        ll_title = findViewById(R.id.ll_title);
        resolveTitleDsl();
    }

    /**
     * 解析标题语言
     */
    protected void resolveTitleDsl() {
        if (getTag() == null || !(getTag() instanceof String))
            return;

        Log.d(TitleView.TAG, getTag().toString());

        String[] strArr = ((String)getTag()).split(":");
        for (String str : strArr) {
            if (str.startsWith("《")) {
                rl_left.setVisibility(View.VISIBLE);
                Map<String,Object> attrsMap = resolveAttrs(str.substring(1));
                Log.d(TitleView.TAG,"leftAttrs: " + attrsMap.toString());
                executeLeftScope(attrsMap);
            }
            if (str.startsWith("(") && str.endsWith(")")) {
                String titleStr = str.substring(1, str.length() - 1);
                String[] titleStrings = titleStr.split(",");
                if (titleStrings != null && titleStrings.length > 1) {
                    setTitleStrings(titleStrings,null);
                }
                else {
                    setTitle(titleStr);
                }
            }
            if (str.startsWith("》")) {
                rl_right.setVisibility(View.VISIBLE);
                Map<String,Object> attrsMap = resolveAttrs(str.substring(1));
                Log.d(TitleView.TAG,"rightAttrs: " + attrsMap.toString());
                executeRightScope(attrsMap);
            }
        }
    }

    /**
     * 执行右边布局参数
     */
    protected void executeRightScope(Map<String, Object> attrsMap) {
        if (attrsMap.get("label") != null) {
            setRightText(attrsMap.get("label").toString());
        }

        if (attrsMap.get("enable") != null) {
            boolean enable = Boolean.valueOf(attrsMap.get("enable").toString());
            rightEnable(enable);
        }
    }

    /**
     * 执行左边布局参数
     */
    protected void executeLeftScope(Map<String, Object> attrsMap) {
        if (attrsMap == null || attrsMap.isEmpty())return;

        if (attrsMap.get("label") != null) {
            setLeftText(attrsMap.get("label").toString());
            setLeftDrawable(-1);
        }
    }

    /**
     * 解析参数
     * @param str
     * @return
     */
    protected Map<String, Object> resolveAttrs(String str) {
        str = str == null ? "" : str;
        Map<String,Object> attrsMap = new HashMap<String,Object>();
        String[] itemArray = str.split(",");
        for (String item : itemArray) {
            if (item.startsWith("(") && item.endsWith(")")) {
                attrsMap.put("label",item.substring(1,item.length() - 1));
            }
            else if (item.matches(".{1,}=.{1,}")) {
                String[] keyValue = item.split("=");
                String action = keyValue[0];
                String info = keyValue[1];
                attrsMap.put(action,info);
            }
        }
        return attrsMap;
    }

    public TitleView setTitle(int textId) {
        return this.setTitle(getResources().getText(textId));
    }

    public TitleView setTitle(CharSequence text) {
        tv_title.setText(text);
        this.iv_title_arrow.setVisibility(GONE);
        ll_title.setOnClickListener(null);
        return this;
    }

    public TitleView setTitleColor(int color) {
        tv_title.setTextColor(color);
        return this;
    }

    public TitleView hideLeft() {
        rl_left.setVisibility(View.INVISIBLE);
        return this;
    }

    public TitleView showLeft() {
        rl_left.setVisibility(View.VISIBLE);
        return this;
    }

    public TitleView setLeftText(CharSequence text) {
        rl_left.setVisibility(View.VISIBLE);
        tv_left.setText(text);
        return this;
    }

    public TitleView setLeftDrawable(int resId) {
        rl_left.setVisibility(View.VISIBLE);
        if (resId == -1) {
            tv_left.setCompoundDrawables(null,null,null,null);
        } else {
            Drawable drawable= getResources().getDrawable(resId);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            tv_left.setCompoundDrawables(drawable,null,null,null);
        }

        return this;
    }

    public TitleView hideRight() {
        rl_right.setVisibility(View.INVISIBLE);
        return this;
    }

    public TitleView showRight() {
        rl_right.setVisibility(View.VISIBLE);
        return this;
    }

    public TitleView setLeftClickListener(OnClickListener listener) {
        rl_left.setOnClickListener(listener);
        return this;
    }

    public TitleView setRightText(CharSequence text) {
        tv_right.setVisibility(View.VISIBLE);
        tv_right.setText(text);
        return this;
    }

    public String getRightText() {
        return tv_right.getText().toString().trim();
    }

    public TitleView setRightText(int textId) {
        return setRightText(getResources().getText(textId));
    }

    public TitleView setRightImageResource(int drawableId) {
        iv_right.setVisibility(View.VISIBLE);
        iv_right.setImageResource(drawableId);
        return this;
    }

    public TitleView setRightClickListener(OnClickListener listener) {
        rl_right.setOnClickListener(listener);
        return this;
    }

    public TitleView rightEnable(boolean enable) {
        rl_right.setEnabled(enable);
        if (enable) {
            tv_right.setTextColor(getResources().getColor(R.color.right_text_color));
        } else {
            tv_right.setTextColor(getResources().getColor(R.color.right_text_color_disable));
        }
        return this;
    }

    public TitleView setTitleClickListener(OnClickListener listener) {
        ll_title.setOnClickListener(listener);
        return this;
    }

    public void setTitleStrings(final String[] strings,final OnTitleItemClickListener listener) {
        if (strings == null || strings.length == 0) {
            return;
        }
        this.mTitleArray = strings;
        this.mOnTitleItemClickListener = listener;
        this.iv_title_arrow.setImageResource(R.drawable.title_arrow_bottom);
        this.iv_title_arrow.setVisibility(VISIBLE);
        this.mTitlePosition = 0;

        tv_title.setText(strings[0]);
        ll_title.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ll_title.getTag() == null) {
                    iv_title_arrow.setImageResource(R.drawable.title_arrow_top);
                    //未显示
                    alertView = new AlertView(null, null, null, null, strings, getContext(), AlertView.Style.Alert, new com.bigkoo.alertview.OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, int position) {
                            tv_title.setText(strings[position]);
                            iv_title_arrow.setImageResource(R.drawable.title_arrow_bottom);
                            ll_title.setTag(null);
                            mTitlePosition = position;
                            if (mOnTitleItemClickListener != null) {
                                mOnTitleItemClickListener.onTitleItemClick(position);
                            }
                        }
                    });
                    alertView.show();
                    ll_title.setTag(new Object());
                } else {
                    iv_title_arrow.setImageResource(R.drawable.title_arrow_bottom);
                    ll_title.setTag(null);
                }
            }
        });
    }

    public int getTitlePosition() {
        return mTitlePosition;
    }

    public String[] getTitleArray() {
        return mTitleArray;
    }

    public boolean isTitleArrayViewShowing() {
        return alertView != null && alertView.isShowing();
    }

    public void closeTitleArrayView() {
        if (alertView != null) {
            alertView.dismiss();
        }
        iv_title_arrow.setImageResource(R.drawable.title_arrow_bottom);
        ll_title.setTag(null);
    }

    public void setOnTitleItemClickListener(OnTitleItemClickListener listener) {
        this.mOnTitleItemClickListener = listener;
    }

    public String getTitleText() {
        return tv_title.getText().toString();
    }

    public interface OnTitleItemClickListener {
        void onTitleItemClick(int position);
    }
}

