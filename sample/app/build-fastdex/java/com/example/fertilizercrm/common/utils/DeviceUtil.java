package com.example.fertilizercrm.common.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.CallLog;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 手机信息工具类 主要包括手机硬件信息和软件信息 通过接口方法调用就可以获取到安装应用的当前手机的基本信息
 * 
 * @author xuekun
 * 
 */
public class DeviceUtil {
	private static final String TAG = DeviceUtil.class.getSimpleName();

	/**
	 * 获取手机分辨率
	 * 
	 * @param activity
	 *            继承Activity的都可以
	 * @return 宽和高的数组 单位为px 注：返回int数组中，第一个值为宽度，第二个参数为高度
	 */
	public static int[] getPhoneResolution(Activity activity) {
		int[] result = new int[2];
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		result[0] = dm.widthPixels;
		result[1] = dm.heightPixels;
		return result;
	}
	
	/**
	 * 获取屏幕高度
	 * @param context
	 * @return
	 */
	public static int getWindowHeight(Context context) {
		WindowManager windowManager = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
		return windowManager.getDefaultDisplay().getHeight();
	}
	/**
	 * 获取屏幕宽度
	 * @param context
	 * @return
	 */
	public static int getWindowWidth(Context context) {
		WindowManager windowManager = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
		return windowManager.getDefaultDisplay().getWidth();
	}

	/**
	 * 获取手机唯一编号——IMEI
	 * 
	 * @param context
	 *            Activity或者应用的上下文
	 * @return 获取手机的唯一编号，如果获取直接返回，如果获取不到返回null
	 */
	public static String getDeviceId(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String phoneImei = telephonyManager.getDeviceId();
		if (!TextUtils.isEmpty(phoneImei)) {
			// 如果存在，直接返回
			return phoneImei;
		} else {
			return "";
		}
	}

	/**
	 * 获取手机的mac地址
	 * 
	 * @param context
	 *            Activity的上下文或者应用程序的上下文
	 * @return 获取正确直接返回，获取不到时返回null
	 */
	public static String getMacInfo(Context context) {
		String result = null;
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = (wifiManager == null ? null : wifiManager
				.getConnectionInfo());
		if (info != null) {
			result = info.getMacAddress();
		}
		return result;
	}

	/**
	 * 获取终端设备的CPU信息
	 * 
	 * @return 返回CPU的基本信息字符串
	 */
	public static String getCpuInfo() {
		String str = null;
		FileReader localFileReader = null;
		BufferedReader localBufferedReader = null;
		try {
			localFileReader = new FileReader("/proc/cpuinfo");
			if (localFileReader != null)
				try {
					localBufferedReader = new BufferedReader(
							localFileReader, 1024);
					str = localBufferedReader.readLine();
					localBufferedReader.close();
					localFileReader.close();
				} catch (IOException localIOException) {
					Log.e(TAG, "Could not read from file /proc/cpuinfo",
							localIOException);
				}
		} catch (FileNotFoundException localFileNotFoundException) {
			Log.e(TAG, "Could not open file /proc/cpuinfo",
					localFileNotFoundException);
		}
		if (str != null) {
			int i = str.indexOf(58) + 1;
			str = str.substring(i);
		}
		return str;
	}

	/**
	 * 实时获取CPU当前频率（单位KHZ）
	 * @return
	 */
	public static  String getCurCpuFreq() {
		String result = "N/A";
		try {
			FileReader fr = new FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
			BufferedReader br = new BufferedReader(fr);
			String text = br.readLine();
			result = text;
			if(result.contains(".")){
				result = result.substring(0,result.indexOf("."));	
			}
			long cpufreq =  Long.parseLong(result);

			cpufreq = cpufreq/1000;
			result =cpufreq+" Mhz";
		} catch (FileNotFoundException e) {
			result ="N/A";
			e.printStackTrace();
		} catch (IOException e) {
			result ="N/A";
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 判断当前SD是否存在
	 * 
	 * @return 返回为boolean值，如果sd卡存在则为true否则为false
	 */
	public static boolean existSDCard() {
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			return true;
		} else
			return false;
	}

	/** =================================================================== **/

	private static Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
	public static final int CHINA_MOBILE = 1;// 中国移动
	public static final int CHINA_TELECOM = 2;// 中国电信
	public static final int CHINA_UNICOM = 3;// 中国联通


	/**
	 * 获取网络状态 2G,3G,WIFI等
	 * 
	 * @param context
	 *            Activity的上下文或者应用程序的上下文
	 * @return 返回值为int类型 取值有1，2，3，4 返回值说明： 1-3G； 2-2G； 3-WIFI; 4-无连接或者其他连接
	 */
	public static int getNetState(Context context) {
		ConnectivityManager connect = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		State mobile = connect.getNetworkInfo(
				ConnectivityManager.TYPE_MOBILE).getState();
		State wifi = connect.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.getState();

		if (wifi == State.CONNECTED || wifi == State.CONNECTING) {
			return 3;// wifi
		}
		if (mobile == State.CONNECTED || mobile == State.CONNECTING) {
			NetworkInfo info = connect.getActiveNetworkInfo();
			if (info != null) {
				if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
					if (info.getSubtype() == TelephonyManager.NETWORK_TYPE_GPRS
							|| info.getSubtype() == TelephonyManager.NETWORK_TYPE_CDMA
							|| info.getSubtype() == TelephonyManager.NETWORK_TYPE_EDGE) {
						return 2;// 2G网络
					}
				} else {
					return 1;// 3g
				}

			}
		}
		return 4;// Other
	}

	/**
	 * 判断wifi是否连接
	 * 
	 * @param context
	 *            Activity的上下文或者应用程序的上下文
	 * @return 返回boolean值，连接为true否则为false
	 */
	public static boolean isWIFIConnection(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (networkInfo != null)
			return networkInfo.isConnected();
		return false;
	}

	/**
	 * 判断gprs是否连接
	 * 
	 * @param context
	 *            Activity的上下文或者应用程序的上下文
	 * @return 返回boolean值，连接为true否则为false
	 */
	public static boolean isMobileConnection(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (networkInfo != null)
			return networkInfo.isConnected();
		return false;

	}

	/**
	 * 判断网络是否已连接
	 * 
	 * @param context
	 *            Activity的上下文或者应用程序的上下文
	 * @return 返回值为boolean型 连接返回true，否则返回false
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			Log.i(TAG, "newwork is off");
			return false;
		} else {
			NetworkInfo info = connectivity.getActiveNetworkInfo();
			if (info == null) {
				Log.i(TAG, "newwork is off");
				return false;
			} else {
				if (info.isAvailable()) {
					Log.i(TAG, "newwork is on");
					return true;
				}
			}
		}
		Log.i(TAG, "newwork is off");
		return false;
	}

	/**
	 * 获取当前的手机号
	 * 
	 * @param context
	 *            Activity的上下文或者应用程序的上下文
	 * @return 返回值为手机号码，如果获取成功返回手机号码，获取失败则返回null
	 */
	public static String getPhoneNumber(Context context) {
		TelephonyManager tManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String number = tManager.getLine1Number();
		if (TextUtils.isEmpty(number)) {
			return null;
		}
		Log.e(TAG, "所获得的手机号：" + number);
		return number;
	}

	/**
	 * 得到应用发布渠道
	 * 
	 * @param mContext
	 *            Activity的上下文或者应用程序的上下文
	 */
	public static String getChannel(Context mContext) {
		String tempChannel = "Unknown";
		try {
			PackageManager localPackageManager = mContext
					.getPackageManager();
			ApplicationInfo localApplicationInfo = localPackageManager
					.getApplicationInfo(mContext.getPackageName(), 128);
			if ((localApplicationInfo != null)
					&& (localApplicationInfo.metaData != null)) {
				String str = localApplicationInfo.metaData
						.getString("CMCCSTATS _CHANNEL");
				if (str != null)
					tempChannel = str;
				else
					Log.i(TAG,
							"Could not read CMCCSTATS _CHANNEL meta-data from AndroidManifest.xml.");
			}
		} catch (Exception localException) {
			Log.i(TAG,
					"Could not read CMCCSTATS _CHANNEL meta-data from AndroidManifest.xml.",
					localException);
		}
		return tempChannel;
	}

	/**
	 * 卡槽类型-仅支持单卡
	 * @param mContext Activity的上下文或者应用程序的上下文
	 * @return
	 */
	public static String getCarrier(Context mContext) {
		TelephonyManager localTelephonyManager = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		return localTelephonyManager.getNetworkOperatorName();
	}

	/**
	 * 获取国家标识
	 * @param mContext Activity的上下文或者应用程序的上下文
	 * @return 获取配置国家标识，默认为中国
	 */
	public static String getCountry(Context mContext) {
		Configuration curConfiguration = mContext.getResources()
				.getConfiguration();
		if ((curConfiguration != null) && (curConfiguration.locale != null)) {
			return curConfiguration.locale.getCountry();
		}
		return "CN";
	}

	/**
	 * 获取运行商国家码信息
	 * @param context Activity的上下文或者应用程序的上下文
	 * @return 获取sim卡或者uim卡的国家标识，中国返回CN，其他返回标识码，未能获取返回null
	 */
	public static String getCountryCode(Context context) {
		String countryCode = null;
		String imsi = null;
		if (imsi == null) {
			TelephonyManager telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			imsi = telephonyManager.getSubscriberId();
		}
		if (imsi != null && !"".equals(imsi)) {
			String strCountryCode = imsi.substring(0, 3);
			int code = Integer.parseInt(strCountryCode);
			if (code == 460) {
				countryCode = "CN";
			} else {
				countryCode = Integer.toString(code);
			}
		} else {
			Log.i(TAG, "未能获取国家码，可能未装sim卡或者权限不允许");
		}
		return countryCode;
	}

	/**
	 * 获得当前手机运营商
	 * 
	 * @param context
	 *            Activity的上下文或者应用程序的上下文
	 * @return -1 无法获取 1:中国移动 2:中国电信 3:中国联通
	 */
	public static int getNetProdiver(Context context) {
		int result = -1;
		String imsi = null;
		if (imsi == null) {
			TelephonyManager telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			imsi = telephonyManager.getSubscriberId();
		}
		if (imsi != null && !"".equals(imsi)) {
			if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
				result = CHINA_MOBILE;
			} else if (imsi.startsWith("46001")) {
				result = CHINA_UNICOM;
			} else if (imsi.startsWith("46003")) {
				result = CHINA_TELECOM;
			}
		}
		return result;
	}

	/**
	 * 是否飞行模式  
	 * @param context Activity的上下文或者应用程序的上下文
	 * @return 返回值为boolean型 飞行模式返回true，否则返回false
	 */
	public static boolean isAirplaneModeOn(Context context) {  
		return Settings.System.getInt(context.getContentResolver(),  
				Settings.System.AIRPLANE_MODE_ON, 0) != 0;  
	} 

	/**
	 * 获取内核版本
	 * @return 返回手机内核版本号信息，类型为String
	 */
	public static String  getKernelVersion(){
		String  phoneProcess = "";
		Process process = null;
		try {
			process = Runtime.getRuntime().exec("cat /proc/version");

		} catch (IOException e) {
			phoneProcess = "N/A";
		}
		InputStream outs = process.getInputStream();
		InputStreamReader isrout = new InputStreamReader(outs);
		BufferedReader brout = new BufferedReader(isrout,8*1024);
		String result = "";
		String line;
		try {
			while ( (line = brout.readLine()) != null) {
				result += line;
			}
		} catch (IOException e) {
			phoneProcess = "N/A";
		}
		if( result != "" ) {
			String Keyword = "version ";
			int index = result.indexOf(Keyword);
			line = result.substring(index + Keyword.length());
			index = line.indexOf(" ");
			phoneProcess = line.substring(0,line.length());
		}
		return phoneProcess;
	}

	/** 
	 * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的 
	 * @param context 
	 * @return true 表示开启 
	 */  
	public static boolean isGpsOpen(final Context context) {  
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);  
		// 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）  
		boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (gps ) {  
			return true;  
		}  
		return false;  
	} 

	/**
	 * 获得当前安卓系统版本
	 * 
	 * @return 返回手机android的当前版本，类型为String
	 */
	public static String getSystemVersion() {
		return android.os.Build.VERSION.RELEASE;
	}

	/**
	 * 获得通话记录
	 * 
	 * @param context
	 */
	public static void printCallLog(Context context) {

		String str = "";
		String str2 = "";
		int type;
		Date date;
		String time = "";
		ContentResolver cr = context.getContentResolver();
		final Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI, new String[] {
				CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME, CallLog.Calls.TYPE,
				CallLog.Calls.DATE
		}, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			str = cursor.getString(0);// 电话好吗
			str2 = cursor.getString(1);// 名字
			type = cursor.getInt(2);// 1，打进来的电话。2， 拨打出去的，3，未接电话
			SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			date = new Date(Long.parseLong(cursor.getString(3)));
			time = sfd.format(date);
			Log.e(TAG, "str:" + str + " , str2:" + str2 + " , type:" + type + " , time:" + time);
		}
	}

	/**
	 * 隐藏软键盘
	 */
	public static void hideSoftInputMode(Context context, View windowToken) {
		((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
		.hideSoftInputFromWindow(windowToken.getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	/**
	 * 返回当前系统里面有多少个进程正在运行
	 * @param context
	 * @return
	 */
	public static int getRunningProcessCount(Context context){
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> infos = am.getRunningAppProcesses();
		return infos.size();
	}
	/**
	 * 获取手机的可用内存
	 * @param context
	 * @return
	 */
	public static long getAvailMemory(Context context){
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo outInfo = new MemoryInfo();
		am.getMemoryInfo(outInfo);
		return  outInfo.availMem; //byte
	}
	/**
	 * 获取手机的总内存
	 * @param context
	 * @return long byte
	 */
	public static long getTotalMemory(Context context){
		try {
			File file = new File("/proc/meminfo");
			FileInputStream fis = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String info = br.readLine();
			//MemTotal:         513000 kB
			StringBuffer sb = new StringBuffer();
			char[] chararray = info.toCharArray();
			for(char c : chararray){
				if(c>='0'&&c<='9'){
					sb.append(c);
				}
			}
			return Integer.parseInt(sb.toString()) * 1024;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * 获取当前App的VersionCode
	 * @return versionCode   0获取失败
	 */
	public static int getCurrentVersionCode(Context context) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return info.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return 0;
	}
	/**
	 * 获取当前App的VersionName
	 * @return 
	 */
	public static String getCurrentVersionName(Context context) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return  info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 
	 * @param context Context上下文
	 * @param apkPath apk在SD中的路径
	 * @return
	 */
	public static Drawable getApkIcon(Context context, String apkPath) {

		PackageManager pm = context.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(apkPath,
				PackageManager.GET_ACTIVITIES);
		if (info != null) {
			ApplicationInfo appInfo = info.applicationInfo;
			appInfo.sourceDir=apkPath;
			return appInfo.loadIcon(pm);
		}
		return null;
	}
}
