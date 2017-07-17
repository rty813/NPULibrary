package com.npu.zhang.npulibrary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhang on 2017/7/13.
 */

public class MyDatabase {
    private DBHelper dbHelper;

    public MyDatabase(Context mContext){
        dbHelper = new DBHelper(mContext, "npulibrary", null, 1);
    }

    public boolean insertStore(Map<String, String> bookinfo){
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM STORE WHERE BOOKLINK = ?", new String[]{bookinfo.get("bookLink")});
        if (cursor.getCount() == 0){
            database.execSQL("INSERT INTO STORE VALUES(?, ?, ?, ?, ?, ?)", new Object[]{
                    bookinfo.get("bookname"), bookinfo.get("bookdetail"),
                    bookinfo.get("bookpic"), bookinfo.get("bookLink"),
                    bookinfo.get("bookNameReal"), bookinfo.get("bookPlace")
            });
            database.close();
            return true;
        }
        database.close();
        return false;
    }

    public void removeStore(String bookLink){
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM STORE WHERE BOOKLINK=?", new String[]{bookLink});
        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            System.out.println(cursor.getString(cursor.getColumnIndex("BOOKNAME")));
            database.delete("STORE", "BOOKLINK=?", new String[]{bookLink});
        }
        database.close();
    }

    public ArrayList<Map<String, String>> getStore(){
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM STORE", null);
        cursor.moveToLast();
        ArrayList<Map<String, String>> list = new ArrayList<>();
        if (cursor.getCount() == 0){
            database.close();
            return list;
        }
        Map<String, String> map = new HashMap<>();
        map.put("bookname", cursor.getString(cursor.getColumnIndex("BOOKNAME")));
        map.put("bookdetail", cursor.getString(cursor.getColumnIndex("BOOKDETAIL")));
        map.put("bookpic", cursor.getString(cursor.getColumnIndex("BOOKPIC")));
        map.put("bookLink", cursor.getString(cursor.getColumnIndex("BOOKLINK")));
        map.put("bookNameReal", cursor.getString(cursor.getColumnIndex("BOOKNAMEREAL")));
        map.put("bookPlace", cursor.getString(cursor.getColumnIndex("BOOKPLACE")));
        list.add(map);
        while(cursor.moveToPrevious()){
            map = new HashMap<>();
            map.put("bookname", cursor.getString(cursor.getColumnIndex("BOOKNAME")));
            map.put("bookdetail", cursor.getString(cursor.getColumnIndex("BOOKDETAIL")));
            map.put("bookpic", cursor.getString(cursor.getColumnIndex("BOOKPIC")));
            map.put("bookLink", cursor.getString(cursor.getColumnIndex("BOOKLINK")));
            map.put("bookNameReal", cursor.getString(cursor.getColumnIndex("BOOKNAMEREAL")));
            map.put("bookPlace", cursor.getString(cursor.getColumnIndex("BOOKPLACE")));
            list.add(map);
        }
        database.close();
        return list;
    }

    public void insertaHistory(String bookname, Long time){
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM HISTORY WHERE BOOKNAME = ?", new String[]{bookname});
        if (cursor.getCount() != 0){
            ContentValues values = new ContentValues();
            values.put("TIME", time);
            database.update("HISTORY", values, "BOOKNAME=?", new String[]{bookname});
        }
        else{
            database.execSQL("INSERT INTO HISTORY VALUES (?, ?)", new Object[]{bookname, time});
        }
        database.close();
    }

    public String[] getHistory(){
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM HISTORY ORDER BY TIME DESC", null);
        int count = cursor.getCount();
        String[] history = new String[count];

        cursor.moveToFirst();
        for (int i = 0; i < count; i++){
            history[i] = cursor.getString(0);
            cursor.moveToNext();
        }
        database.close();
        return history;
    }
}
