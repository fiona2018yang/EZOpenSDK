package com.videogo.warning;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.gson.Gson;
import com.videogo.EzvizApplication;
import com.videogo.ToastNotRepeat;
import com.videogo.adapter.WarningAdapter;
import com.videogo.been.AlarmContant;
import com.videogo.been.LocationData;
import com.videogo.been.SnCal;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.ui.util.FTPutils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ezviz.ezopensdk.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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

    private void addJsonData(){
        String url = "http://192.168.60.103:8080/api/addWarning";
        Map<String,String> map = new HashMap<>();
        map.put("message","message1");
        map.put("type","0");
        map.put("latitude","30.2555");
        map.put("longitude","114.56586");
        map.put("altitude","");
        map.put("address","");
        map.put("imgPath","");
        map.put("videoPath","");
        map.put("createTime","2019-11-29");//非空
        //map.put("startTime","1110000");
        //map.put("endTime","1565656");
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
    private class DownImg extends AsyncTask<Void,Void,Boolean>{
        @Override
        protected Boolean doInBackground(Void... voids) {
            FTPutils ftPutils = new FTPutils();
            Boolean flag = ftPutils.connect("192.168.60.79",21,"wh","wanghao");
            if (flag){
                String localpath = Environment.getExternalStorageDirectory().toString()+"/EZOpenSDK/cash";
                try {
                    ftPutils.downloadSingleFile("uftp/78b8998fe074fcfc708f8d91d93678aa.jpg", localpath,
                            "78b8998fe074fcfc708f8d91d93678aa.jpg", new FTPutils.FtpProgressListener() {
                                @Override
                                public void onFtpProgress(int currentStatus, long process, File targetFile) {
                                    Log.d("TAG","currenstatus="+currentStatus);
                                    Log.d("TAG","process="+process);
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return flag;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Log.d("TAG","flag="+aBoolean);

        }
    }
}
