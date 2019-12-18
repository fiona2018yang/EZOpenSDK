package com.videogo.scanvideo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;

import com.videogo.adapter.ScanPicAdapter;
import com.videogo.adapter.ScanVideoAdapter;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.scanpic.CameraPicActivity;

import java.util.ArrayList;
import java.util.List;

import ezviz.ezopensdk.R;

public class ScanVideoActivity extends Activity {
    private RecyclerView rv;
    private ScanVideoAdapter adapter;
    private ImageButton back;
    private List<EZCameraInfo> list_ezCameras = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_video);
        initView();
    }

    private void initView() {
        rv = findViewById(R.id.rv);
        back = findViewById(R.id.back);
        LinearLayoutManager layoutManager =new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(layoutManager);
        list_ezCameras = getIntent().getParcelableArrayListExtra("cameras_pic");

        EZCameraInfo info = new EZCameraInfo();
        info.setCameraName("最近");
        list_ezCameras.add(0,info);

        rv.addItemDecoration(new DividerItemDecoration(getApplicationContext(),DividerItemDecoration.VERTICAL));
        adapter = new ScanVideoAdapter(list_ezCameras);
        rv.setAdapter(adapter);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        adapter.setOnItemClickListener(new ScanVideoAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent icamera = new Intent(v.getContext(), CameraVideoActivity.class);
                icamera.putExtra("video",list_ezCameras.get(position).getCameraName());
                startActivity(icamera);
            }
        });
    }
}
