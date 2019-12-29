package com.videogo.scanpic;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.videogo.EzvizApplication;
import com.videogo.MyDatabaseHelper;
import com.videogo.MyImageButton;
import com.videogo.ToastNotRepeat;
import com.videogo.adapter.ImageRecyclerAdapter;
import com.videogo.adapter.MyPaddingDecoration;
import com.videogo.adapter.TitleAdapter;
import com.videogo.remoteplayback.list.PlaybackActivity2;
import com.videogo.ui.util.DataUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import ezviz.ezopensdk.R;

public class CameraPicActivity extends Activity  {
    private RecyclerView rv;
    private TextView tv;
    private List<String> datalist;
    private List<String> datalist_db = new ArrayList<>();
    private List<List<String>> file_list ;
    private List<String> title_list;
    private List<String> path_checked_list;
    private HashMap<String,List<String>> data_map;
    private TitleAdapter adapter;
    private String camera_name;
    private Boolean show_flag = true;
    private int width;
    private SQLiteDatabase db;
    private MyReceiver myReceiver;
    private LinearLayout linearLayout;
    private LinearLayout linear_1;
    private LinearLayout linear_2;
    private MyImageButton myImageButton1 = null;
    private MyImageButton myImageButton2 = null;
    private Context context;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_pic);
        initView();
        getDataFromDB();
    }

    private void getDataFromDB() {
        Cursor cursor = db.query("picfilepath", null, null, null, null, null, null);
        if (cursor.moveToFirst()){
            do {
                String file_path = cursor.getString(cursor.getColumnIndex("path"));
                datalist_db.add(file_path);
            }while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void initView() {
        context=getApplicationContext();
        myReceiver = new MyReceiver();
        //注册广播接收
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.refresh.pic");
        registerReceiver(myReceiver,filter);
        db = ((EzvizApplication) getApplication()).getDatebase();
        rv = (RecyclerView) findViewById(R.id.recyclerView);
        tv = (TextView) findViewById(R.id.text);
        linearLayout = findViewById(R.id.linearLayout);
        linear_1 = findViewById(R.id.linear_1);
        linear_2 = findViewById(R.id.linear_2);
        camera_name = getIntent().getStringExtra("pic");
        width = getScreenProperty();
        title_list = new ArrayList<>();
        file_list = new ArrayList<>();
        datalist = new ArrayList<>();
        path_checked_list = new ArrayList<>();
        myImageButton1 = new MyImageButton(context,R.mipmap.send,"发送",60,60);
        myImageButton2 = new MyImageButton(context,R.mipmap.delate,"删除",60,60);
        linear_1.addView(myImageButton1);
        linear_2.addView(myImageButton2);
        initData();
        addClickListner();
        if(adapter == null){
            rv.setLayoutManager(new LinearLayoutManager(context));
            adapter = new TitleAdapter(context, title_list, file_list, width, show_flag,new TitleAdapter.Callback() {
                @Override
                public void callback(boolean flag) {
                    if (flag){
                        path_checked_list.clear();
                        adapter.notifyDataSetChanged();
                        linearLayout.setVisibility(View.GONE);
                    }else{
                        path_checked_list.clear();
                        adapter.notifyDataSetChanged();
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                }

                //选中Checkbox
                @Override
                public void addStringPath(int p1, int p2) {
                    path_checked_list.add(file_list.get(p1).get(p2));
                }
                //取消Checkbox
                @Override
                public void removeStringPath(int p1, int p2) {
                    path_checked_list.remove(file_list.get(p1).get(p2));
                }
            });
            rv.setAdapter(adapter);
        }
    }

    private void addClickListner() {
        //发送
        myImageButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList <Uri> files = new ArrayList<>();
                //发送
                for (int i = 0 ; i < path_checked_list.size() ; i++){
                    //Uri uri = FileProvider.getUriForFile(CameraPicActivity.this,"ezviz.ezopensdk.fileprovider",new File(path_checked_list.get(i)));
                    Uri uri = Uri.parse(path_checked_list.get(i));
                    files.add(uri);
                }
                senfiles("分享",files);
                adapter.setCheck(true);
                path_checked_list.clear();
                adapter.notifyDataSetChanged();
                linearLayout.setVisibility(View.GONE);
            }
        });
        //删除
        myImageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0 ; i < path_checked_list.size() ; i++){
                    File file = new File(path_checked_list.get(i));
                    if (file.exists()){
                        file.delete();
                    }
                    try {
                        if (datalist_db.contains(path_checked_list.get(i))){
                            db.delete("picfilepath","path=?",new String[]{path_checked_list.get(i)});
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                //更新数据
                Iterator ia = file_list.iterator();
                while (ia.hasNext()){
                    List<String> s = (List<String>) ia.next();
                    Iterator ib = s.iterator();
                    while (ib.hasNext()){
                        String str = (String) ib.next();
                        for (int m = 0 ; m < path_checked_list.size() ; m++){
                            if (str.equals(path_checked_list.get(m))){
                                ib.remove();
                            }
                        }
                    }
                }
                adapter.setCheck(true);
                path_checked_list.clear();
                adapter.notifyDataSetChanged();
                linearLayout.setVisibility(View.GONE);
                ToastNotRepeat.show(CameraPicActivity.this,"删除成功");
            }
        });
    }

    private void initData() {
        title_list.clear();
        file_list.clear();
        datalist.clear();
        List<String> time_list = new ArrayList<>();
        List<Integer> index_list = new ArrayList<>();

        if (!camera_name.equals("最近")){
            //获取所有文件的路径
            String path = Environment.getExternalStorageDirectory().toString()+"/EZOpenSDK/CapturePicture/"+camera_name;
            datalist.addAll(DataUtils.getImagePathFromSD(path));
            Log.i("TAG","datalist.size="+datalist.size());
            if (datalist.size() != 0){
                //获取所有文件最后修改时间
                time_list.addAll(getFileTime(datalist));
                //获取时间相同的文件的下标
                index_list.addAll(getIndex(time_list));
                //根据下标，截取出时间相同的文件集合
                file_list.addAll(getData(datalist,index_list));
                for (int i = 0 ; i < index_list.size() ; i++){
                    title_list.add(time_list.get(index_list.get(i)));
                }
                Log.i("TAG","size1="+file_list.get(0).size());
            }else{
                rv.setVisibility(View.GONE);
                tv.setVisibility(View.VISIBLE);
            }
        }else{
            Cursor cursor = db.query("picfilepath", null, null, null, null, null, null);
            if (cursor.moveToFirst()){
                do {
                    String file_path = cursor.getString(cursor.getColumnIndex("path"));
                    datalist.add(file_path);
                }while (cursor.moveToNext());
            }
            cursor.close();
            if (datalist.size() != 0){
                time_list = getFileTime(datalist);
                index_list = getIndex(time_list);
                file_list.addAll(getData(datalist,index_list));
                for (int i = 0 ; i < index_list.size() ; i++){
                    title_list.add(time_list.get(index_list.get(i)));
                }
            }else{
                rv.setVisibility(View.GONE);
                tv.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 集合截取
     * @param datalist
     * @param index_list
     * @return
     */
    private List<List<String>> getData( List<String> datalist,List<Integer> index_list){
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
        intent.putExtra("flag",false);
        intent.putStringArrayListExtra("list", (ArrayList<String>) list);
        context.startActivity(intent);
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }

    /**
     * 文件发送
     * @param dlgTitle
     * @param files
     */
    private void senfiles(String dlgTitle,ArrayList<Uri> files) {
        if (files.size() == 0) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        //Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        intent.setType("*/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        // 设置弹出框标题
        if (dlgTitle != null && !"".equals(dlgTitle)) { // 自定义标题
            startActivity(Intent.createChooser(intent, dlgTitle));
        } else { // 系统默认标题
            startActivity(intent);
        }
    }
    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String path = intent.getStringExtra("path");
            try {
                if (datalist_db.contains(path)){
                    db.delete("picfilepath","path=?",new String[]{path});
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            initData();
            adapter = new TitleAdapter(context, title_list, file_list, width, show_flag,new TitleAdapter.Callback() {
                @Override
                public void callback(boolean flag) {
                    if (flag){
                        path_checked_list.clear();
                        adapter.notifyDataSetChanged();
                        linearLayout.setVisibility(View.GONE);
                    }else{
                        path_checked_list.clear();
                        adapter.notifyDataSetChanged();
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                }
                //选中Checkbox
                @Override
                public void addStringPath(int p1, int p2) {
                    path_checked_list.add(file_list.get(p1).get(p2));
                }
                //取消Checkbox
                @Override
                public void removeStringPath(int p1, int p2) {
                    path_checked_list.remove(file_list.get(p1).get(p2));
                }
            });
            rv.setAdapter(adapter);
        }
    }
}
