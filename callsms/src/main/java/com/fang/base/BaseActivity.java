package com.fang.base;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.fang.common.CustomConstant;
import com.fang.common.base.Global;
import com.fang.util.SharedPreferencesHelper;
import com.fang.weixin.WXEntryActivity;

import java.util.Date;

public abstract class BaseActivity extends WXEntryActivity {
	
	protected Context mContext;
	private static Toast mToast;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;

        Global.mContext = this;
	}

    @Override
    protected void onResume() {
        super.onResume();

        Global.mContext = this;
    }

    /**
	 * 显示 Toast
	 * @param str
	 */
	public void showTip(final String str) {
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
		long last = SharedPreferencesHelper.getInstance().getLong(
                SharedPreferencesHelper.LAUNCH_LAST_TIME, 0);
		SharedPreferencesHelper.getInstance().setLong(
                SharedPreferencesHelper.LAUNCH_LAST_TIME, new Date().getTime());
		if (now - last > CustomConstant.ONE_DAY) {
			return true;
		}
		return false;
	}
	
}
