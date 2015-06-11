package com.fang.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.fang.callsms.MainActivity;
import com.fang.callsms.R;
import com.fang.common.util.StringUtil;
import com.fang.datatype.CallFrom;
import com.fang.datatype.ExtraName;
import com.fang.logs.LogCode;
import com.fang.logs.LogOperate;
import com.fang.webview.WebViewActivity;

/**
 * 通知栏帮助类
 * Created by fang on 3/2/15.
 */
public class NotificationHelper {

    public static final int EXPRESS_ID = 0;
    public static final int WEATHER_ID = 1;
    public static final int PUSH_ID = 2;


    /**
     * 显示消息推送的通知栏
     * @param context
     * @param title
     * @param content
     * @param task
     */
    public static void showPushNotification(Context context, String title, String content, int task, String url) {
        Intent notificationIntent = new Intent();
        if (StringUtil.isEmpty(url)) {
            notificationIntent.setClass(context, MainActivity.class);
        } else {
            notificationIntent.setClass(context, WebViewActivity.class);
        }
        notificationIntent.putExtra(ExtraName.URL, url);
        notificationIntent.putExtra(ExtraName.TASK_ACTION, task);
        notificationIntent.putExtra(ExtraName.CALL_FROM, CallFrom.NOTIFICATION_CLICK);
        showNotification(
                context,
                NotificationHelper.PUSH_ID,
                title,
                content,
                notificationIntent,
                true);
        // 记录日志
        LogOperate.updateLog(context, LogCode.PUSH_REQUEST_RECEIVED);
    }
    /**
     * 显示通知栏
     *
     * @param context
     * @param id
     * @param title
     * @param content
     * @param notificationIntent
     */
    public static void showNotification(Context context, int id, String title,
                                        String content, Intent notificationIntent, boolean quiet) {

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(ns);

        // 定义Notification的各种属性
        int icon = R.drawable.icon;
        long when = System.currentTimeMillis();
        Notification notification = new Notification(icon, content, when);

        if (false == quiet) {
            notification.sound = Uri.parse("android.resource://"
                    + context.getPackageName() + "/" + R.raw.notify);
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        }

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        notification.setLatestEventInfo(context, title, content, contentIntent);

        notificationManager.notify(id, notification);
    }
    /**
     * 显示通知栏
     *
     * @param context
     * @param id
     * @param title
     * @param content
     * @param notificationIntent
     */
    public static void showResidentNotification(Context context, int id, String title,
                                                String content, Intent notificationIntent) {

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(ns);

        // 定义Notification的各种属性
        int icon = R.drawable.icon;
        long when = System.currentTimeMillis();
        Notification notification = new Notification(icon, content, when);

        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        notification.setLatestEventInfo(context, title, content, contentIntent);

        notificationManager.notify(id, notification);
    }

}
