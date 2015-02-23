package com.fang.sms;

import java.util.List;

import android.app.PendingIntent;
import android.content.Context;
import android.database.Cursor;
import android.telephony.SmsManager;

import com.fang.common.CustomConstant;
import com.fang.util.SharedPreferencesHelper;

/**
 * 短信帮助类
 * 
 * @author fang
 * 
 */
public class SMSHelper {

	/**
	 * 获取收件箱最后一条短信的时间
	 * 
	 * @return
	 */
	public static long getLastTime(Context context) {
		Cursor c = context.getContentResolver().query(
				CustomConstant.SMS_INBOX_URI, null, null, null, "date desc");
		long lastTime = 0;
		if (c != null) {
			while (c.moveToNext()) {
				lastTime = c.getLong(c.getColumnIndex("date"));
				break;
			}
			c.close();
		}
		return lastTime;
	}

	/**
	 * 发送短信
	 * 
	 * @param destinationAddress
	 * @param scAddress
	 * @param text
	 * @param sentIntent
	 * @param deliveryIntent
	 */
	public static void sendSMS(List<String> destinationAddress, String text,
			PendingIntent sentIntent, PendingIntent deliveryIntent) {

		if (null != destinationAddress && null != text) {
			SmsManager smsManager = SmsManager.getDefault();
			for (String number : destinationAddress) {
				smsManager.sendTextMessage(number, null, text, sentIntent,
						deliveryIntent);
			}
		}
	}

	/**
	 * 发送短信
	 * 
	 * @param number
	 * @param content
	 */
	public static void sendSMS(String number, String content) {

		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(number, null, content, null, null);
	}

	/**
	 * 获取定时短信信息
	 * 
	 * @return
	 */
	public static List<SendSMSInfo> getSendSMSInfos(Context context) {
		return (List<SendSMSInfo>) SharedPreferencesHelper.getObject(context,
				SharedPreferencesHelper.TIMING_SMS_INFO);
	}

	/**
	 * 获取定时短信信息
	 * 
	 * @return
	 */
	public static void saveSendSMSInfos(Context context,
			List<SendSMSInfo> sendSMSInfoList) {
		SharedPreferencesHelper.setObject(context,
				SharedPreferencesHelper.TIMING_SMS_INFO, sendSMSInfoList);
	}

	/**
	 * 移除闹钟
	 * 
	 * @param requestCode
	 */
	public static void removeSMSInfo(Context context, int requestCode) {
		List<SendSMSInfo> sendSMSInfoList = (List<SendSMSInfo>) SharedPreferencesHelper
				.getObject(context, SharedPreferencesHelper.TIMING_SMS_INFO);
		if (null != sendSMSInfoList) {
			for (SendSMSInfo info : sendSMSInfoList) {
				if (info.getResultCode() == requestCode) {
					sendSMSInfoList.remove(info);
					SharedPreferencesHelper.setObject(context,
							SharedPreferencesHelper.TIMING_SMS_INFO,
							sendSMSInfoList);
				}
			}
		}

	}
}
