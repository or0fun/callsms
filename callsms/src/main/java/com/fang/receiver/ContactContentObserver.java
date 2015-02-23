package com.fang.receiver;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;

import com.fang.contact.ContactHelper;
import com.fang.util.DebugLog;

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
			ContactHelper.setReaded(false);
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					ContactHelper.readContact(mContext);
				}
			}).start();
		};
	}

}