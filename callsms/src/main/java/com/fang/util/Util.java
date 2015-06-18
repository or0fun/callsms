package com.fang.util;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import com.fang.common.base.Global;
import com.fang.callsms.MainActivity;
import com.fang.callsms.R;
import com.fang.common.controls.CustomDialog;
import com.fang.common.util.BaseUtil;
import com.fang.common.util.DebugLog;
import com.fang.common.util.MIUIHelper;
import com.fang.datatype.CallFrom;
import com.fang.datatype.ExtraName;
import com.fang.listener.IDeleteConfirmListener;
import com.fang.receiver.AlarmReceiver;

public class Util {

    private static final String TAG = "Util";
    private static final String ACTION_URL = "com.fang.url";

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
		DebugLog.d(TAG, "cancel" + requestCode);
	}

	/**
	 * 删除确认
	 */
	public static void deleteConfirm(Context context,
			final WindowManager windowManager, final int id,
			final int position, final IDeleteConfirmListener deleteConfirm) {
		final View confirmView = LayoutInflater.from(context).inflate(
				R.layout.delete_confirm, null);
        confirmView.findViewById(R.id.todelete).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                BaseUtil.removeView(windowManager, confirmView);
                BaseUtil.excute(new Runnable() {
                    @Override
                    public void run() {
                        if (null != deleteConfirm) {
                            deleteConfirm.delete(id, position);
                        }
                    }
                });
            }
        });
		confirmView.findViewById(R.id.cancel).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View arg0) {
                        windowManager.removeView(confirmView);
					}
				});
        BaseUtil.addView(windowManager, confirmView);
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
	 * 建立快捷方式
	 * 
	 * @param context
	 */
	public static void createShortCut(Context context, String name, int callFrom) {
		Intent returnIntent = new Intent();
		// 设置创建快捷方式的过滤器action
		returnIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		// 不允许重复创建
        returnIntent.putExtra("duplicate", false);
		// 设置生成的快捷方式的名字
		returnIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
		// 设置生成的快捷方式的图标
		returnIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
				Intent.ShortcutIconResource.fromContext(context,
						R.drawable.we));
		Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(ExtraName.CALL_FROM, callFrom);
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
									Util.createShortCut(context, context.getString(R.string.app_name), CallFrom.LOCAL);
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
								MIUIHelper.getInstance().openFloatWindowSetting(context);
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
     * 打开webview
     * @param url
     */
    public static void openUrl(String url) {
        Intent intent = new Intent();
        intent.setAction(ACTION_URL);
        intent.putExtra(ExtraName.URL, url);
        Global.mContext.startActivity(intent);
    }
}
