package com.fang.sms;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseIntArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;

import com.fang.base.BaseActivity;
import com.fang.callsms.R;
import com.fang.common.CustomConstant;
import com.fang.contact.ContactHelper;
import com.fang.contact.ContactInfo;

/**
 * 选择联系人
 * 
 * @author fang
 * 
 */
public class SelectContactActivity extends BaseActivity implements OnClickListener {

	/** 所有联系人信息 */
	private ArrayList<HashMap<String, Object>> mContactListData;
	/** 所有联系人信息 */
	private List<ContactInfo> mContactInfos;
	
	private ContactCheckboxAdapter mListItemAdapter;
	/** 已选择的联系人信息 */
	private ArrayList<HashMap<String, Object>> mSelectedContacts;
	/** 映射联系人列表 */
	private SparseIntArray mContactPositionMapArray;

	/** 确认按钮 */
	private Button mConfirmButton;
	/** 取消按钮 */
	private Button mCancelButton;
	/** 查找联系人 */
	private EditText mSearchEditText;
	/** 全选 */
	private CheckBox mSelectedAllCheckBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_contact_layout);
		
		ListView list = (ListView) findViewById(R.id.contact_list);
		mConfirmButton = (Button) findViewById(R.id.confirm);
		mCancelButton = (Button) findViewById(R.id.back);
		mConfirmButton.setOnClickListener(this);
		mCancelButton.setOnClickListener(this);
		
		mSelectedAllCheckBox = (CheckBox)findViewById(R.id.selectedAll);
		mSelectedAllCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				selectedAll(isChecked);
			}
		});
		
		mSearchEditText = (EditText)findViewById(R.id.search);
		mSearchEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence text, int arg1, int arg2, int arg3) {
				searchContacts(text);
			}
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});

		mContactListData = new ArrayList<HashMap<String, Object>>();

		mSelectedContacts = (ArrayList<HashMap<String, Object>>) getIntent().getSerializableExtra(CustomConstant.SELECT_CONTACT_PARAMETER);
		mContactPositionMapArray = new SparseIntArray();
		
		getPhoneContacts();
		
		mListItemAdapter = new ContactCheckboxAdapter(this, mContactListData, mContactPositionMapArray);

		list.setAdapter(mListItemAdapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				CheckBox checkBox = (CheckBox) view.findViewById(R.id.selected);
				checkBox.setChecked(!checkBox.isChecked());
			}
		});
	}

	/**
	 * 获取联系人列表
	 */
	private void getPhoneContacts() {

		ContentResolver resolver = this.getContentResolver();
		Cursor phoneCursor = resolver.query(ContactHelper.getContactURI(),
				ContactHelper.getProjection(), null, null, "sort_key");

		if (phoneCursor != null) {
			while (phoneCursor.moveToNext()) {
				String phoneNumber = phoneCursor.getString(ContactHelper.NUMBER);
				if (TextUtils.isEmpty(phoneNumber))
					continue;
				String contactName = phoneCursor
						.getString(ContactHelper.NAME);
				Long contactid = phoneCursor.getLong(ContactHelper.CONTACT_ID);
				Long photoid = phoneCursor.getLong(ContactHelper.PHOTO_ID);
				Bitmap contactPhoto = null;
				
				if (photoid > 0) {
					Uri uri = ContentUris.withAppendedId(
							ContactsContract.Contacts.CONTENT_URI, contactid);
					InputStream input = ContactsContract.Contacts
							.openContactPhotoInputStream(resolver, uri);
					contactPhoto = BitmapFactory.decodeStream(input);
				} else {
					contactPhoto = BitmapFactory.decodeResource(getResources(),
							R.drawable.contact_photo);
				}
				boolean isSelected = false;
				if (mSelectedContacts != null) {
					for (HashMap<String, Object> hashmap : mSelectedContacts) {
						if (hashmap.get(ContactHelper.PARAM_NUMBER).equals(phoneNumber)) {
							isSelected = true;
							break;
						}
					}
				}

				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put(ContactHelper.PARAM_PHOTO_ID, contactPhoto);
				map.put(ContactHelper.PARAM_NAME, contactName);
				map.put(ContactHelper.PARAM_NUMBER, phoneNumber);
				map.put(ContactHelper.PARAM_IS_SELECTED, isSelected);
				mContactListData.add(map);
			}

			phoneCursor.close();
		}
		int len = mContactListData.size();
		for (int i = 0; i < len; i++) {
			mContactPositionMapArray.put(i, i);
		}
	}

	@Override
	public void onClick(View view) {
		if (view == mSelectedAllCheckBox) {
			
		}
		else if (view == mCancelButton) {
			Intent intent = getIntent();
			setResult(RESULT_CANCELED, intent);
			this.finish();
		} else if (view == mConfirmButton) {
			Intent intent = getIntent();
			ArrayList<HashMap<String, Object>> dataArrayList = mListItemAdapter.getListData();
			if (null != dataArrayList) {
				ArrayList<HashMap<String, Object>> listDatachecked = new ArrayList<HashMap<String, Object>>();
				for (HashMap<String, Object> hashMap : dataArrayList) {
					if ((Boolean)hashMap.get(ContactHelper.PARAM_IS_SELECTED)) {
						listDatachecked.add(hashMap);
					}
				}
				intent.putExtra(CustomConstant.SELECT_CONTACT_PARAMETER, listDatachecked);
			}
			setResult(RESULT_OK, intent);
			this.finish();
		}
	}
	/**
	 * 查找联系人
	 * @param text
	 */
	private void searchContacts(CharSequence text) {
		mContactListData = mListItemAdapter.getListData();
		mContactPositionMapArray.clear();
		
		int len = mContactListData.size();
		int positon = 0;
		boolean isSearched = false;
		for (int i = 0; i < len; i++) {
			HashMap<String, Object> data = mContactListData.get(i);
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
					String number = (String) data.get(ContactHelper.PARAM_NUMBER);
					if (number.contains(text)) {
						mContactPositionMapArray.put(positon, i);
						positon++;
						isSearched = true;
					}
				}
			}
			isSearched = false;
		}
		mListItemAdapter.setPositionMaoArray(mContactPositionMapArray);
		mListItemAdapter.notifyDataSetChanged();
	}
	/**
	 * 全选
	 * @param isChecked
	 */
	private void selectedAll(boolean isChecked) {
		int len = mContactPositionMapArray.size();
		mContactListData = mListItemAdapter.getListData();
		for (int i = 0; i < len; i++) {
			mContactListData.get(mContactPositionMapArray.get(i))
			.put(ContactHelper.PARAM_IS_SELECTED,
					isChecked);
		}
		mListItemAdapter.setListData(mContactListData);
		mListItemAdapter.notifyDataSetChanged();
	}
}

