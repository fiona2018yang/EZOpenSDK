package com.videogo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ezviz.ezopensdk.R;

public class MainActivity extends FragmentActivity {
    private ViewPager mViewPager;
    private List<Fragment> list_frag =new ArrayList<>();
    //private FragmentPagerAdapter mAdapter;
    private FragmentStatePagerAdapter mAdapter;
    private MapFrag mapFrag;
    private CameraListFrag cameraListFrag;
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
        mViewPager = findViewById(R.id.viewpager);
        mapFrag = new MapFrag();
        list_frag.add(mapFrag);
        cameraListFrag = new CameraListFrag();
        list_frag.add(cameraListFrag);
        mAdapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return list_frag.get(position);
            }

            @Override
            public int getCount() {
                return list_frag.size();
            }
        };
//        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
//            @Override
//            public Fragment getItem(int position) {
//                return list_frag.get(position);
//            }
//
//            @Override
//            public int getCount() {
//                return list_frag.size();
//            }
//        };
        mViewPager.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkpermission();
    }
    public void reloadFragView(){
        if (list_frag.contains(mapFrag)){
            list_frag.remove(mapFrag);
            getSupportFragmentManager().beginTransaction().remove(mapFrag).commit();
            mapFrag = new MapFrag();
            list_frag.add(mapFrag);
            Log.i("TAG","reload");
            mAdapter.notifyDataSetChanged();
        }
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
