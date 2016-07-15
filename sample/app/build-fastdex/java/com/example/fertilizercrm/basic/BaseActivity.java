package com.example.fertilizercrm.basic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.svprogresshud.SVProgressHUD;
import com.example.fertilizercrm.FertilizerApplication;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.bean.LoginResponse;
import com.example.fertilizercrm.http.Callback;
import com.example.fertilizercrm.role.Role;
import com.example.fertilizercrm.view.TitleView;
import java.util.HashSet;
import java.util.Set;
import com.example.fertilizercrm.common.utils.EventEmitter;

/**
 * Created by tong on 15/12/7.
 */
public class BaseActivity extends FragmentActivity implements View.OnClickListener {
    /**
     * 数据key
     */
    public static final String KEY_DATA = "data";

    /**
     * 默认业务类型
     */
    public static final int OPEN_TYPE_DEFAULT = 0;

    /**
     * 编辑状态
     */
    public static final int OPEN_TYPE_EDIT = 100;

    /**
     * 业务类型key
     */
    public static final String KEY_OPEN_TYPE = "open_type";

    private TitleView mTitleView;
    protected SVProgressHUD svProgressHUD;
    protected int openType;
    protected Set<OnActivityResultListener> activityResultListeners;

    protected AlertView alertView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openType = getIntent().getIntExtra(KEY_OPEN_TYPE,OPEN_TYPE_DEFAULT);

        FertilizerApplication.getInstance().addActivity(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (getTitleView() != null) {
            getTitleView().setLeftClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean result = onBack();
                    if (!result) {
                        finish();
                    }
                }
            });
        }
    }

    public TitleView getTitleView() {
        if (mTitleView == null) {
            mTitleView = (TitleView)findViewById(R.id.title_view);
        }
        return mTitleView;
    }

    protected boolean onBack() {
        if (getTitleView() != null && getTitleView().isTitleArrayViewShowing()) {
            getTitleView().closeTitleArrayView();
            return true;
        }
        if (alertView != null && alertView.isShowing()) {
            alertView.dismiss();
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == event.KEYCODE_BACK && onBack()) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public void showShortToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void showShortToast(int resId) {
        showShortToast(getString(resId));
    }

    public void showLongToast(String msg) {
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
    }

    public void showLongToast(int resId) {
        showLongToast(getString(resId));
    }

    public Activity getActivity() {
        return this;
    }

    public void showProgress() {
        if (svProgressHUD == null) {
            svProgressHUD = new SVProgressHUD(this);
        }
        svProgressHUD.show();
    }

    public void hideProgress() {
        svProgressHUD.dismiss();
    }

    public void startActivity(Class<? extends Activity> clazz) {
        startActivity(new Intent(this, clazz));
    }

    public void startActivityForResult(Class<? extends Activity> clazz,int requestCode) {
        startActivityForResult(new Intent(this, clazz), requestCode);
    }

    @Override
    public void onClick(View view) {

    }

    /**
     * 隐藏软键盘
     */
    public void hideKeyboard() {
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null) {
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public Role currentRole() {
        return Role.currentRole();
    }

    public abstract class DefaultCallback<T> extends Callback<T> {
        @Override
        public void onStart() {
            showProgress();
        }

        @Override
        public void onFinish() {
            hideProgress();
        }
    }


    public void registerActivityResultListener(OnActivityResultListener listener) {
        if (activityResultListeners == null) {
            activityResultListeners = new HashSet<>();
        }
        activityResultListeners.add(listener);
    }

    public void unregisterActivityResultListener(OnActivityResultListener listener) {
        if (activityResultListeners == null) {
            activityResultListeners = new HashSet<>();
        }
        activityResultListeners.remove(listener);
    }

    @Override
    protected void onDestroy() {
        if (activityResultListeners != null) {
            try {
                activityResultListeners.clear();
            } catch (Throwable e) {

            }
        }
        FertilizerApplication.getInstance().removeActivity(this);
        super.onDestroy();
    }

    public Context getContext() {
        return this;
    }

    public EventEmitter eventEmitter() {
        return FertilizerApplication.getInstance().getEventEmitter();
    }

    public LoginResponse loginResponse() {
        return DataManager.getInstance().getLoginResponse();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (activityResultListeners != null) {
            for (OnActivityResultListener listener : activityResultListeners) {
                if (listener != null) {
                    listener.onActivityResult(requestCode,resultCode,data);
                }
            }
        }
    }

    public interface OnActivityResultListener {
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }
}
