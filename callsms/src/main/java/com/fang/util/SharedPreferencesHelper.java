package com.fang.util;

import android.content.Context;

import com.fang.common.util.AbsSharedPre;

public class SharedPreferencesHelper extends AbsSharedPre {

    private final static String PREFS_FILE_NAME = "com.fang.callsms";

	/** 短信最后时间*/
	public static final String SENT_SMS_LAST_TIME = "SENT_SMS_LAST_TIME";
	/** 第一次启动 */
	public static final String FIRST_TIME_OPEN = "FIRST_TIME_OPEN";
	/** 设置项 */
	public static final String SETTING_SMS_POPUP = "SETTING_SMS_POPUP";
	public static final String SETTING_NEW_CALL_POPUP = "SETTING_NEW_CALL_POPUP";
	public static final String SETTING_MISSED_CALL_POPUP = "SETTING_MISSED_CALL_POPUP";
	public static final String SETTING_OUTGOING_CALL_POPUP = "SETTING_OUTGOING_CALL_POPUP";
	public static final String SETTING_BROADCAST_WHEN_WIREDHEADSETON = "SETTING_BROADCAST_WHEN_WIREDHEADSETON";
	public static final String SETTING_EXPRESS_TRACK = "SETTING_EXPRESS_TRACK";
	public static final String SETTING_WEATHER_NOTIFICATION = "SETTING_WEATHER_NOTIFICATION";
	/** 定时短信*/
	public static final String TIMING_SMS_INFO = "TIMING_SMS_INFO";
    /** 最后启动的时间 */
	public static final String LAUNCH_LAST_TIME = "LAUNCH_LAST_TIME";
    /** 用户ID */
	public static final String USER_ID = "USER_ID";
	/** 未上传成功的数据 */
	public static final String OFFLINE_DATA = "OfflineData";
	/** 快递列表 */
	public static final String EXPRESS_LIST = "EXPRESS_LIST";
	/** 最近一次查询快递的时间 */
	public static final String LAST_UPDATE_EXPRESS_LIST = "LAST_UPDATE_EXPRESS_LIST";	
	
	/** 选择的快递 */
	public static final String SELECTED_EXPRESS_COMPANY = "SELECTED_EXPRESS_COMPANY";
	/** 最后开启点页面 */
	public static final String SELECTED_PAGE = "SELECTED_PAGE";

    /** 查询的号码 */
    public static final String NUMBER_SEARCH = "NUMBER_SEARCH";

    /** 农历 */
    public static final String NONGLI = "NONGLI";
    /** 天气 */
    public static final String WEATHER = "WEATHER";

    //扫一扫 快捷方式
    public static final String SCAN = "scan";

    //l¥立即检查更新
    public static final String UPDATE_VERSION = "UPDATE_VERSION";

    @Override
    public void init(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
    }
}
