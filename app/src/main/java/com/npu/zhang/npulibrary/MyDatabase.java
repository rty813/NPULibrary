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
    private SQLiteDatabase database;

    public MyDatabase(Context mContext){
        String dbName = mContext.getFilesDir().getPath().split("files")[0] + "databases/" + "npulibrary.db";
        database = SQLiteDatabase.openOrCreateDatabase(dbName, null);
        String sql = "CREATE TABLE IF NOT EXISTS HISTORY(BOOKNAME VARCHAR, TIME INTEGER)";
        database.execSQL(sql);
        sql = "CREATE TABLE IF NOT EXISTS STORE(BOOKNAME VARCHAR, BOOKDETAIL VARCHAR, BOOKPIC VARCHAR," +
                " BOOKLINK VARCHAR, BOOKNAMEREAL VARCHAR, BOOKPLACE VARCHAR)";
        database.execSQL(sql);
    }

    public boolean insertStore(Map<String, String> bookinfo){
        Cursor cursor = database.rawQuery("SELECT * FROM STORE WHERE BOOKLINK = ?", new String[]{bookinfo.get("bookLink")});
        if (cursor.getCount() == 0){
            database.execSQL("INSERT INTO STORE VALUES(?, ?, ?, ?, ?, ?)", new Object[]{
                    bookinfo.get("bookname"), bookinfo.get("bookdetail"),
                    bookinfo.get("bookpic"), bookinfo.get("bookLink"),
                    bookinfo.get("bookNameReal"), bookinfo.get("bookPlace")
            });
            return true;
        }
        return false;
    }

    public void removeStore(String bookLink){
        Cursor cursor = database.rawQuery("SELECT * FROM STORE WHERE BOOKLINK=?", new String[]{bookLink});
        System.out.println(cursor.getCount());
        database.delete("STORE", "BOOKLINK=?", new String[]{bookLink});
    }

    public ArrayList<Map<String, String>> getStore(){
        Cursor cursor = database.rawQuery("SELECT * FROM STORE", null);
        cursor.moveToLast();
        ArrayList<Map<String, String>> list = new ArrayList<>();
        if (cursor.getCount() == 0){
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
        return list;
    }


    public void clearHistory(){
        database.execSQL("DELETE FROM HISTORY");
    }

    public void insertaHistory(String bookname, Long time){
        Cursor cursor = database.rawQuery("SELECT * FROM HISTORY WHERE BOOKNAME = ?", new String[]{bookname});
        if (cursor.getCount() != 0){
            ContentValues values = new ContentValues();
            values.put("TIME", time);
            database.update("HISTORY", values, "BOOKNAME=?", new String[]{bookname});
        }
        else{
            database.execSQL("INSERT INTO HISTORY VALUES (?, ?)", new Object[]{bookname, time});
        }
    }

    public String[] getHistory(){
        Cursor cursor = database.rawQuery("SELECT * FROM HISTORY ORDER BY TIME DESC", null);
        int count = cursor.getCount();
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
