package com.fang.logs;

import android.content.Context;

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
}
