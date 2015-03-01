package com.fang.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.fang.callsms.MainActivity;
import com.fang.callsms.R;
import com.fang.controls.CustomDialog;
import com.fang.listener.IDeleteConfirmListener;
import com.fang.receiver.AlarmReceiver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Util {

	public static String INCOMING_TYPE = "INCOMING_TYPE";
	public static String OUTGOING_TYPE = "OUTGOING_TYPE";
	public static String MISSED_TYPE = "MISSED_TYPE";

	/**
	 * 复制
	 * 
	 * @param context
     * @param content
	 **/

	@SuppressLint("NewApi")
	public static void copy(Context context, String content) {
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
					.getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("label", content);
			clipboard.setPrimaryClip(clip);
		} else {
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context
					.getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(content);
		}
	}

	/**
	 * 获取粘贴板数据
	 * 
	 * @param context
	 * @return
	 */
	public static String paste(Context context) {
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
					.getSystemService(Context.CLIPBOARD_SERVICE);
			CharSequence data = clipboard.getText();
			if (null != data) {
				return data.toString();
			}
			return "";
		} else {
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context
					.getSystemService(Context.CLIPBOARD_SERVICE);
			CharSequence data = clipboard.getText();
			if (null != data) {
				return data.toString();
			}
			return "";
		}
	}

	/**
	 * 拨打电话
	 * 
	 * @param number
	 */
	public static void gotoCall(Context context, String number) {
		Uri uri = Uri.parse("tel:" + number);
		Intent intent = new Intent(Intent.ACTION_CALL, uri);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	/**
	 * 启动新的Activity
	 * 
	 * @param context
	 * @param intent
	 */
	public static void startActivityNewTask(Context context, Intent intent) {
		if (null != intent) {
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
	}

	/**
	 * 启动新的Activity
	 * 
	 * @param context
	 * @param name
	 */
	public static void startActivity(Context context, Class<?> name) {
		Intent intent = new Intent(context, name);
		context.startActivity(intent);
	}

	/**
	 * 发送邮件
	 * 
	 * @param context
	 * @param email
	 */
	public static void email(Context context, String email) {
		Uri uri = Uri.parse("mailto:" + email);
		Intent intent = new Intent(Intent.ACTION_SENDTO, uri);

		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	/**
	 * 打开链接
	 * 
	 * @param context
	 * @param url
	 */
	public static void openURL(Context context, String url) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		Uri content_url = Uri.parse(url);
		intent.setData(content_url);

		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	/**
	 * 添加fragment
	 * 
	 * @param activity
	 * @param fragment
	 * @param viewLayout
	 */
	public static void addFragment(Activity activity, Fragment fragment,
			int viewLayout) {
		FragmentManager fragmentManager = activity.getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		fragmentTransaction.add(viewLayout, fragment);
		fragmentTransaction.commit();

	}

	/**
	 * 添加悬浮窗
	 * 
	 * @param windowManager
	 * @param view
	 */
	public static void addView(WindowManager windowManager, View view) {
		WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
		layoutParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
		layoutParams.gravity = Gravity.CENTER;
		layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
		layoutParams.flags |= LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		windowManager.addView(view, layoutParams);
	}

	/**
	 * 安装应用
	 */
	public static void installAPK(Context context, Uri uri) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(uri, "application/vnd.android.package-archive");
		startActivityNewTask(context, intent);
	}

	/**
	 * 时间戳 long to string
	 * 
	 * @param d
	 */
	public static String longDateToStringDate(long d) {
		String time = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(d);

        Calendar now = Calendar.getInstance();
		if (calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
			if (calendar.get(Calendar.MONTH) == now.get(Calendar.MONTH)) {
				if (calendar.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)) {
					SimpleDateFormat sfd = new SimpleDateFormat("HH:mm",
							Locale.US);
					time = "今天" + sfd.format(calendar.getTime());
				} else if (calendar.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH) - 1) {
					SimpleDateFormat sfd = new SimpleDateFormat("HH:mm",
							Locale.US);
					time = "昨天" + sfd.format(calendar.getTime());
				} else if (calendar.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH) - 2) {
					SimpleDateFormat sfd = new SimpleDateFormat("HH:mm",
							Locale.US);
					time = "前天" + sfd.format(calendar.getTime());
				} else {
					SimpleDateFormat sfd = new SimpleDateFormat("MM-dd HH:mm",
							Locale.US);
					time = sfd.format(calendar.getTime());
				}
			} else {
				SimpleDateFormat sfd = new SimpleDateFormat("MM-dd HH:mm",
						Locale.US);
				time = sfd.format(calendar.getTime());
			}
		} else {
			SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd HH:mm",
					Locale.US);
			time = sfd.format(calendar.getTime());
		}
		return time;
	}

	/**
	 * 秒数转成字符串形式的时间
	 * 
	 * @param d
	 * @return
	 */
	public static String secondsToString(long d) {
		StringBuffer ptime = new StringBuffer();
		if (d >= 3600) {
			ptime.append(d / 3600);
			ptime.append("小时");
			d = d % 3600;
		}
		if (d >= 60) {
			ptime.append(d / 60);
			ptime.append("分");
			d = d % 60;
		}
		if (d > 0) {
			ptime.append(d);
			ptime.append("秒");
		}
		return ptime.toString();
	}

	/**
	 * 注册闹钟
	 * 
	 * @param context
	 * @param intent
	 * @param requestCode
	 * @param l
	 */
	public static void registerAlarm(Context context, Intent intent,
			int requestCode, long l) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Activity.ALARM_SERVICE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
				requestCode, intent, 0);
		am.set(AlarmManager.RTC_WAKEUP, l, pendingIntent);
		DebugLog.d("haha", "register" + requestCode + "  " + l / 1000);
	}

	/**
	 * 取消闹钟
	 * 
	 * @param context
	 * @param requestCode
	 */
	public static void cancelAlarm(Context context, int requestCode) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Activity.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
				requestCode, intent, 0);
		am.cancel(pendingIntent);
		DebugLog.d("haha", "cancel" + requestCode);
	}

	/**
	 * 删除确认
	 */
	public static void deleteConfirm(Context context,
			final WindowManager windowManager, final int id,
			final int position, final IDeleteConfirmListener deleteConfirm) {
		final View confirmView = LayoutInflater.from(context).inflate(
				R.layout.delete_confirm, null);
        final Dialog dialog = new CustomDialog.Builder(context).setContentView(confirmView)
                .create();
        confirmView.findViewById(R.id.todelete).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (null != deleteConfirm) {
                            deleteConfirm.delete(id, position);
                        }
                    }
                }).start();
            }
        });
		confirmView.findViewById(R.id.cancel).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View arg0) {
                        dialog.dismiss();
					}
				});
        dialog.show();
	}

	/**
	 * 获取短信URI
	 * 
	 * @return
	 */
	public static Uri getSmsUriALL() {
		return Uri.parse("content://sms/");
	}

	/**
	 * 获取通讯录URI
	 * 
	 * @return
	 */
	public static Uri getContactUriALL() {
		return ContactsContract.Contacts.CONTENT_URI;
	}

	/**
	 * 判断耳机是否插上
	 * 
	 * @return
	 */
	public static boolean isWiredHeadsetOn(Context context) {
		AudioManager localAudioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		return localAudioManager.isWiredHeadsetOn();
	}

	/**
	 * 获取版本名字
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String getVersionName(Context context) {
		String version = "1.0";
		PackageManager packageManager = context.getPackageManager();
		PackageInfo packInfo;
		try {
			packInfo = packageManager.getPackageInfo(context.getPackageName(),
					0);
			version = packInfo.versionName;
		} catch (NameNotFoundException e) {
		}
		return version;
	}

	/**
	 * 获取版本号
	 * 
	 * @return
	 * @throws Exception
	 */
	public static int getVersionCode(Context context) {
		int version = 1;
		PackageManager packageManager = context.getPackageManager();
		PackageInfo packInfo;
		try {
			packInfo = packageManager.getPackageInfo(context.getPackageName(),
					0);
			version = packInfo.versionCode;
		} catch (NameNotFoundException e) {
		}
		return version;
	}

	/**
	 * 显示通知栏
	 *
     * @param context
     * @param id
	 * @param title
	 * @param content
     * @param notificationIntent
	 */
	public static void showNotification(Context context, int id, String title,
			String content, Intent notificationIntent) {

		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(ns);

		// 定义Notification的各种属性
		int icon = R.drawable.icon;
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, content, when);

		notification.sound = Uri.parse("android.resource://"
				+ context.getPackageName() + "/" + R.raw.notify);
		notification.defaults |= Notification.DEFAULT_VIBRATE;

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(context, title, content, contentIntent);

		notificationManager.notify(id, notification);
	}
    /**
     * 显示通知栏
     *
     * @param context
     * @param id
     * @param title
     * @param content
     * @param notificationIntent
     */
    public static void showResidentNotification(Context context, int id, String title,
                                        String content, Intent notificationIntent) {

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(ns);

        // 定义Notification的各种属性
        int icon = R.drawable.icon;
        long when = System.currentTimeMillis();
        Notification notification = new Notification(icon, content, when);

        notification.sound = Uri.parse("android.resource://"
                + context.getPackageName() + "/" + R.raw.notify);
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        notification.setLatestEventInfo(context, title, content, contentIntent);

        notificationManager.notify(id, notification);
    }

	/**
	 * 建立快捷方式
	 * 
	 * @param context
	 */
	public static void createShortCut(Context context) {
		Intent returnIntent = new Intent();
		// 设置创建快捷方式的过滤器action
		returnIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		// 设置生成的快捷方式的名字
		returnIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
				context.getString(R.string.app_name));
		// 设置生成的快捷方式的图标
		returnIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
				Intent.ShortcutIconResource.fromContext(context,
						R.drawable.ic_launcher));
		Intent intent = new Intent(context, MainActivity.class);
		returnIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
		// 发送广播生成快捷方式
		context.sendBroadcast(returnIntent);
	}

	/**
	 * 判断快捷方式是否存在
	 * 
	 * @param context
	 * @return
	 */
	public static boolean hasShortCut(Context context) {
		String url = "";
		System.out.println(getSystemVersion());
		if (getSystemVersion() < 8) {
			url = "content://com.android.launcher.settings/favorites?notify=true";
		} else {
			url = "content://com.android.launcher2.settings/favorites?notify=true";
		}
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = resolver.query(Uri.parse(url), null, "title=?",
				new String[] { context.getString(R.string.app_name) }, null);

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				cursor.close();
				return true;
			}
			cursor.close();
		}

		return false;
	}

	private static int getSystemVersion() {
		return android.os.Build.VERSION.SDK_INT;
	}

	/**
	 * 显示创建快捷方式的提示
	 * @param context
	 */
	public static void showCreateShortDialog(final Context context) {

		Dialog dialog = new CustomDialog.Builder(context)
				.setTitle(context.getString(R.string.create_short))
				.setMessage(context.getString(R.string.create_short_desc))
				.setPositiveButton(context.getString(R.string.confirm),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (!Util.hasShortCut(context)) {
									Util.createShortCut(context);
								}
								dialog.dismiss();
							}
						})
				.setNegativeButton(context.getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.cancel();
							}
						}).create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}
	/**
	 * 显示开启悬浮窗提示
	 * @param context
	 */
	public static void showOpenFloatDialog(final Context context) {

		Dialog dialog = new CustomDialog.Builder(context)
				.setTitle(context.getString(R.string.open_float))
				.setMessage(context.getString(R.string.open_float_desc))
				.setPositiveButton(context.getString(R.string.confirm),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								MIUIHelper.getInstance().OpenFloatWindowSetting(context);
								dialog.dismiss();
							}
						})
				.setNegativeButton(context.getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.cancel();
							}
						}).create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

}
