package com.fang.callsms;

import android.app.Application;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.fang.background.BackgroundService;
import com.fang.common.CustomConstant;
import com.fang.receiver.MainService;
import com.fang.util.SharedPreferencesHelper;

/**
 * Created by fang on 3/2/15.
 */
public class MainApp extends Application implements Thread.UncaughtExceptionHandler{
    @Override
    public void onCreate() {
        super.onCreate();

        //获取渠道号
        try {
            ApplicationInfo appInfo = this.getPackageManager()
                    .getApplicationInfo(getPackageName(),
                            PackageManager.GET_META_DATA);
            CustomConstant.sPACKAGE_CHANNEL = appInfo.metaData.getString("channel");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        com.fang.util.SharedPreferencesHelper.getInstance().init(this);
        com.fang.util.SharedPreferencesHelper.getInstance().init(this);

        this.startService(new Intent(this, MainService.class));
        this.startService(new Intent(this, BackgroundService.class));
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
    }
}
