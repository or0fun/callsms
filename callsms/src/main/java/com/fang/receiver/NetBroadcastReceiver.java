package com.fang.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.fang.common.CustomConstant;
import com.fang.common.base.Global;
import com.fang.logs.LogOperate;
import com.fang.net.ServerUtil;
import com.fang.number.NumberServiceHelper;
import com.fang.util.NetWorkUtil;

public class NetBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
        	if (System.currentTimeMillis() - Global.netBroadcastTime > CustomConstant.QUARTER_HOUR
            && NetWorkUtil.isNetworkConnected(context)) {
                Global.netBroadcastTime = System.currentTimeMillis();
                if (NetWorkUtil.isWifiConnected(context)) {
                    //上传crash日志
                    LogOperate.uploadCrashLog(context);
                    //获取离线号码
                    NumberServiceHelper.getNumberInfo(context);
                }

                ServerUtil server = ServerUtil.getInstance(context);
                server.checkUserID(context);
                server.checkOffLineData(context);

                Intent mainintent = new Intent(context, MainService.class);
                mainintent.putExtra(MainService.TASK, MainService.TASK_TYPE_POST_WEATHER_NOTIFICATION);
                context.startService(mainintent);
			}
        }
    }

}
