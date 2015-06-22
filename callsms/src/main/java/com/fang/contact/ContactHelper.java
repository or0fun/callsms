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
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.text.TextUtils;

import com.fang.call.CallHelper;
import com.fang.callsms.R;
import com.fang.common.util.BaseUtil;
import com.fang.common.util.DebugLog;
import com.fang.common.util.StringUtil;
import com.fang.datatype.ExtraName;
import com.fang.util.MessageWhat;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通讯录帮助类
 *
 * @author fang
 */
public class ContactHelper {

    private static final String TAG = "ContactHelper";

    public static final int ID = 0;
    public static final int NAME = 1;
    public static final int NUMBER = 2;
    public static final int SORT_KEY = 3;
    public static final int PHOTO_ID = 4;
    public static final int CONTACT_ID = 5;
    public static final int TIMES_CONTACTED = 6;

    public static final String PARAM_SORT_KEY = "sort_key";
    public static final String PARAM_PHOTO_ID = "photo_id";
    public static final String PARAM_PHOTO = "photo";
    public static final String PARAM_CONTACT_ID = "contact_id";
    public static final String PARAM_IS_SELECTED = "selected";
    public static final String PARAM_LAST_RECORD_DATE = "last_record_date";
    public static final String PARAM_TIMES_CONTACTED = "total_contacted";

    private static boolean mHasReaded = false;

    private static boolean isReading = false;

    private static Bitmap mDefaultBitmap;
    private static long Default_CONTACT_PHOTO_ID = -1;

    /**
     * 按名字排序通讯录数据
     */
    private static List<HashMap<String, Object>> mByNameList = new ArrayList<HashMap<String, Object>>();
    /**
     * 按通话次数排序通讯录数据
     */
    private static List<HashMap<String, Object>> mByTimesList = new ArrayList<HashMap<String, Object>>();

    protected static List<IContactListener> mContactListeners = new ArrayList<IContactListener>();

    public static List<HashMap<String, Object>> getContactByNameList() {
        return mByNameList;
    }

    public static List<HashMap<String, Object>> getContactByTimesList() {
        return mByTimesList;
    }

    public static void registerListener(IContactListener listener) {
        mContactListeners.add(listener);
    }

    public static void unregisterListener(IContactListener listener) {
        mContactListeners.remove(listener);
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
     * 获取姓名
     *
     * @param context
     * @param callRecords
     * @param handler
     */
    public static void getPersonInfo(final Context context, final List<Map<String, Object>> callRecords,
                                     final Handler handler) {
        if (null == callRecords) {
            return;
        }
        BaseUtil.excute(new Runnable() {
            @Override
            public void run() {
                int len = 0;
                for (Map<String, Object> map : callRecords) {
                    String number = map.get(ExtraName.PARAM_NUMBER).toString();
                    String name = getPerson(context, number);
                    map.put(ExtraName.PARAM_NAME, name);
                    len++;

                    if (len % 20 == 0) {
                        if (null != handler) {
                            handler.sendEmptyMessage(MessageWhat.UPDATE_NUMBER_DATABASE);
                        }
                    }
                }
                if (null != handler) {
                    handler.sendEmptyMessage(MessageWhat.UPDATE_NUMBER_DATABASE);
                }
            }
        });
    }

    /**
     * 根据号码获取通讯录里的姓名
     *
     * @param num
     * @return
     */
    public static String getPerson(Context context, String num) {
        String name = "";

        if (StringUtil.isEmpty(num)) {
            return name;
        }

        name = getPersonFromContact(context, num);

        if (StringUtil.isEmpty(name)) {
            if (num.contains(" ")) {
                num = num.replace(" ", "");
            } else if (num.contains("-")) {
                num = num.replace("-", "");
            } else {
                if (num.length() == 11) {
                    num = num.substring(0, 3) + " " + num.substring(3, 7) + " "
                            + num.substring(7, 11);
                } else if (num.length() == 12) {
                    num = num.substring(0, 4) + " " + num.substring(4, 8) + " "
                            + num.substring(8, 12);
                }
            }
            name = getPersonFromContact(context, num);
        }
        return name;
    }

    /**
     * 获取姓名
     *
     * @param context
     * @param num
     * @return
     */
    private static String getPersonFromContact(Context context, String num) {
        String name = "";

        if (StringUtil.isEmpty(num)) {
            return name;
        }
        Cursor cursorOriginal = context
                .getContentResolver()
                .query(getContactURI(),
                        new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                                + " like '%" + num + "'", null, null);

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
        String[] projection = {Phone._ID, Phone.DISPLAY_NAME, Phone.NUMBER,
                Phone.SORT_KEY_PRIMARY, Photo.PHOTO_ID, Phone.CONTACT_ID,
                Phone.TIMES_CONTACTED};
        return projection;
    }

    /**
     * 跳转到添加联系人
     *
     * @param number
     */
    public static void addContact(Context context, String number) {
        if (null == number) {
            DebugLog.d(TAG, "addContact: number is null ");
            return;
        }

        number = number.replace(" ", "");
        number = number.replace("-", "");

        Uri insertUri = android.provider.ContactsContract.Contacts.CONTENT_URI;
        Intent intent = new Intent(Intent.ACTION_INSERT, insertUri);
        intent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE,
                number);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 跳转到系统通讯录
     *
     * @param context
     */
    public static void gotoContact(Context context) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(android.provider.ContactsContract.Contacts.CONTENT_URI);
        context.startActivity(intent);
    }

    /**
     * 读取通讯录
     */
    public static void readContact(final Context context) {
        if (isReading) {
            DebugLog.d(TAG, "readContact: isReading is true");
            return;
        }
        if (null == context || mHasReaded) {
            for (IContactListener listener : mContactListeners) {
                listener.onResult(true);
            }
            return;
        }

        isReading = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                        map.put(ExtraName.PARAM_NAME,
                                cursor.getString(ContactHelper.NAME));
                        map.put(ExtraName.PARAM_NUMBER, number);
                        map.put(ContactHelper.PARAM_SORT_KEY,
                                cursor.getString(ContactHelper.SORT_KEY));
                        long contactid = cursor.getLong(ContactHelper.CONTACT_ID);
                        long photoid = cursor.getLong(ContactHelper.PHOTO_ID);
                        if (photoid <= 0) {
                            contactid = Default_CONTACT_PHOTO_ID;
                        } else {
                        }
                        map.put(ContactHelper.PARAM_PHOTO_ID, contactid);
//				map.put(ContactHelper.PARAM_LAST_RECORD_DATE,
//						CallHelper.getLastRecordDate(context, number));
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

                for (IContactListener listener : mContactListeners) {
                    listener.onResult(true);
                }
                ContactHelper.setReaded(true);
                isReading = false;

//        getContactPhoto(context);
            }
        }).start();
    }

    /**
     * 获取姓名
     * @param context
     */
//    public static void getContactPhoto(final Context context) {
//        if (null == mByNameList) {
//            return;
//        }
//        if (null == mContactListeners) {
//            return;
//        }
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    for (Map<String, Object> map : mByNameList) {
//                        long id = (Long) map.get(PARAM_PHOTO_ID);
//                        Bitmap bitmap = getContactPhoto(context, id);
//                        if (null != bitmap) {
//                            map.put(PARAM_PHOTO, bitmap);
//                        }
//                    }
//
//                    mByTimesList.clear();
//                    mByTimesList.addAll(mByNameList);
//                    Collections.sort(mByTimesList, new ContactCompare());
//
//                    for (IContactListener listener : mContactListeners) {
//                        listener.onResult(true);
//                    }
//                } catch (Exception e) {
//                    BaseUtil.addCrashException(e);
//                }
//            }
//        }).run();
//    }

    /**
     * 获取头像
     *
     * @param context
     * @param contactid
     * @return
     */
    public static Bitmap getContactPhoto(Context context, long contactid) {
        if (contactid > 0) {
            Uri uri = ContentUris.withAppendedId(
                    ContactsContract.Contacts.CONTENT_URI, contactid);
            InputStream input = ContactsContract.Contacts
                    .openContactPhotoInputStream(context.getContentResolver(), uri);
            return BitmapFactory.decodeStream(input);
        }
        return null;
    }

    /**
     * 获取通讯录
     *
     * @author fang
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
                map.put(ExtraName.PARAM_NAME,
                        cursor.getString(ContactHelper.NAME));
                map.put(ExtraName.PARAM_NUMBER, number);
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

    public static void setReaded(boolean hasReaded) {
        mHasReaded = hasReaded;
    }
}
