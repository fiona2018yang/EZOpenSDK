package com.videogo.warning;

import android.app.Activity;
import android.content.Context;
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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.videogo.EzvizApplication;
import com.videogo.ToastNotRepeat;
import com.videogo.adapter.TitleWarningAdatter;
import com.videogo.been.AlarmContant;
import com.videogo.been.AlarmMessage;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.remoteplayback.list.PlaybackActivity2;
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
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

public class GarbageActivity extends Activity {
    private int alarm_type;
    private String TAG = "GarbageActivity";
    private List<String> title = new ArrayList<>();
    private List<String> time_title = new ArrayList<>();
    private List<AlarmMessage> alarmMessageList = new ArrayList<>();
    private List<EZCameraInfo> cameraInfoList = new ArrayList<>();
    private ExecutorService cachedThreadPool;
    private ExecutorService cachedThreadPool_1;
    private RecyclerView rv;
    private int page = 1;
    private int page_size = 12;
    private int list_size = 0;
    private SQLiteDatabase db;
    private RefreshLayout refreshLayout;
    private TitleWarningAdatter adatper;
    private Spinner spinner_time;
    private Spinner spinner_location;
    private TextView title_text;
    private List<AlarmMessage> list = new ArrayList<>();
    private ImageButton query;
    private ImageButton back;
    private String table_name;
    private Boolean refreshType = true;
    private String s1 = "全部";
    private String s2 = "全部";
    private SharedPreferences sharedPreferences;
    private String userid;
    private Context context;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 103:
                    Bundle bundle = msg.getData();
                    list.clear();
                    list = bundle.getParcelableArrayList("datalist");
                    if (alarmMessageList.size()!=0){
                        alarmMessageList.clear();
                    }
                    if (refreshType){
                        if (page_size<=list.size()){
                            alarmMessageList.addAll(list.subList(0,page_size));
                        }else{
                            alarmMessageList.addAll(list);
                        }
                        adatper.notifyDataSetChanged();
                    }
                    break;
                case 102:
                    ToastNotRepeat.show(GarbageActivity.this,"网络异常！");
                    break;
                case 101:
                    //                       try {
                    Bundle bundle2 = msg.getData();
                    List<AlarmMessage> list = new ArrayList<>();
                    list = bundle2.getParcelableArrayList("datalist");
                    list_size = list.size();
                    Log.d(TAG,"list.isze="+list_size);
                    if (refreshType){
                        //刷新
                        if (alarmMessageList.size()!=0){
                            alarmMessageList.clear();
                        }
                        alarmMessageList.addAll(list);
                        adatper.notifyDataSetChanged();
                    }else{
                        //加载更多
                        alarmMessageList.addAll(list);
                        adatper.notifyItemRangeInserted(alarmMessageList.size()-list_size,alarmMessageList.size());
                        adatper.notifyItemRangeChanged(alarmMessageList.size()-list_size,alarmMessageList.size());
                    }
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garbage);
        initView();
        initdata();
    }
    private void initView() {
        context = getApplicationContext();
        sharedPreferences = getSharedPreferences("userid",MODE_PRIVATE);
        userid = sharedPreferences.getString("id","1");
        db = ((EzvizApplication) getApplication()).getDatebase();
        cachedThreadPool = Executors.newFixedThreadPool(50);
        cachedThreadPool_1 = Executors.newFixedThreadPool(50);
        refreshLayout = findViewById(R.id.refreshLayout);
        spinner_time = findViewById(R.id.spinner_1);
        spinner_location = findViewById(R.id.spinner_2);
        query = findViewById(R.id.query);
        back = findViewById(R.id.back);
        title_text = findViewById(R.id.title);
        table_name = EzvizApplication.table_name;
        alarm_type = getIntent().getIntExtra("type",0);
        String str = getIntent().getStringExtra("title");
        cameraInfoList = getIntent().getParcelableArrayListExtra("camerainfo_list");
        title_text.setText(str);
        //查询数据
        //querydata(alarm_type);
        queryDataFromService(alarm_type,1);
        rv = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        rv.setLayoutManager(layoutManager);
        rv.addItemDecoration(CommItemDecoration.createVertical(context,getResources().getColor(R.color.blue_bg),4));
        rv.setItemAnimator(new DefaultItemAnimator());
        adatper = new TitleWarningAdatter(alarmMessageList,cameraInfoList,cachedThreadPool_1,context);
        rv.setAdapter(adatper);
        adatper.setSetOnItemClickListener(new TitleWarningAdatter.OnClickListener() {
            @Override
            public void OnItemClick(View view, int position , String address ) {
                Intent intent = new Intent(context, PlaybackActivity2.class);
                intent.putExtra("alarmMessage", alarmMessageList.get(position));
                intent.putExtra("address",address);
                startActivity(intent);
            }
        });
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE){
                    adatper.setScrolling(false);
                    Log.d("TAG","***********************************************************");
                    adatper.notifyDataSetChanged();
                }else{
                    adatper.setScrolling(true);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    private void initdata() {
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                Log.d("TAG","刷新");
                //刷新
                refreshType = true;
                page = 1;
                queryDataFromService(alarm_type,page);
                refreshLayout.finishRefresh(100);
                page++;
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                refreshType = false;
                queryDataFromService(alarm_type,page);
                if (list_size<page_size){
                    ToastNotRepeat.show(GarbageActivity.this,"暂无更多的数据啦");
                    refreshLayout.finishLoadMoreWithNoMoreData();
                    return;
                }else{
                    refreshLayout.setEnableLoadMore(true);
                    refreshLayout.finishLoadMore(100);
                    page++;
                }
//                if (page*page_size>list.size()||(page+1)*page_size>list.size()){
//                    ToastNotRepeat.show(GarbageActivity.this,"暂无更多的数据啦");
//                    refreshLayout.finishLoadMoreWithNoMoreData();
//                    return;
//                }else{
//                    //加载更多数据
//                    querydata(alarm_type);
//                    refreshLayout.setEnableLoadMore(true);
//                    refreshLayout.finishLoadMore(500);
//                }
            }
        });
        //自动刷新
        refreshLayout.autoRefresh();

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        cachedThreadPool.shutdown();
        cachedThreadPool_1.shutdown();
    }


    private void queryDataFromService(int type,int page){
        String url = AlarmContant.service_url+"api/getEarlyWarning";
        Map<String,String> map = new HashMap<>();
        map.put("userId",userid);
        map.put("type",String.valueOf(type));
        map.put("limit",String.valueOf(page_size));
        map.put("page",String.valueOf(page));
        OkHttpUtil.post(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message message = Message.obtain();
                message.what = 102;
                handler.sendMessage(message);
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
                    if (result.equals("true")){
                        String data = object.get("data").toString();
                        JSONObject objectdata = new JSONObject(data);
                        Gson gson = new Gson();
                        List<JsonObject> list_objects = gson.fromJson(objectdata.get("data").toString(),new TypeToken<List<JsonObject>>(){}.getType());
                        for (JsonObject object1 : list_objects){
                            AlarmMessage alarmMessage = gson.fromJson(object1,AlarmMessage.class);
                            alarmMessageList.add(alarmMessage);
                        }
                        Message message = Message.obtain();
                        message.what = 101;
                        Bundle bundle = new Bundle();
                        bundle.putParcelableArrayList("datalist", (ArrayList<? extends Parcelable>) alarmMessageList);
                        message.setData(bundle);
                        handler.sendMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },map);
    }

    private void querydata(int alarmtype){
        if (!refreshType && alarmMessageList.size() != 0){
            alarmMessageList.addAll(list.subList(page_size*page,page_size*(page+1)));
            adatper.notifyItemRangeInserted(list.size()-page_size,list.size());
            adatper.notifyItemRangeChanged(list.size()-page_size,list.size());
            page++;
        }else{
            Log.d("TAG","startrefresh");
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Cursor cursor = db.query(table_name,null,"type = ?",new String[]{String.valueOf(alarmtype)},null,null,null);
                    List<AlarmMessage> list = new ArrayList<>();
                    if (cursor.moveToFirst()){
                        do {
                            String message = cursor.getString(cursor.getColumnIndex("message"));
                            String type = cursor.getString(cursor.getColumnIndex("type"));
                            String latitude = cursor.getString(cursor.getColumnIndex("latitude"));
                            String longitude = cursor.getString(cursor.getColumnIndex("longitude"));
                            String altitude = cursor.getString(cursor.getColumnIndex("altitude"));
                            String address = cursor.getString(cursor.getColumnIndex("address"));
                            String imgPath = cursor.getString(cursor.getColumnIndex("imgPath"));
                            String videoPath = cursor.getString(cursor.getColumnIndex("videoPath"));
                            String createTime = cursor.getString(cursor.getColumnIndex("createTime"));
                            String startTime = cursor.getString(cursor.getColumnIndex("startTime"));
                            String endTime = cursor.getString(cursor.getColumnIndex("endTime"));
                            String channelNumber = cursor.getString(cursor.getColumnIndex("channelNumber"));
                            AlarmMessage alarmMessage = new AlarmMessage(message,type,latitude,longitude,altitude,
                                    address,imgPath,videoPath,createTime,startTime,endTime,channelNumber);
                            list.add(0,alarmMessage);
                        }while (cursor.moveToNext());
                    }
                    Message message = new Message();
                    message.what = 103;
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("datalist", (ArrayList<? extends Parcelable>) list);
                    message.setData(bundle);
                    handler.sendMessage(message);
                }
            };
            cachedThreadPool.execute(runnable);
        }
    }

}
