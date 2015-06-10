package com.fang.span;

import android.content.Context;
import android.os.Handler;

import com.fang.common.util.BaseUtil;

public class MyCodeSpan extends MySpan {

	public MyCodeSpan(Context context, String text, Handler handler, boolean floatView) {
		super(context, text, handler, floatView);
	}
	@Override
	public void handle(final Context context, final String str) {
		super.handle(context, str);
		BaseUtil.copy(mContext, mText);
	}
}