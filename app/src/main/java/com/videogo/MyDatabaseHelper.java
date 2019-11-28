package com.videogo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String CREATE_PIC_PATH ="create table picfilepath("+"id integer primary key autoincrement,"+"path text,"+"name text)";
    private static final String CREATE_VIDEO_PATH ="create table videofilepath("+"id integer primary key autoincrement,"+"path text,"+"name text)";
    //用户名，密码，用户类型
    private static final String USER_DATA ="create table userData("+"id integer primary key autoincrement,"+"name text,"+"password text,"+"type integer)";
    //设备序列号，验证码
    private static final String CREATE_VERIFY_CODE = "create table verifycode("+"id integer primary key autoincrement,"+"name text,"+"code text)";
    //报警信息
    private static final String CREAT_ALARM_MESSAGE="create table alarmMessage("+"id integer primary key autoincrement,"+"message text,"+"type text,"+"latitude text,"
            +"longitude text,"+"altitude text,"+"address text,"+"imgPath text,"+"videoPath text,"+"createTime text,"+"startTime text,"+"endTime text)";
    public MyDatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PIC_PATH);
        db.execSQL(CREATE_VIDEO_PATH);
        db.execSQL(USER_DATA);
        db.execSQL(CREATE_VERIFY_CODE);
        db.execSQL(CREAT_ALARM_MESSAGE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists picfilepath");
        db.execSQL("drop table if exists videofilepath");
        db.execSQL("drop table if exists userData");
        db.execSQL("drop table if exists verifycode");
        db.execSQL("drop table if exists alarmMessage");
        onCreate(db);
    }
}
