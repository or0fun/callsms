package com.fang.span;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.fang.util.Util;

public class MyCodeSpan extends MySpan {

	public MyCodeSpan(Context context, String text, Handler handler) {
		super(context, text, handler);
	}
	@Override
	public void handle(final Context context, final String str) {
		super.handle(context, str);
		Util.copy(mContext, mText);
		Toast.makeText(mContext, mText + " 已复制。",
				Toast.LENGTH_SHORT).show();
	}
}