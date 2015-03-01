package com.fang.receiver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.fang.call.CallHelper;
import com.fang.callsms.MainActivity;
import com.fang.common.CustomConstant;
import com.fang.contact.ContactHelper;
import com.fang.express.ExpressHelper;
import com.fang.net.ServerUtil;
import com.fang.sms.SendSMSInfo;
import com.fang.speach.SpeachHelper;
import com.fang.util.DebugLog;
import com.fang.util.MessageWhat;
import com.fang.util.NetWorkUtil;
import com.fang.util.NotifycationHelper;
import com.fang.util.StringUtil;
import com.fang.util.Util;

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

    public static int TASK_POST_WEATHER_NOTIFICATION = 1;
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
                postWeatherNotification();
            }
        }, 1000 * 10);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
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
	}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int task = intent.getIntExtra(TASK, 0);
        if (TASK_POST_WEATHER_NOTIFICATION == task) {
            postWeatherNotification();
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
		Uri smsUri = Util.getSmsUriALL();
		Uri contatcUri = Util.getContactUriALL();
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
				Util.registerAlarm(context, intent,
						sendSMSInfo.getResultCode(),
						sendSMSInfo.getTimeInMillis());
			}
		}
	}

    /**
     * 显示天气的常驻通知栏
     */
    private void postWeatherNotification() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (NetWorkUtil.isNetworkAvailable(mContext)) {
                    String weather = NetWorkUtil.getInstance().searchWeather();
                    if (!StringUtil.isEmpty(weather)) {
                        Intent notificationIntent = new Intent(
                                mContext,
                                MainActivity.class);
                        String[] str = weather.split("\\|");
                        Util.showResidentNotification(
                                mContext,
                                NotifycationHelper.WEATHER_ID,
                                str[0],
                                "明天" + str[1],
                                notificationIntent);
                    }
                }
            }
        }).start();
    }
}
