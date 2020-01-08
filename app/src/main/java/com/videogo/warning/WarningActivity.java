package com.videogo.warning;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.videogo.EzvizApplication;
import com.videogo.adapter.WarningAdapter;
import com.videogo.been.AlarmContant;
import com.videogo.been.AlarmRead;
import com.videogo.openapi.bean.EZCameraInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ezviz.ezopensdk.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.videogo.been.AlarmContant.gettype;

public class WarningActivity  extends Activity {
    private String TAG ="WarningActivity";
    private List<EZCameraInfo> cameraInfoList = new ArrayList<>();
    private List<String> type_size = new ArrayList<>();
    private List<Integer> type_list = new ArrayList<>();
    private List<Map<String,Integer>> type_size_url = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private RecyclerView rv;
    private ImageButton back;
    private RefreshLayout refreshLayout;
    private ExecutorService executors;
    private ExecutorService executors_1;
    private WarningAdapter adapter;
    private List<String> list;
    private int user_type;
    private int alarm_type;
    private Button chaxun;
    private SQLiteDatabase db;
    private Cursor cursor;
    private String userid;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case AlarmContant.MESSAGE_TYPE_TRUCK_IDENTITY:
                case AlarmContant.MESSAGE_TYPE_ILLEGAL_BUILDING:
                case AlarmContant.MESSAGE_TYPE_ILLEGAL_PLANT:
                case AlarmContant.MESSAGE_TYPE_STRAW_BURNING:
                case AlarmContant.MESSAGE_TYPE_RIVER_MONITOR:
                case AlarmContant.MESSAGE_TYPE_COMPANY_MANAGE:
                    Bundle bundle1 = msg.getData();
                    int count1 = Integer.parseInt(bundle1.getString("count"));
                    Log.d(TAG,"COUNT="+count1);
                    if (count1>=0){
                        Map<String , Integer> map = new HashMap<>();
                        map.put("type",msg.what);
                        map.put("size",count1);
                        type_size_url.add(map);
                        adapter.setType_size_url(type_size_url);
                        adapter.notifyDataSetChanged();
                    }
                    break;
                case 101:
                    Bundle bundle = msg.getData();
                    List<String> list = bundle.getStringArrayList("type_size");
                    type_size.clear();
                    type_size.addAll(list);
                    adapter.setType_size(type_size);
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warning);
        initView();
        queryTypeSize();
        queryTypeSizeFromUrl();
        refresh();
    }

    private void refresh() {
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                queryTypeSizeFromUrl();
                refreshLayout.finishRefresh(100);
            }
        });
    }
    private void queryTypeSizeFromUrl() {
        for (int i = 0 ; i < list.size() ; i++){
            String url = AlarmContant.service_url + "api/getEarlyWarning";
            Map<String, String> map = new HashMap<>();
            map.put("userId", userid);
            map.put("type", String.valueOf(gettype(list.get(i))));
            map.put("limit", "10");
            map.put("page", "0");
            int finalI = i;
            OkHttpUtil.post(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    try {
                        JSONObject object = new JSONObject(responseBody);
                        String result = object.get("success").toString();
                        if (result.equals("true")) {
                            String data = object.get("data").toString();
                            JSONObject obj_data = new JSONObject(data);
                            String count = obj_data.get("count").toString();
                            Message msg = Message.obtain();
                            msg.what = gettype(list.get(finalI));
                            Bundle bundle = new Bundle();
                            bundle.putString("count",count);
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },map);
        }
    }

    private void queryTypeSize() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                List<String> list = new ArrayList<>();
                cursor = db.query("alarmSize", null, null, null, null, null, null);
                if (cursor.moveToLast()){
                    for (int i = 0 ; i < type_list.size() ; i++ ){
                        String size = cursor.getString(cursor.getColumnIndex("size"+String.valueOf(type_list.get(i))));
                        list.add(size);
                    }
                }
                Message msg = new Message();
                msg.what = 101;
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("type_size", (ArrayList<String>) list);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        };
        executors_1.execute(runnable);
    }

    private void initView() {
        rv = findViewById(R.id.rv);
        back = findViewById(R.id.back);
        user_type = EzvizApplication.user_type;
        chaxun = findViewById(R.id.chaxun);
        cameraInfoList = getIntent().getParcelableArrayListExtra("cameras_pic");
        db = ((EzvizApplication) getApplication()).getDatebase();
        sharedPreferences = getSharedPreferences("userid", MODE_PRIVATE);
        userid = sharedPreferences.getString("id", "1");
        executors = Executors.newFixedThreadPool(5);
        executors_1 = Executors.newFixedThreadPool(5);
        refreshLayout = findViewById(R.id.refresh_layout);
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
        Log.d(TAG,"list.size="+list.size());
        for (int i = 0 ; i < list.size() ; i++){
            int type= gettype(list.get(i));
            type_list.add(type);
        }
        LinearLayoutManager layoutManager =new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(layoutManager);
        //添加分割线
        rv.addItemDecoration(new DividerItemDecoration(getApplicationContext(),DividerItemDecoration.VERTICAL));
        adapter = new WarningAdapter(getApplicationContext(), list, new WarningAdapter.setOnclick() {
            @Override
            public void onClick(View view,int position,int size_url) {
                if (position==6){
                    Intent i = new Intent(view.getContext(),DataQueryActivity.class);
                    startActivity(i);
                }else {
                    upDateUi(position,size_url);
                    alarm_type = gettype(list.get(position));
                    Log.d("TAG","alarm_type="+alarm_type);
                    String title = list.get(position);
                    Intent i1 = new Intent(view.getContext(), GarbageActivity.class);
                    i1.putExtra("type",alarm_type);
                    i1.putExtra("title",title);
                    i1.putParcelableArrayListExtra("camerainfo_list", (ArrayList<? extends Parcelable>) cameraInfoList);
                    startActivity(i1);
                }
            }
        });
        rv.setAdapter(adapter);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void upDateUi(int position,int size_url){
        String type = list.get(position);
        String size_db = type_size.get(position);
        type_size.set(position,String.valueOf(size_url));
        adapter.setType_size(type_size);
        adapter.notifyDataSetChanged();
        //更新数据库
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ContentValues values = new ContentValues();
                values.put("size"+ gettype(type),String.valueOf(size_url));
                db.update("alarmSize", values, "size"+gettype(type)+"=?", new String[]{size_db});
            }
        };
        executors.execute(runnable);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        executors.shutdown();
        executors_1.shutdown();
        handler.removeCallbacksAndMessages(null);
        cursor.close();
    }
}
