package com.fang.span;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.fang.callsms.R;
import com.fang.common.controls.CustomDialog;
import com.fang.common.util.BaseUtil;
import com.fang.common.util.DebugLog;
import com.fang.common.util.Patterns;

public class MySpan extends ClickableSpan {

	private static String TAG = MySpan.class.getSimpleName();
	protected String mText;
	protected Context mContext;
	protected Handler mHandler;
    protected boolean mFloatView;
    protected WindowManager mWindowManager;
    protected Dialog mDialog;

	public MySpan(Context context, String text, Handler handler, boolean floatView) {
		mContext = context;
		mText = text;
		mHandler = handler;
        mFloatView = floatView;
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		super.updateDrawState(ds);
		ds.setColor(Color.argb(255, 54, 92, 124));
		ds.setUnderlineText(true);
	}

	@Override
	public void onClick(View widget) {
		handle(mContext, mText);
	}

	public void handle(final Context context, final String str) {
        if (mFloatView) {
            if (null == mWindowManager) {
                mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            }
        }
	}

    public void cancel(View view) {
        if (mFloatView) {
            BaseUtil.removeView(mWindowManager, view);
        } else  {
            mDialog.cancel();
        }
    }

    public void show(final View view) {

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                cancel(view);
            }
        });

        if (mFloatView) {
		    BaseUtil.addView(mWindowManager, view);
        } else  {
            if (null == mDialog) {
                mDialog = new CustomDialog.Builder(mContext).setContentView(view)
                        .setHeight(WindowManager.LayoutParams.MATCH_PARENT)
                        .create();
                mDialog.setCanceledOnTouchOutside(true);
            }
            mDialog.show();
        }
    }

	/**
	 * 格式化文本框里的内容，为号码 网址 验证码 添加链接
	 * @param context
	 * @param textView
	 * @param value
	 */
	public static void formatTextView(Context context, TextView textView, String value, boolean floatView) {
		if (null == textView || null == value) {
			return;
		}
		textView.setMovementMethod(LinkMovementMethod.getInstance());
		textView.setText(value);
		textView.setText(MySpan.formatText(context, textView.getText(), null, floatView));
	}
	/**
	 * 格式化文字
	 * 
	 * @param context
	 * @param text
	 * @param myHandler
	 * @return
	 */
	private static SpannableStringBuilder formatText(final Context context,
			CharSequence text, Handler myHandler, boolean floatView) {
		if (text instanceof Spannable) {
			String specialCharStr = "!/.?&%$#\"'()=-+:@_*";
			SpannableStringBuilder style = new SpannableStringBuilder(text);
			style.clearSpans();

			int end = text.length();
			String tmpSubString = "";

			int codeStart = -1;
			for (int i = 0; i < end; i++) {
				if (text.charAt(i) >= '0' && text.charAt(i) <= '9') {
					if (codeStart == -1) {
						codeStart = i;
					}
				} else if (text.charAt(i) >= 'a' && text.charAt(i) <= 'z') {
					if (codeStart == -1) {
						codeStart = i;
					}
				} else if (text.charAt(i) >= 'A' && text.charAt(i) <= 'Z') {
					if (codeStart == -1) {
						codeStart = i;
					}
				} else {
					if (codeStart >= 0) {
						if (text.charAt(i) == '.') {
							continue;
						}
						
						tmpSubString = text.subSequence(codeStart, i)
								.toString();
						DebugLog.d(TAG, tmpSubString);
                        if (tmpSubString.contains(".") || tmpSubString.contains("http")) {
                            if (specialCharStr.indexOf(text.charAt(i)) >= 0) {
                                continue;
                            }
                        }

                        // 链接
						if (tmpSubString.matches(Patterns.URL_PATTERN)) {
							MyURLSpan span = new MyURLSpan(context, tmpSubString, myHandler, floatView);
							style.setSpan(span, codeStart, i,
									Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						}
						// 邮箱
						else if (tmpSubString.matches(Patterns.MAIL_PATTERN)) {
							MyEmailSpan span = new MyEmailSpan(context,tmpSubString, myHandler, floatView);
							style.setSpan(span, codeStart, i,
									Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						}
						// 电话号码
						else if (tmpSubString.matches(Patterns.PHONE_NUMBER_PATTERN)) {
							MyPhoneSpan span = new MyPhoneSpan(context, tmpSubString, myHandler, floatView);
							style.setSpan(span, codeStart, i,
									Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						}
						// 验证码
						else if (tmpSubString.matches(Patterns.CODE_PATTERN)) {
							MyCodeSpan span = new MyCodeSpan(context, text
									.subSequence(codeStart, i).toString(),
									myHandler, floatView);
							style.setSpan(span, codeStart, i,
									Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						}
						codeStart = -1;
					}
				}
			}
			//最后
			if (codeStart >= 0) {
				int i = end;
				tmpSubString = text.subSequence(codeStart, i).toString();
				DebugLog.d(TAG, "2:" + tmpSubString);
				// 链接
				if (tmpSubString.matches(Patterns.URL_PATTERN)) {
					MyURLSpan span = new MyURLSpan(context, tmpSubString,
							myHandler, floatView);
					style.setSpan(span, codeStart, i,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
				// 邮箱
				else if (tmpSubString.matches(Patterns.MAIL_PATTERN)) {
					MyEmailSpan span = new MyEmailSpan(context,
							tmpSubString, myHandler, floatView);
					style.setSpan(span, codeStart, i,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
				// 电话号码
				else if (tmpSubString.matches(Patterns.PHONE_NUMBER_PATTERN)) {
					MyPhoneSpan span = new MyPhoneSpan(context, tmpSubString, myHandler, floatView);
					style.setSpan(span, codeStart, i,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
				// 验证码
				else if (tmpSubString.matches(Patterns.CODE_PATTERN)) {
					MyCodeSpan span = new MyCodeSpan(context, text.subSequence(codeStart, i).toString(),
							myHandler, floatView);
					style.setSpan(span, codeStart, i,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
				codeStart = -1;
			}
			return style;
		}
		return new SpannableStringBuilder(text);
	}
}
