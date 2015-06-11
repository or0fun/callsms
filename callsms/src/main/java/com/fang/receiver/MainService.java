package com.fang.receiver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.fang.call.CallHelper;
import com.fang.common.CustomConstant;
import com.fang.common.util.BaseUtil;
import com.fang.common.util.DebugLog;
import com.fang.contact.ContactHelper;
import com.fang.express.ExpressHelper;
import com.fang.logs.LogOperate;
import com.fang.net.ServerUtil;
import com.fang.push.PushHelper;
import com.fang.sms.SendSMSInfo;
import com.fang.speach.SpeachHelper;
import com.fang.util.MessageWhat;
import com.fang.util.NetWorkUtil;
import com.fang.weather.WeatherHelper;

import java.util.List;

/**
 * 后台Service
 * 
 * @author fang
 * 
 */
public class MainService extends Service {

	private final String TAG = "MainService";
	private static Context mContext;
    public static String TASK = "task";

	protected SMSContentObserver mSMSContentObserver;
	protected ContactContentObserver mContactContentObserver;
	/** 定时发送的短信 */
	protected static List<SendSMSInfo> mSendSMSInfoList;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MessageWhat.TIMER_REQUEST_EXPRESS:
				ExpressHelper.checkExpressInfo(mContext);
				mHandler.sendEmptyMessageDelayed(MessageWhat.TIMER_REQUEST_EXPRESS, CustomConstant.QUARTER_HOUR);
				break;
			default:
				DebugLog.d(TAG, "unhandled event:" + msg.what);
			}
		}
	};

//	private Thread mythread = new Thread() {
//		@Override
//		public void run() {
//			// 重新设置广播
//			mSendSMSInfoList = (List<SendSMSInfo>) SharedPreferencesHelper
//					.getObject(mContext,
//							SharedPreferencesHelper.TIMING_SMS_INFO);
//			registerTimingSMS(mContext, mSendSMSInfoList);
//		}
//	};

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		registerContentObservers();
		ServerUtil.getInstance(mContext).checkUserID(mContext);
		SpeachHelper.getInstance(mContext);
//		mythread.start();
		
		mHandler.sendEmptyMessageDelayed(MessageWhat.TIMER_REQUEST_EXPRESS, CustomConstant.QUARTER_HOUR);

        //延时显示天气通知栏
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                WeatherHelper.postWeatherNotification(mContext);
            }
        }, 1000 * 10);

        new Thread(new Runnable() {
            @Override
            public void run() {
                //获取通讯录
                ContactHelper.readContact(mContext);
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //获取通话记录
                CallHelper.getCallRecordsList(mContext);
            }
        }).start();

        //上次crash日志
        if (NetWorkUtil.isNetworkConnected(mContext)) {
            LogOperate.uploadCrashLog(mContext);
        }
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null != intent) {
            int task = intent.getIntExtra(TASK, -1);
            DebugLog.d(TAG, "task = " + task);
            if (TASK_TYPE.POST_WEATHER_NOTIFICATION.ordinal() == task) {
                WeatherHelper.postWeatherNotification(mContext);
            } else if (TASK_TYPE.PUSH_REQUEST.ordinal() == task) {
                PushHelper.getInstance().checkPushRequest(mContext);
            } else if (TASK_TYPE.REFRESH_CALL_RECORDS.ordinal() == task) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //获取通话记录
                        CallHelper.getCallRecordsList(mContext);
                    }
                }).start();
            } else if (TASK_TYPE.REFRESH_CONTACTS.ordinal() == task) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //获取通讯录
                        ContactHelper.readContact(mContext);
                    }
                }).start();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
	public void onDestroy() {
		super.onDestroy();
		unregisterContentObservers();

	}

	/**
	 * 取消监听短信变化
	 */
	private void unregisterContentObservers() {
		getApplicationContext().getContentResolver().unregisterContentObserver(
				mSMSContentObserver);
		getApplicationContext().getContentResolver().unregisterContentObserver(
				mContactContentObserver);
	}

	/**
	 * 监听短信变化
	 */
	private void registerContentObservers() {
		Uri smsUri = BaseUtil.getSmsUriALL();
		Uri contatcUri = BaseUtil.getContactUriALL();
		mSMSContentObserver = new SMSContentObserver(this, null);
		getApplicationContext().getContentResolver().registerContentObserver(
				smsUri, true, mSMSContentObserver);
		mContactContentObserver = new ContactContentObserver(this, null);
		getApplicationContext().getContentResolver().registerContentObserver(
				contatcUri, true, mContactContentObserver);
	}


	/**
	 * 重新注册广播
	 * 
	 * @param infos
	 */
	private void registerTimingSMS(Context context, List<SendSMSInfo> infos) {
		if (null != infos) {
			for (SendSMSInfo sendSMSInfo : infos) {
				Intent intent = new Intent(context, AlarmReceiver.class);
				BaseUtil.registerAlarm(context, intent,
                        sendSMSInfo.getResultCode(),
                        sendSMSInfo.getTimeInMillis());
			}
		}
	}

    /** 任务类型 */
    public enum TASK_TYPE {

        POST_WEATHER_NOTIFICATION,//天气通知栏
        PUSH_REQUEST,//消息推送
        REFRESH_CALL_RECORDS,//更新通话记录
        REFRESH_CONTACTS,//更新通讯录
    }

}
