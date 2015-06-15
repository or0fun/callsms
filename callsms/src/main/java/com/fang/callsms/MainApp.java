package com.fang.callsms;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.fang.background.BackgroundService;
import com.fang.common.CustomConstant;
import com.fang.common.base.Global;
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

        Global.application = this;

        Thread.setDefaultUncaughtExceptionHandler(this);

        //获取渠道号
        try {
            ApplicationInfo appInfo = this.getPackageManager()
                    .getApplicationInfo(getPackageName(),
                            PackageManager.GET_META_DATA);
            String channel = appInfo.metaData.getString("CHANNEL");
            if (!StringUtil.isEmpty(channel)) {
                CustomConstant.sPACKAGE_CHANNEL = channel;
            }
            String debug = appInfo.metaData.getString("DEBUG");
            CustomConstant.DEBUG = Boolean.parseBoolean(appInfo.metaData.getString("DEBUG"));

        } catch (PackageManager.NameNotFoundException e) {
            if (CustomConstant.DEBUG) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (CustomConstant.DEBUG) {
                e.printStackTrace();
            }
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

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent restartIntent = PendingIntent.getActivity(
                getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        //退出程序
        AlarmManager mgr = (AlarmManager)Global.application.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100,
                restartIntent); // 1秒钟后重启应用
        finishActivity();
    }

    /**
     * 关闭Activity列表中的所有Activity*/
    public void finishActivity(){
        //杀死该应用进程
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
