package com.fang.util;

import android.util.Log;

/**
 * 日志统一
 * @author fang
 *
 */
public class DebugLog {
	/** log 开头 */
	private static final String LOG_HEAD = "FANG_";
	/** log 开关 */
	private static boolean LOG_SWITCH = true;

	public static void d(String tag, String msg) {
		if (LOG_SWITCH) {
			Log.d(LOG_HEAD + tag, msg);
		}
	}

	public static void e(String tag, String msg) {
		if (LOG_SWITCH) {
			Log.e(LOG_HEAD + tag, msg);
		}
	}

	public static void i(String tag, String msg) {
		if (LOG_SWITCH) {
			Log.i(LOG_HEAD + tag, msg);
		}
	}

	public static void v(String tag, String msg) {
		if (LOG_SWITCH) {
			Log.v(LOG_HEAD + tag, msg);
		}
	}
}
