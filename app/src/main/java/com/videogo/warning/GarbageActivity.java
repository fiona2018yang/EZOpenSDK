package com.videogo.warning;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.videogo.EzvizApplication;
import com.videogo.adapter.TitleWarningAdatter;
import com.videogo.been.AlarmMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ezviz.ezopensdk.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class GarbageActivity extends Activity {
    private int alarm_type;
    private String TAG = "GarbageActivity";
    private List<String> title = new ArrayList<>();
    private List<String> time_title = new ArrayList<>();
    private List<AlarmMessage> alarmMessageList = new ArrayList<>();
    private RecyclerView rv;
    private SQLiteDatabase db;
    private TitleWarningAdatter adatper;
    private Spinner spinner_time;
    private Spinner spinner_location;
    private ImageButton query;
    private ImageButton back;
    private Handler handler;
    private String s1 = "全部";
    private String s2 = "全部";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garbage);
        initView();
        initdata();
    }

    private void initView() {
        db = ((EzvizApplication) getApplication()).getDatebase();
        spinner_time = findViewById(R.id.spinner_1);
        spinner_location = findViewById(R.id.spinner_2);
        query = findViewById(R.id.query);
        back = findViewById(R.id.back);
        alarm_type = getIntent().getIntExtra("type",0);
//        int number=0;
//        Cursor c = db.rawQuery("select * from alarmMessage",null);
//        number = c.getCount();
//        c.close();
//        if (number == 0 ){
//        }else{
//            querydatafromsqlite();
//        }
        startquerydata(alarm_type,1);

        rv = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);
        rv.addItemDecoration(CommItemDecoration.createVertical(this,getResources().getColor(R.color.viewfinder_frame),16));
        rv.setItemAnimator(new DefaultItemAnimator());
        adatper = new TitleWarningAdatter(alarmMessageList,this);
        rv.setAdapter(adatper);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 103:
                        Bundle bundle = msg.getData();
                        List<AlarmMessage> list = bundle.getParcelableArrayList("datalist");
                        alarmMessageList.addAll(list);
                        adatper.notifyDataSetChanged();
                        Log.d(TAG, "list.size1="+list.size());
                        break;
                }
            }
        };
    }

    private void initdata() {
        ArrayAdapter adapter = new ArrayAdapter<String>(this,R.layout.spinner_item,title);
        adapter.setDropDownViewResource(R.layout.dropdown_stytle);
        spinner_time.setAdapter(adapter);
        spinner_time.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                s1 = getResources().getStringArray(R.array.location_title)[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter adapter2 = new ArrayAdapter<String>(this,R.layout.spinner_item,time_title);
        adapter2.setDropDownViewResource(R.layout.dropdown_stytle);
        spinner_location.setAdapter(adapter2);
        spinner_location.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                s2 = getResources().getStringArray(R.array.time_title)[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void querydatafromsqlite(int alarmtype,int page){

    }

    private void startquerydata(int alarmtype,int page) {
        String url = "http://192.168.60.103:8080/api/getEarlyWarning";
        Map<String,String> map = new HashMap<>();
        map.put("userId","1194134346510815234");
        map.put("type",String.valueOf(alarmtype));
        map.put("limit","3");
        map.put("page",String.valueOf(page));
        OkHttpUtil.post(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: ",e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d(TAG, "result="+responseBody);
                List<AlarmMessage> alarmMessageList = new ArrayList<>();
                try {
                    JSONObject object = new JSONObject(responseBody);
                    String result = object.get("success").toString();
                    String data = object.get("data").toString();
                    JSONObject objectdata = new JSONObject(data);
                    Gson gson = new Gson();
                    List<JsonObject> list_objects = gson.fromJson(objectdata.get("data").toString(),new TypeToken<List<JsonObject>>(){}.getType());
                    for (JsonObject object1 : list_objects){
                        AlarmMessage alarmMessage = gson.fromJson(object1,AlarmMessage.class);
                        alarmMessageList.add(0,alarmMessage);
                    }
                    Message message = new Message();
                    message.what = 103;
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("datalist", (ArrayList<? extends Parcelable>) alarmMessageList);
                    message.setData(bundle);
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },map);
    }
}
