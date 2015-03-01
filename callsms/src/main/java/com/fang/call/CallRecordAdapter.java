package com.fang.call;

import android.content.Context;
import android.content.Intent;
import android.provider.CallLog;
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
import com.fang.database.NumberDatabaseManager;
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
        // 如果名字为空，就显示号码
		if (StringUtil.isEmpty(record.get(CallHelper.PARAM_NAME).toString())) {
            String number = record.get(CallHelper.PARAM_NUMBER).toString();
			holder.name.setText(number);

            String info = NumberDatabaseManager.getInstance(mContext).query(number);
            if (null == info) {
                holder.number.setText("");
            } else {
                if (StringUtil.isEmpty(info)) {
                    holder.number.setText("");
                }else {
                    holder.number.setText(info);
                }
            }
		}else {
			holder.name.setText(record.get(CallHelper.PARAM_NAME).toString());
            holder.number.setText(record.get(CallHelper.PARAM_NUMBER).toString());
		}
        // 根据类型显示名字的颜色
		holder.name.setTextColor(CallHelper.getCallTypeColor(mContext, callType));
        // 显示次数
		int count = (Integer) record.get(CallHelper.PARAM_COUNT);
		if (count > 1) {
			holder.count.setText("(" + count + ")");
		}else {
			holder.count.setText("");
		}

		holder.icon.setImageResource((Integer)record.get(CallHelper.PARAM_ICON));
		holder.date.setText(Util.longDateToStringDate(Long
                .parseLong(record.get(CallHelper.PARAM_DATE).toString())));
        if (callType != CallLog.Calls.MISSED_TYPE) {
            holder.duration.setText(record.get(CallHelper.PARAM_DURATION).toString());
        } else {
            holder.duration.setText("响铃" + record.get(CallHelper.PARAM_DURATION).toString());
        }
        if (StringUtil.isEmpty(record.get(CallHelper.PARAM_DURATION).toString())) {
            holder.duration.setText("未接通");
        }
		
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
		ImageView icon;
		TextView name;
		TextView count;
		TextView number;
		TextView date;
		TextView duration;
		Button comment;
	}

}
