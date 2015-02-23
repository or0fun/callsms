package com.fang.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * 数据库帮助类
 * @author fang
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private String sqlCreateString;
    public DatabaseHelper(Context context, String dbName, String sql,  int version) {
        super(context, dbName, null, version);
        sqlCreateString = sql;
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(sqlCreateString);
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
    }
 
}
