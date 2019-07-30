package com.videogo;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.view.MapView;

import ezviz.ezopensdk.R;

public class MainActivity extends FragmentActivity {
    private MapView map = null;
    //屏幕点
    private Point mapPoint;
    //画点、线、面、涂鸦辅助图层
    private GraphicsLayer drawLayer;
    //画图工具
    private DrawTool drawTool;
    //地图储存位置
    private String earthPath = "";
    private String path;
    private ArcGISLocalTiledLayer localtitleLayer;
    private SharedPreferences sharedPreferences;

    private ImageView amplify, narrow, distmeasure, areameasure, clean, back, openFile, xiazai;
    private CheckBox local;
    private int isChecked = 0; //0为清空，1为画线，2为画面
    private LinearLayout linearLayout;
    private TextView txy, tsc, tre, result;

    private MyApplication app = (MyApplication) x.app();
    //数据更新频率
    private long gpstime;
    private Point gpsPoint, myPoint;
    private boolean isTrue = true;

    private static String[] allpermissions = {
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private boolean isNeedCheck = true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkpermission();
    }
    private void initViews() {

        }
    @Override
    protected void onResume() {
        super.onResume();
        checkpermission();
    }
    /**
     * 权限管理
     */
    private void checkpermission() {
        if (Build.VERSION.SDK_INT>=23){
            boolean needapply = false;
            for(int i = 0;i <allpermissions.length;i++ ){
                int checkpermission = ContextCompat.checkSelfPermission(getApplicationContext(),allpermissions[i]);
                if (checkpermission!= PackageManager.PERMISSION_GRANTED){
                    needapply = true;
                }
            }
            if(needapply){
                ActivityCompat.requestPermissions(MainActivity.this,allpermissions,1);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int  i = 0 ;i<grantResults.length;i++){
            if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(MainActivity.this, permissions[i]+"已授权",Toast.LENGTH_SHORT).show();
                initViews();
                isNeedCheck = false;
            }else{
                Toast.makeText(MainActivity.this,permissions[i]+"拒绝授权",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
