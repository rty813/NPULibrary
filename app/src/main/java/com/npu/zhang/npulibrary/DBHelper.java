package com.npu.zhang.npulibrary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zhang on 2017/7/17.
 */

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS HISTORY(BOOKNAME VARCHAR, TIME INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS STORE(BOOKNAME VARCHAR, BOOKDETAIL VARCHAR, BOOKPIC VARCHAR," +
                " BOOKLINK VARCHAR, BOOKNAMEREAL VARCHAR, BOOKPLACE VARCHAR)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
