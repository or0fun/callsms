package com.fang.sms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.fang.base.BaseActivity;
import com.fang.callsms.R;
import com.fang.contact.ContactHelper;
import com.fang.controls.CustomProgressDialog;
import com.fang.util.BaseUtil;
import com.fang.util.Util;

import java.util.List;

public class SMSQueueActivity extends BaseActivity {

	private List<SendSMSInfo> mSmsList;
	private MyAdapter mAdapter;
	private ListView mSmsListView;
	private TextView mTipTextView;
	private Button mBackButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sms_queue_layout);

		mTipTextView = (TextView) findViewById(R.id.tip);
		mSmsListView = (ListView) findViewById(R.id.queue);
		mBackButton = (Button) findViewById(R.id.back);
		mBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onBackPressed();
			}
		});

		mAdapter = new MyAdapter();
		mSmsListView.setAdapter(mAdapter);

	}

	private class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			if (null == mSmsList) {
				return 0;
			}
			return mSmsList.size();
		}

		@Override
		public Object getItem(int arg0) {
			if (null == mSmsList) {
				return null;
			}
			return mSmsList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {

			if (null == convertView) {
				LayoutInflater mInflater = LayoutInflater.from(mContext);
				convertView = mInflater.inflate(R.layout.sms_queue_item, null);
			}
			final SendSMSInfo info = mSmsList.get(position);
			
			TextView nameTextView = (TextView) convertView
					.findViewById(R.id.name);
			TextView contentTextView = (TextView) convertView
					.findViewById(R.id.content);
			TextView dateTextView = (TextView) convertView
					.findViewById(R.id.date);
			contentTextView.setText(info.getContent());
			dateTextView.setText(BaseUtil.longDateToStringDate(info
                    .getTimeInMillis()));
            StringBuilder nameStringBuffer = new StringBuilder();
			for (String number : info.getmReceiverList()) {
				nameStringBuffer.append(ContactHelper.getPerson(mContext,
						number));
				
				nameStringBuffer.append(";");
			}
			if (nameStringBuffer.length() > 0) {
				nameTextView.setText(nameStringBuffer.substring(0, nameStringBuffer.length() - 1));
			}
			Button cancelButton = (Button) convertView
					.findViewById(R.id.cancel);
			cancelButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					CustomProgressDialog.show(mContext);
					int requestCode = info.getResultCode();
                    Util.cancelAlarm(mContext, requestCode);
					SMSHelper.removeSMSInfo(mContext, requestCode);
					refreshList();
					CustomProgressDialog.cancel(mContext);
				}
			});
			return convertView;
		}
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
		mSmsList = SMSHelper.getSendSMSInfos(mContext);
		if (null != mSmsList && mSmsList.size() > 0) {
			mTipTextView.setVisibility(View.GONE);
		} else {
			mTipTextView.setVisibility(View.VISIBLE);
		}
		mAdapter.notifyDataSetChanged();
	}
}
