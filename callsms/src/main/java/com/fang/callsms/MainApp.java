package com.fang.callsms;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.fang.background.BackgroundService;
import com.fang.common.CustomConstant;
import com.fang.common.base.Global;
import com.fang.common.util.BaseUtil;
import com.fang.common.util.MapUtil;
import com.fang.map.BDMapListener;
import com.fang.receiver.MainService;
import com.fang.util.SharedPreferencesHelper;
import com.fang.weixin.WXCommon;
import com.squareup.leakcanary.LeakCanary;

import java.util.concurrent.Executors;

/**
 * Created by fang on 3/2/15.
 */
public class MainApp extends Application implements Thread.UncaughtExceptionHandler{

    @Override
    public void onCreate() {
        super.onCreate();

        Global.application = this;
        Global.debug = BuildConfig.DEBUG;
        Global.channel = BuildConfig.FLAVOR;
        if (Global.channel.equals("")) {
            Global.channel = "develop";
        }

        Thread.setDefaultUncaughtExceptionHandler(this);

        if (Global.debug) {
            LeakCanary.install(this);
        }

        Global.fixedThreadPool = Executors.newFixedThreadPool(CustomConstant.MAX_THREAD_COUNT);

        WXCommon.init(this);
        com.fang.util.SharedPreferencesHelper.getInstance().init(this);
        com.fang.util.SharedPreferencesHelper.getInstance().init(this);

        MapUtil.init(getApplicationContext(), BDMapListener.getInstance());

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

        SharedPreferencesHelper.getInstance().setBoolean(SharedPreferencesHelper.UPDATE_VERSION, true);

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
