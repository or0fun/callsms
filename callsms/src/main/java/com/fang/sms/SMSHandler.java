package com.fang.sms;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.fang.callsms.MySMSMessage;
import com.fang.util.SharedPreferencesHelper;

public class SMSHandler extends Handler{

	private Context mContext;
	private static SMSHandler mInstance;
	public final static int SHOW_MSG = 1;
	private SMSHandler(Context context) {
		mContext = context;
	}
	public static SMSHandler getInstance(Context context) {
		if (null == mInstance) {
			synchronized (SMSHandler.class) {
				if (null == mInstance) {
					mInstance = new SMSHandler(context);
				}
			}
		}
		mInstance.mContext = context;
		return mInstance;
	}
	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
		case SHOW_MSG:
			if (SharedPreferencesHelper.getBoolean(mContext,
					SharedPreferencesHelper.SETTING_SMS_POPUP, true)) {
				new SMSDialog(mContext, (MySMSMessage) msg.obj).show();
			}
			break;
		default:
			break;
		}
	}

}
