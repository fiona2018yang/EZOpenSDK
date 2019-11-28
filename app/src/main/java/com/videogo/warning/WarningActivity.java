package com.videogo.warning;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.videogo.EzvizApplication;
import com.videogo.ToastNotRepeat;
import com.videogo.adapter.WarningAdapter;
import com.videogo.been.AlarmContant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ezviz.ezopensdk.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WarningActivity  extends Activity {
    private String TAG ="WarningActivity";
    private RecyclerView rv;
    private ImageButton back;
    private WarningAdapter adapter;
    private List<String> list;
    private int user_type;
    private int alarm_type;
    private Button add;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warning);
        initView();
    }

    private void initView() {
        rv = findViewById(R.id.rv);
        back = findViewById(R.id.back);
        add = findViewById(R.id.add);
        user_type = EzvizApplication.user_type;
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
                switch (position){
                    case 0:
                        String name = list.get(position);
                        alarm_type = gettype(name);
                        Intent i1 = new Intent(view.getContext(), GarbageActivity.class);
                        i1.putExtra("type",alarm_type);
                        startActivity(i1);
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
                ToastNotRepeat.show(WarningActivity.this,"点击了"+list.get(position));
            }
        });
        rv.setAdapter(adapter);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //for (int i = 0 ; i < 10 ; i ++){
                String url = "http://192.168.60.103:8080/api/addWarning";
                Map<String,String> map = new HashMap<>();
                map.put("message","mdsfsfsdsfdf");
                map.put("type","2");
                map.put("latitude","30.2555");
                map.put("longitude","114.56586");
                map.put("altitude","");
                map.put("address","");
                map.put("imgPath","");
                map.put("videoPath","");
                map.put("createTime","");
                map.put("startTime","");
                map.put("endTime","");
                map.put("channelNumber","2");
                OkHttpUtil.post(url, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(TAG, "onFailure: ",e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseBody = response.body().string();
                        Log.d(TAG, "responseBody="+responseBody);
                    }
                },map);
            }
            //  }
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
