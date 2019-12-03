package com.videogo.warning;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.esri.core.geometry.Point;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.videogo.EzvizApplication;
import com.videogo.ToastNotRepeat;
import com.videogo.adapter.TitleWarningAdatter;
import com.videogo.been.AlarmMessage;
import com.videogo.constant.IntentConsts;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.remoteplayback.list.PlaybackActivity;
import com.videogo.remoteplayback.list.PlaybackActivity2;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ezviz.ezopensdk.R;

public class GarbageActivity extends Activity {
    private int alarm_type;
    private String TAG = "GarbageActivity";
    private List<String> title = new ArrayList<>();
    private List<String> time_title = new ArrayList<>();
    private List<AlarmMessage> alarmMessageList = new ArrayList<>();
    private List<EZCameraInfo> cameraInfoList = new ArrayList<>();
    private List<String> address_list = new ArrayList<>();
    private RecyclerView rv;
    private int page = 1;
    private int page_size = 3;
    private SQLiteDatabase db;
    private RefreshLayout refreshLayout;
    private TitleWarningAdatter adatper;
    private Spinner spinner_time;
    private Spinner spinner_location;
    private TextView title_text;
    private List<AlarmMessage> list = new ArrayList<>();
    private ImageButton query;
    private ImageButton back;
    private Handler handler;
    private GeoCoder geoCoder;
    private Boolean refreshType = true;
    private String s1 = "全部";
    private String s2 = "全部";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garbage);
        initView();
        initdata();
    }
    //上拉加载更新address
    private void searchaddress_push(){
        for (int i = page_size*page ; i < page_size*(page+1) ; i++){
            int finalI = i;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    double la = Double.parseDouble(alarmMessageList.get(finalI).getLatitude());
                    double ln = Double.parseDouble(alarmMessageList.get(finalI).getLongitude());
                    //坐标转换
                    CoordinateConverter converter  = new CoordinateConverter().from(CoordinateConverter.CoordType.GPS).coord(tramsform(new Point(ln,la)));
                    LatLng latLng = converter.convert();
                    Log.d("TAG","latlng....="+latLng.toString());
                    if (latLng!=null){
                        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng).radius(500));
                    }
                }
            },100*i);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0 ; i < alarmMessageList.size() ; i++){
                        if (address_list.get(i)!=null){
                            alarmMessageList.get(i).setAddress(address_list.get(i));
                        }
                    }
                    adatper.notifyDataSetChanged();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },200*page_size);

    }

    //下拉刷新，更新address
    private void searchAddress() {
        Log.d("TAG","alarm.size="+alarmMessageList.size());
        Handler handler = new Handler();
        for (int i = 0 ; i < alarmMessageList.size() ; i++){
            int finalI = i;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    double la = Double.parseDouble(alarmMessageList.get(finalI).getLatitude());
                    double ln = Double.parseDouble(alarmMessageList.get(finalI).getLongitude());
                    //坐标转换
                    CoordinateConverter converter  = new CoordinateConverter().from(CoordinateConverter.CoordType.GPS).coord(tramsform(new Point(ln,la)));
                    LatLng latLng = converter.convert();
                    Log.d("TAG","latlng....="+latLng.toString());
                    if (latLng!=null){
                        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng).radius(500));
                    }
                }
            },100*i);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0 ; i < alarmMessageList.size() ; i++){
                        if (address_list.get(i)!=null){
                            alarmMessageList.get(i).setAddress(address_list.get(i));
                        }
                    }
                    adatper.notifyDataSetChanged();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },200*page_size);
    }
    private void initView() {
        geoCoder = GeoCoder.newInstance();
        db = ((EzvizApplication) getApplication()).getDatebase();
        refreshLayout = findViewById(R.id.refreshLayout);
        spinner_time = findViewById(R.id.spinner_1);
        spinner_location = findViewById(R.id.spinner_2);
        query = findViewById(R.id.query);
        back = findViewById(R.id.back);
        title_text = findViewById(R.id.title);
        alarm_type = getIntent().getIntExtra("type",0);
        String str = getIntent().getStringExtra("title");
        cameraInfoList = getIntent().getParcelableArrayListExtra("camerainfo_list");
        title_text.setText(str);
        //查询数据
        querydata(alarm_type);
        rv = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);
        rv.addItemDecoration(CommItemDecoration.createVertical(this,getResources().getColor(R.color.blue_bg),4));
        rv.setItemAnimator(new DefaultItemAnimator());
        adatper = new TitleWarningAdatter(alarmMessageList,cameraInfoList,this);
        rv.setAdapter(adatper);
        adatper.setSetOnItemClickListener(new TitleWarningAdatter.OnClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                Intent intent = new Intent(GarbageActivity.this, PlaybackActivity2.class);
                intent.putExtra("alarmMessage", alarmMessageList.get(position));
                intent.putParcelableArrayListExtra("camerainfo_list", (ArrayList<? extends Parcelable>) cameraInfoList);
                startActivity(intent);
            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 103:
                        Bundle bundle = msg.getData();
                        list.clear();
                        list = bundle.getParcelableArrayList("datalist");
                        if (alarmMessageList.size()!=0){
                            alarmMessageList.clear();
                            address_list.clear();
                        }
                        if (refreshType){
                            if (page_size<=list.size()){
                                alarmMessageList.addAll(list.subList(0,page_size));
                            }else{
                                alarmMessageList.addAll(list);
                            }
                            adatper.notifyDataSetChanged();
                            searchAddress();
                        }
                        Log.d(TAG, "list.size....="+list.size());
                        break;
                }
            }
        };
        geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                String address = "";
                if (reverseGeoCodeResult == null||reverseGeoCodeResult.error!= SearchResult.ERRORNO.NO_ERROR){
                    Log.d("TAG","没有检测到结果");
                    address = "未知";
                    return ;
                }else{
                    ReverseGeoCodeResult.AddressComponent addressComponent = reverseGeoCodeResult.getAddressDetail();
                    //address = reverseGeoCodeResult.getAddress();
                    String detaddress = addressComponent.countryName+addressComponent.province+addressComponent.city+
                            addressComponent.district+addressComponent.town+addressComponent.street+addressComponent.streetNumber;
                    String des = reverseGeoCodeResult.getSematicDescription();
                    if (detaddress == null||detaddress.equals("")){
                        address = "未知";
                    }else{
                        if (!des.equals("")){
                            address = detaddress+"("+des+")";
                        }else{
                            address = detaddress;
                        }
                    }
                    address_list.add(address);
                }
            }
        });
    }

    private void initdata() {
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.getLayout().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //刷新
                        refreshType = true;
                        page = 1;
                        querydata(alarm_type);
                        refreshLayout.finishRefresh();
                        refreshLayout.resetNoMoreData();
                    }
                },1000);
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.getLayout().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshType = false;
                        if (page*(page_size+1)>list.size()){
                            ToastNotRepeat.show(GarbageActivity.this,"暂无更多的数据啦");
                            refreshLayout.finishLoadMoreWithNoMoreData();
                            return;
                        }else{
                            //加载更多数据
                            querydata(alarm_type);
                            refreshLayout.setEnableLoadMore(true);
                            refreshLayout.finishLoadMore();
                        }
                    }
                },1000);
            }
        });
        //自动刷新
        //refreshLayout.autoRefresh();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        geoCoder.destroy();
    }
    private LatLng tramsform(Point p ){
        LatLng latLng = new LatLng(p.getY(),p.getX());
        return latLng;
    }
    private void querydata(int alarmtype){
        if (!refreshType && alarmMessageList.size() != 0){
            alarmMessageList.addAll(list.subList(page_size*page,page_size*(page+1)));
            adatper.notifyItemRangeInserted(list.size()-page_size,list.size());
            adatper.notifyItemRangeChanged(list.size()-page_size,list.size());
            searchaddress_push();
            page++;
        }else{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Cursor cursor = db.query("alarmMessage",null,"type = ?",new String[]{String.valueOf(alarmtype)},null,null,null);
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
            }).start();
        }
    }

}
