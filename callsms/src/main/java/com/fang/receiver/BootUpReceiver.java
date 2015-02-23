package com.fang.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootUpReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// start service
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		context.startService(new Intent(context, MainService.class));
	}
}
