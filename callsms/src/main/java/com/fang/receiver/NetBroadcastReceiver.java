package com.fang.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.fang.net.ServerUtil;
import com.fang.util.NetWorkUtil;

public class NetBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
        	if (NetWorkUtil.isNetworkAvailable(context)) {
        		ServerUtil server = ServerUtil.getInstance(context);
        		server.checkUserID(context);
        		server.checkOffLineData(context);

                Intent mainintent = new Intent(context, MainService.class);
                mainintent.putExtra(MainService.TASK, MainService.TASK_POST_WEATHER_NOTIFICATION);
                context.startService(mainintent);
			}
        }
    }

}
