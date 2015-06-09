package com.fang.number;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.fang.callsms.R;
import com.fang.logs.LogCode;
import com.fang.logs.LogOperate;
import com.fang.util.BaseUtil;

public class NumberServiceActivity extends ListActivity {

	private Context mContext;
	private SimpleAdapter mAdapter;
	private List<HashMap<String, Object>> mList;
	private Button mBackButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.service_list);
		mContext = this;
		
		mBackButton = (Button) findViewById(R.id.back);
		mBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onBackPressed();
			}
		});
		
		getData();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		getData();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		String number = (String) mList.get(position).get(NumberServiceHelper.PARAM_NUMBER);
		BaseUtil.gotoCall(mContext, number);
		//日志上传
		LogOperate.updateLog(mContext, LogCode.CALL_COMMON_NUMBER);
	}
	/**
	 * 获取数据
	 */
	protected void getData() {
		mList = new ArrayList<HashMap<String, Object>>();
		Intent intent = getIntent();
		if (null != intent) {
			String actionString = intent.getAction();
			Iterator<Entry<String, String>> iter = null;
			Entry<String, String> entry;
			if (NumberServiceHelper.ACTION_FOOD.equals(actionString)) {
				setTitle(mContext.getString(R.string.number_meal));
				iter = NumberServiceHelper.NUMBERS_FOOD.entrySet().iterator();
			} else if (NumberServiceHelper.ACTION_HOUSE.equals(actionString)) {
				setTitle(mContext.getString(R.string.number_house));
				iter = NumberServiceHelper.NUMBERS_HOUSE.entrySet().iterator();
			} else if (NumberServiceHelper.ACTION_EXPRESS.equals(actionString)) {
				setTitle(mContext.getString(R.string.number_express));
				iter = NumberServiceHelper.NUMBERS_EXPRESS.entrySet().iterator();
			} else if (NumberServiceHelper.ACTION_SERVICE.equals(actionString)) {
				setTitle(mContext.getString(R.string.number_service));
				iter = NumberServiceHelper.NUMBERS_SERVICE.entrySet().iterator();
			}
			if (null != iter) {
				while (iter.hasNext()) {
					entry = iter.next();
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put(NumberServiceHelper.PARAM_NAME, entry.getKey());
					map.put(NumberServiceHelper.PARAM_NUMBER, entry.getValue());
					mList.add(map);
				}
				// 绑定数据
				mAdapter = new SimpleAdapter(mContext, mList,
						R.layout.service_list_item, new String[] {
								NumberServiceHelper.PARAM_NAME,
								NumberServiceHelper.PARAM_NUMBER }, new int[] {
								R.id.name, R.id.number });
				setListAdapter(mAdapter);
			}

		}
	}


}
