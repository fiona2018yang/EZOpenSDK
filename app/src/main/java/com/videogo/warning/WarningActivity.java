package com.videogo.warning;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import com.videogo.EzvizApplication;
import com.videogo.adapter.WarningAdapter;
import com.videogo.been.AlarmContant;
import com.videogo.openapi.bean.EZCameraInfo;
import java.util.ArrayList;
import java.util.List;
import ezviz.ezopensdk.R;

public class WarningActivity  extends Activity {
    private String TAG ="WarningActivity";
    private List<EZCameraInfo> cameraInfoList = new ArrayList<>();
    private RecyclerView rv;
    private ImageButton back;
    private WarningAdapter adapter;
    private List<String> list;
    private int user_type;
    private int alarm_type;
    private Button chaxun;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warning);
        initView();
    }

    private void initView() {
        rv = findViewById(R.id.rv);
        back = findViewById(R.id.back);
        user_type = EzvizApplication.user_type;
        chaxun = findViewById(R.id.chaxun);
        cameraInfoList = getIntent().getParcelableArrayListExtra("cameras_pic");
        switch (user_type){
            case AlarmContant.USER_TYPE_CHENGGUAN:
                list = AlarmContant.getList_chengguan();
                break;
            case AlarmContant.USER_TYPE_SHIWUJU:
                list = AlarmContant.getList_shiwuju();
                break;
            case AlarmContant.USER_TYPE_HUANBAOJU:
                list = AlarmContant.getList_huanbaoju();
                break;
            case AlarmContant.USER_TYPE_ZHIFAJU:
                list = AlarmContant.getList_zhifaju();
                break;
            case AlarmContant.USER_TYPE_FAZHANJU:
                list = AlarmContant.getList_fazhanju();
                break;
            case AlarmContant.USER_TYPE_SUPER:
                list = AlarmContant.getList_super();
                break;
        }
        LinearLayoutManager layoutManager =new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);
        //添加分割线
        rv.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        adapter = new WarningAdapter(this, list, new WarningAdapter.setOnclick() {
            @Override
            public void onClick(View view,int position) {
                Log.i("TAG","list.size="+list.size());
                alarm_type = gettype(list.get(position));
                String title = list.get(position);
                Intent i1 = new Intent(view.getContext(), GarbageActivity.class);
                i1.putExtra("type",alarm_type);
                i1.putExtra("title",title);
                i1.putParcelableArrayListExtra("camerainfo_list", (ArrayList<? extends Parcelable>) cameraInfoList);
                startActivity(i1);
            }
        });
        rv.setAdapter(adapter);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        chaxun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private int gettype(String str){
        if (str.equals("渣土车识别定位跟踪")){
            return AlarmContant.MESSAGE_TYPE_TRUCK_IDENTITY;
        }else if (str.equals("违法乱建")){
            return AlarmContant.MESSAGE_TYPE_ILLEGAL_BUILDING;
        }else if (str.equals("违章种植")){
            return AlarmContant.MESSAGE_TYPE_ILLEGAL_PLANT;
        }else if (str.equals("秸秆焚烧")){
            return AlarmContant.MESSAGE_TYPE_STRAW_BURNING;
        }else if (str.equals("河道监测")){
            return AlarmContant.MESSAGE_TYPE_RIVER_MONITOR;
        }else if (str.equals("园区企业监管")){
            return AlarmContant.MESSAGE_TYPE_COMPANY_MANAGE;
        }
        return 0;
    }
}
