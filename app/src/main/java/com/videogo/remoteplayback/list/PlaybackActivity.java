package com.videogo.remoteplayback.list;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import com.videogo.EzvizApplication;
import com.videogo.constant.Constant;
import com.videogo.constant.IntentConsts;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.ErrorCode;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceRecordFile;
import com.videogo.ui.util.DataManager;
import com.videogo.util.LocalInfo;
import com.videogo.util.Utils;
import com.videogo.widget.loading.LoadingView;

import java.util.Calendar;
import java.util.Date;

import ezviz.ezopensdk.R;

public class PlaybackActivity extends Activity implements SurfaceHolder.Callback ,Handler.Callback{
    private static final String TAG = "PlayBackActivity";
    private EZPlayer mPlayer = null;
    private EZCameraInfo mCameraInfo = null;
    // 播放界面SurfaceView
    private SurfaceView surfaceView = null;
    // 本地信息
    //private LocalInfo localInfo = null;
    // 屏幕方向
    private int mOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    // 播放分辨率
    private float mRealRatio = Constant.LIVE_VIEW_RATIO;
    private EZDeviceRecordFile mDeviceRecordInfo = null;
    private Calendar startTime;
    private Calendar endTime;
    private SQLiteDatabase db;
    private String mVerifyCode;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        setContentView(R.layout.activity_play_back);
        initView();
        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getData();
        initEZPlayer();
    }

    private void initView() {
        surfaceView = (SurfaceView) findViewById(R.id.remote_playback_wnd_sv);
        surfaceView.getHolder().addCallback(this);
        setRemoteListSvLayout();
        mDeviceRecordInfo = new EZDeviceRecordFile();
        startTime = Calendar.getInstance();
        endTime = Calendar.getInstance();
        long time=System.currentTimeMillis();
        startTime.setTime(new Date(time-41110000));
        endTime.setTime(new Date(time-41100000));
    }

    private void getData() {
        //localInfo = LocalInfo.getInstance();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mCameraInfo = getIntent().getParcelableExtra(IntentConsts.EXTRA_CAMERA_INFO);
        }
        //Application application = (Application) getApplication();
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        //localInfo.setScreenWidthHeight(metric.widthPixels, metric.heightPixels);
        //localInfo.setNavigationBarHeight((int) Math.ceil(25 * getResources().getDisplayMetrics().density));
        mVerifyCode = searchCode();
        Log.i(TAG,"code="+mVerifyCode);
    }

    private void initEZPlayer() {
            mPlayer = EzvizApplication.getOpenSDK().createPlayer(mCameraInfo.getDeviceSerial(),mCameraInfo.getCameraNo());
            mPlayer.setPlayVerifyCode(mVerifyCode);
            mPlayer.startPlayback(startTime,endTime);
    }
    private String searchCode(){
        //查询设备验证码
        String name = mCameraInfo.getDeviceSerial()+String.valueOf(mCameraInfo.getCameraNo());
        String code = "";
        db = ((EzvizApplication) getApplication()).getDatebase();
        Cursor cursor = db.query("verifycode", null, "name = ?", new String[]{name}, null, null, null);
        if (cursor.moveToFirst()){
            do {
                code = cursor.getString(cursor.getColumnIndex("code"));
            }while (cursor.moveToNext());
        }
        cursor.close();
        return  code;
    }
    private void setRemoteListSvLayout() {
        // 设置播放窗口位置
//        final int screenWidth = localInfo.getScreenWidth();
//        final int screenHeight = (mOrientation == Configuration.ORIENTATION_PORTRAIT) ? (localInfo.getScreenHeight() - localInfo
//                .getNavigationBarHeight()) : localInfo.getScreenHeight();
//        final RelativeLayout.LayoutParams realPlaySvlp = Utils.getPlayViewLp(mRealRatio, mOrientation,
//                localInfo.getScreenWidth(), (int) (localInfo.getScreenWidth() * Constant.LIVE_VIEW_RATIO), screenWidth,
//                screenHeight);

//        RelativeLayout.LayoutParams svLp = new RelativeLayout.LayoutParams(realPlaySvlp.width, realPlaySvlp.height);
//        svLp.addRule(RelativeLayout.CENTER_IN_PARENT);
//        surfaceView.setLayoutParams(svLp);

        //mRemotePlayBackTouchListener.setSacaleRect(Constant.MAX_SCALE, 0, 0, realPlaySvlp.width, realPlaySvlp.height);
        //setPlayScaleUI(1, null, null);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(mPlayer != null) {
            mPlayer.setSurfaceHold(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if(mPlayer != null) {
            mPlayer.setSurfaceHold(null);
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        return false;
    }
}
