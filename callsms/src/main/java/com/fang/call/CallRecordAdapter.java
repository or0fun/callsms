package com.fang.call;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fang.callsms.R;
import com.fang.comment.CommentActivity;
import com.fang.util.StringUtil;
import com.fang.util.Util;

import java.util.List;
import java.util.Map;

public class CallRecordAdapter extends BaseAdapter {
	
	protected List<Map<String, Object>> mCallRecords;
	private LayoutInflater mInflater;
	private Context mContext;

	public CallRecordAdapter(Context context, List<Map<String, Object>> list) {
		this.mInflater = LayoutInflater.from(context);
		mContext = context;
		mCallRecords = list;
	}
	@Override
	public int getCount() {
		if (null == mCallRecords) {
			return 0;
		}
		return mCallRecords.size();
	}

	@Override
	public Object getItem(int arg0) {
		if (null == mCallRecords) {
			return null;
		}
		return mCallRecords.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int index, View convertView, ViewGroup arg2) {
		
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater
					.inflate(R.layout.call_list_item, null);
			holder = new ViewHolder();
			holder.tip = (TextView) convertView.findViewById(R.id.tip);
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.count = (TextView) convertView.findViewById(R.id.count);
			holder.number = (TextView) convertView.findViewById(R.id.number);
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			holder.date = (TextView) convertView.findViewById(R.id.date);
			holder.duration = (TextView) convertView.findViewById(R.id.duration);
			holder.comment = (Button) convertView.findViewById(R.id.commentBtn);
			convertView.setTag(holder);
			
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		final Map<String, Object> record = mCallRecords.get(index);
		int callType = Integer.parseInt(record.get(CallHelper.PARAM_TYPE).toString());
		holder.tip.setText(CallHelper.getCallTypeString(mContext, callType));
		if (StringUtil.isEmpty(record.get(CallHelper.PARAM_NAME).toString())) {
			holder.name.setText(record.get(CallHelper.PARAM_NUMBER).toString());
		}else {
			holder.name.setText(record.get(CallHelper.PARAM_NAME).toString());
		}
		holder.name.setTextColor(CallHelper.getCallTypeColor(mContext, callType));
		int count = (Integer) record.get(CallHelper.PARAM_COUNT);
		if (count > 1) {
			holder.count.setText("(" + count + ")");
		}else {
			holder.count.setText("");
		}
		if (StringUtil.isEmpty(record.get(CallHelper.PARAM_NAME).toString()) ||
				record.get(CallHelper.PARAM_NUMBER).toString().equals(record.get(CallHelper.PARAM_NAME).toString())) {
			if (null == record.get(CallHelper.PARAM_INFO) ||
					StringUtil.isEmpty(record.get(CallHelper.PARAM_INFO).toString())) {
				holder.number.setText("");
			}else {
				holder.number.setText(record.get(CallHelper.PARAM_INFO).toString());
			}
		}else {
			holder.number.setText(record.get(CallHelper.PARAM_NUMBER).toString());
		}
		holder.icon.setImageResource((Integer)record.get(CallHelper.PARAM_ICON));
		holder.date.setText(Util.longDateToStringDate(Long
                .parseLong(record.get(CallHelper.PARAM_DATE).toString())));
		holder.duration.setText(record.get(CallHelper.PARAM_DURATION).toString());
		
		holder.comment.setFocusable(false);//无此句点击item无响应的  
		holder.comment.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(mContext, CommentActivity.class);
				intent.putExtra(CallHelper.PARAM_NUMBER, record.get(CallHelper.PARAM_NUMBER).toString());
				mContext.startActivity(intent);
			}
		});
		return convertView;
	}

	
	public void setData(List<Map<String, Object>> list) {
		this.mCallRecords = list;
	}
	
	private class ViewHolder {
		TextView tip;
		ImageView icon;
		TextView name;
		TextView count;
		TextView number;
		TextView date;
		TextView duration;
		Button comment;
	}

}
