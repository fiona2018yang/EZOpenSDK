package com.videogo.warning;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.videogo.been.WarningData;

import java.util.ArrayList;
import java.util.List;

import ezviz.ezopensdk.R;

public class GarbageActivity extends Activity {
    private List<String> timelist = new ArrayList<>();
    private List<WarningData> dataList_1 = new ArrayList<>();
    private List<WarningData> dataList_2 = new ArrayList<>();
    private List<WarningData> dataList_3 = new ArrayList<>();
    private List<String> title = new ArrayList<>();
    private List<String> time_title = new ArrayList<>();
    private List<List<WarningData>> datalist_list = new ArrayList<>();
    private RecyclerView rv;
    private TitleWarningAdatper adatper;
    private Spinner spinner_time;
    private Spinner spinner_location;
    private ImageButton query;
    private ImageButton back;
    private String s1 = "全部";
    private String s2 = "全部";
    private WarningData data1;
    private WarningData data2;
    private WarningData data3;
    private WarningData data4;
    private WarningData data5;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garbage);
        initdata();
    }

    private void initdata() {
        spinner_time = findViewById(R.id.spinner_1);
        spinner_location = findViewById(R.id.spinner_2);
        query = findViewById(R.id.query);
        back = findViewById(R.id.back);

        String path1 = Environment.getExternalStorageDirectory().toString()+"/EZOpenSDK/CapturePicture/大运河(C47666812)/150753234.jpg";
        String path2 = Environment.getExternalStorageDirectory().toString()+"/EZOpenSDK/CapturePicture/河湾公园(203530572)/150350042.jpg";
        String path3 = Environment.getExternalStorageDirectory().toString()+"/EZOpenSDK/CapturePicture/双塔(203571341)/150356009.jpg";
        String path4 = Environment.getExternalStorageDirectory().toString()+"/EZOpenSDK/CapturePicture/双塔(203571341)/150356009.jpg";
        String path5 = Environment.getExternalStorageDirectory().toString()+"/EZOpenSDK/CapturePicture/大运河(C47666812)/150753234.jpg";
        data1 = new WarningData("2019-08","西区","[114.3587,37.8568]",path1);
        data2 = new WarningData("2019-09","西区","[114.3587,37.8568]",path2);
        data3 = new WarningData("2019-08","南区","[114.3587,37.8568]",path3);
        data4 = new WarningData("2019-10","南区","[114.3587,37.8568]",path4);
        data5 = new WarningData("2019-10","西区","[114.3587,37.8568]",path5);

        dataList_1.add(data1);
        dataList_1.add(data3);
        dataList_2.add(data2);
        dataList_3.add(data4);
        dataList_3.add(data5);
        timelist.add(data1.getTime());
        timelist.add(data2.getTime());
        timelist.add(data4.getTime());
        datalist_list.add(dataList_1);
        datalist_list.add(dataList_2);
        datalist_list.add(dataList_3);

        title.add("全部");
        title.add("西区");
        title.add("南区");

        time_title.add("全部");
        time_title.add("2019-08");
        time_title.add("2019-09");
        time_title.add("2019-10");

        rv = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);
        rv.addItemDecoration(CommItemDecoration.createVertical(this,getResources().getColor(R.color.viewfinder_frame),16));
        rv.setItemAnimator(new DefaultItemAnimator());
        adatper = new TitleWarningAdatper(this,timelist,datalist_list);
        rv.setAdapter(adatper);

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
                if (s1.equals("全部")&&s2.equals("全部")){
                    timelist.clear();
                    timelist.add(data1.getTime());
                    timelist.add(data2.getTime());
                    timelist.add(data4.getTime());
                    datalist_list.clear();
                    datalist_list.add(dataList_1);
                    datalist_list.add(dataList_2);
                    datalist_list.add(dataList_3);
                    adatper.notifyDataSetChanged();
                }else if(s1.equals("全部")&&s2.equals("2019-08")){
                    timelist.clear();
                    timelist.add(data1.getTime());
                    datalist_list.clear();
                    datalist_list.add(dataList_1);
                    adatper.notifyDataSetChanged();
                }else if(s1.equals("全部")&&s2.equals("2019-09")){
                    timelist.clear();
                    timelist.add(data2.getTime());
                    datalist_list.clear();
                    datalist_list.add(dataList_2);
                    adatper.notifyDataSetChanged();
                }else if(s1.equals("全部")&&s2.equals("2019-10")){
                    timelist.clear();
                    timelist.add(data4.getTime());
                    datalist_list.clear();
                    datalist_list.add(dataList_3);
                    adatper.notifyDataSetChanged();
                }else if(s1.equals("西区")&&s2.equals("全部")){
                    timelist.clear();
                    timelist.add(data1.getTime());
                    timelist.add(data2.getTime());
                    timelist.add(data4.getTime());
                    List<WarningData> list1 = new ArrayList<>();
                    list1.add(data1);
                    List<WarningData> list2 = new ArrayList<>();
                    list2.add(data2);
                    List<WarningData> list3 = new ArrayList<>();
                    list3.add(data5);
                    datalist_list.clear();
                    datalist_list.add(list1);
                    datalist_list.add(list2);
                    datalist_list.add(list3);
                    adatper.notifyDataSetChanged();
                }else if(s1.equals("西区")&&s2.equals("2019-08")){
                    timelist.clear();
                    timelist.add(data1.getTime());
                    List<WarningData> list1 = new ArrayList<>();
                    list1.add(data1);
                    datalist_list.clear();
                    datalist_list.add(list1);
                    adatper.notifyDataSetChanged();
                }else if(s1.equals("西区")&&s2.equals("2019-09")){
                    timelist.clear();
                    timelist.add(data2.getTime());
                    List<WarningData> list1 = new ArrayList<>();
                    list1.add(data2);
                    datalist_list.clear();
                    datalist_list.add(list1);
                    adatper.notifyDataSetChanged();
                }else if(s1.equals("西区")&&s2.equals("2019-10")){
                    timelist.clear();
                    timelist.add(data5.getTime());
                    List<WarningData> list1 = new ArrayList<>();
                    list1.add(data5);
                    datalist_list.clear();
                    datalist_list.add(list1);
                    adatper.notifyDataSetChanged();
                }else if(s1.equals("南区")&&s2.equals("全部")){
                    timelist.clear();
                    timelist.add(data3.getTime());
                    timelist.add(data4.getTime());
                    List<WarningData> list1 = new ArrayList<>();
                    list1.add(data3);
                    List<WarningData> list2 = new ArrayList<>();
                    list2.add(data4);
                    datalist_list.clear();
                    datalist_list.add(list1);
                    datalist_list.add(list2);
                    adatper.notifyDataSetChanged();
                }else if(s1.equals("南区")&&s2.equals("2019-08")){
                    timelist.clear();
                    timelist.add(data3.getTime());
                    List<WarningData> list1 = new ArrayList<>();
                    list1.add(data3);
                    datalist_list.clear();
                    datalist_list.add(list1);
                    adatper.notifyDataSetChanged();
                }else if(s1.equals("南区")&&s2.equals("2019-09")){
                    timelist.clear();
                    datalist_list.clear();
                    adatper.notifyDataSetChanged();
                }else if(s1.equals("南区")&&s2.equals("2019-10")){
                    timelist.clear();
                    timelist.add(data4.getTime());
                    List<WarningData> list1 = new ArrayList<>();
                    list1.add(data4);
                    datalist_list.clear();
                    datalist_list.add(list1);
                    adatper.notifyDataSetChanged();
                }
            }
        });
    }
}
