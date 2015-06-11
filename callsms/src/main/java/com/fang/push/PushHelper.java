package com.fang.push;

import android.content.Context;
import android.util.Log;

import com.fang.net.NetRequestListener;
import com.fang.net.NetRequestResult;
import com.fang.net.NetRequestResultCode;
import com.fang.net.NetResuestHelper;
import com.fang.net.ServerUtil;
import com.fang.util.NotificationHelper;
import com.fang.common.util.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 消息推送帮助类
 * Created by fang on 3/2/15.
 */
public class PushHelper {

    private final static String TAG = "PushHelper";

    private static PushHelper mInstance;

    private Context mContext;

    private PushHelper() {}

    public static PushHelper getInstance() {
        if (null == mInstance) {
            synchronized (PushHelper.class) {
                if (null == mInstance) {
                    mInstance = new PushHelper();
                }
            }
        }
        return mInstance;
    }

    /**
     * 检查消息推送
     * @param context
     */
    public void checkPushRequest(final Context context) {
        mContext = context;

        new Thread(new Runnable() {
            @Override
            public void run() {
                ServerUtil server = ServerUtil.getInstance(context);
                server.request(NetResuestHelper.PUSH, mNetRequestListener);
            }
        }).start();
    }
    /**
     * 网络监听
     */
    protected NetRequestListener mNetRequestListener = new NetRequestListener() {
        @Override
        public void onResult(NetRequestResult result) {
            if (null == result) {
                return;
            }
            if (result.getResultCode() == NetRequestResultCode.HTTP_OK) {

                JSONArray verjson = null;
                try {
                    verjson = new JSONArray(result.getValue());
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing data " + e.toString());
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing data " + e.toString());
                }
                if (verjson != null) {
                    try {
                        JSONObject object = (JSONObject) verjson.get(0);
                        String user = object.getString(ParamType.USER);
                        if (StringUtil.isEmpty(user) || user.equals(ServerUtil.getInstance(mContext).getUserID())) {
                            String title = object.getString(ParamType.TITLE);
                            String content = object.getString(ParamType.CONTENT);
                            String url = object.getString(ParamType.URL);

                            int task = object.getInt(ParamType.TASK);

                            NotificationHelper.showPushNotification(mContext, title, content, task, url);
                        }else {
                            Log.d(TAG, "user is " + user);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing data " + e.toString());
                    }
                }
            }
        }
    };
}
