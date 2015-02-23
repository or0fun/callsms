package com.fang.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.telephony.SmsMessage;

import com.fang.callsms.MySMSMessage;
import com.fang.common.CustomConstant;
import com.fang.sms.SMSHandler;
import com.fang.sms.SMSHelper;
import com.fang.util.DebugLog;
import com.fang.util.SharedPreferencesHelper;

public class SMSReceiver extends BroadcastReceiver {
	
	private String TAG = SMSReceiver.class.getSimpleName();
	protected Handler mHandler;
	
	@Override
	public void onReceive(Context context, Intent intent) {

		SmsMessage[] messages = getMessagesFromIntent(intent);
		mHandler = SMSHandler.getInstance(context);

		for (SmsMessage message : messages) {
			DebugLog.d(TAG, message.getOriginatingAddress() + " : " +
			message.getDisplayOriginatingAddress() + " : " +
			message.getDisplayMessageBody() + " : " +
			message.getTimestampMillis());

			String addressString = message.getDisplayOriginatingAddress();
			String bodyString = message.getDisplayMessageBody();
			Cursor cusor = context.getContentResolver().query(CustomConstant.SMS_INBOX_URI, null,
					"address = " + addressString, null, "date desc limit 1");
			if (cusor != null) {
				long smsTime = 0;
				long lastTime = SharedPreferencesHelper.getLong(context,
						SharedPreferencesHelper.SENT_SMS_LAST_TIME, SMSHelper.getLastTime(context) - 1);
				while (cusor.moveToNext()) {
					smsTime = cusor.getLong(cusor.getColumnIndex("date"));
					if (smsTime > lastTime) {
						SharedPreferencesHelper.setLong(context,
								SharedPreferencesHelper.SENT_SMS_LAST_TIME, smsTime);
						int id = cusor.getInt(cusor.getColumnIndex("_id"));
						mHandler.sendMessage(mHandler.obtainMessage(
									SMSHandler.SHOW_MSG,
									new MySMSMessage(
										addressString, 
										bodyString,
										cusor.getString(cusor.getColumnIndex("date")),
										id)));
					}
				}
				cusor.close();
			}
		}
	}

	/**
	 * 从Intent获取Message
	 * 
	 * @param intent
	 * @return
	 */
	public final SmsMessage[] getMessagesFromIntent(Intent intent) {
		Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
		byte[][] pduObjs = new byte[messages.length][];
		for (int i = 0; i < messages.length; i++) {
			pduObjs[i] = (byte[]) messages[i];
		}
		byte[][] pdus = new byte[pduObjs.length][];
		int pduCount = pdus.length;
		SmsMessage[] msgs = new SmsMessage[pduCount];
		for (int i = 0; i < pduCount; i++) {
			pdus[i] = pduObjs[i];
			msgs[i] = SmsMessage.createFromPdu(pdus[i]);
		}
		return msgs;

	}
}
