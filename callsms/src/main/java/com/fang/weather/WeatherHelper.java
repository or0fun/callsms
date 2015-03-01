package com.fang.weather;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fang.callsms.MainActivity;
import com.fang.callsms.R;
import com.fang.logs.LogCode;
import com.fang.util.DebugLog;
import com.fang.util.NetWorkUtil;
import com.fang.util.NotifycationHelper;
import com.fang.util.SharedPreferencesHelper;
import com.fang.util.StringUtil;
import com.fang.util.Util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * 天气帮助类
 * Created by fang on 3/1/15.
 */
public class WeatherHelper {

    private static final String TAG = "WeatherHelper";
    /** 天气常驻通知栏 */
    protected static boolean mIsShowWeatherNotification = false;
    /**
     * 显示天气的常驻通知栏
     */
    public static void postWeatherNotification(final Context context) {

        if (false == SharedPreferencesHelper.getBoolean(context, SharedPreferencesHelper.SETTING_WEATHER_NOTIFICATION, true)) {
            DebugLog.d(TAG, "postWeatherNotification: SETTING_WEATHER_NOTIFICATION is false");
            return;
        }
        if (mIsShowWeatherNotification && System.currentTimeMillis() - SharedPreferencesHelper.getLong(context, SharedPreferencesHelper.WEATHER_NOTIFICATION_TIME) < 600000) {
            DebugLog.d(TAG, "postWeatherNotification: time is so short");
            return;
        }
        SharedPreferencesHelper.setLong(context, SharedPreferencesHelper.WEATHER_NOTIFICATION_TIME, System.currentTimeMillis());

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (NetWorkUtil.isNetworkAvailable(context)) {
                    String weather = NetWorkUtil.getInstance().searchWeather(2);
                    if (!StringUtil.isEmpty(weather)) {
                        Intent notificationIntent = new Intent(
                                context,
                                MainActivity.class);
                        notificationIntent.putExtra(MainActivity.ENTRY_FROM, LogCode.WEATHER_NOTIFICATION_CLICK);
                        String[] str = weather.split("\\|");
                        Util.showResidentNotification(
                                context,
                                NotifycationHelper.WEATHER_ID,
                                str[0],
                                "明天" + str[1],
                                notificationIntent);
                        mIsShowWeatherNotification = true;
                    }
                }
            }
        }).start();
    }


    /**
     * 天气预报的项
     * @param weather
     * @param index
     * @return
     */
    public static View createWeatherItem(Context context, String weather, int index) {
        String[] items = weather.split(",");

        LinearLayout linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(12, 10, 12, 10);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);

        ImageView imageView = new ImageView(context);
        imageView.setMaxHeight(50);
        imageView.setMaxWidth(50);

        if (items[0].contains("多云转晴") || items[0].contains("晴转多云")) {
            imageView.setImageResource(R.drawable.w_1);
        } else if (items[0].contains("晴转小雨") || items[0].contains("小雨转晴")) {
            imageView.setImageResource(R.drawable.w_8);
        } else if (items[0].contains("雷阵雨")) {
            imageView.setImageResource(R.drawable.w_5);
        }  else if (items[0].contains("雨")) {
            imageView.setImageResource(R.drawable.w_7);
        } else if (items[0].contains("雪")) {
            imageView.setImageResource(R.drawable.w_6);
        } else if (items[0].contains("阴")) {
            imageView.setImageResource(R.drawable.w_3);
        } else if (items[0].contains("多云")) {
            imageView.setImageResource(R.drawable.w_4);
        } else if (items[0].contains("晴")) {
            imageView.setImageResource(R.drawable.w_9);
        } else if (items[0].contains("少云")) {
            imageView.setImageResource(R.drawable.w_2);
        }

        TextView text = new TextView(context);
        text.setText(items[0]);
        text.setTextColor(context.getResources().getColor(R.color.number_service));

        TextView temperature = new TextView(context);
        temperature.setText(items[1]);
        temperature.setTextColor(context.getResources().getColor(R.color.number_service));

        TextView date = new TextView(context);
        date.setTextColor(context.getResources().getColor(R.color.number_service));
        if (index == 0) {
            date.setText("今天");
        } else if (index == 1) {
            date.setText("明天");
        } else if (index == 1) {
            date.setText("后天");
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, index);
            SimpleDateFormat sfd = new SimpleDateFormat("MM-dd",
                    Locale.US);
            date.setText(sfd.format(calendar.getTime()));
        }

        linearLayout.addView(imageView);
        linearLayout.addView(text);
        linearLayout.addView(temperature);
        linearLayout.addView(date);

        return linearLayout;
    }
}
