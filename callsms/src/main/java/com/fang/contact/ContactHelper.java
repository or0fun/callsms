package com.fang.contact;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.text.TextUtils;

import com.fang.call.CallHelper;
import com.fang.callsms.R;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * 通讯录帮助类
 * 
 * @author fang
 * 
 */
public class ContactHelper {

	public static final int ID = 0;
	public static final int NAME = 1;
	public static final int NUMBER = 2;
	public static final int SORT_KEY = 3;
	public static final int PHOTO_ID = 4;
	public static final int CONTACT_ID = 5;
	public static final int TIMES_CONTACTED = 6;

	public static final String PARAM_ID = "id";
	public static final String PARAM_NAME = "name";
	public static final String PARAM_NUMBER = "number";
	public static final String PARAM_SORT_KEY = "sort_key";
	public static final String PARAM_PHOTO_ID = "photo_id";
	public static final String PARAM_CONTACT_ID = "contact_id";
	public static final String PARAM_IS_SELECTED = "selected";
	public static final String PARAM_LAST_RECORD_DATE = "last_record_date";
	public static final String PARAM_TIMES_CONTACTED = "total_contacted";
	
	private static boolean mHasReaded = false;

    private static Bitmap mDefaultBitmap;

	/** 按名字排序通讯录数据 */
	private static List<HashMap<String, Object>> mByNameList = new ArrayList<HashMap<String, Object>>();
	/** 按通话次数排序通讯录数据 */
	private static List<HashMap<String, Object>> mByTimesList = new ArrayList<HashMap<String, Object>>();

	protected static List<IContactListener> mconContactListeners = new ArrayList<IContactListener>();

	public static List<HashMap<String, Object>> getContactByNameList() {
		return mByNameList;
	}

	public static List<HashMap<String, Object>> getContactByTimesList() {
		return mByTimesList;
	}

	public static void registerListener(IContactListener listener) {
		mconContactListeners.add(listener);
	}

	public static void unregisterListener(IContactListener listener) {
		mconContactListeners.remove(listener);
	}

	/**
	 * 获取联系人列表
	 */
	public static List<ContactInfo> getPhoneContacts(Context context,
			String order) {

		ContentResolver resolver = context.getContentResolver();
		String[] projection = getProjection();

		List<ContactInfo> dataList = new ArrayList<ContactInfo>();

		Cursor phoneCursor = resolver.query(Phone.CONTENT_URI, projection,
				null, null, order);

		if (phoneCursor != null) {
			while (phoneCursor.moveToNext()) {
				int id = phoneCursor.getInt(ID);
				String number = phoneCursor.getString(NUMBER);
				if (TextUtils.isEmpty(number))
					continue;
				String name = phoneCursor.getString(NAME);
				String sort_key = phoneCursor.getString(SORT_KEY);
				Long contactid = phoneCursor.getLong(CONTACT_ID);
				Long photoid = phoneCursor.getLong(PHOTO_ID);
				Bitmap contactPhoto = null;
				if (photoid > 0) {
					Uri uri = ContentUris.withAppendedId(
							ContactsContract.Contacts.CONTENT_URI, contactid);
					InputStream input = ContactsContract.Contacts
							.openContactPhotoInputStream(resolver, uri);
					contactPhoto = BitmapFactory.decodeStream(input);
				} else {
					contactPhoto = BitmapFactory.decodeResource(
							context.getResources(), R.drawable.contact_photo);
				}

				ContactInfo info = new ContactInfo();
				info.setBitmap(contactPhoto);
				info.setID(id);
				info.setName(name);
				info.setNumber(number);
				info.setSort_key(sort_key);
				dataList.add(info);
			}
			phoneCursor.close();
		}
		return dataList;
	}

	/**
	 * 根据号码获取通讯录里的姓名
	 * 
	 * @param num
	 * @return
	 */
	public static String getPerson(Context context, String num) {
		Cursor cursorOriginal = context
				.getContentResolver()
				.query(getContactURI(),
						new String[] { ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME },
						ContactsContract.CommonDataKinds.Phone.NUMBER
								+ " like '%" + num + "'", null, null);
		String name = "";
		if (null != cursorOriginal) {
			if (cursorOriginal.moveToFirst()) {
				name = cursorOriginal
						.getString(cursorOriginal
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			}
			cursorOriginal.close();
		}
		return name;
	}

	/**
	 * 获取通讯录URI
	 * 
	 * @return
	 */
	public static Uri getContactURI() {
		return ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
	}

	/**
	 * 获取通讯录projection
	 * 
	 * @return
	 */
	public static String[] getProjection() {
		String[] projection = { Phone._ID, Phone.DISPLAY_NAME, Phone.NUMBER,
				Phone.SORT_KEY_PRIMARY, Photo.PHOTO_ID, Phone.CONTACT_ID,
				Phone.TIMES_CONTACTED };
		return projection;
	}

	/**
	 * 跳转到添加联系人
	 * 
	 * @param number
	 */
	public static void addContact(Context context, String number) {
		Uri insertUri = android.provider.ContactsContract.Contacts.CONTENT_URI;
		Intent intent = new Intent(Intent.ACTION_INSERT, insertUri);
		intent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE,
				number);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	/**
	 * 读取通讯录
	 */
	public static void readContact(Context context) {
		if (mHasReaded) {
			for (IContactListener listener : mconContactListeners) {
				listener.onResult(true);
			}
			return;
		}
		Uri contactURI = ContactHelper.getContactURI();
		String[] projection = ContactHelper.getProjection();

		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = resolver.query(contactURI, projection, null, null,
				"sort_key COLLATE LOCALIZED asc");
		List<HashMap<String, Object>> byNameList = new ArrayList<HashMap<String, Object>>();
		List<HashMap<String, Object>> byTimesList = new ArrayList<HashMap<String, Object>>();

		if (null != cursor) {

			cursor.moveToFirst();
			while (cursor.moveToNext()) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				String number = cursor.getString(ContactHelper.NUMBER);
				map.put(ContactHelper.PARAM_NAME,
						cursor.getString(ContactHelper.NAME));
				map.put(ContactHelper.PARAM_NUMBER, number);
				map.put(ContactHelper.PARAM_SORT_KEY,
						cursor.getString(ContactHelper.SORT_KEY));
				Long contactid = cursor.getLong(ContactHelper.CONTACT_ID);
				Long photoid = cursor.getLong(ContactHelper.PHOTO_ID);
				Bitmap contactPhoto = null;
				if (photoid > 0) {
					Uri uri = ContentUris.withAppendedId(
							ContactsContract.Contacts.CONTENT_URI, contactid);
					InputStream input = ContactsContract.Contacts
							.openContactPhotoInputStream(resolver, uri);
					contactPhoto = BitmapFactory.decodeStream(input);
				} else {
                    if (null == mDefaultBitmap) {
                        mDefaultBitmap = BitmapFactory.decodeResource(
                                context.getResources(), R.drawable.contact_photo);
                    }
					contactPhoto = mDefaultBitmap;
				}
				map.put(ContactHelper.PARAM_PHOTO_ID, contactPhoto);
				map.put(ContactHelper.PARAM_LAST_RECORD_DATE,
						CallHelper.getLastRecordDate(context, number));
				map.put(ContactHelper.PARAM_TIMES_CONTACTED,
						CallHelper.getCallTimes(context, number));
				byNameList.add(map);
			}
			cursor.close();
		}
		byTimesList.addAll(byNameList);
		Collections.sort(byTimesList, new ContactCompare());

		mByNameList = byNameList;
		mByTimesList = byTimesList;

		for (IContactListener listener : mconContactListeners) {
			listener.onResult(true);
		}
		ContactHelper.setReaded(true);
	}

	/**
	 * 获取通讯录
	 * 
	 * @author fang
	 * 
	 */
	public static class MyAsyncQueryHandler extends AsyncQueryHandler {

		private Context mContext;

		public MyAsyncQueryHandler(Context context, ContentResolver cr) {
			super(cr);
			mContext = context;
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {

			ContentResolver resolver = mContext.getContentResolver();
			List<HashMap<String, Object>> byNameList = new ArrayList<HashMap<String, Object>>();
			List<HashMap<String, Object>> byTimesList = new ArrayList<HashMap<String, Object>>();

			cursor.moveToFirst();
			while (cursor.moveToNext()) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				String number = cursor.getString(ContactHelper.NUMBER);
				map.put(ContactHelper.PARAM_NAME,
						cursor.getString(ContactHelper.NAME));
				map.put(ContactHelper.PARAM_NUMBER, number);
				map.put(ContactHelper.PARAM_SORT_KEY,
						cursor.getString(ContactHelper.SORT_KEY));
				Long contactid = cursor.getLong(ContactHelper.CONTACT_ID);
				Long photoid = cursor.getLong(ContactHelper.PHOTO_ID);
				Bitmap contactPhoto = null;
				if (photoid > 0) {
					Uri uri = ContentUris.withAppendedId(
							ContactsContract.Contacts.CONTENT_URI, contactid);
					InputStream input = ContactsContract.Contacts
							.openContactPhotoInputStream(resolver, uri);
					contactPhoto = BitmapFactory.decodeStream(input);
				} else {
					contactPhoto = BitmapFactory.decodeResource(
							mContext.getResources(), R.drawable.contact_photo);
				}
				map.put(ContactHelper.PARAM_PHOTO_ID, contactPhoto);
				map.put(ContactHelper.PARAM_LAST_RECORD_DATE,
						CallHelper.getLastRecordDate(mContext, number));
				map.put(ContactHelper.PARAM_TIMES_CONTACTED,
						CallHelper.getCallTimes(mContext, number));
				byNameList.add(map);
			}
			
			cursor.close();
			byTimesList.addAll(byNameList);
			Collections.sort(byTimesList, new ContactCompare());

			mByNameList = byNameList;
			mByTimesList = byTimesList;

		}

	}

	/**
	 * 按通话次数排序
	 * 
	 * @author fang
	 * 
	 */
	private static class ContactCompare implements
			Comparator<HashMap<String, Object>> {
		@Override
		public int compare(HashMap<String, Object> arg0,
				HashMap<String, Object> arg1) {
			int times0 = (Integer) arg0
					.get(ContactHelper.PARAM_TIMES_CONTACTED);
			int times1 = (Integer) arg1
					.get(ContactHelper.PARAM_TIMES_CONTACTED);
			if (times0 > times1) {
				return -1;
			} else if (times0 < times1) {
				return 1;
			}
			return 0;
		}
	}

	public static boolean hasReaded() {
		return mHasReaded;
	}

	public static void setReaded(boolean hasReaded ) {
		mHasReaded = hasReaded;
	}
}
