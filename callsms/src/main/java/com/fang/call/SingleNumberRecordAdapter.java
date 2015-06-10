package com.fang.call;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fang.callsms.R;
import com.fang.common.util.StringUtil;

import java.util.List;
import java.util.Map;

public class SingleNumberRecordAdapter extends BaseAdapter {
	
	protected List<Map<String, Object>> mCallRecords;
	private LayoutInflater mInflater;
	private Context mContext;

	public SingleNumberRecordAdapter(Context context, List<Map<String, Object>> list) {
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
					.inflate(R.layout.record_list_item, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			holder.date = (TextView) convertView.findViewById(R.id.date);
			holder.duration = (TextView) convertView.findViewById(R.id.duration);
			convertView.setTag(holder);
			
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		Map<String, Object> record = mCallRecords.get(index);
		int callType = Integer.parseInt(record.get(CallHelper.PARAM_TYPE).toString());
		holder.date.setText(record.get(CallHelper.PARAM_DATE).toString());
		int color = CallHelper.getCallTypeColor(mContext, callType);
		holder.date.setTextColor(color);
		holder.duration.setTextColor(color);
		
		holder.icon.setImageResource((Integer)record.get(CallHelper.PARAM_ICON));

        if (StringUtil.isEmpty(record.get(CallHelper.PARAM_DURATION).toString())) {
            if (callType == CallLogType.ADD_TYPE) {
                holder.duration.setText("新建");
            }else {
                holder.duration.setText("未接通");
            }
        } else {
            holder.duration.setText(record.get(CallHelper.PARAM_DURATION).toString());
        }
		
		return convertView;
	}

	
	public void setData(List<Map<String, Object>> list) {
		this.mCallRecords = list;
	}
	
	private class ViewHolder {
		ImageView icon;
		TextView date;
		TextView duration;
	}

}