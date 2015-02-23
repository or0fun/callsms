package com.fang.call;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog.Calls;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.fang.base.BaseFragment;
import com.fang.business.BusinessHelper;
import com.fang.callsms.R;
import com.fang.controls.CustomProgressDialog;
import com.fang.database.NumberDatabaseManager;
import com.fang.listener.IDeleteConfirmListener;
import com.fang.listener.IPhoneStateListener;
import com.fang.logs.LogCode;
import com.fang.logs.LogOperate;
import com.fang.receiver.MainService;
import com.fang.receiver.PhoneReceiver;
import com.fang.util.MessageWhat;
import com.fang.util.StringUtil;
import com.fang.util.Util;

public class CallFragment extends BaseFragment implements OnClickListener, ICallRecordListener {
	/** 电话记录List */
	protected ListView mCallRecordListView;
	private final int SORT_BY_ALL = 0;
	private final int SORT_BY_OUTGOING = 1;
	private final int SORT_BY_INCOMING = 2;
	private final int SORT_BY_MISSED = 3;
	/** 通话记录数据 */
	protected List<Map<String, Object>> mCallRecords;
	protected List<Map<String, Object>> mAllCallRecords;
	protected List<Map<String, Object>> mOutgoingCallRecords;
	protected List<Map<String, Object>> mIncomingCallRecords;
	protected List<Map<String, Object>> mMissedCallRecords;

	private int mSortBy = SORT_BY_ALL;

	/** 刷新list通知 */
	protected final int REFRESH_LIST = 1;
	/** 删除通知 */
	protected final int REMOVE_AND_REFRESH_LIST = 2;
	/** 信息弹出框 */
	protected CallRecordDialog mCallRecordDialog;
	/** 每次通话记录条数 */
	protected final int READ_RECORD_COUNT_PER_TIME = 15;
	/** 填充列表的适配器 */
	private CallRecordAdapter mAdapter;
	/** 排序按钮 */
	private Button mOrderByAllButton;
	private ImageButton mOrderByOutgoingButton;
	private ImageButton mOrderByIncomingButton;
	private ImageButton mOrderByMissedButton;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MessageWhat.FRESH_CALL_RECORD:
				// 获取号码信息
				BusinessHelper.getNumberInfo(mContext, mAllCallRecords, mHandler);

				mOutgoingCallRecords.clear();
				mIncomingCallRecords.clear();
				mMissedCallRecords.clear();
				
				for (Map<String, Object> map : mAllCallRecords) {
					if (map.get(CallHelper.PARAM_TYPE).equals(
							Calls.INCOMING_TYPE)) {
						if (!mIncomingCallRecords.contains(map)) {
							mIncomingCallRecords.add(map);
						}
					} else if (map.get(CallHelper.PARAM_TYPE).equals(
							Calls.OUTGOING_TYPE)) {
						if (!mOutgoingCallRecords.contains(map)) {
							mOutgoingCallRecords.add(map);
						}
					} else if (map.get(CallHelper.PARAM_TYPE).equals(
							Calls.MISSED_TYPE)) {
						if (!mMissedCallRecords.contains(map)) {
							mMissedCallRecords.add(map);
						}
					}
				}
				UpdateList();
				CustomProgressDialog.cancel(mContext);
				break;
			case MessageWhat.UPDATE_NUMBER_DATABASE:
				if (null != mAllCallRecords) {
					for (Map<String, Object> map : mAllCallRecords) {
						if (null == map.get(CallHelper.PARAM_INFO)
								|| StringUtil.isEmpty(map.get(
										CallHelper.PARAM_INFO).toString())) {
							map.put(CallHelper.PARAM_INFO,
									NumberDatabaseManager.getInstance(mContext)
											.query(map.get(
													CallHelper.PARAM_NUMBER)
													.toString()));
						}
					}
					UpdateList();
				}
				break;
			case REMOVE_AND_REFRESH_LIST:
				freshCallRecords();
				break;
			default:
				break;
			}
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		PhoneReceiver.addListener(mPhoneStateListener);

		mCallRecords = new ArrayList<Map<String, Object>>();
		mAllCallRecords = new ArrayList<Map<String, Object>>();
		mOutgoingCallRecords = new ArrayList<Map<String, Object>>();
		mIncomingCallRecords = new ArrayList<Map<String, Object>>();
		mMissedCallRecords = new ArrayList<Map<String, Object>>();

		mCallRecords = mAllCallRecords;

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.call_layout, container, false);
		mOrderByAllButton = (Button) view.findViewById(R.id.byAll);
		mOrderByOutgoingButton = (ImageButton) view
				.findViewById(R.id.byOutgoing);
		mOrderByIncomingButton = (ImageButton) view
				.findViewById(R.id.byIncoming);
		mOrderByMissedButton = (ImageButton) view.findViewById(R.id.byMissed);
		mOrderByAllButton.setOnClickListener(this);
		mOrderByOutgoingButton.setOnClickListener(this);
		mOrderByIncomingButton.setOnClickListener(this);
		mOrderByMissedButton.setOnClickListener(this);

		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mCallRecords = mAllCallRecords;
		mAdapter = new CallRecordAdapter(mContext, mCallRecords);
		initListView();

		mOrderByAllButton.setBackgroundDrawable(mContext.getResources()
				.getDrawable(R.drawable.sub_pressed));
		
		CallHelper.registerLisetener(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (false == CallHelper.hasRead()) {
			freshCallRecords();
		}else {
			mAllCallRecords = CallHelper.getCallRecords();
			mHandler.sendEmptyMessage(MessageWhat.FRESH_CALL_RECORD);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		CallHelper.unregisterListener(this);
	}

	/**
	 * 初始化ListView
	 */
	protected void initListView() {
		mCallRecordListView = (ListView) mView.findViewById(R.id.callRecord);
		mCallRecordListView.setAdapter(mAdapter);
		mCallRecordListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				if (null == mCallRecordDialog) {
					mCallRecordDialog = new CallRecordDialog(mContext,
							(String) mCallRecords.get(position).get(
									CallHelper.PARAM_NUMBER),
							(String) mCallRecords.get(position).get(
									CallHelper.PARAM_NAME),
							CallHelper.getCallTypeString(
									mContext,
									(Integer) mCallRecords.get(position).get(
											CallHelper.PARAM_TYPE)),
							(Integer) mCallRecords.get(position).get(
									CallHelper.PARAM_ICON));
				} else {
					mCallRecordDialog.setContent(
							(String) mCallRecords.get(position).get(
									CallHelper.PARAM_NUMBER),
							(String) mCallRecords.get(position).get(
									CallHelper.PARAM_NAME),
							CallHelper.getCallTypeString(
									mContext,
									(Integer) mCallRecords.get(position).get(
											CallHelper.PARAM_TYPE)),
							(Integer) mCallRecords.get(position).get(
									CallHelper.PARAM_ICON));
				}
				mCallRecordDialog.show();
				// 日志
				LogOperate.updateLog(mContext, LogCode.CALL_ITEM_CLICK);
			}
		});
		mCallRecordListView
				.setOnItemLongClickListener(new OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> arg0,
							View arg1, int position, long arg3) {
						Util.deleteConfirm(
								mContext,
								mWindowManager,
								(Integer) mCallRecords.get(position).get(
										CallHelper.PARAM_ID), position,
								mCallRecordDeleteConfirm);
						return true;
					}
				});
	}

	@Override
	public boolean onBackPressed() {
		if (null != mCallRecordDialog && mCallRecordDialog.isShowing()) {
			mCallRecordDialog.remove();
			return true;
		}

		return super.onBackPressed();
	}

	/**
	 * 更新列表
	 */
	protected void UpdateList() {
		if (mSortBy == SORT_BY_ALL) {
			mCallRecords = mAllCallRecords;
		}else if (mSortBy == SORT_BY_OUTGOING) {
			mCallRecords = mOutgoingCallRecords;
		}else if (mSortBy == SORT_BY_INCOMING) {
			mCallRecords = mIncomingCallRecords;
		}else if (mSortBy == SORT_BY_MISSED) {
			mCallRecords = mMissedCallRecords;
		}
		mAdapter.setData(mCallRecords);
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * 删除确认监听
	 */
	protected IDeleteConfirmListener mCallRecordDeleteConfirm = new IDeleteConfirmListener() {
		@Override
		public void delete(int id, int position) {
			CallHelper.deleteCallRecord(mContext, id);
			CallHelper.setHasRead(false);

			Message msgMessage = mHandler.obtainMessage();
			msgMessage.what = REMOVE_AND_REFRESH_LIST;
			msgMessage.arg1 = position;
			mHandler.sendMessage(msgMessage);
		}
	};
	/**
	 * 电话状态监听
	 */
	protected IPhoneStateListener mPhoneStateListener = new IPhoneStateListener() {
		@Override
		public void onResult(int callType, String number) {
			if (callType == PhoneReceiver.CALL_STATE_OUTGOING
					|| callType == PhoneReceiver.CALL_STATE_RINGING) {
				mContext.startService(new Intent(mContext, MainService.class));
			}
		}
	};

	@Override
	public void onClick(View view) {
		mOrderByAllButton.setBackgroundDrawable(mContext.getResources()
				.getDrawable(R.drawable.sub_button_selector));
		mOrderByOutgoingButton.setBackgroundDrawable(mContext.getResources()
				.getDrawable(R.drawable.sub_button_selector));
		mOrderByIncomingButton.setBackgroundDrawable(mContext.getResources()
				.getDrawable(R.drawable.sub_button_selector));
		mOrderByMissedButton.setBackgroundDrawable(mContext.getResources()
				.getDrawable(R.drawable.sub_button_selector));
		if (view == mOrderByAllButton) { 
			mOrderByAllButton.setBackgroundDrawable(mContext.getResources()
					.getDrawable(R.drawable.sub_pressed));
			mSortBy = SORT_BY_ALL;
		} else if (view == mOrderByOutgoingButton) { 
			mOrderByOutgoingButton.setBackgroundDrawable(mContext
					.getResources().getDrawable(R.drawable.sub_pressed));
			mSortBy = SORT_BY_OUTGOING;
		} else if (view == mOrderByIncomingButton) { 
			mOrderByIncomingButton.setBackgroundDrawable(mContext
					.getResources().getDrawable(R.drawable.sub_pressed));
			mSortBy = SORT_BY_INCOMING;
		} else if (view == mOrderByMissedButton) { 
			mOrderByMissedButton.setBackgroundDrawable(mContext.getResources()
					.getDrawable(R.drawable.sub_pressed));
			mSortBy = SORT_BY_MISSED;
		}
		UpdateList();
	}

	@Override
	public void onResult(boolean result) {
		if (result) {
			if (mAllCallRecords.size() == 0) {
				mAllCallRecords.addAll(CallHelper.getCallRecords());
				mHandler.sendEmptyMessage(MessageWhat.FRESH_CALL_RECORD);
			}
		}
	}

	@Override
	public boolean isNeedLoading() {
		if (false == CallHelper.hasRead() && null == mAllCallRecords) {
			return true;
		}
		return false;
	}
	
	/**
	 *  重新获取通话记录
	 */
	private void freshCallRecords() {
		CustomProgressDialog.show(mContext);
		mAllCallRecords.clear();
		new Thread() {
			@Override
			public void run() {
				CallHelper.getCallRecordsList(mContext);
			}
		}.start();
	}
}
