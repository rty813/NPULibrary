package com.npu.zhang.npulibrary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by zhang on 2017/7/13.
 */

public class MyDatabase {
    private SQLiteDatabase database;

    public MyDatabase(@Nullable String dbName){
        if (dbName == null){
            dbName = "/data/data/com.npu.zhang.npulibrary/databases/npulibrary.db";
        }
        database = SQLiteDatabase.openOrCreateDatabase(dbName, null);
        String sql = "CREATE TABLE IF NOT EXISTS HISTORY(BOOKNAME VARCHAR, TIME INTEGER)";
        database.execSQL(sql);
        sql = "CREATE TABLE IF NOT EXISTS STORE(BOOKNAME VARCHAR, BOOKDETAIL VARCHAR, BOOKPIC VARCHAR)";
        database.execSQL(sql);
    }

    public void insertaHistory(String bookname, Long time){
        Cursor cursor = database.rawQuery("SELECT * FROM HISTORY WHERE BOOKNAME = ?", new String[]{bookname});
        if (cursor.getCount() != 0){
            ContentValues values = new ContentValues();
            values.put("TIME", time);
            database.update("HISTORY", values, "BOOKNAME=?", new String[]{bookname});
        }

        database.execSQL("INSERT INTO HISTORY VALUES (?, ?)", new Object[]{bookname, time});
    }

    public String[] getHistory(int maxCount){
        Cursor cursor = database.rawQuery("SELECT * FROM HISTORY ORDER BY TIME DESC", null);
        int count = cursor.getCount();
        if (count > maxCount){
            count = maxCount;
        }
        String[] history = new String[count];

        cursor.moveToFirst();
        for (int i = 0; i < count; i++){
            history[i] = cursor.getString(0);
            cursor.moveToNext();
        }
        return history;
    }

    public void closeDB(){
        database.close();
    }
}
