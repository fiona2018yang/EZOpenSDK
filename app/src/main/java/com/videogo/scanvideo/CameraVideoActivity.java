package com.videogo.scanvideo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.videogo.MyDatabaseHelper;
import com.videogo.adapter.TitleAdapter;
import com.videogo.scanpic.PictureActivity;
import com.videogo.ui.util.DataUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import ezviz.ezopensdk.R;

public class CameraVideoActivity extends Activity {
    private RecyclerView rv;
    private TextView tv;
    private List<String> datalist;
    private List<String> time_list;
    private List<Integer> index_list;
    private List<List<String>> file_list ;
    private List<String> title_list;
    private TitleAdapter adapter;
    private String device_name;
    private int width;
    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase db;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_pic);
        initView();
    }
    private void initView() {
        dbHelper = new MyDatabaseHelper(CameraVideoActivity.this, "filepath.db", null, 1);
        db = dbHelper.getWritableDatabase();
        rv = (RecyclerView) findViewById(R.id.recyclerView);
        tv = (TextView) findViewById(R.id.text);
        device_name = getIntent().getStringExtra("video");
        width = getScreenProperty();
        initData();
        if(adapter == null){
            rv.setLayoutManager(new LinearLayoutManager(this));
            adapter = new TitleAdapter(this,title_list,file_list,width);
            rv.setAdapter(adapter);
        }
    }

    private void initData() {
        datalist = new ArrayList<>();
        title_list = new ArrayList<>();

        if (!device_name.equals("最近")){
            //获取所有文件的路径
            String path = Environment.getExternalStorageDirectory().toString()+"/EZOpenSDK/CaptureVideo/"+device_name;
            datalist = DataUtils.getImagePathFromSD(path);
            //获取所有文件最后修改时间
            time_list = getFileTime(datalist);
            //获取时间相同的文件的下标
            index_list = getIndex(time_list);
            //根据下标，截取出时间相同的文件集合
            file_list = getData(datalist,index_list);
            if (datalist.size()==0){
                rv.setVisibility(View.GONE);
                tv.setVisibility(View.VISIBLE);
            }
            for (int i = 0 ; i < index_list.size() ; i++){
                title_list.add(time_list.get(index_list.get(i)));
            }
        }else{
            Cursor cursor = db.query("videofilepath", null, null, null, null, null, null);
            if (cursor.moveToFirst()){
                do {
                    String file_path = cursor.getString(cursor.getColumnIndex("path"));
                    datalist.add(file_path);
                }while (cursor.moveToNext());
            }
            cursor.close();
            time_list = getFileTime(datalist);
            index_list = getIndex(time_list);
            file_list = getData(datalist,index_list);
            if (datalist.size()==0){
                rv.setVisibility(View.GONE);
                tv.setVisibility(View.VISIBLE);
            }
            for (int i = 0 ; i < index_list.size() ; i++){
                title_list.add(time_list.get(index_list.get(i)));
            }
        }
    }

    /**
     * 集合截取
     * @param datalist
     * @param index_list
     * @return
     */
    private List<List<String>> getData(List<String> datalist,List<Integer> index_list){
        List<List<String>> list = new ArrayList<>();
        for (int  i = 0 ; i < index_list.size() ; i++){
            List<String> list_a = new ArrayList<>();
            if (i == 0 ){
                list_a.addAll(datalist.subList(0,index_list.get(i)+1));
                list.add(list_a);
            }else if (i > 0 ){
                list_a.addAll(datalist.subList(index_list.get(i-1)+1,index_list.get(i)+1));
                list.add(list_a);
            }
        }
        return list;
    }
    /**
     * 获取相同元素的下标
     * @param time_list
     * @return
     */
    private List<Integer> getIndex(List<String> time_list){
        int index = 0;
        int a = 0;
        List<Integer> list_index = new ArrayList<>();
        while (index <= time_list.size()-1){
            if (a < time_list.size()){
                a = time_list.lastIndexOf(time_list.get(index));
                index = a+1;
                list_index.add(a);
            }
        }
        return list_index;
    }
    /**
     * 获取file时间
     * @return
     */
    private List<String> getFileTime(List<String> datalist){
        List<String> list = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0 ; i < datalist.size() ; i++){
            long time = new File(datalist.get(i)).lastModified();
            String date = format.format(time);
            list.add(date);
        }
        return list;
    }
    private int getScreenProperty(){
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;  //屏幕宽度(像素)
        float density = dm.density;  //屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = dm.densityDpi;     // 屏幕密度dpi（120 / 160 / 240）
        // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
        int screenWidth = (int) (width / density);  // 屏幕宽度(dp)
        return width;
    }
    public static void startActivity(Context context, ArrayList<String> list, int position){
        Intent intent = new Intent(context, PictureActivity.class);
        intent.putExtra("position",position);
        intent.putStringArrayListExtra("list", (ArrayList<String>) list);
        context.startActivity(intent);
    }
    @Override
    protected void onResume() {
        super.onResume();
        initData();
        adapter.notifyDataSetChanged();
    }
}
