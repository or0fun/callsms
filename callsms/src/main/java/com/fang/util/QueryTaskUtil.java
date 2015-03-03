package com.fang.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.fang.background.BackgroundService;
import com.fang.receiver.MainService;


/**
 * 后台定时任务，启动前台MainService执行相应代码
 * Created by fang on 2/5/15.
 */
public class QueryTaskUtil {

    /**
     * 开启请求的闹钟任务, 前台的MainService
     * @param context
     * @param task
     * @param intervalMillis
     */
    public static void startTask(Context context, int task, long intervalMillis, long startTime) {
        //获取AlarmManager系统服务
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        manager.setRepeating(AlarmManager.RTC_WAKEUP, startTime,
                intervalMillis, getPendingIntent(context, task));
    }

    /**
     * 开启请求的闹钟任务，后台的BackgroundService
     * @param context
     * @param action
     */
    public static void startTask(Context context, String action, long startTime) {
        //获取AlarmManager系统服务
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        manager.set(AlarmManager.RTC_WAKEUP, startTime, getPendingIntent(context, action));
    }

    /**
     * 停止请求的闹钟任务
     * @param context
     * @param task
     */
    public static void stopTask(Context context, int task) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //取消正在执行的服务
        manager.cancel(getPendingIntent(context, task));
    }

    private static PendingIntent getPendingIntent(Context context, int task) {
        Intent intent = new Intent(context, MainService.class);
        intent.putExtra(MainService.TASK, task);
        return PendingIntent.getService(context, task,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent getPendingIntent(Context context, String action) {
        Intent intent = new Intent(context, BackgroundService.class);
        intent.setAction(action);
        return PendingIntent.getService(context, action.hashCode(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
