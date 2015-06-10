package com.fang.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.fang.logs.LogOperate;
import com.fang.net.ServerUtil;
import com.fang.util.NetWorkUtil;

public class NetBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
        	if (NetWorkUtil.isNetworkConnected(context)) {
                if (NetWorkUtil.isWifiConnected(context)) {
                    ServerUtil server = ServerUtil.getInstance(context);
                    server.checkUserID(context);
                    server.checkOffLineData(context);

                    LogOperate.uploadCrashLog(context);
                }

                Intent mainintent = new Intent(context, MainService.class);
                mainintent.putExtra(MainService.TASK, MainService.TASK_TYPE.POST_WEATHER_NOTIFICATION.ordinal());
                context.startService(mainintent);
			}
        }
    }

}
