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
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.scanpic.CameraPicActivity;

import java.util.ArrayList;
import java.util.List;

import ezviz.ezopensdk.R;

public class ScanVideoActivity extends Activity {
    private RecyclerView rv;
    private ScanVideoAdapter adapter;
    private ImageButton back;
    private List<EZDeviceInfo> list_ezdevices = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_video);
        initView();
    }

    private void initView() {
        rv = findViewById(R.id.rv);
        back = findViewById(R.id.back);
        LinearLayoutManager layoutManager =new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);
        list_ezdevices = getIntent().getParcelableArrayListExtra("devices_video");

        EZDeviceInfo info = new EZDeviceInfo();
        info.setDeviceName("最近");
        list_ezdevices.add(0,info);

        rv.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        adapter = new ScanVideoAdapter(list_ezdevices);
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
                icamera.putExtra("video",list_ezdevices.get(position).getDeviceName());
                startActivity(icamera);
            }
        });
    }
}
