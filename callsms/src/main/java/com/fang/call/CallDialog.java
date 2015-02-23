package com.fang.call;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fang.business.BusinessHelper;
import com.fang.callsms.R;
import com.fang.comment.CommentHelper;
import com.fang.contact.ContactHelper;
import com.fang.logs.LogCode;
import com.fang.logs.LogOperate;
import com.fang.receiver.PhoneReceiver;
import com.fang.speach.SpeachHelper;
import com.fang.util.DebugLog;
import com.fang.util.MessageWhat;
import com.fang.util.SharedPreferencesHelper;
import com.fang.util.StringUtil;
import com.fang.util.Util;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

/**
 * 来电弹窗
 * 
 * @author fang
 * 
 */
public class CallDialog implements OnClickListener {

	protected final String TAG = "CallDialog";
	public static final int MSG_REMOVE = 100;
	public static final int NET_REQUEST = 101;
	// 显示单条吐槽
	public static final int MSG_SHOW_ONE_COMMENT = 102;
	protected View mView;
	protected Context mContext;
	protected WindowManager mWindowManager = null;
	protected WindowManager.LayoutParams mLayoutParams = null;
	protected boolean mIsShowing = false;
	protected int mType = PhoneReceiver.INCOMING_CALL;

	private Vibrator mVibrator;
	// 提示语
	protected TextView mTipTextView;
	// 号码
	private String mNumberString;
	// 联系人
	private String mNameString;
	// 号码资源
	private String mInfoString;
	// 发信人
	protected TextView mSenderTextView;
	// 归属地
	protected TextView mInfoTextView;
	// 记录
	protected TextView mRecordsTextView;
	// 挂断
	protected Button mHangupButton;
	// 接听
	protected Button mAnswerButton;
	// 吐槽
	protected TextView mCommentTextView;
	// 吐槽layout
	protected LinearLayout mCommentLayout;
	// 天气
	protected TextView mWeatherTextView;
	// 上一次通话信息
	protected TextView mLastRecordTextView;

	Animation mFlashingAnimation;

	JSONArray mCommentsArray;

	int mFlashingIndex = 0;

	//动画时间
	int ANIMATION_TIME = 4000;

	float mRawX = 0;
	float mRawY = 0;
	float mTouchStartX = 0;
	float mTouchStartY = 0;

	protected Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MessageWhat.NET_REQUEST_NUMBER:
				if (null == msg.obj) {
					break;
				}
				mInfoString = (String) msg.obj;
				mInfoTextView.setText(mInfoString);
				if (mType == PhoneReceiver.INCOMING_CALL) {
					// 来电播报
					if (Util.isWiredHeadsetOn(mContext)) {
						if (SharedPreferencesHelper
								.getBoolean(
										mContext,
										SharedPreferencesHelper.SETTING_BROADCAST_WHEN_WIREDHEADSETON,
										true)) {
							broadcastContent(createCallBroadcastContent());
							// 日志
							LogOperate.updateLog(mContext,
									LogCode.CALL_BROADCAST);
						}
					}
				}
				String []infosStrings = mInfoString.split(" ");
				String city = "";
				if (infosStrings.length == 1) {
					if (infosStrings[0].length() < 5) {
						city = infosStrings[0];
					}
				}else if (infosStrings.length == 2) {
					if (infosStrings[1].contains("中国")) {
						city = infosStrings[0];
					}else {
						city = infosStrings[1];
					}
				}else {
					city = infosStrings[1];
				}
				if (!StringUtil.isEmpty(city)) {
					BusinessHelper.getWeatherInfo(mContext, city, myHandler);
				}
				break;
			case MessageWhat.NET_REQUEST_WEATHER:
				if (null != msg.obj) {
					mWeatherTextView.setText((String) msg.obj);
					mWeatherTextView.setVisibility(View.VISIBLE);
				}
				break;
			case MessageWhat.CALL_RECORDS:
				if (null != msg.obj) {
					mRecordsTextView.setText((String) msg.obj);
				}
				break;
			case MessageWhat.MSG_SHOW_COMMENTS:
				if (null != msg.obj) {
					mCommentsArray = null;
					try {
						mCommentsArray = new JSONArray((String) msg.obj);
						if (null != mCommentsArray && mCommentsArray.length() > 0) {
							mFlashingIndex = -1;
							mCommentLayout.setVisibility(View.VISIBLE);
							showFlashComment();
						}
					} catch (JSONException e) {
						DebugLog.d(TAG, e.toString());
					}
				}
				break;
			case MSG_SHOW_ONE_COMMENT:
				if (null != msg.obj) {
					mCommentTextView.setText((String) msg.obj);
					mFlashingAnimation.reset();
					mCommentTextView.startAnimation(mFlashingAnimation);
				}
				break;
			case MSG_REMOVE:
				remove();
				break;
			case MessageWhat.MSG_LAST_RECORD_DATE:
				if (null != msg.obj) {
					mLastRecordTextView.setText((String) msg.obj);
					mLastRecordTextView.setVisibility(View.VISIBLE);
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	public CallDialog(Context context, String number, int type) {

		mContext = context;
		mFlashingAnimation = AnimationUtils.loadAnimation(mContext,
				R.anim.textviewtranslate);

		mView = LayoutInflater.from(context).inflate(R.layout.calling_dialog,
				null);
		mView.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				// 获取相对屏幕的坐标，即以屏幕左上角为原点
				mRawY = event.getRawY() - 20;
				int eventaction = event.getAction();
				switch (eventaction) {
				case MotionEvent.ACTION_DOWN: // 按下事件，记录按下时手指在悬浮窗的XY坐标值
					// 获取相对View的坐标，即以此View左上角为原点
					mTouchStartY = event.getY();
					break;
				case MotionEvent.ACTION_MOVE:
					updateViewPosition();
					break;
				case MotionEvent.ACTION_UP:
					updateViewPosition();
					mTouchStartY = 0;
				}
				return true;
			}
		});
		mView.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {

			@Override
			public void onSystemUiVisibilityChange(int vis) {
				if ((vis & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0) {
					// 当前状态是隐藏，显示它
					mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
				}
			}
		});
		
		mVibrator = (Vibrator) context.getApplicationContext()
				.getSystemService(Context.VIBRATOR_SERVICE);
		mWindowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		setLayoutParams();

		mCommentLayout = (LinearLayout) mView.findViewById(R.id.commentLayout);
		mSenderTextView = (TextView) mView.findViewById(R.id.sender);
		mInfoTextView = (TextView) mView.findViewById(R.id.info);
		mRecordsTextView = (TextView) mView.findViewById(R.id.record);
		mTipTextView = (TextView) mView.findViewById(R.id.tip);
		mCommentTextView = (TextView) mView.findViewById(R.id.comments);
		mWeatherTextView = (TextView) mView.findViewById(R.id.weather);
		mLastRecordTextView = (TextView) mView.findViewById(R.id.lastRecord);
		setType(type);
		setContent(number);
	}

	/**
	 * 设置类型
	 * 
	 * @param type
	 */
	public void setType(int type) {
		mType = type;
		if (type == PhoneReceiver.INCOMING_CALL) {
			mTipTextView.setText(mContext.getString(R.string.incoming));
		} else {
			mTipTextView.setText(mContext.getString(R.string.outgoing));
		}

	}

	// 设置LayoutParams
	private void setLayoutParams() {
		if (null == mLayoutParams) {
			mLayoutParams = new WindowManager.LayoutParams();
			mLayoutParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
			mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
			mLayoutParams.x = 0;
			mLayoutParams.y = 0;
			mLayoutParams.format = PixelFormat.RGBA_8888;
			mLayoutParams.width = WindowManager.LayoutParams.FILL_PARENT;
			mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
			mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
					| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		}
	}

	/**
	 * 设置内容
	 * 
	 * @param number
	 */
	private void setContent(String number) {
		mNumberString = number;

		mWeatherTextView.setVisibility(View.GONE);

		CommentHelper.getComments(mContext, mNumberString, myHandler);
		BusinessHelper.getNumberInfo(mContext, mNumberString, myHandler);
		CallHelper.getCallRecordsInfo(mContext, mNumberString, myHandler);
		CallHelper.getLastRecordInfo(mContext, mNumberString, myHandler);

		mNameString = ContactHelper.getPerson(mContext, mNumberString);
		String senderString = "";
		if (null != mNameString && mNameString.length() > 0
				&& !mNameString.equals(mNumberString)) {
			senderString = mNameString + "(" + mNumberString + ")";
		} else {
			senderString = mNumberString;
		}
		mSenderTextView.setText(senderString);
		mInfoTextView.setText(mContext.getString(R.string.getting_info));
	}

	/**
	 * 显示弹窗
	 */
	public void show() {
		if (false == mIsShowing) {
			mIsShowing = true;
			if (mType == PhoneReceiver.INCOMING_CALL) {
				long[] pattern = { 100, 500 }; // 停止 开启
				mVibrator.vibrate(pattern, -1); // 重复两次上面的pattern
												// 如果只想震动一次，index设为-1
			}
			mWindowManager.addView(mView, mLayoutParams);
		}
	}

	/**
	 * 构造播报内容
	 * 
	 * @return
	 */
	private String createCallBroadcastContent() {
		String senderString = "";
		if (null != mNameString && mNameString.length() > 0) {
			senderString = mNameString;
		} else {
			senderString = mNumberString;
		}
		String content = mContext.getString(R.string.call_broadcast_content);
		return String.format(content, senderString, mInfoString);
	}

	/**
	 * 播报短信内容
	 * 
	 * @param content
	 */
	protected void broadcastContent(String content) {
		SpeechSynthesizer tts = SpeachHelper.getInstance(mContext)
				.getSpeechSynthesizer();
		if (null != tts) {
			if (tts.isSpeaking()) {
				tts.stopSpeaking();
			} else {
				tts.startSpeaking(content, mSynListener);
			}
		}
	}

	/**
	 * 关闭弹窗
	 */
	public void remove() {
//		if (mIsShowing) {
			mIsShowing = false;
			mWindowManager.removeView(mView);
//		}
	}

	/**
	 * 复制号码
	 */
	protected void copyNumber() {
		Util.copy(mContext, mNumberString);
		Toast.makeText(mContext, "号码" + mNumberString + "已复制。",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		// case R.id.answer:
		// remove();
		// break;
		// case R.id.hangup:
		// copyNumber();
		// break;
		default:
			break;
		}
	}

	/**
	 * 更新悬浮窗
	 */
	private void updateViewPosition() {
		mLayoutParams.x = (int) (mRawX - mTouchStartX);
		mLayoutParams.y = (int) (mRawY - mTouchStartY);
		mWindowManager.updateViewLayout(mView, mLayoutParams);

	}

	/**
	 * 合成监听器
	 */
	private SynthesizerListener mSynListener = new SynthesizerListener() {
		// 会话结束回调接口,没有错误时,error为null
		public void onCompleted(SpeechError error) {
		}

		// 缓冲进度回调
		// //percent为缓冲进度0~100,beginPos为缓冲音频在文本中开始位置,endPos表示缓冲音频在文本中结束位置,info为附加信息。
		public void onBufferProgress(int percent, int beginPos, int endPos,
				String info) {
		}

		// 开始播放
		public void onSpeakBegin() {
		}

		// 暂停播放
		public void onSpeakPaused() {
		}

		// 播放进度回调
		// //percent为播放进度0~100,beginPos为播放音频在文本中开始位置,endPos表示播放音频在文本中结束位置.
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
		}

		// 恢复播放回调接口
		public void onSpeakResumed() {
		}
	};

	/**
	 * 显示flashing吐槽
	 */
	protected void showFlashComment() {

		mFlashingIndex++;
		if (null != mCommentsArray && mCommentsArray.length() > 0) {
			if (mCommentsArray.length() <= mFlashingIndex) {
				mFlashingIndex = 0;
			}
			String commentsString = "";
			JSONObject json;
			try {
				json = (JSONObject) mCommentsArray.get(mFlashingIndex);
				if (null != json) {
					commentsString = json
							.getString(CommentHelper.PARAM_CONTENT);
					myHandler.sendMessage(myHandler.obtainMessage(
							MSG_SHOW_ONE_COMMENT, commentsString));

					myHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							showFlashComment();
						}
					}, ANIMATION_TIME);
				}
			} catch (JSONException e) {
				DebugLog.d(TAG, e.toString());
			}
		}
	}

}