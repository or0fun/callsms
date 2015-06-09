package com.fang.call;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.SparseIntArray;
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
import com.fang.logs.LogCode;
import com.fang.logs.LogOperate;
import com.fang.util.BaseUtil;
import com.fang.util.DebugLog;
import com.fang.util.StringUtil;

import java.util.List;
import java.util.Map;

public class CallRecordAdapter extends BaseAdapter {

    private String TAG = "CallRecordAdapter";
	protected List<Map<String, Object>> mCallRecords;
	private LayoutInflater mInflater;
	private Context mContext;
    /** 信息弹出框 */
    private CallRecordDialog mCallRecordDialog;
    /** 映射联系人列表 */
    private SparseIntArray mContactPositionMapArray;

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
        if (null == mContactPositionMapArray) {
            return mCallRecords.size();
        } else  {
            return mContactPositionMapArray.size();
        }
	}

	@Override
	public Object getItem(int index) {
		if (null == mCallRecords) {
			return null;
		}
        return getRecordMap(index);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(final int index, View convertView, ViewGroup arg2) {
		
		final ViewHolder holder;
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
        final Map<String, Object> record = getRecordMap(index);
		int callType = Integer.parseInt(record.get(CallHelper.PARAM_TYPE).toString());
        // 如果名字为空，就显示号码
        String number = record.get(CallHelper.PARAM_NUMBER).toString();
        String info = NumberDatabaseManager.getInstance(mContext).query(number);
        String name = record.get(CallHelper.PARAM_NAME).toString();
		if (StringUtil.isEmpty(name)) {
			holder.name.setText(number);
            if (StringUtil.isEmpty(info)) {
                holder.number.setText("");
            } else {
                holder.number.setText(info);
            }
		}else {
            if (StringUtil.isEmpty(info)) {
                holder.name.setText(name);
            } else {
                info = info.replace("中国", "");
                holder.name.setText(Html.fromHtml(name + "<font color='#7f7f7f'>" + "|" + info + "</font>"));
            }
            holder.number.setText(number);
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
		holder.date.setText(BaseUtil.longDateToStringDate(Long
                .parseLong(record.get(CallHelper.PARAM_DATE).toString())));

        //时长
        holder.duration.setTextColor(mContext.getResources().getColor(R.color.hint));
        if (StringUtil.isEmpty(record.get(CallHelper.PARAM_DURATION).toString())) {
            if (callType == CallLogType.ADD_TYPE) {
                holder.duration.setText("新建");
            }else {
                holder.duration.setText("未接通");
                holder.duration.setTextColor(mContext.getResources().getColor(R.color.missed));
            }
        }else {
            if (callType != CallLogType.MISSED_TYPE) {
                holder.duration.setText(record.get(CallHelper.PARAM_DURATION).toString());
            } else {
                holder.duration.setText("响铃" + record.get(CallHelper.PARAM_DURATION).toString());
            }
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

        convertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DebugLog.d(TAG, "click index :" + index);
                if (null == mCallRecordDialog) {
                    mCallRecordDialog = new CallRecordDialog(mContext,
                            (String) getRecordMap(index).get(CallHelper.PARAM_NUMBER),
                            (String) getRecordMap(index).get(CallHelper.PARAM_NAME),
                            CallHelper.getCallTypeString(
                                    mContext,
                                    (Integer) getRecordMap(index).get(CallHelper.PARAM_TYPE)),
                            (Integer) getRecordMap(index).get(CallHelper.PARAM_ICON),
                            new ICallRecordDialogListener() {
                                @Override
                                public void remove(String info) {
                                    if (!StringUtil.isEmpty(info)) {
                                        String num = holder.number.getText().toString();
                                        if (StringUtil.isEmpty(num)) {
                                            holder.number.setText(info);
                                        }
                                    }
                                }
                            }
                    );
                } else {
                    mCallRecordDialog.setContent(
                            (String) getRecordMap(index).get(CallHelper.PARAM_NUMBER),
                            (String) getRecordMap(index).get(CallHelper.PARAM_NAME),
                            CallHelper.getCallTypeString(
                                    mContext,
                                    (Integer) getRecordMap(index).get(CallHelper.PARAM_TYPE)),
                            (Integer) getRecordMap(index).get(CallHelper.PARAM_ICON),
                            new ICallRecordDialogListener() {
                                @Override
                                public void remove(String info) {
                                    if (!StringUtil.isEmpty(info)) {
                                        String num = holder.number.getText().toString();
                                        if (StringUtil.isEmpty(num)) {
                                            holder.number.setText(info);
                                        }
                                    }
                                }
                            });
                }
                mCallRecordDialog.show();
                // 日志
                LogOperate.updateLog(mContext, LogCode.CALL_ITEM_CLICK);
            }
        });
		return convertView;
	}

	
	public void setData(List<Map<String, Object>> list) {
		this.mCallRecords = list;
	}

    public boolean removeDialog() {
        if (null != mCallRecordDialog && mCallRecordDialog.isShowing()) {
            mCallRecordDialog.remove();
            return true;
        }
        return false;
    }

    public void setPositionMapArray(SparseIntArray contactPositionMapArray) {
        mContactPositionMapArray = contactPositionMapArray;
    }

    private Map<String, Object> getRecordMap(int index) {
        final Map<String, Object> record;
        if (null == mContactPositionMapArray) {
            record = mCallRecords.get(index);
        } else {
            record = mCallRecords.get(mContactPositionMapArray.get(index));
        }
        return record;
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
