package com.fang.span;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.fang.callsms.R;
import com.fang.util.Util;

public class MyURLSpan extends MySpan {

	public MyURLSpan(Context context, String text, Handler handler) {
		super(context, text, handler);
	}

	@Override
	public void handle(final Context context, final String str) {
		super.handle(context, str);
		final WindowManager mWindowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		final View view = LayoutInflater.from(mContext).inflate(
				R.layout.handle_link, null);
		((TextView) view.findViewById(R.id.tip)).setText(str);
		view.findViewById(R.id.open).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mWindowManager.removeView(view);
				Util.openURL(mContext, str);
			}
		});
		view.findViewById(R.id.copy).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mWindowManager.removeView(view);
				Util.copy(context, str);
			}
		});
		view.findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mWindowManager.removeView(view);
			}
		});
		view.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_OUTSIDE:
					mWindowManager.removeView(view);
					break;
				}
				return false;
			}
		});

		Util.addView(mWindowManager, view);
	}
}
