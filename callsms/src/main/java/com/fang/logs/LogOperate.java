package com.fang.logs;

import android.content.Context;

import com.fang.common.util.SharedPreferencesHelper;
import com.fang.common.util.StringUtil;
import com.fang.net.NetRequestListener;
import com.fang.net.NetRequestResult;
import com.fang.net.NetRequestResultCode;
import com.fang.net.NetResuestHelper;
import com.fang.net.ServerUtil;

public class LogOperate {

	/**
	 * 上传日志
	 * @param code
	 */
	static public void updateLog(final Context context, final String code) {
		ServerUtil.getInstance(context).request(NetResuestHelper.CODE, code, null);
	}

    /**
     * 上次crash日志
     * @param context
     */
    public static void uploadCrashLog(final Context context) {
        String str = SharedPreferencesHelper.getInstance().getString(SharedPreferencesHelper.CRASH_EXCEPTION, "");
        if (StringUtil.isEmpty(str)) {
            return;
        }
        ServerUtil.getInstance(context).request(NetResuestHelper.CRASH, str, new NetRequestListener() {
            @Override
            public void onResult(NetRequestResult result) {
                if (null != result && NetRequestResultCode.HTTP_OK == result.getResultCode()) {
                    SharedPreferencesHelper.getInstance().setString(SharedPreferencesHelper.CRASH_EXCEPTION, "");
                }
            }
        });
    }

    /**
     * 上次request error日志
     * @param context
     */
    public static void uploadRequestError(final Context context, final String str) {
        ServerUtil.getInstance(context).request(NetResuestHelper.REQUEST_ERROR, str, null);
    }
}
