package com.fang.receiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;

import com.fang.call.CallDialog;
import com.fang.call.CallHelper;
import com.fang.call.CallRecordDialog;
import com.fang.callsms.R;
import com.fang.contact.ContactHelper;
import com.fang.listener.IPhoneStateListener;
import com.fang.logs.LogCode;
import com.fang.logs.LogOperate;
import com.fang.util.DebugLog;
import com.fang.util.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;
/**
 * 电话广播接听
 * @author fang
 *
 */
public class PhoneReceiver extends BroadcastReceiver {

	private String TAG = "PhoneReceiver";
	
	final String RINGING = "RINGING";
	final String OFFHOOK = "OFFHOOK";
	final String IDLE = "IDLE";
	
	public static final String ACTION_REMOVE = "com.fang.action.remove";

	public static final int INCOMING_CALL = 0;
	public static final int OUTGOING_CALL = 1;

	//电话状态
	public static final int CALL_STATE_RINGING = 0;
	public static final int CALL_STATE_OFFHOOK = 1;
	public static final int CALL_STATE_IDLE = 2;
	public static final int CALL_STATE_OUTGOING = 3;

	protected final int REMOVE_MSG = 2;
	protected final int MISSED_CALL_SHOW_MSG = 3;
	protected final int OUTGOING_SHOW_MSG = 4;

	protected static int callType = -1;
	protected static CallDialog mCallDialog;
	protected static CallRecordDialog mMissedCallDialog;
	protected static String mPhoneNumber;
	protected static Context mContext;
	
	protected static List<IPhoneStateListener> mPhoneStateListeners;

	protected Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case INCOMING_CALL:
				if (SharedPreferencesHelper.getBoolean(mContext,
						SharedPreferencesHelper.SETTING_NEW_CALL_POPUP, true)) {
					if (null != mCallDialog) {
						mCallDialog.remove();
						mCallDialog = null;
					}
					mCallDialog = new CallDialog(mContext, (String) msg.obj,
							INCOMING_CALL);
					mCallDialog.show();
					//日志
					LogOperate.updateLog(mContext, LogCode.CALL_INCOMING_DIALOG_SHOW);
				}
				break;
			case OUTGOING_CALL:
				if (SharedPreferencesHelper.getBoolean(mContext,
						SharedPreferencesHelper.SETTING_OUTGOING_CALL_POPUP,
						true)) {
					if (null != mCallDialog) {
						mCallDialog.remove();
						mCallDialog = null;
					}
					mCallDialog = new CallDialog(mContext, (String) msg.obj,
							OUTGOING_CALL);
					mCallDialog.show();
					//日志
					LogOperate.updateLog(mContext, LogCode.CALL_OUTGOING_DIALOG_SHOW);
				}
				break;
			case REMOVE_MSG:
				if (null != mCallDialog) {
					mCallDialog.remove();
					mCallDialog = null;
				}
				break;
			case MISSED_CALL_SHOW_MSG:
				if (SharedPreferencesHelper
						.getBoolean(
								mContext,
								SharedPreferencesHelper.SETTING_MISSED_CALL_POPUP,
								true)) {
					if (null != mMissedCallDialog) {
						mMissedCallDialog.remove();
						mMissedCallDialog = null;
					}
					String number = (String) msg.obj;
					mMissedCallDialog = new CallRecordDialog(mContext,
							number, ContactHelper.getPerson(mContext, number), 
							mContext.getString(R.string.record_missed),
							R.drawable.missed_type, true);

					mMissedCallDialog.show();
					//日志
					LogOperate.updateLog(mContext, LogCode.CALL_MISSED_DIALOG_SHOW);
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	public static void addListener(IPhoneStateListener phoneStateListener) {
		if (null == mPhoneStateListeners) {
			mPhoneStateListeners = new ArrayList<IPhoneStateListener>();
		}
		mPhoneStateListeners.add(phoneStateListener);
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		
		if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			//去电
			mPhoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
			DebugLog.d(TAG, mPhoneNumber);
			DebugLog.d(TAG, "CALL_STATE_OUTGOING");
			callType = CALL_STATE_OUTGOING;
			myHandler.sendMessage(myHandler.obtainMessage(OUTGOING_CALL,
					mPhoneNumber));

		} else if (intent.getAction().equals(ACTION_REMOVE)) {
			myHandler.sendEmptyMessage(REMOVE_MSG);
		}
		else if(intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)){
			TelephonyManager tm = (TelephonyManager) context
					.getSystemService(Service.TELEPHONY_SERVICE);
			
			int state = tm.getCallState();

			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE: // 空闲
				DebugLog.d(TAG, "CALL_STATE_IDLE");
				myHandler.sendEmptyMessage(REMOVE_MSG);
				if (callType == CALL_STATE_RINGING) {
					//if (CallHelper.isMissedCall(context, mPhoneNumber)) {
						myHandler.sendMessage(myHandler.obtainMessage(
								MISSED_CALL_SHOW_MSG, mPhoneNumber));
					//}
				}
				callType = CALL_STATE_IDLE;
				
				break;
			case TelephonyManager.CALL_STATE_RINGING: // 来电
				DebugLog.d(TAG, "CALL_STATE_RINGING");
				callType = CALL_STATE_RINGING;
				mPhoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
				myHandler.sendMessage(myHandler.obtainMessage(INCOMING_CALL,
						mPhoneNumber));
				
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK: // 摘机（正在通话中）
				DebugLog.d(TAG, "CALL_STATE_OFFHOOK");
				callType = CALL_STATE_OFFHOOK;
				
				break;
			}
		}
		if (null != mPhoneStateListeners) {
			for (IPhoneStateListener phoneStateListener : mPhoneStateListeners) {
				phoneStateListener.onResult(callType, mPhoneNumber);
			}
		}
		//更新通话记录内存
		CallHelper.setHasRead(false);
        Intent serviceIntent = new Intent(mContext, MainService.class);
        serviceIntent.putExtra(MainService.TASK, MainService.TASK_TYPE.REFRESH_CALL_RECORDS);
        mContext.startService(serviceIntent);
	}
}
