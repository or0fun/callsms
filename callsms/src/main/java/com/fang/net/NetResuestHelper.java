package com.fang.net;

/**
 * 网络请求帮助类
 * @author fang
 *
 */
public class NetResuestHelper {

	public static String KEY = "key";
	public static String DATA = "data";
	/** 日志类型 */
	public static String CODE = "code";
	/** 获取用户ID 的参数 */
	public static String USER_ID = "user_id";
	/** 携带用户ID 的参数 */
	public static String VERSION = "version";
	/** 版本 的参数 */
	public static String CHANNEL = "channel";
	/** 渠道 的参数 */
	public static String USER = "user";
	/** 手机型号 */
	public static String MODEL = "model";
	/** 反馈意见 */
	public static String FEEDBACK = "feedback";
	/** 吐槽 */
	public static String COMMENT = "comment";
	/** 获取吐槽 */
	public static String GETCOMMENTS = "getcomments";
	
	private static int code = 0;
	public static synchronized int getRequestCode() {
		code++;
		if (code > 10000) {
			code = 1;
		}
		return code;
	}
}
