package com.videogo;

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
import com.videogo.openapi.bean.EZDeviceInfo;
import java.util.ArrayList;
import java.util.List;
import ezviz.ezopensdk.R;

public class ScanPicActivity extends Activity {
    private RecyclerView rv;
    private ScanPicAdapter adapter;
    private ImageButton back;
    private List<EZDeviceInfo> list_ezdevices = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_pic);
        initView();
    }

    private void initView() {
        rv = findViewById(R.id.rv);
        back = findViewById(R.id.back);
        LinearLayoutManager layoutManager =new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);
        list_ezdevices = getIntent().getParcelableArrayListExtra("devices_pic");
        rv.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        adapter = new ScanPicAdapter(list_ezdevices);
        rv.setAdapter(adapter);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        adapter.setOnItemClickListener(new ScanPicAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent icamera = new Intent(v.getContext(),CameraPicActivity.class);
                icamera.putExtra("pic",list_ezdevices.get(position).getDeviceName());
                startActivity(icamera);
            }
        });
    }
}
