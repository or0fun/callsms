package com.fang.receiver;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;

import com.fang.call.CallHelper;
import com.fang.contact.ContactHelper;
import com.fang.common.util.DebugLog;

public class ContactContentObserver extends ContentObserver {

	private Context mContext;

	protected Handler mHandler;

	public ContactContentObserver(Context context, Handler handler) {
		super(handler);
		mContext = context;
	}

	@Override
	public void onChange(boolean selfChange) {
		DebugLog.d("SMSContentObserver", "selfChange");

		if (selfChange) {
            //更新通讯录
			ContactHelper.setReaded(false);
            Intent serviceIntent1 = new Intent(mContext, MainService.class);
            serviceIntent1.putExtra(MainService.TASK, MainService.TASK_TYPE.REFRESH_CONTACTS);
            mContext.startService(serviceIntent1);

            //更新通话记录内存
            CallHelper.setHasRead(false);
            Intent serviceIntent2 = new Intent(mContext, MainService.class);
            serviceIntent2.putExtra(MainService.TASK, MainService.TASK_TYPE.REFRESH_CALL_RECORDS);
            mContext.startService(serviceIntent2);
		};
	}

}