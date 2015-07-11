package com.fang.base;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.fang.callsms.R;
import com.fang.common.CustomConstant;
import com.fang.util.SharedPreferencesHelper;
import com.fang.weixin.WXEntryActivity;
import com.fang.weixin.WXShareHandler;

import java.util.Date;

public abstract class WEActivity extends WXEntryActivity {
	
	protected Context mContext;
	private static Toast mToast;
    protected WXShareHandler mShareHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;

        overridePendingTransition(R.anim.right_in, 0);

        if (null == mShareHandler) {
            mShareHandler = new WXShareHandler(mContext);
        }
	}

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.right_out);
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

    public WXShareHandler getShareHandler() {
        return mShareHandler;
    }
	
}
