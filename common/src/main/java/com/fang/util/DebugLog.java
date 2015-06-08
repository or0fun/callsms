package com.fang.util;

import android.util.Log;

import com.fang.common.CustomConstant;


/**
 * 日志统一
 * @author fang
 *
 */
public class DebugLog {
	/** log 开头 */
	private static final String LOG_HEAD = "FANG_";

	public static void d(String tag, String msg) {
		if (CustomConstant.DEBUG) {
			Log.d(LOG_HEAD + tag, msg);
		}
	}

	public static void e(String tag, String msg) {
		if (CustomConstant.DEBUG) {
			Log.e(LOG_HEAD + tag, msg);
		}
	}

	public static void i(String tag, String msg) {
		if (CustomConstant.DEBUG) {
			Log.i(LOG_HEAD + tag, msg);
		}
	}

	public static void v(String tag, String msg) {
		if (CustomConstant.DEBUG) {
			Log.v(LOG_HEAD + tag, msg);
		}
	}
}
