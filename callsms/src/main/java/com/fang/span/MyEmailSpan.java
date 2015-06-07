package com.fang.span;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.fang.callsms.R;
import com.fang.util.Util;

public class MyEmailSpan extends MySpan {

	public MyEmailSpan(Context context, String text, Handler handler, boolean floatView) {
		super(context, text, handler, floatView);
	}

	@Override
	public void handle(final Context context, final String str) {
		super.handle(context, str);
		final View view = LayoutInflater.from(mContext).inflate(
				R.layout.handle_email, null);
		((TextView) view.findViewById(R.id.tip)).setText(str);
		view.findViewById(R.id.email).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
                cancel(view);
				Util.email(mContext, str);
			}
		});
		view.findViewById(R.id.copy).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
                cancel(view);
				Util.copy(context, str);
			}
		});

        show(view);
	}
}
