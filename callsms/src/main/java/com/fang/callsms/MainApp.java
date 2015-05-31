package com.fang.callsms;

import android.app.Application;
import android.content.Intent;

import com.fang.background.BackgroundService;
import com.fang.receiver.MainService;

/**
 * Created by fang on 3/2/15.
 */
public class MainApp extends Application implements Thread.UncaughtExceptionHandler{
    @Override
    public void onCreate() {
        super.onCreate();

        this.startService(new Intent(this, MainService.class));
        this.startService(new Intent(this, BackgroundService.class));
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
    }
}
