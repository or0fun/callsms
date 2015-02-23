package com.fang.comment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;

import com.fang.net.NetRequestListener;
import com.fang.net.NetRequestResult;
import com.fang.net.NetRequestResultCode;
import com.fang.net.NetResuestHelper;
import com.fang.net.ServerUtil;
import com.fang.util.DebugLog;
import com.fang.util.MessageWhat;

/**
 * 吐槽帮助类
 * @author fang
 *
 */
public class CommentHelper {

	private static String TAG = "CommentHelper";
	public static String PARAM_CONTENT = "content";
	public static String PARAM_NUMBER = "number";
	
	/**
	 * 获取吐槽
	 * @param context
	 * @param number
	 * @param handler
	 */
	public static void getComments(Context context, String number, final Handler handler) {

		if (null == handler) {
			DebugLog.d(TAG, "handler is null");
			return;
		}
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(PARAM_NUMBER, number);
		} catch (JSONException e) {
			DebugLog.d(TAG, e.toString());
		}
		
		ServerUtil.getInstance(context).request(NetResuestHelper.GETCOMMENTS, jsonObject.toString(), 
				new NetRequestListener() {
					
					@Override
					public void onResult(NetRequestResult result) {
						if (null != result && NetRequestResultCode.HTTP_OK == result.getResultCode()) {
							String jsonsString = result.getValue();
							handler.sendMessage(handler.obtainMessage(MessageWhat.MSG_SHOW_COMMENTS, jsonsString));
						}
					}
				});
	}
	
	/**
	 * 解析所有吐槽
	 * @param jsonsString
	 * @return
	 */
	public static String parseAllComments(String jsonsString) {

		DebugLog.d(TAG, jsonsString);
		String commentsString = "";
		try {
			JSONArray jsonArray = new JSONArray(jsonsString);
			if (null != jsonArray && jsonArray.length() > 0) {
				int len = jsonArray.length();
				for (int i = 0; i < len; i++) {
					JSONObject json = (JSONObject) jsonArray.get(i);
					if (null != json) {
						commentsString += json.getString(PARAM_CONTENT);
						if (i < len - 1) {
							commentsString += "\n--------------------------------\n";
						}
					}
				}
			}
		} catch (JSONException e) {
			DebugLog.d(TAG, e.toString());
		}
		return commentsString;
	}
}
