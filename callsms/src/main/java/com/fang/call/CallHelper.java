package com.fang.call;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.CallLog.Calls;

import com.fang.callsms.R;
import com.fang.contact.ContactHelper;
import com.fang.database.NumberDatabaseManager;
import com.fang.util.DebugLog;
import com.fang.util.MessageWhat;
import com.fang.util.StringUtil;
import com.fang.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 电话帮助类
 * 
 * @author fang
 * 
 */
public class CallHelper {

    private static final String TAG = "CallHelper";

	public static final String PARAM_ID = "id";
	public static final String PARAM_NAME = "name";
	public static final String PARAM_NUMBER = "number";
	public static final String PARAM_ICON = "icon";
	public static final String PARAM_TYPE = "type";
	public static final String PARAM_DATE = "date";
	public static final String PARAM_DURATION = "duration";
	public static final String PARAM_INFO = "info";
	public static final String PARAM_COUNT = "count";
	
	private static boolean isHasRead = false;

    private static boolean isReading = false;
	
	protected static List<Map<String, Object>> mAllCallRecords = new ArrayList<Map<String,Object>>();
	
	protected static List<ICallRecordListener> mCallRecordListeners = new ArrayList<ICallRecordListener>();

	public static String[] CALL_RECORD_PARAMETERS = new String[] {
			CallLog.Calls._ID, CallLog.Calls.TYPE, CallLog.Calls.CACHED_NAME,
			CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.DURATION };

	/**
	 * 添加监听者
	 * @param listener
	 */
	public static void registerLisetener(ICallRecordListener listener) {
		mCallRecordListeners.add(listener);
	}

	public static void unregisterListener(ICallRecordListener listener) {
		mCallRecordListeners.remove(listener);
	}
	/**
	 * 获取通话记录
	 * @return
	 */
	public static List<Map<String, Object>> getCallRecords() {
		return mAllCallRecords;
	}
	
	/**
	 * 获取最近的通话时间
	 * 
	 * @param context
	 * @param number
	 * @return
	 */
	public static String getLastRecordDate(final Context context,
			final String number) {
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI,
				new String[] { CallLog.Calls.DATE }, "number=?",
				new String[] { number }, CallLog.Calls.DATE + " desc limit 1");
		String lastDateString = null;
		if (null != cursor) {
			if (cursor.moveToFirst()) {
				long date = Long.parseLong(cursor.getString(cursor
						.getColumnIndexOrThrow(Calls.DATE)));
				lastDateString = Util.longDateToStringDate(date);
			}
			cursor.close();
		}
		return lastDateString;
	}

	/**
	 * 获取通话次数
	 * 
	 * @param number
	 * @param callType
	 */
	public static int getCallTimes(Context context, String number, int callType) {
		String tmpString = String.format(Locale.US, "number=? and type=%d",
				callType);
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI,
				new String[] { CallLog.Calls.DURATION }, tmpString,
				new String[] { number }, null);
		if (null != cursor) {
			int t = cursor.getCount();
			cursor.close();
			return t;
		}
		return 0;
	}

	/**
	 * 获取通话次数
	 *
	 * @param context
     * @param number
	 */
	public static int getCallTimes(Context context, String number) {
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI,
				new String[] { CallLog.Calls.DURATION }, "number=? ",
				new String[] { number }, null);
		if (null != cursor) {
			int t = cursor.getCount();
			cursor.close();
			return t;
		}
		return 0;
	}
	
	/**
	 * 获取上次通话信息
	 * @param context
	 * @param number
	 * @param handler
	 */
	public static void getLastRecordInfo(final Context context,
			final String number, final Handler handler) {
		if (StringUtil.isEmpty(number)) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				String lastDateString = getLastRecordDate(context, number);
				
				if (false == StringUtil.isEmpty(lastDateString)) {
					if (null != handler) {
						handler.sendMessage(handler.obtainMessage(
								MessageWhat.MSG_LAST_RECORD_DATE, context.getString(R.string.last_record) + lastDateString));
					}
				}
			}
		}).start();
	}

	/**
	 * 统计通话记录
	 * 
	 * @param context
	 * @param number
	 * @param handler
	 * @return
	 */
	public static void getCallRecordsInfo(final Context context,
			final String number, final Handler handler) {
		if (StringUtil.isEmpty(number)) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				StringBuffer result = new StringBuffer();
				int t = getCallTimes(context, number,
                        CallLogType.INCOMING_TYPE);
				result.append(String.format("来电%d次", t));

				t = getCallTimes(context, number, CallLogType.OUTGOING_TYPE);
				result.append(String.format(" 拨出%d次", t));

				t = getCallTimes(context, number, CallLogType.MISSED_TYPE);
				result.append(String.format(" 未接来电%d次", t));
				if (null != handler) {
					handler.sendMessage(handler.obtainMessage(
							MessageWhat.CALL_RECORDS, result.toString()));
				}
			}
		}).start();
	}

	/**
	 * 统计某个号码的通话记录
	 * 
	 * @param context
	 * @param number
	 * @param handler
	 * @return
	 */
	public static void getCallRecordsList(final Context context,
			final String number, final Handler handler) {
		if (null == context || null == handler || StringUtil.isEmpty(number)) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				String sortOrder = String.format("%s desc", CallLog.Calls.DATE);
                Cursor cursor = context.getContentResolver().query(
                        CallLog.Calls.CONTENT_URI,
                        CALL_RECORD_PARAMETERS, "number=?",
                        new String[] { number }, sortOrder);
				List<Map<String, Object>> callRecords = new ArrayList<Map<String, Object>>();
				if (null != cursor) {
					if (cursor.moveToFirst()) {
						do {
							Map<String, Object> callRecord = new HashMap<String, Object>();
							// id
							int id = cursor.getInt(cursor
									.getColumnIndex(Calls._ID));
							// 号码
							String numberString = cursor.getString(cursor
									.getColumnIndex(Calls.NUMBER));

							callRecord.put(PARAM_ID, id);

							// 类型
                            int callType = Integer.parseInt(cursor.getString(cursor
                                    .getColumnIndex(Calls.TYPE)));
                            callRecord.put(PARAM_TYPE, callType);
                            //icon
                            callRecord.put(PARAM_ICON, getIcon(callType));

							callRecord.put(
									PARAM_DATE,
									Util.longDateToStringDate(Long.parseLong(cursor.getString(cursor
											.getColumnIndexOrThrow(Calls.DATE)))));
							long duration = Long.parseLong(cursor.getString(cursor
									.getColumnIndexOrThrow(Calls.DURATION)));

							callRecord.put(PARAM_DURATION,
									Util.secondsToString(duration));
							

							callRecords.add(callRecord);

						} while (cursor.moveToNext());
					}
					cursor.close();
				}
				handler.sendMessage(handler.obtainMessage(
						MessageWhat.FRESH_CALL_RECORD, callRecords));
			}
		}).start();
	}

	/**
	 * 判断是否未接来电
	 * 
	 * @param context
	 * @param number
	 * @return
	 */
	public static boolean isMissedCall(Context context, String number) {
		Cursor cursor = context.getContentResolver().query(
				CallLog.Calls.CONTENT_URI, CALL_RECORD_PARAMETERS,
				CallLog.Calls.NUMBER + " = ?", new String[] { number },
				CallLog.Calls.DATE + " desc limit 1");
		boolean isMissCall = false;
		if (null != cursor) {
			if (cursor.moveToFirst()) {
				switch (Integer.parseInt(cursor.getString(cursor
						.getColumnIndex(Calls.TYPE)))) {
				case CallLogType.MISSED_TYPE:
					isMissCall = true;
					break;
				}
			}
			cursor.close();
		}
		return isMissCall;
	}

	/**
	 * 删除通话记录
	 * 
	 * @param context
	 * @param id
	 * @return
	 */
	public static int deleteCallRecord(Context context, int id) {
		ContentResolver resolver = context.getContentResolver();
		return resolver.delete(CallLog.Calls.CONTENT_URI, "_id=?",
				new String[] { id + "" });

	}

	/**
	 * 判断未接来电
	 * 
	 * @param intent
	 * @return
	 */
	public static boolean isMissedIncomingCall(Intent intent) {
		if (null == intent) {
			return false;
		}
		String action = intent.getAction();
		String type = intent.getType();
		if (null != action && action.contains("android.intent.action.VIEW")
				&& null != type
				&& type.contains("vnd.android.cursor.dir/calls")) {
			return true;
		} else if (null != action
				&& action.contains("com.android.phone.action.RECENT_CALLS")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获取通话记录
	 * 
	 * @param context
	 * @return
	 */
	public static void getCallRecordsList(Context context) {
        DebugLog.d(TAG, "getCallRecordsList");
        if (isReading) {
            DebugLog.d(TAG, "getCallRecordsList: isReading is true");
            return;
        }
		if (null == context || isHasRead) {
			for (ICallRecordListener listener : mCallRecordListeners) {
				listener.onResult(true);
			}
			return;
		}

        isReading = true;
		String sortOrder = String.format("%s desc", CallLog.Calls.DATE);
		Cursor cursor = context.getContentResolver().query(
				CallLog.Calls.CONTENT_URI, CALL_RECORD_PARAMETERS, null, null,
				sortOrder);
		List<Map<String, Object>> callRecords = new ArrayList<Map<String, Object>>();
		int number = 0;
		if (null != cursor) {
			if (cursor.moveToFirst()) {
				int count = 0;
				Map<String, Object> callRecord = null;
				do {
					// id
					int id = cursor.getInt(cursor.getColumnIndex(Calls._ID));
					// 号码
					String numberString = cursor.getString(cursor
							.getColumnIndex(Calls.NUMBER));
					if (null == callRecord) {
						callRecord = new HashMap<String, Object>();
						count++;
					} else if (numberString
							.equals(callRecord.get(PARAM_NUMBER))) {
						count++;
						continue;
					} else {
						callRecord.put(PARAM_COUNT, count);
						count = 0;
						callRecords.add(callRecord);
						number++;
						callRecord = new HashMap<String, Object>();
					}

					callRecord.put(PARAM_ID, id);
                    //号码
					callRecord.put(PARAM_NUMBER, numberString);

					// 联系人姓名
					String nameString = cursor.getString(cursor
							.getColumnIndexOrThrow(Calls.CACHED_NAME));
					if (StringUtil.isEmpty(nameString)) {
						nameString = ContactHelper.getPerson(context,
								numberString);
					}
					if (StringUtil.isEmpty(nameString)) {
						callRecord.put(PARAM_NAME, "");
					} else {
						callRecord.put(PARAM_NAME, nameString);
					}
					// 类型
                    int callType = Integer.parseInt(cursor.getString(cursor
                            .getColumnIndex(Calls.TYPE)));
                    callRecord.put(PARAM_TYPE, callType);
                    //icon
                    callRecord.put(PARAM_ICON, getIcon(callType));
                    //日期
					callRecord.put(PARAM_DATE, cursor.getString(cursor
									.getColumnIndexOrThrow(Calls.DATE)));
					long duration = Long.parseLong(cursor.getString(cursor
							.getColumnIndexOrThrow(Calls.DURATION)));

                    //时长
					callRecord.put(PARAM_DURATION,
							Util.secondsToString(duration));
                    //信息
					callRecord.put(
							PARAM_INFO,
							NumberDatabaseManager.getInstance(context).query(
									numberString));

				} while (cursor.moveToNext());
				if (null != callRecord) {
                    //次数
					callRecord.put(PARAM_COUNT, count);
					callRecords.add(callRecord);
					number++;
				}
			}
			cursor.close();

			mAllCallRecords = callRecords;
			
			for (ICallRecordListener listener : mCallRecordListeners) {
				listener.onResult(true);
			}
			
			CallHelper.setHasRead(true);
            isReading = false;
		}
	}

	/**
	 * 获取通话类型对中文描述
	 *
     * @param context
	 * @param callType
	 * @return
	 */
	public static String getCallTypeString(Context context, int callType) {

		String tip = "";
		switch (callType) {
		case CallLogType.INCOMING_TYPE:
			tip = context.getString(R.string.record_incoming);
			break;
		case CallLogType.OUTGOING_TYPE:
			tip = context.getString(R.string.record_outgoing);
			break;
		case CallLogType.MISSED_TYPE:
			tip = context.getString(R.string.record_missed);
			break;
        case CallLogType.ADD_TYPE:
            tip = context.getString(R.string.record_add);
            break;
		default:
			tip = context.getString(R.string.record_idle);// 应该是挂断.根据我手机类型判断出的
			break;
		}
		return tip;
	}

	/**
	 * 获取通话类型对中文描述
	 *
     * @param context
	 * @param callType
	 * @return
	 */
	public static int getCallTypeColor(Context context, int callType) {

		int color = context.getResources().getColor(R.color.black);
		switch (callType) {
		case CallLogType.INCOMING_TYPE:
			color = context.getResources().getColor(R.color.incoming);
			break;
		case CallLogType.OUTGOING_TYPE:
			color = context.getResources().getColor(R.color.outgoing);
			break;
		case CallLogType.MISSED_TYPE:
			color = context.getResources().getColor(R.color.missed);
			break;
		}
		return color;
	}

	public static boolean hasRead() {
		return isHasRead;
	}

	public static void setHasRead(boolean isHasRead) {
        DebugLog.d(TAG, "setHasRead: isHasRead = " + isHasRead);
		CallHelper.isHasRead = isHasRead;
	}

    /**
     * 根据类型获取icon
     * @param callType
     */
    private static int getIcon(int callType) {
        switch (callType) {
            case CallLogType.INCOMING_TYPE:
                return R.drawable.incoming_type;
            case CallLogType.OUTGOING_TYPE:
                return R.drawable.outgoing_type;
            case CallLogType.MISSED_TYPE:
                return R.drawable.missed_type;
            case CallLogType.ADD_TYPE:
                return R.drawable.add_type;
            default:
                return R.drawable.incoming_type;
        }
    }
}
