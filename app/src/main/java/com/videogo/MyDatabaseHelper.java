package com.videogo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String CREATE_PIC_PATH ="create table picfilepath("+"id integer primary key autoincrement,"+"path text,"+"name text)";
    private static final String CREATE_VIDEO_PATH ="create table videofilepath("+"id integer primary key autoincrement,"+"path text,"+"name text)";
    public MyDatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PIC_PATH);
        db.execSQL(CREATE_VIDEO_PATH);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists picfilepath");
        db.execSQL("drop table if exists videofilepath");
        onCreate(db);
    }
}
