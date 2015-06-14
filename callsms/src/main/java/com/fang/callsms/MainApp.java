package com.fang.callsms;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.fang.background.BackgroundService;
import com.fang.common.base.Global;
import com.fang.common.CustomConstant;
import com.fang.common.util.BaseUtil;
import com.fang.common.util.StringUtil;
import com.fang.receiver.MainService;
import com.fang.weixin.WXCommon;

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

        WXCommon.init(this);
        com.fang.util.SharedPreferencesHelper.getInstance().init(this);
        com.fang.util.SharedPreferencesHelper.getInstance().init(this);

        this.startService(new Intent(this, MainService.class));
        this.startService(new Intent(this, BackgroundService.class));

        WindowManager windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        Global.fullScreeHeight = dm.heightPixels;
        Global.fullScreenWidth = dm.widthPixels;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        BaseUtil.addCrashException(ex);
    }
}
