/* 
 * @ProjectName VideoGoJar
 * @Copyright HangZhou Hikvision System Technology Co.,Ltd. All Right Reserved
 * 
 * @FileName EzvizApplication.java
 * @Description 这里对文件进行描述
 * 
 * @author chenxingyf1
 * @data 2014-7-12
 * 
 * @note 这里写本文件的详细功能描述和注释
 * @note 历史记录
 * 
 * @warning 这里写本文件的相关警告
 */
package com.videogo;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import com.squareup.leakcanary.LeakCanary;
import com.videogo.constant.Constant;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZAccessToken;
import com.videogo.scanvideo.CameraVideoActivity;
import com.videogo.ui.cameralist.EZCameraListActivity;
import com.videogo.util.LogUtil;
import com.videogo.util.PermissionUtil;

import cn.jpush.android.api.JPushInterface;

import static com.videogo.been.AlarmContant.AppKey;

/**
 * 自定义应用
 *
 */
public class EzvizApplication extends Application {

    //开发者需要填入自己申请的appkey//ABC
    //public static String AppKey = "76d8a02ae81a4260a02e470ebb48077d";
    public  static int user_type;
    public static String table_name;
    private MyDatabaseHelper dbHelper;
    private  SQLiteDatabase db;
    private IntentFilter intentFilter;
    private EzvizBroadcastReceiver receiver;
    public static EZOpenSDK getOpenSDK() {
        return EZOpenSDK.getInstance();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initleakcanary();
        initSDK();
        initData();
        SDKInitializer.initialize(this);
        JPushInterface.init(this);
    }

    private void initleakcanary() {
        if (LeakCanary.isInAnalyzerProcess(this)){
            return;
        }
        LeakCanary.install(this);
    }

    private void initData() {
        dbHelper = new MyDatabaseHelper(this, "filepath.db", null, 1);
        db = dbHelper.getWritableDatabase();
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.videogo.action.ADD_DEVICE_SUCCESS_ACTION");
        intentFilter.addAction("com.videogo.action.OAUTH_SUCCESS_ACTION");
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        receiver = new EzvizBroadcastReceiver();
        registerReceiver(receiver,intentFilter);


    }
    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(receiver);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    public  SQLiteDatabase getDatebase(){
        return db;
    }

    public static void setUser_type(int type){
        user_type = type;
    }

    public static void setTable_name(String name){
        table_name = name;
    }

    private void initSDK() {
        {
            /**
             * sdk日志开关，正式发布需要去掉
             */
            EZOpenSDK.showSDKLog(true);

            /**
             * 设置是否支持P2P取流,详见api
             */
            EZOpenSDK.enableP2P(true);

            /**
             * APP_KEY请替换成自己申请的
             */
            EZOpenSDK.initLib(this, AppKey);
        }
    }
    private class EzvizBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.i("TAG","action = "+action);
            if (action.equals(Constant.OAUTH_SUCCESS_ACTION)){
                Log.i("TAG", "onReceive: OAUTH_SUCCESS_ACTION");
                //Intent i = new Intent(context, EZCameraListActivity.class);
//                Intent i = new Intent(context, MainActivity.class);
                Intent i = new Intent(context, HomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                /*******   获取登录成功之后的EZAccessToken对象   *****/
                EZAccessToken token =EzvizApplication.getOpenSDK().getEZAccessToken();
                context.startActivity(i);
            }
        }
    }
}
