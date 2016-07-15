package com.example.fertilizercrm.common.httpclient.expand;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import org.apache.http.Header;

import java.io.File;

import com.example.fertilizercrm.common.httpclient.AsyncHttpClient;
import com.example.fertilizercrm.common.httpclient.FileAsyncHttpResponseHandler;
import com.example.fertilizercrm.common.httpclient.TextHttpResponseHandler;
import com.example.fertilizercrm.common.utils.DeviceUtil;
import com.example.fertilizercrm.common.utils.IntentUtils;

/**
 * 应用更新处理器，子类需要实现比较版本的compare方法,以及实现getAppDownloadUrl方法把最新版本的下载地址返回过来；
 * 如果获取最新版本号和获取最新版本的下载地址的接口不是同一个，可以直接在getAppDownloadUrl进行网络访问操作；
 * 如果当前版本不是最新的，默认弹出对话框让用户选择是否下载，如果不想弹出对话直接开始下载，则覆盖ifShowAskDownloadDialog方法，返回false，
 * 如果觉的默认弹出的询问是否下载的对话框不好看，可以覆盖getAskDownLoadDialog方法，把自定义的dialog返回过来，如果使用自定义dialog必须在此dialog的确定按钮的
 * 处理逻辑中调用downloadApk()方法,以完成后面的下载操作；和下载类似，当下载完成后默认弹出对话框让用户选择是否安装，如果不想弹出对话直接开始安装，
 * 则覆盖ifShowAskInstallDialog方法，返回false,如果觉的默认弹出的询问是否安装的对话框不好看，可以覆盖getAskInstallDialog方法，把自定义的dialog返回过来，
 * 如果使用自定义dialog必须在此dialog的确定按钮的处理逻辑中调用installApk()方法,以完成后面的安装操作
 * @author tong
 */
public abstract class AppUpdateHttpResponseHandler extends TextHttpResponseHandler {
	private static final int GET_DOWNLOAD_URL_COMPLETE = 1;
	protected Context mContext;
	/*
	 * 下载完成后的apk保存路径
	 */
	protected File mApkFile;
	private String mResponse;
	private boolean success;
    private ProgressDialog mProgressDialog;
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GET_DOWNLOAD_URL_COMPLETE:
				String url = (String) msg.obj;
				if (url != null) {
					AsyncHttpClient client = new AsyncHttpClient();
					client.get(url, new FileAsyncHttpResponseHandler(mApkFile) {
						public void onStart() {
							onStartDownload();
						};
						@Override
						public void onSuccess(int statusCode, Header[] headers, File file) {
							success = true;
							onDownloadSuccess(statusCode, headers, file);
							if (ifInstall(mApkFile)) {
								if (!ifShowAskInstallDialog()) {
									installApk();
								} else {
									showAskInstallDialog(mContext);
								}
							}
						}
						@Override
						public void onFailure(int statusCode, Header[] headers,
								Throwable throwable, File file) {
							success = false;
							onDownloadFailure(statusCode, headers, throwable, file);
						}
						@Override
						public void onProgress(int bytesWritten, int totalSize) {
							onDownloadProgress(bytesWritten, totalSize);
						}
						public void onFinish() {
							onFinishDownload();
						};
					});
				}
				break;
			}
		};
	};

	public AppUpdateHttpResponseHandler(Context context,File mApkFile) {
		this.mContext = context;
		this.mApkFile = mApkFile;
        this.mApkFile.delete();
	}

	public Context getContext() {
		return mContext;
	}

	@Override
	public final void onSuccess(int statusCode, Header[] headers,
			String responseString) {
		mResponse = responseString;
		int curVersionCode = DeviceUtil.getCurrentVersionCode(mContext);
		String curVersionName = DeviceUtil.getCurrentVersionName(mContext);
		boolean isOldVersion = compare(responseString, curVersionCode,curVersionName);
		if (isOldVersion && ifDownload(responseString, curVersionCode,curVersionName)) {
			if (!ifShowAskDownloadDialog()) {
				downloadApk();
			} else {
				showAskDownLoadDialog(mContext, responseString);
			}
		}
		onSuccess(statusCode, headers, responseString, !isOldVersion);
	}
	//开始下载
	public void downloadApk() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = getAppDownloadUrl(mResponse);
				Message msg = Message.obtain();
				msg.what = GET_DOWNLOAD_URL_COMPLETE;
				msg.obj = url;
				handler.sendMessage(msg);
			}
		}).start();
	}
	/**
	 * 得到询问是否下载的dialog，子类可以覆盖此方法使用自定义的dialog，但必须在用户点击确定的处理逻辑中调用downloadApk()方法
	 */
	public void showAskDownLoadDialog(Context context, String response) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context)
		.setTitle("提示")
		.setMessage("当前版本过旧，是否更新到最新版本")
		.setPositiveButton("立即更新",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,
					int which) {
				dialog.dismiss();
				// 点击立即更新，则下载最新版本APK
				downloadApk();
			}
		});
		if (!isNeedUpdate(response)) {
			builder.setNegativeButton("稍后", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					
				}
			});
		}
		builder.create().show();
	}
	/**
	 * 得到询问是否下载的dialog，子类可以覆盖此方法使用自定义的dialog，但必须在用户点击确定的处理逻辑中调用doInstall()方法
	 */
	public void showAskInstallDialog(Context mContext) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
		.setTitle("提示")
		.setMessage("下载完毕，是否安装")
		.setPositiveButton("立即安装",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,
					int which) {
				dialog.dismiss();
				installApk();
			}
		});
		if (!isNeedUpdate(mResponse)) {
			builder.setNegativeButton("稍后", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					
				}
			});
		}
		AlertDialog dialog = builder.create();
		dialog.setCancelable(!isNeedUpdate(mResponse));
		dialog.show();
	}
	/**
	 * 是否为强制更新
	 * @return
	 */
	public boolean isNeedUpdate(String response) {
		return true;
	}
	/**
	 * 是否显示  询问是否开始下载的对话框
	 * @return true显示，反之不显示  直接开始下载
	 */
	public boolean ifShowAskDownloadDialog() {
		return true;
	}
	/**
	 * 是否显示  询问是否开始安装的对话框
	 * @return true显示，反之不显示  直接开始下载
	 */
	public boolean ifShowAskInstallDialog() {
		return true;
	}
	/**
	 * 如果当前应用的版本不是最新的，调用此方法决定是否下载
	 * @param response 服务器端返回的数据
	 * @param curVersionCode 当前app版本号
	 * @param curVersionName  当前app版本名称
	 * @return
	 */
	public boolean ifDownload(String response,int curVersionCode, String curVersionName) {
		return true;
	}

	/**
	 * 下载完成后，调用此方法决定是否进行安装
	 * @param apkFile
	 * @return
	 */
	public boolean ifInstall(File apkFile) {
		return true;
	}
	
	/**
	 * apk下载成功
	 * @param statusCode
	 * @param headers
	 * @param file
	 */
	public void onDownloadSuccess(int statusCode, Header[] headers, File file) {
		
	}
	/**
	 * 安装下载完毕的最新版本
	 */
	public void installApk() {
		if (success) {			
			IntentUtils.installApk(mContext, mApkFile.getAbsolutePath());
		}
	}
	/**
	 * 下载apk的进度
	 * @param bytesWritten 已接收的大小
	 * @param totalSize apk总大小
	 */
	public void onDownloadProgress(int bytesWritten, int totalSize) {
        mProgressDialog.setMax(totalSize);
        mProgressDialog.setProgress(bytesWritten);
	}
	/**
	 * 当开始下载apk时的回调
	 */
	public void onStartDownload() {
        mProgressDialog = getProgressDialog();
		mProgressDialog.setCancelable(false);
        mProgressDialog.show();
	}
	/**
	 * 当结束下载apk时的回调
	 */
	public void onFinishDownload() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}

    /**
     * 获取进度条dialog
     * @return
     */
    public ProgressDialog getProgressDialog() {
        ProgressDialog dialog =  new ProgressDialog(mContext);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setTitle("正在下载");
        dialog.setProgress(59);
        dialog.setIndeterminate(false);
        return dialog;
    }

	/**
	 * 应用信息获取成功
	 * @param statusCode
	 * @param headers
	 * @param response
	 * @param latest 当前应用版本是不是最新的  比较逻辑有compare方法完成
	 */
     public void onSuccess(int statusCode, Header[] headers,String response,boolean latest) {
         if (latest) {
             //Toast.makeText(mContext,"已是最新版本",Toast.LENGTH_LONG).show();
         }
     }


    @Override
    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
        //Toast.makeText(mContext,"网络异常",Toast.LENGTH_LONG).show();
    }

    /**
	 * apk下载失败
	 * @param statusCode
	 * @param headers
	 * @param throwable
	 * @param file
	 */
     public void onDownloadFailure(int statusCode, Header[] headers,Throwable throwable, File file) {
         Toast.makeText(mContext, "下载失败", Toast.LENGTH_LONG).show();
     }
	/**
	 * 比较当前app版本与服务器上的此app版本
	 * @param response 服务器返回的数据
	 * @param curVersionCode 当前app版本
	 * @return 如果当前app不是最新的返回true，反之false
	 */
	abstract public boolean compare(String response,int curVersionCode,String curVersionName);
	/**
	 * 得到服务器端当前应用的最新版本的下载地址，如果当前应用不是最新版本，就可以根据这个url地址去下载最新版本
	 * 注意：此方法是在子线程中调用的，可以直接执行网络访问相关的操作，但不要执行与ui界面相关的操作
	 * @param response 服务器端返回的数据
	 * @return
	 */
	abstract public String getAppDownloadUrl(String response);
}
