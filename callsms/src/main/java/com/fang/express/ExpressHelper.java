package com.fang.express;

import java.util.List;

import android.content.Context;
import android.content.Intent;

import com.fang.business.BusinessHelper;
import com.fang.logs.LogCode;
import com.fang.logs.LogOperate;
import com.fang.net.IResponseListener;
import com.fang.net.ResponseInfo;
import com.fang.util.DebugLog;
import com.fang.util.NetWorkUtil;
import com.fang.util.NotifycationHelper;
import com.fang.util.SharedPreferencesHelper;
import com.fang.util.Util;

/**
 * 快递帮助类
 * 
 * @author fang
 * 
 */
public class ExpressHelper {

	private static final String TAG = "ExpressHelper";
	
	public static final String NOTIFY_TITLE = "快递追踪";

	public static final String[] COMPANY_NAMES = { "请选择快递公司", "顺丰快递", "圆通快递",
			"韵达快递", "全峰快递", "联邦快递", "申通快递", "优速快递", "汇通快递", "如风达快递", "UPS快递",
			"天天快递", "速尔快递", "EMS快递", "宅急送"

	};

	/**
	 * 获取快递信息
	 * 
	 * @return
	 */
	public static List<ExpressInfo> getExpressInfos(Context context) {
		return (List<ExpressInfo>) SharedPreferencesHelper.getObject(context,
				SharedPreferencesHelper.EXPRESS_LIST);
	}

	/**
	 * 保存快递信息
	 * 
	 * @return
	 */
	public static void saveExpressInfos(Context context,
			List<ExpressInfo> infoList) {
		SharedPreferencesHelper.setObject(context,
				SharedPreferencesHelper.EXPRESS_LIST, infoList);
	}

	/**
	 * 查询快递信息
	 */
	public static void checkExpressInfo(final Context context) {
		if (false == NetWorkUtil.isNetworkAvailable(context)) {
			return;
		}
		new Thread() {
			@Override
			public void run() {
				super.run();
				final List<ExpressInfo> infoList = getExpressInfos(context);
				if (null == infoList) {
					DebugLog.d(TAG, "infoList is null");
					return;
				}
				for (final ExpressInfo expressInfo : infoList) {
					BusinessHelper.getExpressInfo(context, expressInfo,
							new IResponseListener() {
								@Override
								public void onResult(ResponseInfo info) {
									if (null != info
											&& info instanceof ExpressInfo) {
										ExpressInfo responseInfo = (ExpressInfo)info;
										if (expressInfo.isChanged()) {

											if (SharedPreferencesHelper
													.getBoolean(
															context,
															SharedPreferencesHelper.SETTING_EXPRESS_TRACK,
															true)) {

												expressInfo.setChanged(false);
												expressInfo.setInfo(responseInfo
														.getInfo());
												String[] sentences = responseInfo
														.getInfo().split("\n");
												String content = "";
												if (sentences.length > 1) {
													content = sentences[0]
															+ " "
															+ sentences[1];
												} else {
													content = sentences[0];
												}
												Intent notificationIntent = new Intent(
														context,
														ExpressListActivity.class);
												Util.showNotification(
														context,
														NotifycationHelper.EXPRESS_ID,
														NOTIFY_TITLE
																+ "-"
																+ expressInfo
																		.getCompany(),
														content,
														notificationIntent);
												// 日志上传
												LogOperate.updateLog(context, LogCode.NOTIFY_EXPRESS_CHANGED);
											}
										}
									}
								}
							});
				}
				// 保存
				saveExpressInfos(context, infoList);
			}
		}.start();
	}
}
