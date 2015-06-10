package com.fang.receiver;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;

import com.fang.callsms.MySMSMessage;
import com.fang.common.CustomConstant;
import com.fang.sms.SMSHandler;
import com.fang.sms.SMSHelper;
import com.fang.common.util.DebugLog;
import com.fang.util.SharedPreferencesHelper;

public class SMSContentObserver extends ContentObserver {

	private Context mContext;
	protected final int SHOW_MSG = 1;
	private long lastTime = 0;
	public final String[] smsProjection = new String[] { "address", "person",
			"body", "date", "type", "read" };

	protected Handler mHandler;

	public SMSContentObserver(Context context, Handler handler) {
		super(handler);
		mContext = context;
		SharedPreferencesHelper.getInstance().setLong(
				SharedPreferencesHelper.SENT_SMS_LAST_TIME, SMSHelper.getLastTime(mContext));
		mHandler = SMSHandler.getInstance(context);
	}

	@Override
	public void onChange(boolean selfChange) {
		DebugLog.d("SMSContentObserver", "selfChange");
		lastTime = SharedPreferencesHelper.getInstance().getLong(
				SharedPreferencesHelper.SENT_SMS_LAST_TIME, SMSHelper.getLastTime(mContext) - 1);

		Cursor cusor = mContext.getContentResolver().query(CustomConstant.SMS_INBOX_URI, null,
				null, null, "date desc");
		if (cusor != null) {
			long smsTime = 0;
			long maxTime = 0;
			while (cusor.moveToNext()) {
				smsTime = cusor.getLong(cusor.getColumnIndex("date"));
				if (smsTime > lastTime) {
					if (smsTime > maxTime) {
						maxTime = smsTime;
					}
					mHandler
							.sendMessage(mHandler.obtainMessage(
									SHOW_MSG,
									new MySMSMessage(cusor.getString(cusor
											.getColumnIndex("address")), cusor
											.getString(cusor
													.getColumnIndex("body")),
											cusor.getString(cusor
													.getColumnIndex("date")),
											cusor.getInt(cusor
													.getColumnIndex("_id")))));

				} else {
					break;
				}
			}
			cusor.close();
			if (maxTime > lastTime) {
				SharedPreferencesHelper.getInstance().setLong(
						SharedPreferencesHelper.SENT_SMS_LAST_TIME, maxTime);
			}
		}
	}

}
