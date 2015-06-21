package com.fang.express;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.fang.base.WEActivity;
import com.fang.business.BusinessHelper;
import com.fang.callsms.R;
import com.fang.logs.LogCode;
import com.fang.logs.LogOperate;
import com.fang.util.MessageWhat;
import com.fang.util.NetWorkUtil;
import com.fang.util.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

public class ExpressListActivity extends WEActivity {

	private Spinner mSpinner;
	private ArrayAdapter<String> mAdapter;
	private Button mBackButton;
	private List<ExpressInfo> mExpressList;
	private ListView mExpressListView;
	private MyAdapter mListAdapter;
	private Button mTrackButton;
	private EditText mEditText;

	protected Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MessageWhat.NET_REQUEST_EXPRESS:
				if (null != msg.obj) {
					if (null != mExpressList) {
						for (ExpressInfo info : mExpressList) {
							if (info == msg.obj) {
								mListAdapter.setData(mExpressList);
								ExpressHelper.saveExpressInfos(mContext,
										mExpressList);
								break;
							}
						}
					}
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.express_layout);
		setTitle(mContext.getString(R.string.number_searchExpress));
		mSpinner = (Spinner) findViewById(R.id.companySpinner);
		mAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item,
				ExpressHelper.COMPANY_NAMES);

		mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinner.setAdapter(mAdapter);
		mSpinner.setSelection(
                SharedPreferencesHelper.getInstance().getInt(
                        SharedPreferencesHelper.SELECTED_EXPRESS_COMPANY, 0));
		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				SharedPreferencesHelper.getInstance().setInt(
                        SharedPreferencesHelper.SELECTED_EXPRESS_COMPANY,
                        position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		mBackButton = (Button) findViewById(R.id.back);
		mBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onBackPressed();
			}
		});
		mExpressListView = (ListView) findViewById(R.id.queue);
		mListAdapter = new MyAdapter();
		mExpressListView.setAdapter(mListAdapter);

		mEditText = (EditText) findViewById(R.id.search);

		mTrackButton = (Button) findViewById(R.id.trackBtn);
		mTrackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mEditText.getText().toString().trim().length() == 0) {
					showTip(mContext.getString(R.string.express_number_tip));
					return;
				}
				if (0 == mSpinner.getSelectedItemPosition()) {
					showTip(mContext.getString(R.string.express_company_tip));
					return;
				}
				ExpressInfo info = new ExpressInfo();
				info.setCompany(mSpinner.getSelectedItem().toString());
				info.setNumber(mEditText.getText().toString());
				info.setInfo(mContext.getString(R.string.express_tracking));
				mExpressList.add(info);
				mListAdapter.setData(mExpressList);

				ExpressHelper.saveExpressInfos(mContext, mExpressList);

				if (false == NetWorkUtil.isNetworkConnected(mContext)) {
					showTip(mContext
							.getString(R.string.open_network_to_recognise_express));
				} else {
					BusinessHelper.getExpressInfo(mContext, info, myHandler);
				}
				mEditText.setText("");
				//日志
				LogOperate.updateLog(mContext, LogCode.ADD_EXPRESS_NUMBER);
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshList();
	}

	/**
	 * 刷新列表
	 */
	private void refreshList() {
		mExpressList = ExpressHelper.getExpressInfos(mContext);
		if (null == mExpressList) {
			mExpressList = new ArrayList<ExpressInfo>();
		}
		mListAdapter.setData(mExpressList);
		for (ExpressInfo info : mExpressList) {
			BusinessHelper.getExpressInfo(mContext, info, myHandler);
		}
		SharedPreferencesHelper.getInstance().setLong(
                SharedPreferencesHelper.LAST_UPDATE_EXPRESS_LIST,
                System.currentTimeMillis());
	}

	private class MyAdapter extends BaseAdapter {

		private List<ExpressInfo> list;

		@Override
		public int getCount() {
			if (null == list) {
				return 0;
			}
			return list.size();
		}

		@Override
		public Object getItem(int arg0) {
			if (null == list) {
				return null;
			}
			return list.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup arg2) {
			ViewHolder holder;
			if (null == convertView) {
				LayoutInflater mInflater = LayoutInflater.from(mContext);
				convertView = mInflater.inflate(R.layout.express_queue_item,
						null);
				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.number = (TextView) convertView
						.findViewById(R.id.number);
				holder.info = (TextView) convertView.findViewById(R.id.info);
				holder.cancel = (Button) convertView.findViewById(R.id.cancel);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			ExpressInfo info = mExpressList.get(position);
			holder.name.setText(info.getCompany());
			holder.number.setText(info.getNumber());
			holder.info.setText(info.getInfo());

			holder.cancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					list.remove(position);
					ExpressHelper.saveExpressInfos(mContext, list);
					notifyDataSetChanged();
				}
			});
			return convertView;
		}

		public void setData(List<ExpressInfo> list) {
			this.list = list;
			notifyDataSetChanged();
		}

		private class ViewHolder {
			TextView name;
			TextView number;
			TextView info;
			Button cancel;
		}
	}
}
