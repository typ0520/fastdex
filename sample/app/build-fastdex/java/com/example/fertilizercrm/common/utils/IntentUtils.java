package com.example.fertilizercrm.common.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

/**
 * 常用的系统调用
 * @author tong
 *
 */
public class IntentUtils {
	/**
	 * 打开网络设置页面
	 * @param context
	 */
	public static void openNetSetting(Context context) {
		Intent intent = new Intent("android.settings.WIRELESS_SETTINGS");
		intent.addCategory("android.intent.category.DEFAULT");
		context.startActivity(intent);
	}
	/**
	 * 安装应用
	 * @param context
	 * @param apkPath apk路径
	 */
	public static void installApk(Context context,String apkPath) {
		installApk(context, new File(apkPath));
	}	
	/**
	 * 安装应用
	 * @param context
	 * @param apkfile apk路径
	 */
	public static void installApk(Context context,File apkfile) {
		Intent intent = new Intent(Intent.ACTION_VIEW);   
		intent.setDataAndType(Uri.fromFile(apkfile), "application/vnd.android.package-archive");   
		context.startActivity(intent);	
	}	
	/**
	 * 拨打电话 显示按键页面
	 * @param context
	 * @param number 电话号码
	 */
	public static void callPhoneDail(Context context,String number) {
		Uri uri = Uri.parse("tel:" + number);
		Intent intent = new Intent(Intent.ACTION_DIAL,uri);
		context.startActivity(intent);
	}
	/**
	 * 直接拨打电话
	 * @param context
	 * @param number 电话号码
	 */
	public static void callPhone(Context context,String number) {
		Uri uri = Uri.parse("tel:" + number);
		Intent intent = new Intent(Intent.ACTION_CALL,uri);
		context.startActivity(intent);
	}

    /**
     * 调用系统发送短信页面
     * @param context
     * @param mobile
     * @param content
     */
    public static void sendMessage(Context context,String mobile,String content) {
        Uri smsToUri = Uri.parse("smsto:" + mobile);
        Intent intent = new Intent( Intent.ACTION_SENDTO,smsToUri);
        intent.putExtra("sms_body", content == null ? "" : content);
        context.startActivity(intent);
    }
	/**
	 * 调用浏览器打开一个网址
	 * @param context
	 * @param url 网址
	 */
	public static void openBrowser(Context context,String url) {
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		intent.setData(Uri.parse(url));
		context.startActivity(intent);
	}
	/**
	 * 发送邮件页面
	 * @param context
	 * @param emailAddr  邮箱地址
	 */
	public static void sendEmail(Context context,String emailAddr) {
		openBrowser(context, emailAddr);
	}

	/**
	 * 回到桌面
	 */
	public static void backToLauncher(Context context) {
		context.startActivity(getBackToLauncherIntent());
	}

    public static Intent getBackToLauncherIntent() {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addCategory(Intent.CATEGORY_HOME);
        return i;
    }
}
