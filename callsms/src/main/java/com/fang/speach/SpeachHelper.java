package com.fang.speach;

import android.content.Context;

import com.fang.common.util.DebugLog;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;

public class SpeachHelper {

	private final String TAG = "SpeachHelper";
	private static SpeachHelper mInstance;
	// 初始化合成对象
	private SpeechSynthesizer mTts;

	private SpeachHelper(Context context) {
		SpeechUtility
				.createUtility(context, SpeechConstant.APPID + "=53bff086");

		// 初始化合成对象
		mTts = SpeechSynthesizer.createSynthesizer(context, mInitListener);
		mTts.setParameter(SpeechConstant.VOICE_NAME, "vinn");//童年女声
		mTts.setParameter(SpeechConstant.SPEED, "50");
		mTts.setParameter(SpeechConstant.VOLUME, "80");
		mTts.setParameter(SpeechConstant.STREAM_TYPE, "2");
		
		mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");
	}

	public static SpeachHelper getInstance(Context context) {
		if (null == mInstance) {
			synchronized (SpeachHelper.class) {
				if (null == mInstance) {
					mInstance = new SpeachHelper(context);
				}
			}
		}
		return mInstance;
	}

	public SpeechSynthesizer getSpeechSynthesizer() {
		return mTts;
	}

	public void onDestroy() {
		if (null != mTts) {
			mTts.destroy();
		}
	}

	public SpeechSynthesizer getmTts() {
		return mTts;
	}

	/**
	 * 初始化监听器。
	 */
	private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			DebugLog.d(TAG, "SpeechRecognizer init() code = " + code);
			if (code == ErrorCode.SUCCESS) {
			}
		}
	};
}
