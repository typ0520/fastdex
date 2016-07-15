package com.example.fertilizercrm.common.utils;

import android.util.Log;

/**
 * Logger是一个日志打印类
 */
public class Logger {

	private static boolean debugOn = false;

	protected static final String TAG = "logger";


	public static void d(String message) {
		d(TAG, message);
	}

	public static void e(String message) {
		e(TAG, message);
	}

	public static void d(String tag, String message) {
		if (isDebugOn()) {
			Log.d(tag,message);
		}
	}

	public static void e(String tag, String message) {
		if (isDebugOn()) {
			Log.e(tag,message);
		}
	}

	public static boolean isDebugOn() {
		return debugOn;
	}

	public static void setDebugOn(boolean debugOn) {
		Logger.debugOn = debugOn;
	}
}
