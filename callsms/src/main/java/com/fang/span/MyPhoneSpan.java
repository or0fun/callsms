package com.fang.span;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.fang.callsms.R;
import com.fang.contact.ContactHelper;
import com.fang.common.util.BaseUtil;

public class MyPhoneSpan extends MySpan {

	public MyPhoneSpan(Context context, String text, Handler handler, boolean floatView) {
		super(context, text, handler, floatView);
	}

	@Override
	public void handle(final Context context, final String str) {
		super.handle(context, str);
		final View view = LayoutInflater.from(mContext).inflate(
				R.layout.handle_phone_number, null);
		((TextView) view.findViewById(R.id.tip)).setText(str);
		view.findViewById(R.id.call).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
                cancel(view);
				BaseUtil.gotoCall(mContext, mText);
			}
		});
		view.findViewById(R.id.add).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
                cancel(view);
				ContactHelper.addContact(context, str);
			}
		});
		view.findViewById(R.id.copy).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
                cancel(view);
				BaseUtil.copy(context, str);
			}
		});
        show(view);
	}
}