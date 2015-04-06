package com.fang.call;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog.Calls;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.fang.base.BaseFragment;
import com.fang.business.BusinessHelper;
import com.fang.callsms.R;
import com.fang.contact.ContactHelper;
import com.fang.controls.CustomProgressDialog;
import com.fang.listener.IDeleteConfirmListener;
import com.fang.listener.IPhoneStateListener;
import com.fang.receiver.MainService;
import com.fang.receiver.PhoneReceiver;
import com.fang.util.MessageWhat;
import com.fang.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	/** 每次通话记录条数 */
	protected final int READ_RECORD_COUNT_PER_TIME = 15;
	/** 填充列表的适配器 */
	private CallRecordAdapter mAdapter;
	/** 排序按钮 */
	private Button mOrderByAllButton;
	private ImageButton mOrderByOutgoingButton;
	private ImageButton mOrderByIncomingButton;
	private ImageButton mOrderByMissedButton;
    /** 查找联系人 */
    private EditText mSearchEditText;
    /** 映射联系人列表 */
    private SparseIntArray mContactPositionMapArray;

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
				updateList();
				CustomProgressDialog.cancel(mContext);
				break;
			case MessageWhat.UPDATE_NUMBER_DATABASE:
				if (null != mAllCallRecords) {
					updateList();
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
        mContactPositionMapArray = new SparseIntArray();
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
        view.findViewById(R.id.system_call).setOnClickListener(this);

        mSearchEditText = (EditText) view.findViewById(R.id.search);
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence text, int arg1, int arg2,
                                      int arg3) {
                searchContacts(text);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

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
		if (null != mAdapter && mAdapter.removeDialog()) {
			return true;
		}

		return super.onBackPressed();
	}

	/**
	 * 更新列表
	 */
	protected void updateList() {
		if (mSortBy == SORT_BY_ALL) {
			mCallRecords = mAllCallRecords;
		}else if (mSortBy == SORT_BY_OUTGOING) {
			mCallRecords = mOutgoingCallRecords;
		}else if (mSortBy == SORT_BY_INCOMING) {
			mCallRecords = mIncomingCallRecords;
		}else if (mSortBy == SORT_BY_MISSED) {
			mCallRecords = mMissedCallRecords;
		}
        mContactPositionMapArray.clear();
        for (int i = 0; i < mCallRecords.size(); i++) {
            mContactPositionMapArray.put(i, i);
        }
        mAdapter.setPositionMapArray(mContactPositionMapArray);
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
		} else if (view.getId() == R.id.system_call) {
            CallHelper.gotoCall(mContext);
        }
		updateList();
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


    /**
     * 查找联系人
     *
     * @param text
     */
    private void searchContacts(CharSequence text) {

        if (null == mAdapter || null == mCallRecords) {
            return;
        }
        if (null == text || text.length() == 0) {
            mContactPositionMapArray.clear();
            for (int i = 0; i < mCallRecords.size(); i++) {
                mContactPositionMapArray.put(i, i);
            }
        } else {

            mContactPositionMapArray.clear();
            int len = mCallRecords.size();
            int positon = 0;
            boolean isSearched = false;
            for (int i = 0; i < len; i++) {
                Map<String, Object> data = mCallRecords.get(i);
                if (null != data.get(ContactHelper.PARAM_NAME)) {
                    String name = (String) data.get(ContactHelper.PARAM_NAME);
                    if (name.contains(text)) {
                        mContactPositionMapArray.put(positon, i);
                        positon++;
                        isSearched = true;
                    }
                }
                if (false == isSearched) {
                    if (null != data.get(ContactHelper.PARAM_NUMBER)) {
                        String number = (String) data
                                .get(ContactHelper.PARAM_NUMBER);
                        if (number.contains(text)) {
                            mContactPositionMapArray.put(positon, i);
                            positon++;
                        }
                    }
                }
                isSearched = false;
            }
        }
        mAdapter.setPositionMapArray(mContactPositionMapArray);
        mAdapter.notifyDataSetChanged();
    }
}
