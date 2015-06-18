package com.fang.sms;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.fang.business.BusinessHelper;
import com.fang.callsms.MainActivity;
import com.fang.callsms.MySMSMessage;
import com.fang.callsms.R;
import com.fang.common.util.BaseUtil;
import com.fang.contact.ContactHelper;
import com.fang.listener.IDeleteConfirmListener;
import com.fang.logs.LogCode;
import com.fang.logs.LogOperate;
import com.fang.span.MySpan;
import com.fang.speach.SpeachHelper;
import com.fang.util.MessageWhat;
import com.fang.util.SharedPreferencesHelper;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

/**
 * 来信弹窗
 * 
 * @author fang
 * 
 */
public class SMSDialog implements OnClickListener {

	private String TAG = "SMSDialog";

	public final String MAIL_PATTERN = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
	public final String PHONE_NUMBER_PATTERN = "^((\\d{3,4}-)?\\d{7,8})|(13[0-9]{9})$";
	public final String CODE_PATTERN = "^([0-9a-zA-Z]+)$";
	public final String URL_PATTERN = "^[a-zA-z]+://[^\\s]*$";

	public static final int MSG_REMOVE = 100;
	protected View mView;
	protected Context mContext;
	protected WindowManager mWindowManager = null;
	protected WindowManager.LayoutParams mLayoutParams = null;
	protected boolean isShowing = false;
	private Vibrator mVibrator;
	// 号码
	private String mNumberString;
	// 联系人
	private String mNameString;
	// 短信内容
	private String mBodyString;
	// 短信ID
	private int mID;

	// logo
	protected ImageButton mLogoImageButton;
	// 发信人
	protected TextView mSenderTextView;
	// 归属地
	protected TextView mInfoTextView;
	// 内容
	protected TextView mMsgBodyTextView;
	// 复制号码
	protected Button mCopyNumberButton;
	// 添加为联系人／ 复制名字
	protected Button mAddOrCopyButton;

	// 删除
	protected Button mDeleteButton;
	// 播报
	protected Button mBroadcastButton;

	protected Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MessageWhat.NET_REQUEST_NUMBER:
				mInfoTextView.setText((String) msg.obj);
				break;
			case MSG_REMOVE:
				remove();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	public SMSDialog(Context context, MySMSMessage msg) {

		mContext = context;
		mView = LayoutInflater.from(context).inflate(R.layout.sms_dialog, null);
		mVibrator = (Vibrator) context.getApplicationContext()
				.getSystemService(Context.VIBRATOR_SERVICE);
		mWindowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		setLayoutParams();

		mLogoImageButton = (ImageButton) mView.findViewById(R.id.logo);
		mLogoImageButton.setOnClickListener(this);
		mSenderTextView = (TextView) mView.findViewById(R.id.sender);
		mInfoTextView = (TextView) mView.findViewById(R.id.info);
		mMsgBodyTextView = (TextView) mView.findViewById(R.id.body);
		mCopyNumberButton = (Button) mView.findViewById(R.id.copy);
		mCopyNumberButton.setOnClickListener(this);
		mAddOrCopyButton = (Button) mView.findViewById(R.id.add);
		mAddOrCopyButton.setOnClickListener(this);

		mDeleteButton = (Button) mView.findViewById(R.id.delete);
		mDeleteButton.setOnClickListener(this);
		mBroadcastButton = (Button) mView.findViewById(R.id.broadcast);
		mBroadcastButton.setOnClickListener(this);

        mView.findViewById(R.id.reply).setOnClickListener(this);
        mView.findViewById(R.id.call).setOnClickListener(this);
        mView.findViewById(R.id.close).setOnClickListener(this);

		setContent(msg);
	}

	// 设置LayoutParams
	private void setLayoutParams() {
		if (null == mLayoutParams) {
			mLayoutParams = new WindowManager.LayoutParams();
			mLayoutParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
			mLayoutParams.gravity = Gravity.CENTER;
			mLayoutParams.width = WindowManager.LayoutParams.FILL_PARENT;
			mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		}
	}

	// 填充内容
	private void setContent(MySMSMessage msg) {
		mNumberString = msg.getSenderString();
		mBodyString = msg.getBodyString();
		mID = msg.getmID();

		BusinessHelper.getNumberInfo(mContext, mNumberString, myHandler);
		mNameString = ContactHelper.getPerson(mContext, mNumberString);
		if (!TextUtils.isEmpty(mNameString)) {
			mAddOrCopyButton.setText(mContext.getString(R.string.copyName));
		}
		String senderString = "";
		if (null != mNameString && mNameString.length() > 0) {
			senderString = mNameString + "(" + mNumberString + ")";
		} else {
			senderString = mNumberString;
		}
		mSenderTextView.setText(senderString);
		mInfoTextView.setText(mContext.getString(R.string.getting_info));

		MySpan.formatTextView(mContext, mMsgBodyTextView, mBodyString, true);
	}

	/**
	 * 显示弹窗
	 */
	public void show() {
		if (false == isShowing) {
			isShowing = true;
			long[] pattern = { 100, 1000 }; // 停止 开启
			mVibrator.vibrate(pattern, -1); // 重复两次上面的pattern 如果只想震动一次，index设为-1
			mWindowManager.addView(mView, mLayoutParams);
			// 日志
			LogOperate.updateLog(mContext, LogCode.SMS_INCOMING_DIALOG_SHOW);
			//播报
			if (BaseUtil.isWiredHeadsetOn(mContext)) {
				if (SharedPreferencesHelper.getInstance().getBoolean(
                        SharedPreferencesHelper.SETTING_BROADCAST_WHEN_WIREDHEADSETON, true)) {
					broadcastContent(createSMSBroadcastContent());
					// 日志
					LogOperate.updateLog(mContext, LogCode.SMS_BROADCAST);
				}
			}
		}
	}
	/**
	 * 构造播报内容
	 * @return
	 */
	private String createSMSBroadcastContent() {
		String senderString = "";
		if (null != mNameString && mNameString.length() > 0) {
			senderString = mNameString;
		} else {
			senderString = mNumberString;
		}
		String content = mContext.getString(R.string.sms_broadcast_content);  
		return String.format(content, senderString, mBodyString);
	}

	/**
	 * 关闭弹窗
	 */
	public void remove() {
		if (isShowing) {
			isShowing = false;
            BaseUtil.removeView(mWindowManager, mView);
		}
		SpeachHelper.getInstance(mContext).getmTts().stopSpeaking();
	}

	/**
	 * 复制号码
	 */
	protected void copyNumber() {
		BaseUtil.copy(mContext, mNumberString);
	}

	/**
	 * 复制姓名
	 */
	protected void copyName() {
		BaseUtil.copy(mContext, mNameString);
		Toast.makeText(mContext, "姓名" + mNameString + "已复制。",
				Toast.LENGTH_SHORT).show();
	}

	/**
	 * 跳转到回复
	 * 
	 * @param number
	 */
	protected void gotoReply(String number) {
		Uri smsToUri = Uri.parse("smsto:" + number);
		Intent intent = new Intent(android.content.Intent.ACTION_SENDTO,
				smsToUri);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(intent);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.close:
			remove();
			break;
		case R.id.copy:
			copyNumber();
            remove();
			break;
		case R.id.add:
			if (TextUtils.isEmpty(mNameString)) {
				remove();
				ContactHelper.addContact(mContext, mNumberString);
			} else {
				copyName();
			}
			break;
		case R.id.reply:
            remove();
            gotoReply(mNumberString);
			break;
		case R.id.call:
			remove();
			BaseUtil.gotoCall(mContext, mNumberString);
			break;
		case R.id.broadcast:
			broadcastContent(mBodyString);
			break;
		case R.id.delete:
            remove();
            gotoReply(mNumberString);
//            Util.deleteConfirm(mContext, mWindowManager, mID, -1,
//                    mSMSDeleteConfirm);
			break;
		case R.id.logo:
			//记录日志
			LogOperate.updateLog(mContext, LogCode.ENTER_MAIN_ACTIVITY);
			
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setClass(mContext, MainActivity.class);
			BaseUtil.startActivityNewTask(mContext, intent);
			break;
		default:
			break;
		}
	}

	/**
	 * 删除短信
	 * 
	 * @param id
	 * @return
	 */
	protected void deleteSMS(int id) {
		mContext.getContentResolver().delete(Uri.parse("content://sms"),
				"_id=?", new String[] { String.format("%d", id) });
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
				mBroadcastButton
						.setText(mContext.getString(R.string.broadcast));
			} else {
				tts.startSpeaking(content, mSynListener);
				mBroadcastButton.setText(mContext
						.getString(R.string.stop_broadcast));
			}
		}
	}

	/**
	 *  删除监听
	 */
	protected IDeleteConfirmListener mSMSDeleteConfirm = new IDeleteConfirmListener() {
		@Override
		public void delete(int id, int position) {
			remove();
			deleteSMS(id);
		}
	};
	
	/**
	 * 合成监听器
	 */
	private SynthesizerListener mSynListener = new SynthesizerListener() {
		// 会话结束回调接口,没有错误时,error为null
		public void onCompleted(SpeechError error) {
			mBroadcastButton.setText(mContext
				.getString(R.string.broadcast));
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
			mBroadcastButton.setText(mContext
					.getString(R.string.broadcast));
		}

		// 播放进度回调
		// //percent为播放进度0~100,beginPos为播放音频在文本中开始位置,endPos表示播放音频在文本中结束位置.
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
		} 
		// 恢复播放回调接口
		public void onSpeakResumed() {
		}
	};
}
