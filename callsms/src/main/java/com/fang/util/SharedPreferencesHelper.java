package com.fang.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SharedPreferencesHelper {
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

    /** 天气通知时间 */
    public static final String WEATHER_NOTIFICATION_TIME = "WEATHER_NOTIFICATION_TIME";

    /** 7天天气预报更新时间 */
    public static final String WEATHER_UPDATE_TIME = "WEATHER_UPDATE_TIME";

    /** 查询的号码 */
    public static final String NUMBER_SEARCH = "NUMBER_SEARCH";
	
	public static String getString(Context ctx, String key) {
		
		SharedPreferences sharedata = ctx.getSharedPreferences(key, Context.MODE_PRIVATE );
		String value = sharedata.getString(key, "");
		return value;
		
	}
	public static String getString(Context ctx, String key, String defaultValue) {
		
		SharedPreferences sharedata = ctx.getSharedPreferences(key, Context.MODE_PRIVATE );
		String value = sharedata.getString(key, defaultValue);
		return value;
		
	}
	public static void setString(Context ctx, String key, String value) {

	    SharedPreferences.Editor sharedata = ctx.getSharedPreferences(key, Context.MODE_PRIVATE ).edit();  
	    sharedata.putString(key, value);
	    sharedata.commit();
		
	}
	public static int getInt(Context ctx, String key, int i) {
		
		SharedPreferences sharedata = ctx.getSharedPreferences(key, Context.MODE_PRIVATE );
		int value = sharedata.getInt(key, i);
		return value;
		
	}
	public static void setInt(Context ctx, String key, int value) {

	    SharedPreferences.Editor sharedata = ctx.getSharedPreferences(key, Context.MODE_PRIVATE ).edit();  
	    sharedata.putInt(key, value);
	    sharedata.commit();
		
	}

	public static long getLong(Context ctx, String key, long defaultvalue) {
		
		SharedPreferences sharedata = ctx.getSharedPreferences(key, Context.MODE_PRIVATE );
		long value = sharedata.getLong(key, defaultvalue);
		return value;
		
	}

    public static long getLong(Context ctx, String key) {

        SharedPreferences sharedata = ctx.getSharedPreferences(key, Context.MODE_PRIVATE );
        long value = sharedata.getLong(key, 0);
        return value;

    }

	public static void setLong(Context ctx, String key, long value) {

	    SharedPreferences.Editor sharedata = ctx.getSharedPreferences(key, Context.MODE_PRIVATE ).edit();  
	    sharedata.putLong(key, value);
	    sharedata.commit();
		
	}
	public static boolean getBoolean(Context ctx, String key, boolean defaultvalue) {
		SharedPreferences sharedata = ctx.getSharedPreferences(key, Context.MODE_PRIVATE );
		boolean value = sharedata.getBoolean(key, defaultvalue);
		return value;
		
	}
	public static void setBoolean(Context ctx, String key, boolean value) {

	    SharedPreferences.Editor sharedata = ctx.getSharedPreferences(key, Context.MODE_PRIVATE ).edit();  
	    sharedata.putBoolean(key, value);
	    sharedata.commit();
		
	}
	/**
	 * 保存对象信息
	 * @param ctx
     * @param key
     * @param object
	 */
	public static void setObject(Context ctx, String key, Object object) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos;
			oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			String productBase64 = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
			SharedPreferencesHelper.setString(ctx, key, productBase64);
		} catch (IOException e) {
			DebugLog.d("SharedPreferencesHelper", e.toString());
		}
	}
	/**
	 * 保存对象信息
	 * @param ctx
     * @param key
	 */
	public static Object getObject(Context ctx, String key) {
		try {
			String productBase64 = SharedPreferencesHelper.getString(ctx, key);
			byte[] base64Bytes = Base64.decode(productBase64.getBytes(), Base64.DEFAULT);
			ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			return ois.readObject();
		} catch (IOException e) {
		}catch (ClassNotFoundException e) {
			DebugLog.d("SharedPreferencesHelper", e.toString());
		}
		return null;
	}
}
