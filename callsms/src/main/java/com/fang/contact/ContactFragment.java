package com.fang.contact;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.fang.base.BaseFragment;
import com.fang.call.CallRecordDialog;
import com.fang.callsms.R;
import com.fang.contact.MyLetterListView.OnTouchingLetterChangedListener;
import com.fang.controls.CustomProgressDialog;
import com.fang.logs.LogCode;
import com.fang.logs.LogOperate;
import com.fang.util.DebugLog;
import com.fang.util.MessageWhat;
import com.fang.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 通讯录页面
 * 
 * @author fang
 * 
 */
public class ContactFragment extends BaseFragment implements IContactListener {

	private final String TAG = "ContactFragment";
	
	private final int SORT_BY_NAME = 0;
	private final int SORT_BY_TIMES = 1;
	
	private ListAdapter mAdapter;
	private ListView mListView;
	private TextView mOverlay;
	private MyLetterListView mLetterListView;
	private HashMap<String, Integer> mAlphaIndexer;
	private String[] mSections;
	/** 映射联系人列表 */
	private SparseIntArray mContactPositionMapArray;
	/** 查找联系人 */
	private EditText mSearchEditText;
	/** 通讯录数据 */
	public List<HashMap<String, Object>> mList;
	/** 按名字排序通讯录数据 */
	public List<HashMap<String, Object>> mByNameList = new ArrayList<HashMap<String, Object>>();
	/** 按通话次数排序通讯录数据 */
	public List<HashMap<String, Object>> mByTimesList = new ArrayList<HashMap<String, Object>>();
	/** 信息弹出框 */
	protected CallRecordDialog mCallRecordDialog;

	private WindowManager mWindowManager;

	private boolean mIsViewCreated = false;
	
	private int mSortBy = SORT_BY_NAME;
	
	private Button mSortByNameButton;
	private Button mSortByTimesbButton;
	private TextView mTitleTextView;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MessageWhat.FRESH_CONTACT_RECORD:
				
				if(mSortBy == SORT_BY_NAME) {
					mList = mByNameList;
				}else {
					mList = mByTimesList;
				}
				int len = mList.size();
				for (int i = 0; i < len; i++) {
					mContactPositionMapArray.put(i, i);
				}
				setAdapter(mList);
				
				mTitleTextView.setText(mContext.getString(R.string.title_bar_contact) + "(共有" + len + "个联系人)");
				CustomProgressDialog.cancel(mContext);
				break;
				default:
			}
		}
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mWindowManager = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
		mContactPositionMapArray = new SparseIntArray();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.contact_layout, container,
				false);
		mTitleTextView = (TextView) rootView.findViewById(R.id.title);
		mSortByTimesbButton = (Button)rootView.findViewById(R.id.byTimes);
		mSortByNameButton = (Button)rootView.findViewById(R.id.byName);
		mSortByTimesbButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mSortByTimesbButton.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.sub_pressed));
				mSortByNameButton.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.sub_normal));
				updateListViewBySortKey(SORT_BY_TIMES);
			}
		});
		mSortByNameButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mSortByNameButton.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.sub_pressed));
				mSortByTimesbButton.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.sub_normal));
				updateListViewBySortKey(SORT_BY_NAME);
			}
		});
		if (mSortBy == SORT_BY_TIMES) {
			mSortByTimesbButton.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.sub_pressed));
			mSortByNameButton.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.sub_normal));
		}else {
			mSortByNameButton.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.sub_pressed));
			mSortByTimesbButton.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.sub_normal));
		}
		
		mListView = (ListView) rootView.findViewById(R.id.list_view);
		mLetterListView = (MyLetterListView) rootView
				.findViewById(R.id.my_list_view);
		mLetterListView
				.setOnTouchingLetterChangedListener(new LetterListViewListener());

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				int index = mContactPositionMapArray.get(position);
				if (null == mCallRecordDialog) {
					mCallRecordDialog = new CallRecordDialog(mContext,
							(String) mList.get(index).get(
									ContactHelper.PARAM_NUMBER), (String) mList
									.get(index).get(ContactHelper.PARAM_NAME),
							(Bitmap) mList.get(index).get(
									ContactHelper.PARAM_PHOTO_ID));
				} else {
					mCallRecordDialog.setContent(
							(String) mList.get(index).get(
									ContactHelper.PARAM_NUMBER),
							(String) mList.get(index).get(
									ContactHelper.PARAM_NAME),
							(Bitmap) mList.get(index).get(
									ContactHelper.PARAM_PHOTO_ID));
				}
				mCallRecordDialog.show();
				// 日志
				LogOperate.updateLog(mContext, LogCode.CONTACT_ITEM_CLICK);
			}
		});

		initOverlay();

		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
		mSearchEditText = (EditText) rootView.findViewById(R.id.search);
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

        mSearchEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager inputManager =
                        (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mSearchEditText, InputMethodManager.SHOW_FORCED);
                return true;
            }
        });
		DebugLog.d(TAG, "onCreateView");
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mIsViewCreated = true;
		
		ContactHelper.registerListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		DebugLog.d(TAG, "onResume");
		mByNameList = ContactHelper.getContactByNameList();
		mByTimesList = ContactHelper.getContactByTimesList();
		
		if (null == mByNameList || mByNameList.size() == 0) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					//获取通讯录
					ContactHelper.readContact(mContext);
				}
			}).start();
		}else {
			mHandler.sendEmptyMessage(MessageWhat.FRESH_CONTACT_RECORD);
		}
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
	 * @param key
	 */
	protected void updateListViewBySortKey(int key) {
		mSortBy = key;
		if (mSortBy == SORT_BY_TIMES) {
			mLetterListView.setVisibility(View.GONE);
			mList = mByTimesList;
		}else {
			mLetterListView.setVisibility(View.VISIBLE);
			mList = mByNameList;
		}
		mAdapter.setData(mList);
		String textString = mSearchEditText.getText().toString().trim();
		if (textString.length() > 0) {
			searchContacts(textString);
		}
		mAdapter.notifyDataSetChanged();
	}
	/**
	 * 更新通讯录
	 * 
	 * @param showProgressDialog
	 */
	public void updateContacts(boolean showProgressDialog) {
		if (mIsViewCreated) {
			if (null != mList && mList.size() > 0) {
				setAdapter(mList);
			}
		}
	}

	private void setAdapter(List<HashMap<String, Object>> list) {
		mAdapter = new ListAdapter(mContext, list);
		mListView.setAdapter(mAdapter);

	}

	/**
	 * 填充通讯录列表
	 * 
	 * @author fang
	 * 
	 */
	private class ListAdapter extends BaseAdapter {

		private LayoutInflater inflater;

		private List<HashMap<String, Object>> list;

		public ListAdapter(Context context, List<HashMap<String, Object>> list) {
			this.inflater = LayoutInflater.from(context);
			this.list = list;
			mAlphaIndexer = new HashMap<String, Integer>();
			mSections = new String[list.size()];
			int len = mContactPositionMapArray.size();
			for (int i = 0; i < len; i++) {
				String currentStr = getAlpha(list
						.get(mContactPositionMapArray.get(i))
						.get(ContactHelper.PARAM_SORT_KEY).toString());
				String previewStr = (i - 1) >= 0 ? getAlpha(list
						.get(mContactPositionMapArray.get(i - 1))
						.get(ContactHelper.PARAM_SORT_KEY).toString()) : " ";
				if (!previewStr.equals(currentStr)) {
					mAlphaIndexer.put(currentStr, i);
					mSections[i] = currentStr;
				}
			}
		}

		@Override
		public int getCount() {
			return mContactPositionMapArray.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(mContactPositionMapArray.get(position));
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			final int index = mContactPositionMapArray.get(position);

			if (convertView == null) {
				convertView = inflater
						.inflate(R.layout.contact_list_item, null);
				holder = new ViewHolder();
				holder.alpha = (TextView) convertView.findViewById(R.id.alpha);
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.number = (TextView) convertView
						.findViewById(R.id.number);
				holder.icon = (ImageView) convertView
						.findViewById(R.id.imageView);
				holder.lastRecord = (TextView) convertView
						.findViewById(R.id.lastRecord);
				holder.totalRecord = (TextView) convertView
						.findViewById(R.id.totalRecord);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			HashMap<String, Object> cv = list.get(index);
			holder.name.setText(cv.get(ContactHelper.PARAM_NAME).toString());
			holder.number.setText(cv.get(ContactHelper.PARAM_NUMBER).toString());
			String currentStr = getAlpha(list.get(index)
					.get(ContactHelper.PARAM_SORT_KEY).toString());
			String previewStr = (position - 1) >= 0 ? getAlpha(list
					.get(mContactPositionMapArray.get(position - 1))
					.get(ContactHelper.PARAM_SORT_KEY).toString()) : " ";

			if (mSortBy == SORT_BY_NAME && !previewStr.equals(currentStr)) {
				holder.alpha.setVisibility(View.VISIBLE);
				holder.alpha.setText(currentStr);
			} else {
				holder.alpha.setVisibility(View.GONE);
			}
			holder.icon.setImageBitmap((Bitmap) list.get(index).get(
					ContactHelper.PARAM_PHOTO_ID));
			String lastString = (String) list.get(index).get(
					ContactHelper.PARAM_LAST_RECORD_DATE);
			String totalString = String.format("%d",
					list.get(index).get(ContactHelper.PARAM_TIMES_CONTACTED));
			if (StringUtil.isEmpty(lastString)) {
				holder.lastRecord.setText(mContext
						.getString(R.string.contact_no_record));
				holder.totalRecord.setVisibility(View.GONE);
			} else {
				holder.lastRecord.setText(mContext
						.getString(R.string.contact_last_record) + lastString);
				holder.totalRecord.setVisibility(View.VISIBLE);
				holder.totalRecord.setText(mContext
						.getString(R.string.contact_times) + totalString);

			}
			return convertView;
		}
		
		public void setData(List<HashMap<String, Object>> list) {
			this.list = list;
		}

		private class ViewHolder {
			TextView alpha;
			ImageView icon;
			TextView name;
			TextView number;
			TextView totalRecord;
			TextView lastRecord;
		}
	}

	/**
	 * 初始化选中的字母
	 */
	private void initOverlay() {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		mOverlay = (TextView) inflater.inflate(R.layout.contact_overlay, null);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(120,
				240, 100, 0, WindowManager.LayoutParams.TYPE_APPLICATION,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
				PixelFormat.TRANSLUCENT);
        try {
            mWindowManager.addView(mOverlay, lp);
        }catch (Exception e) {
            DebugLog.e(TAG, "initOverlay: " + e.toString());
        }
	}

	/**
	 * 字母触摸监听
	 * 
	 * @author fang
	 * 
	 */
	private class LetterListViewListener implements
			OnTouchingLetterChangedListener {

		@Override
		public void onTouchingLetterChanged(final String s, float y, float x) {
			if (null != mAlphaIndexer && mAlphaIndexer.get(s) != null) {
				int position = mAlphaIndexer.get(s);
				mListView.setSelection(position);
				mOverlay.setText(mSections[position]);
				mOverlay.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onTouchingLetterEnd() {
			mOverlay.setVisibility(View.GONE);
		}
	}

	private String getAlpha(String str) {
		if (str == null) {
			return "#";
		}

		if (str.trim().length() == 0) {
			return "#";
		}

		char c = str.trim().substring(0, 1).charAt(0);

		Pattern pattern = Pattern.compile("^[A-Za-z]+$");
		if (pattern.matcher(c + "").matches()) {
			return (c + "").toUpperCase();
		} else {
			return "#";
		}
	}

	@Override
	public void onDestroy() {
		if (mWindowManager != null) {
			mWindowManager.removeView(mOverlay);
		}
		super.onDestroy();
	}

	/**
	 * 查找联系人
	 * 
	 * @param text
	 */
	private void searchContacts(CharSequence text) {

		if (null == mAdapter) {
			return;
		}
		mContactPositionMapArray.clear();
		int len = mList.size();
		int positon = 0;
		boolean isSearched = false;
		for (int i = 0; i < len; i++) {
			HashMap<String, Object> data = mList.get(i);
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
						isSearched = true;
					}
				}
			}
			isSearched = false;
		}
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onResult(boolean result) {
		if (result) {
			mByNameList = ContactHelper.getContactByNameList();
			mByTimesList = ContactHelper.getContactByTimesList();
			
			mHandler.sendEmptyMessage(MessageWhat.FRESH_CONTACT_RECORD);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ContactHelper.unregisterListener(this);
	}


	@Override
	public boolean isNeedLoading() {
		if (false == ContactHelper.hasReaded() && null != mByNameList && mByNameList.size() == 0) {
			return true;
		}
		return false;
	}
}