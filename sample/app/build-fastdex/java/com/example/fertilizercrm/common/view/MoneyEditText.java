package com.example.fertilizercrm.common.view;

import java.text.DecimalFormat;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 小数点后只能输入两位,最前面只能输入一个0，最前面一位不能为.
 * 不能输入0.00
 * @author tong
 *
 */
public class MoneyEditText extends EditText {
	private OnFocusChangeListener mOnFocusChangeListener;
	private OnEditorActionListener mOnEditorActionListenerBase;

	public MoneyEditText(Context context, AttributeSet attrs) {
		super(context, attrs);

		setLongClickable(false);
		addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			public void afterTextChanged(Editable edt) {
				if (edt.length() == 0)
					return;
				String temp = edt.toString(); 
				if (temp.startsWith(".")) {
					edt.delete(0, 1);
					return;
				}
			/*	if (temp.startsWith("0.00") && edt.length() == 4) {
					edt.delete(3, 4);
					return;
				}*/
				
				if (edt.length() >= 2 && temp.startsWith("0") && !temp.substring(1,2).equals(".")) {
					edt.delete(0, 1);
					return;
				}
				
				//处理小数点后只能输入两位的问题
				int pointIndex = temp.indexOf("."); 
				if (pointIndex > 0 && temp.length() - pointIndex - 1 > 2) {
					edt.delete(pointIndex + 3, pointIndex + 4); 
				}
				
				//小数点前只能输入12位
				pointIndex = temp.indexOf(".");
				if (pointIndex <= 0) {
					if (temp.length() > 12) {
						edt.delete(12, edt.length());
					}
				} else {
					if (pointIndex > 12) {
						edt.delete(12, pointIndex);
					}
				}
			} 	   
		});
		super.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					addZero();
				}
				if (mOnFocusChangeListener != null) {
					mOnFocusChangeListener.onFocusChange(v, hasFocus);
				}
			}
		});
		
		super.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					if(inputMethodManager.isActive()){
						inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
					}
					//移除焦点，添加.00
					clearFocus();
				}
				
				if (mOnEditorActionListenerBase != null) {
					mOnEditorActionListenerBase.onEditorAction(v, actionId, event);
				}
				return false;
			}
		});
	}
	
	@Override
	public void setOnFocusChangeListener(OnFocusChangeListener l) {
		mOnFocusChangeListener = l;
	}
	
	public void addZero() {
		if (TextUtils.isEmpty(getText())) 
			return;
		DecimalFormat format = new DecimalFormat("0.00");
		double value = parseDouble(getText());
		String result = format.format(value);
		if ("0.0".equals(result)) {
			result = "0.00";
		}
		setText(format.format(value));
		setSelection(getText().length());
	}
	
	public String getMoney() {
		return getText().toString();
	}
	
	@Override
	public void setOnEditorActionListener(OnEditorActionListener l) {
		mOnEditorActionListenerBase = l;
	}

	public double parseDouble(Editable text) {
		return parseDouble(text.toString());
	}
	public double parseDouble(String number) {
		try {
			return  Double.parseDouble(number);
		} catch (Exception e) {
			return 0.0;
		}
	}
}
