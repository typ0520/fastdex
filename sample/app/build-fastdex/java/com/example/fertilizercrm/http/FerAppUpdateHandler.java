package com.example.fertilizercrm.http;

import android.content.Context;
import android.text.TextUtils;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.bigkoo.svprogresshud.SVProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import com.example.fertilizercrm.common.httpclient.expand.AppUpdateHttpResponseHandler;
import com.example.fertilizercrm.common.utils.Logger;

/**
 * Created by tong on 16/2/3.
 * 肥肥应用更新
 */
public class FerAppUpdateHandler extends AppUpdateHttpResponseHandler {
    private SVProgressHUD mSVProgressHUD;

    public FerAppUpdateHandler(Context context, File mApkFile) {
        super(context, mApkFile);
    }

    @Override
    public boolean compare(String response, int curVersionCode, String curVersionName) {
        //{"body":{},"code":201,"message":"未获取到版本配置信息!"}\
//                        {
//                            body: {
//                                bsoid: 1,
//                                        createDate: "2016-01-24 01:58:14.0",
//                                        creator: "admin",
//                                        memo: "最新版本",
//                                        modifier: "admin",
//                                        modifyDate: "2016-01-24 01:58:14.0",
//                                        status: 0,
//                                        url: "http://pkg.fir.im/e037a8c237f41abc88b4980f8f5f9289ab304e09.apk?attname=app-release.apk_1.0.apk&e=1453575473&token=LOvmia8oXF4xnLh0IdH05XMYpH6ENHNpARlmPc-T:If8mBnOpLo3iEcYOFx8gqdZl4OE=",
//                                        version: "1.1"
//                            },
//                            code: 200,
//                                    message: "null"
//                        }
        try {
            JSONObject obj = new JSONObject(response);
            if (obj.optInt("code") == 200) {
                obj = obj.optJSONObject("body");
                String newVersion = obj.optString("version");
                if (newVersion == null) {
                    newVersion = "";
                }
                String url = obj.optString("url");
                Logger.e("curVersionName: " + curVersionName + " ,newVersion: " + newVersion);
                int result = newVersion.compareTo(curVersionName);
                if (result > 0
                        && !TextUtils.isEmpty(url)) {
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String getAppDownloadUrl(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            if (obj.optInt("code") == 200) {
                obj = obj.optJSONObject("body");
                String url = obj.optString("url");
                Logger.d("url: " + url);
                return url;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void showAskDownLoadDialog(Context context, String response) {
        String memo = "当前版本过旧，是否更新到最新版本";
        try {
            JSONObject obj = new JSONObject(response);
            obj = obj.optJSONObject("body");

            if (!TextUtils.isEmpty(obj.optString("memo"))) {
                memo = obj.optString("memo");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AlertView alertView = new AlertView("提示", memo, null, new String[]{"立即更新"}, null, getContext(),
                AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                downloadApk();
            }
        });
        alertView.setCancelable(true);
        alertView.show();
    }

    @Override
    public void onStartDownload() {
        mSVProgressHUD = new SVProgressHUD(getContext());
        mSVProgressHUD.getProgressBar().setProgress(0);//先重设了进度再显示，避免下次再show会先显示上一次的进度位置所以要先将进度归0
        mSVProgressHUD.showWithProgress("正在下载 " + 0 + "%", SVProgressHUD.SVProgressHUDMaskType.Black);
    }

    @Override
    public void onDownloadProgress(int bytesWritten, int totalSize) {
        int progress = 0;
        try {
            progress = bytesWritten * 100 / totalSize;
        } catch (Throwable e) {

        }

        if (mSVProgressHUD != null
                && mSVProgressHUD.isShowing()
                && mSVProgressHUD.getProgressBar().getMax() != mSVProgressHUD.getProgressBar().getProgress()) {
            mSVProgressHUD.getProgressBar().setProgress(progress);
            mSVProgressHUD.setText("正在下载 " + progress + "%");
        }
    }

    @Override
    public void onFinishDownload() {
        if (mSVProgressHUD != null) {
            try {
                mSVProgressHUD.dismiss();
            } catch (Throwable e) {

            }
        }
    }

    @Override
    public void showAskInstallDialog(Context mContext) {
        AlertView alertView = new AlertView("提示", "下载完毕，请安装", null, new String[]{"立即安装"}, null, getContext(),
                AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                installApk();
            }
        });
        alertView.setCancelable(false);
        alertView.show();
    }
}
