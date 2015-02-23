package com.fang.controls;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.fang.callsms.R;
import com.fang.util.DebugLog;
import com.fang.util.SharedPreferencesHelper;

/**
 * 自定义switch设置开关
 * @author fang
 *
 */
public class CustomSlideSwitch extends View {

	public static final String TAG = "SlideSwitch";
	public static final int SWITCH_OFF = 0;// 关闭状态
	public static final int SWITCH_ON = 1;// 打开状态
	public static final int SWITCH_SCROLING = 2;// 滚动状态

	private int mSwitchStatus = SWITCH_OFF;

	private boolean mHasScrolled = false;// 表示是否发生过滚动

	private int mSrcX = 0, mDstX = 0;

	private int mBmpWidth = 0;
	private int mBmpHeight = 0;
	private int mThumbWidth = 0;

	private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	private OnSwitchChangedListener mOnSwitchChangedListener = null;
	
	protected String mKeyString;

	// 开关状态图
	Bitmap mSwitch_off, mSwitch_on, mSwitch_thumb;

	public CustomSlideSwitch(Context context) {
		this(context, null);
	}

	public CustomSlideSwitch(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CustomSlideSwitch(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		Resources res = getResources();
		mSwitch_off = BitmapFactory.decodeResource(res,
				R.drawable.bg_switch_off);
		mSwitch_on = BitmapFactory.decodeResource(res, R.drawable.bg_switch_on);
		mSwitch_thumb = BitmapFactory.decodeResource(res,
				R.drawable.switch_thumb);
		mBmpWidth = mSwitch_on.getWidth();
		mBmpHeight = mSwitch_on.getHeight();
		mThumbWidth = mSwitch_thumb.getWidth();
	}

	@Override
	public void setLayoutParams(LayoutParams params) {
		params.width = mBmpWidth;
		params.height = mBmpHeight;
		super.setLayoutParams(params);
	}

	/**
	 * 为开关控件设置状态改变监听函数
	 * 
	 * @param onSwitchChangedListener
	 *            参见 {@link OnSwitchChangedListener}
	 */
	public void setOnSwitchChangedListener(
			OnSwitchChangedListener onSwitchChangedListener) {
		mOnSwitchChangedListener = onSwitchChangedListener;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		DebugLog.d(TAG, "onTouchEvent  x=" + event.getX());
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mSrcX = (int) event.getX();
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			if (mHasScrolled == false)// 如果没有发生过滑动，就意味着这是一次单击过程
			{
				mSwitchStatus = Math.abs(mSwitchStatus - 1);
				int xFrom = 10, xTo = 62;
				if (mSwitchStatus == SWITCH_OFF) {
					xFrom = 62;
					xTo = 10;
				}
				AnimationTransRunnable runnable = new AnimationTransRunnable(
						xFrom, xTo, 1);
				new Thread(runnable).start();
			} else {
				invalidate();
				mHasScrolled = false;
			}
			// 状态改变的时候 回调事件函数
			if (mOnSwitchChangedListener != null) {
				mOnSwitchChangedListener.onSwitchChanged(this, mSwitchStatus);
			}
			break;

		default:
			break;
		}
		return true;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 绘图的时候 内部用到了一些数值的硬编码，其实不太好，
		// 主要是考虑到图片的原因，图片周围有透明边界，所以要有一定的偏移
		// 硬编码的数值只要看懂了代码，其实可以理解其含义，可以做相应改进。
		mPaint.setTextSize(14);
		mPaint.setTypeface(Typeface.DEFAULT_BOLD);

		if (mSwitchStatus == SWITCH_OFF) {
			drawBitmap(canvas, null, null, mSwitch_off);
			drawBitmap(canvas, null, null, mSwitch_thumb);
			mPaint.setColor(Color.rgb(105, 105, 105));
			canvas.translate(mSwitch_thumb.getWidth(), 0);
		} else if (mSwitchStatus == SWITCH_ON) {
			drawBitmap(canvas, null, null, mSwitch_on);
			int count = canvas.save();
			canvas.translate(mSwitch_on.getWidth() - mSwitch_thumb.getWidth(),
					0);
			drawBitmap(canvas, null, null, mSwitch_thumb);
			mPaint.setColor(Color.WHITE);
			canvas.restoreToCount(count);
		} else // SWITCH_SCROLING
		{
			mSwitchStatus = mDstX > 35 ? SWITCH_ON : SWITCH_OFF;
			drawBitmap(canvas, new Rect(0, 0, mDstX, mBmpHeight), new Rect(0,
					0, (int) mDstX, mBmpHeight), mSwitch_on);
			mPaint.setColor(Color.WHITE);

			int count = canvas.save();
			canvas.translate(mDstX, 0);
			drawBitmap(canvas, new Rect(mDstX, 0, mBmpWidth, mBmpHeight),
					new Rect(0, 0, mBmpWidth - mDstX, mBmpHeight), mSwitch_off);
			canvas.restoreToCount(count);

			count = canvas.save();
			canvas.clipRect(mDstX, 0, mBmpWidth, mBmpHeight);
			canvas.translate(mThumbWidth, 0);
			mPaint.setColor(Color.rgb(105, 105, 105));
			canvas.restoreToCount(count);

			count = canvas.save();
			canvas.translate(mDstX - mThumbWidth / 2, 0);
			drawBitmap(canvas, null, null, mSwitch_thumb);
			canvas.restoreToCount(count);
		}

	}

	public void drawBitmap(Canvas canvas, Rect src, Rect dst, Bitmap bitmap) {
		dst = (dst == null ? new Rect(0, 0, bitmap.getWidth(),
				bitmap.getHeight()) : dst);
		Paint paint = new Paint();
		canvas.drawBitmap(bitmap, src, dst, paint);
	}
	/**
	 * 设置对应的key
	 * @param key
	 */
	public void setKey(String key) {
		mKeyString = key;
		mSwitchStatus = SharedPreferencesHelper.getBoolean(getContext(), mKeyString, true) ? SWITCH_ON:SWITCH_OFF;
		invalidate();
	}

	/**
	 * AnimationTransRunnable 做滑动动画所使用的线程
	 */
	public class AnimationTransRunnable implements Runnable {
		private int srcX, dstX;
		private int duration;

		/**
		 * 滑动动画
		 * 
		 * @param srcX
		 *            滑动起始点
		 * @param dstX
		 *            滑动终止点
		 * @param duration
		 *            是否采用动画，1采用，0不采用
		 */
		public AnimationTransRunnable(float srcX, float dstX, final int duration) {
			this.srcX = (int) srcX;
			this.dstX = (int) dstX;
			this.duration = duration;
		}

		@Override
		public void run() {
			final int patch = (dstX > srcX ? 5 : -5);
			if (duration == 0) {
				CustomSlideSwitch.this.mSwitchStatus = SWITCH_SCROLING;
				CustomSlideSwitch.this.postInvalidate();
			} else {
				DebugLog.d(TAG, "start Animation: [ " + srcX + " , " + dstX + " ]");
				int x = srcX + patch;
				while (Math.abs(x - dstX) > 5) {
					mDstX = x;
					CustomSlideSwitch.this.mSwitchStatus = SWITCH_SCROLING;
					CustomSlideSwitch.this.postInvalidate();
					x += patch;
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				mDstX = dstX;
				CustomSlideSwitch.this.mSwitchStatus = mDstX > 35 ? SWITCH_ON
						: SWITCH_OFF;
				CustomSlideSwitch.this.postInvalidate();
				if (CustomSlideSwitch.this.mSwitchStatus == SWITCH_ON) {
					SharedPreferencesHelper.setBoolean(getContext(), mKeyString, true);
				}else {
					SharedPreferencesHelper.setBoolean(getContext(), mKeyString, false);
				}
			}
		}

	}

	public static interface OnSwitchChangedListener {
		/**
		 * 状态改变 回调函数
		 * 
		 * @param status
		 *            SWITCH_ON表示打开 SWITCH_OFF表示关闭
		 */
		public abstract void onSwitchChanged(CustomSlideSwitch obj, int status);
	}

}