package com.fang.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.fang.common.util.DebugLog;
import com.fang.common.util.StringUtil;

import java.util.HashMap;

/**
 * 数据库操作类
 * @author fang
 *
 */
public class NumberDatabaseManager {

    private static final String TAG = "NumberDatabaseManager";
	
    private static final String DB_NAME = "number.db"; //数据库名称
    private static final String TABLE_NAME = "numberData"; //数据表名称
    private static final int VERSION = 1; //数据库版本
    
	private DatabaseHelper mHelper;

    private static NumberDatabaseManager mInstance;

    private HashMap<String, String> numbersMap = new HashMap<String, String>();
      
    private NumberDatabaseManager(Context context) {  
        String sql = "create table if not exists " + TABLE_NAME + "(num text not null , info text );";          
    	mHelper = new DatabaseHelper(context, DB_NAME, sql, VERSION);
    }
    
    public static NumberDatabaseManager getInstance(Context context) {
    	if (null == mInstance) {
			synchronized (NumberDatabaseManager.class) {
				if (null == mInstance) {
					mInstance = new NumberDatabaseManager(context);
				}
			}
		}
    	return mInstance;
    }
    /**
     * 插入数据库
     * @param number
     * @param info
     */
    public synchronized void insert(String number, String info) {
    	if (null != number && null != info) {
            number = number.replace(" ", "");
            number = number.replace("-", "");

            numbersMap.put(number, info);

            DebugLog.d(TAG, "insert: number = " + number + ", " + "info=" + info);
        	ContentValues cv = new ContentValues();
        	cv.put("num", number);
        	cv.put("info", info);
            SQLiteDatabase db = mHelper.getWritableDatabase();
        	long result = db.insert(TABLE_NAME, null, cv);
            db.close();
            DebugLog.d(TAG, "insert: result=" + result);
		}
    }
    /**
     * 更新数据库
     * @param number
     * @param info
     */
    public synchronized void update(String number, String info) {
    	if (null == number || null == info) {
    		return;
    	}
        number = number.replace(" ", "");
        number = number.replace("-", "");

        String infoCache = query(number);
        DebugLog.d(TAG, "update: info=" + info);

    	if (StringUtil.isEmpty(infoCache)) {
			insert(number, info);
		}else {

            numbersMap.put(number, info);

            DebugLog.d(TAG, "update: number = " + number + ", " + "info=" + info);
	    	ContentValues cv = new ContentValues();
	    	cv.put("info", info);
	    	String whereClause = "num=?";
	    	String[] whereArgs = {number};
            SQLiteDatabase db = mHelper.getWritableDatabase();
	    	int result = db.update(TABLE_NAME, cv, whereClause,whereArgs);//执行修改
            db.close();
            DebugLog.d(TAG, "update: result=" + result);
		}
    }
    /**
     * 查询号码
     * @param number
     * @return
     */
    public synchronized String query(String number) {
        DebugLog.d(TAG, "query: db number = " + number);
		if (StringUtil.isEmpty(number)) {
			return null;
		}
        number = number.replace(" ", "");
        number = number.replace("-", "");

        String info = numbersMap.get(number);
        if (StringUtil.isEmpty(info)) {
            SQLiteDatabase db = mHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("select info from " + TABLE_NAME + " where num = ?",new String[]{number});
            if(null != cursor) {
                if(cursor.moveToFirst()) {
                    info = cursor.getString(cursor.getColumnIndex("info"));
                    cursor.close();
                    db.close();
                    numbersMap.put(number, info);
                    DebugLog.d(TAG, "query: db number = " + number + ", " + "info=" + info);
                    return info;
                }
                cursor.close();
                db.close();
            }
            return null;
        }
        DebugLog.d(TAG, "query: cache  number = " + number + ", " + "info=" + info);
    	return info;
    }
}
