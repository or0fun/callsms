package com.fang.background;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.fang.common.CustomConstant;
import com.fang.receiver.MainService;
import com.fang.util.QueryTaskUtil;

/**
 * 后台服务
 * Created by fang on 3/2/15.
 */
public class BackgroundService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 3小时一次消息推送
        QueryTaskUtil.startTask(this, MainService.TASK_TYPE.PUSH_REQUEST.ordinal(),
                CustomConstant.THREE_HOUR,
                System.currentTimeMillis() + 5000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
