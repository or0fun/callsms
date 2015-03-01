package com.fang.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.fang.util.StringUtil;

import java.util.HashMap;

/**
 * 数据库操作类
 * @author fang
 *
 */
public class NumberDatabaseManager {
	
    private static final String DB_NAME = "number.db"; //数据库名称
    private static final String TABLE_NAME = "numberData"; //数据表名称
    private static final int VERSION = 1; //数据库版本
    
	private DatabaseHelper mHelper;  
    private SQLiteDatabase mDb; 
    
    private static NumberDatabaseManager mInstance;

    private HashMap<String, String> numbersMap = new HashMap<String, String>();
      
    private NumberDatabaseManager(Context context) {  
        String sql = "create table if not exists " + TABLE_NAME + "(num text not null , info text );";          
    	mHelper = new DatabaseHelper(context, DB_NAME, sql, VERSION);  
    	mDb = mHelper.getWritableDatabase();  
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
    public void insert(String number, String info) {
    	if (null != number && null != info) {
            number = number.replace(" ", "");
            number = number.replace("-", "");

        	ContentValues cv = new ContentValues();
        	cv.put("num", number);
        	cv.put("info", info);
        	mDb.insert(TABLE_NAME, null, cv);
		}
    }
    /**
     * 更新数据库
     * @param number
     * @param info
     */
    public void update(String number, String info) {
    	if (null == number || null == info) {
    		return;
    	}
        number = number.replace(" ", "");
        number = number.replace("-", "");

        numbersMap.put(number, info);

    	if (StringUtil.isEmpty(query(number))) {
			insert(number, info);
		}else {
	    	ContentValues cv = new ContentValues();
	    	cv.put("info", info);
	    	String whereClause = "num=?";
	    	String[] whereArgs = {number};
	    	mDb.update(TABLE_NAME, cv, whereClause,whereArgs);//执行修改
		}
    }
    /**
     * 查询号码
     * @param number
     * @return
     */
    public String query(String number) {
		if (StringUtil.isEmpty(number)) {
			return null;
		}
        number = number.replace(" ", "");
        number = number.replace("-", "");

        String info = numbersMap.get(number);
        if (StringUtil.isEmpty(info)) {
            Cursor cursor = mDb.rawQuery("select * from " + TABLE_NAME + " where num=?",new String[]{number});
            if(null != cursor) {
                if(cursor.moveToFirst()) {
                    info = cursor.getString(cursor.getColumnIndex("info"));
                    cursor.close();
                    return info;
                }
                cursor.close();
            }
            return null;
        }
    	return info;
    }
}
