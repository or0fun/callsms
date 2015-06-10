package com.fang.business;

import android.content.Context;
import android.os.Handler;

import com.fang.call.CallHelper;
import com.fang.callsms.R;
import com.fang.common.util.DebugLog;
import com.fang.common.util.StringUtil;
import com.fang.database.NumberDatabaseManager;
import com.fang.express.ExpressInfo;
import com.fang.net.IResponseListener;
import com.fang.net.ServerUtil;
import com.fang.util.MessageWhat;
import com.fang.util.NetWorkUtil;

import java.util.List;
import java.util.Map;
/**
 * 共用帮助类
 * @author fang
 *
 */
public class BusinessHelper {

    private static final String TAG = "BusinessHelper";
	/**
	 * 获取号码资源
	 * 
	 * @param number
	 * @param handler
	 */
	public static void getNumberInfo(final Context context, final String number,
			final Handler handler) {
		if (StringUtil.isEmpty(number)) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				String infoString = NumberDatabaseManager.getInstance(context).query(
						number);
				if ((StringUtil.isEmpty(infoString) || infoString.endsWith(context.getString(R.string.number_unknow)))
						&& NetWorkUtil.isNetworkConnected(context)) {
					infoString = NetWorkUtil.getInstance().searchPhone(context, number, ServerUtil.getInstance(context).getUserID());
					if (!infoString.endsWith(context.getString(R.string.number_unknow))) {
						NumberDatabaseManager.getInstance(context).update(number, infoString);
					}
				}
				
				if (StringUtil.isEmpty(infoString) && !NetWorkUtil.isNetworkConnected(context)) {
					infoString = context.getString(R.string.open_network_to_recognise_number);
				}
				
				if (null != handler) {
					handler.sendMessage(handler.obtainMessage(
							MessageWhat.NET_REQUEST_NUMBER, infoString));
				}
			}
		}).start();
	}
	/**
	 * 获取号码资源
	 *
     * @param context
	 * @param callRecords
	 * @param handler
	 */
	public static void getNumberInfo(final Context context, final List<Map<String, Object>> callRecords,
			final Handler handler) {
		if (null == callRecords) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
                int len = 5;
				for (Map<String, Object> map : callRecords) {
					String number = map.get(CallHelper.PARAM_NUMBER).toString();
					String infoString = NumberDatabaseManager.getInstance(context).query(
							number);
					if (StringUtil.isEmpty(infoString) && NetWorkUtil.isNetworkConnected(context)) {
						infoString = NetWorkUtil.getInstance().searchPhone(context, number, ServerUtil.getInstance(context).getUserID());
						NumberDatabaseManager.getInstance(context).update(number, infoString);
                        DebugLog.d(TAG, "getNumberInfo:" + number + "= " + infoString);
					}
                    len++;

                    if (len % 5 == 0) {
                        if (null != handler) {
                            handler.sendEmptyMessage(MessageWhat.UPDATE_NUMBER_DATABASE);
                        }
                    }
				}
				if (null != handler) {
					handler.sendEmptyMessage(MessageWhat.UPDATE_NUMBER_DATABASE);
				}
			}
		}).start();
	}

	/**
	 * 获取号码资源
	 * 
	 * @param context
     * @param info
	 * @param handler
	 */
	public static void getExpressInfo(final Context context, final ExpressInfo info,
			final Handler handler) {
		if (!NetWorkUtil.isNetworkConnected(context)) {
			return;
		}
		if (null == info) {
			return;
		}
		if (StringUtil.isEmpty(info.getNumber()) || StringUtil.isEmpty(info.getCompany()) ) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				String infoString = NetWorkUtil.getInstance().searchExpress(
						context, 
						info.getCompany(), 
						info.getNumber(), 
						ServerUtil.getInstance(context).getUserID());
				
				if (StringUtil.isEmpty(infoString)) {
					infoString = context.getString(R.string.open_network_to_recognise_express);
				}
                if (null != handler) {
                    info.setInfo(infoString);
                    handler.sendMessage(handler.obtainMessage(
                            MessageWhat.NET_REQUEST_EXPRESS, info));
                }
            }
		}).start();
	}

	/**
	 * 获取快递信息
	 * 
	 * @param info
	 * @param expressListener
	 */
	public static void getExpressInfo(final Context context, final ExpressInfo info,
			final IResponseListener expressListener) {
		if (!NetWorkUtil.isNetworkConnected(context)) {
			return;
		}
		if (null == info) {
			return;
		}
		if (StringUtil.isEmpty(info.getNumber()) || StringUtil.isEmpty(info.getCompany()) ) {
			return;
		}
		info.setChanged(false);
		new Thread(new Runnable() {
			@Override
			public void run() {
				String infoString = NetWorkUtil.getInstance().searchExpress(
						context, 
						info.getCompany(), 
						info.getNumber(), 
						ServerUtil.getInstance(context).getUserID());
				
				if (StringUtil.isEmpty(infoString)) {
					infoString = context.getString(R.string.open_network_to_recognise_express);
				}
                if (null != expressListener) {
                    if (!infoString.equals(info.getInfo())) {
                        info.setChanged(true);
                        info.setInfo(infoString);
                    }
                    expressListener.onResult(info);
                }

            }
		}).start();
	}

	/**
	 * 获取号码资源
	 *
     * @param context
	 * @param city
	 * @param handler
	 */
	public static void getWeatherInfo(final Context context, final String city,
			final Handler handler) {
		if (!NetWorkUtil.isNetworkConnected(context)) {
			return;
		}
		if (StringUtil.isEmpty(city)) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				String infoString = NetWorkUtil.getInstance().searchWeather(
						context, 
						city,
						ServerUtil.getInstance(context).getUserID());
				
				if (StringUtil.isEmpty(infoString)) {
					infoString = context.getString(R.string.open_network_to_recognise_express);
				}
                if (null != handler) {
                    handler.sendMessage(handler.obtainMessage(
                            MessageWhat.NET_REQUEST_WEATHER, infoString));
                }
            }
		}).start();
	}
}
