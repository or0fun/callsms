package com.fang.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.fang.common.CustomConstant;
import com.fang.util.SharedPreferencesHelper;

import java.util.Date;

public abstract class BaseActivity extends Activity {
	
	protected Context mContext;
	private static Toast mToast;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
	}

	/**
	 * 显示 Toast
	 * @param str
	 */
	public void showTip(final String str)
	{
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (null == mToast) {
					mToast = Toast.makeText(mContext, str,
							Toast.LENGTH_SHORT);
				} else {
					mToast.setText(str);
				}
				mToast.show();
			}
		});
	}
	
	/**
	 * 检查更新版本
	 */
	public boolean isNeedUpdateVersion() {
		long now = new Date().getTime();
		long last = SharedPreferencesHelper.getLong(mContext,
                SharedPreferencesHelper.LAUNCH_LAST_TIME, 0);
		SharedPreferencesHelper.setLong(mContext,
				SharedPreferencesHelper.LAUNCH_LAST_TIME, new Date().getTime());
		if (now - last > CustomConstant.ONE_DAY) {
			return true;
		}
		return false;
	}
	
}
