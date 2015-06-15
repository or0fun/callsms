
package com.fang.common;

import android.net.Uri;

/**
 * 定义常量
 * @author fang
 *
 */
public class CustomConstant {
    /** 信息获取的API */
    public static final String API_URL = "http://115.29.17.79/icoding/api.php";
    /** 接收post请求的服务器地址 */
    public static final String DEFAULT_POST_URL = "http://115.29.17.79/we/api.php";
    /** 接收post请求的服务器地址 测试地址*/
    public static final String DEFAULT_POST_URL_TEST = "http://115.29.17.79/we/api_test.php";
    /** 接收get请求的服务器地址 */
    public static final String DEFAULT_GET_URL = "";
	/** 版本更新地址 */
	public static final String VERSION_GET_URL = "http://115.29.17.79/we/version.php";
	/** 版本更新地址 测试地址 */
	public static final String VERSION_GET_URL_TEST = "http://115.29.17.79/we/version_test.php";

    /** 历史上的今天 **/
    public static final String HISTORY_OF_TODAY = "http://ie8384.com/we/today.php";
	/** 收件箱Uri */
	public static final Uri SMS_INBOX_URI = Uri.parse("content://sms/inbox");  
    /** 选择联系人 传递数据的参数名 */
	public static final String SELECT_CONTACT_PARAMETER =  "contact_selected";

    /** 一小时 */
    public static final long ONE_HOUR = 1000 * 3600;

    /** 3小时 */
    public static final long THREE_HOUR = 1000 * 3600 * 3;

    /** 12小时 */
    public static final long HALF_DAY = ONE_HOUR * 12;

    /** 一天的毫秒 */
	public static final long ONE_DAY = HALF_DAY * 2;

	/** 一刻钟 */
	public static final long QUARTER_HOUR = 1000 * 60 * 15;
	
	/** 5s */
	public static final long FIVE_SECONDS = 5000;
	
}

