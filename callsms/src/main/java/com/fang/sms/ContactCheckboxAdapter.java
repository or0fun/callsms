package com.fang.sms;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.fang.callsms.R;
import com.fang.contact.ContactHelper;
import com.fang.datatype.ExtraName;

import java.util.ArrayList;
import java.util.HashMap;

public class ContactCheckboxAdapter extends BaseAdapter {

	Context context;
	/** 联系人列表 */
	ArrayList<HashMap<String, Object>> mListData;
	/** 映射联系人列表 */
	private SparseIntArray mContactPositionArray;

	public ContactCheckboxAdapter(Context context,
			ArrayList<HashMap<String, Object>> listData,
			SparseIntArray positionSparseIntArray) {
		this.context = context;
		this.mListData = listData;
		this.mContactPositionArray = positionSparseIntArray;
	}

	public void setPositionMaoArray(SparseIntArray positionSparseIntArray) {
		this.mContactPositionArray = positionSparseIntArray;
	}

	public void setListData(ArrayList<HashMap<String, Object>> listData) {
		this.mListData = listData;
	}

	@Override
	public int getCount() {
		return mContactPositionArray.size();
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public Object getItem(int position) {
		return mListData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (null == convertView) {
			LayoutInflater mInflater = LayoutInflater.from(context);
			convertView = mInflater.inflate(R.layout.select_contact_item, null);
		}
		
		final int index = mContactPositionArray.get(position);
		ImageView image = (ImageView) convertView
				.findViewById(R.id.friend_image);
		image.setImageBitmap((Bitmap) mListData.get(index).get(
				ContactHelper.PARAM_PHOTO_ID));
		TextView username = (TextView) convertView
				.findViewById(R.id.friend_username);
		username.setText((String) mListData.get(index).get(
                ExtraName.PARAM_NAME));
		TextView id = (TextView) convertView.findViewById(R.id.friend_id);
		id.setText((String) mListData.get(index)
				.get(ExtraName.PARAM_NUMBER));
		boolean isSelected = (Boolean) mListData.get(index).get(
				ContactHelper.PARAM_IS_SELECTED);

		CheckBox check = (CheckBox) convertView.findViewById(R.id.selected);
		check.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				mListData.get(index).put(ContactHelper.PARAM_IS_SELECTED,
						isChecked);
			}
		});
		check.setChecked(isSelected);

		return convertView;
	}

	public ArrayList<HashMap<String, Object>> getListData() {
		return mListData;
	}
}