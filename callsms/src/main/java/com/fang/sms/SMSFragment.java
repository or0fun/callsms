package com.fang.sms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.fang.base.BaseActivity;
import com.fang.base.BaseFragment;
import com.fang.callsms.R;
import com.fang.common.CustomConstant;
import com.fang.common.util.BaseUtil;
import com.fang.common.util.StringUtil;
import com.fang.datatype.ExtraName;
import com.fang.logs.LogCode;
import com.fang.logs.LogOperate;
import com.fang.receiver.AlarmReceiver;
import com.fang.util.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class SMSFragment extends BaseFragment implements Runnable,
	 OnClickListener {

	/** 选择联系人 */
	private Button mSelectContact;
	/** 已选择联系人编辑框 */
	private TextView mSelectedContactsTextView;
	/** 已选择联系人提示 */
	private TextView mSelectedContactsTipTextView;
	/** 发送时间 */
	private TextView mSendTimeTextView;
	/** 短信内容 */
	private EditText mContentEditText;
	/** 发送按钮 */
	private Button mSendButton;
	/** 打开队列里的定时短信列表 */
	private ImageButton mOpenListButton;

	private int REQUEST_CODE = 1;
	/** 已选择的联系人信息 */
	private ArrayList<HashMap<String, Object>> mListDataSelected;
	/** 发送时间 */
	private Calendar mSendCalendar;
	/** 收短信的号码 */
	private List<String> mReceiverList;
	/** 定时发送的短信 */
	private List<SendSMSInfo> mSendSMSInfoList;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			default:
				break;
			}
		}

	};

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		mListDataSelected = new ArrayList<HashMap<String, Object>>();
		mReceiverList = new ArrayList<String>();
		mSendSMSInfoList = SMSHelper.getSendSMSInfos(mContext);
		if (null == mSendSMSInfoList) {
			mSendSMSInfoList = new ArrayList<SendSMSInfo>();
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.sms_layout, container, false);

		mSelectContact = (Button) rootView.findViewById(R.id.select);
		mSelectContact.setOnClickListener(this);

		mSelectedContactsTipTextView = (TextView) rootView
				.findViewById(R.id.contactsTip);
		mSelectedContactsTextView = (TextView) rootView
				.findViewById(R.id.contacts);
		mSelectedContactsTipTextView.setText(String.format(
				getString(R.string.sms_contactSelectedTip),
				mListDataSelected.size()));

		mContentEditText = (EditText) rootView.findViewById(R.id.content);

		mSendTimeTextView = (TextView) rootView.findViewById(R.id.sendTime);
		mSendTimeTextView.setOnClickListener(this);

		mSendButton = (Button) rootView.findViewById(R.id.send);
		mSendButton.setOnClickListener(this);
		
		mOpenListButton = (ImageButton)rootView.findViewById(R.id.more);
		mOpenListButton.setOnClickListener(this);

		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void run() {
	}

	@Override
	public boolean onBackPressed() {

		return super.onBackPressed();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				if (null != mReceiverList) {
					mReceiverList = null;
				}
				mReceiverList = new ArrayList<String>();

				mListDataSelected = (ArrayList<HashMap<String, Object>>) data
						.getSerializableExtra(CustomConstant.SELECT_CONTACT_PARAMETER);
				if (mListDataSelected != null) {
                    StringBuilder contactsBuffer = new StringBuilder();
					for (HashMap<String, Object> map : mListDataSelected) {
						contactsBuffer.append(map.get(ExtraName.PARAM_NAME));
						contactsBuffer.append(";");

						mReceiverList.add((String) map.get(ExtraName.PARAM_NUMBER));
					}
					mSelectedContactsTipTextView.setText(String.format(
							getString(R.string.sms_contactSelectedTip),
							mListDataSelected.size()));
					mSelectedContactsTextView
							.setText(contactsBuffer.toString());
				}
			}
		}
	}

	/**
	 * 显示 选择日期时间的对话框
	 */
	private void ShowDateTimePickerDialog() {

		final View dateTimeView = View.inflate(mContext,
				R.layout.date_time_dialog, null);
		final DatePicker datePicker = (DatePicker) dateTimeView
				.findViewById(R.id.date_picker);
		final TimePicker timePicker = (android.widget.TimePicker) dateTimeView
				.findViewById(R.id.time_picker);
		final Button confirmButton = (Button) dateTimeView
				.findViewById(R.id.confirm);
		final Button cancelButton = (Button) dateTimeView
				.findViewById(R.id.back);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
                BaseUtil.removeView(mWindowManager, dateTimeView);
			}
		});
		confirmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                StringBuilder sb = new StringBuilder();
				if (null == mSendCalendar) {
					mSendCalendar = Calendar.getInstance();
				}
				mSendCalendar.set(datePicker.getYear(), datePicker.getMonth(),
						datePicker.getDayOfMonth(),
						timePicker.getCurrentHour(),
						timePicker.getCurrentMinute());

				sb.append(String.format("%d-%02d-%02d", datePicker.getYear(),
						datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
				sb.append("  ");
				sb.append(timePicker.getCurrentHour()).append(":")
						.append(timePicker.getCurrentMinute());
				mSendTimeTextView.setText(sb.toString());
				mWindowManager.removeView(dateTimeView);
			}
		});
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
				cal.get(Calendar.DAY_OF_MONTH), null);

		timePicker.setIs24HourView(true);
		timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
		timePicker.setCurrentMinute(cal.get(Calendar.MINUTE));

		BaseUtil.addView(mWindowManager, dateTimeView);
	}

	@Override
	public void onClick(View view) {
		if (view == mSendButton) {
			// 发送短信
			sendSMS();
		} else if (view == mSelectContact) {
			// 选择联系人
			Intent intent = new Intent(mContext, SelectContactActivity.class);
			intent.putExtra(CustomConstant.SELECT_CONTACT_PARAMETER,
					mListDataSelected);
			startActivityForResult(intent, REQUEST_CODE);
		} else if (mSendTimeTextView == view) {
			ShowDateTimePickerDialog();
		} else if (mOpenListButton == view) {
			BaseUtil.startActivity(mContext, SMSQueueActivity.class);
		}
	}

	/**
	 * 发送短信
	 */
	private void sendSMS() {

		if (null == mReceiverList) {
			Toast.makeText(mContext, getString(R.string.sms_empty_contact_tip), Toast.LENGTH_SHORT).show();
		} else {
			String content = mContentEditText.getText().toString();
			if (StringUtil.isEmpty(content)) {
				Toast.makeText(mContext, getString(R.string.sms_empty_content_tip), Toast.LENGTH_SHORT).show();
			} else {
				// 定时发送短信
				if (null == mSendCalendar || mSendCalendar.before(Calendar.getInstance())) {
					SMSHelper.sendSMS(mReceiverList, content, null, null);
				} else {
					//日志
					LogOperate.updateLog(mContext, LogCode.SMS_TIMING_SMS_USED);
					int code = AlarmReceiver.requestCode++;
					SendSMSInfo info = new SendSMSInfo(mReceiverList, content,
							code, mSendCalendar.getTimeInMillis());

					mSendSMSInfoList.add(info);
					SharedPreferencesHelper.getInstance().setObject(
                            SharedPreferencesHelper.TIMING_SMS_INFO,
                            mSendSMSInfoList);
					
					Intent intent = new Intent(mContext, AlarmReceiver.class);
					intent.putExtra(AlarmReceiver.INFO, info);
					BaseUtil.registerAlarm(mContext, intent, code,
                            mSendCalendar.getTimeInMillis());

				}
				clearSMSInfo();
				mReceiverList = null;
				((BaseActivity)getActivity()).showTip(getString(R.string.sms_into_queue));
			}
		}
	}
	/**
	 * 清空当前界面的短信信息
	 */
	private void clearSMSInfo() {
		mListDataSelected = null;
		mListDataSelected = new ArrayList<HashMap<String,Object>>();
		mReceiverList = null;
		mSelectedContactsTextView.setText("");
		mSendTimeTextView.setText(getString(R.string.sms_selectSendTime));
		mSelectedContactsTipTextView.setText(String.format(
				getString(R.string.sms_contactSelectedTip),0));
		mContentEditText.setText("");
	}
}
