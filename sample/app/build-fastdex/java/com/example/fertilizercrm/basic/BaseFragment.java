package com.example.fertilizercrm.basic;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bigkoo.alertview.AlertView;
import com.example.fertilizercrm.FertilizerApplication;
import com.example.fertilizercrm.R;
import com.example.fertilizercrm.http.Callback;
import com.example.fertilizercrm.role.Role;
import com.example.fertilizercrm.view.TitleView;

import com.example.fertilizercrm.common.utils.EventEmitter;

/**
 * Created by tong on 15/12/7.
 */
public class BaseFragment extends Fragment implements View.OnClickListener {
    private TitleView mTitleView;
    protected View mRootView;
    protected AlertView alertView;

    public View obtainContentView(int layoutId, ViewGroup container) {
        return (mRootView =  View.inflate(getActivity(),layoutId,null));
    }

    public TitleView getTitleView() {
        if (mTitleView == null) {
            mTitleView = (TitleView)mRootView.findViewById(R.id.title_view);
        }
        return mTitleView;
    }

    public void showShortToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    public void showShortToast(int resId) {
        showShortToast(getString(resId));
    }

    public void showLongToast(String msg) {
        Toast.makeText(getActivity(),msg,Toast.LENGTH_LONG).show();
    }

    public void showLongToast(int resId) {
        showLongToast(getString(resId));
    }

    public void showProgress() {
        try {
            ((BaseActivity)getActivity()).showProgress();
        } finally {

        }
    }

    public void hideProgress() {
        try {
            ((BaseActivity)getActivity()).hideProgress();
        } finally {

        }
    }

    public View findViewById(int id) {
        return mRootView.findViewById(id);
    }

    @Override
    public void onClick(View view) {

    }

    public void startActivity(Class<? extends Activity> clazz) {
        startActivity(new Intent(getActivity(), clazz));
    }

    public void startActivity(Class<? extends Activity> clazz,int requestCode) {
        startActivityForResult(new Intent(getActivity(), clazz), requestCode);
    }

    public Role currentRole() {
        return Role.currentRole();
    }

    public abstract class DefaultCallback<T> extends Callback<T> {
        @Override
        public void onStart() {
            //showProgress();
        }

        @Override
        public void onFinish() {
            //hideProgress();
        }
    }

    public EventEmitter eventEmitter() {
        return FertilizerApplication.getInstance().getEventEmitter();
    }
}
