package com.fang.callsms;

import android.app.Application;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.fang.background.BackgroundService;
import com.fang.common.CustomConstant;
import com.fang.common.util.BaseUtil;
import com.fang.common.util.StringUtil;
import com.fang.receiver.MainService;

/**
 * Created by fang on 3/2/15.
 */
public class MainApp extends Application implements Thread.UncaughtExceptionHandler{
    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(this);

        //获取渠道号
        try {
            ApplicationInfo appInfo = this.getPackageManager()
                    .getApplicationInfo(getPackageName(),
                            PackageManager.GET_META_DATA);
            String channel = appInfo.metaData.getString("channel");
            if (!StringUtil.isEmpty(channel)) {
                CustomConstant.sPACKAGE_CHANNEL = channel;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        com.fang.weixin.Common.init(this);
        com.fang.util.SharedPreferencesHelper.getInstance().init(this);
        com.fang.util.SharedPreferencesHelper.getInstance().init(this);

        this.startService(new Intent(this, MainService.class));
        this.startService(new Intent(this, BackgroundService.class));
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        BaseUtil.addCrashException(ex);
    }
}
