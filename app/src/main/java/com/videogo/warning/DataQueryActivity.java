package com.videogo.warning;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.videogo.adapter.LeftAdapter;
import com.videogo.adapter.RightAdapter;
import com.videogo.been.Temp;
import com.videogo.ui.util.MyListView;
import com.videogo.ui.util.SyncHorizontalScrollView;

import org.MediaPlayer.PlayM4.Player;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ezviz.ezopensdk.R;

public class DataQueryActivity extends Activity {
    private String TAG ="DataQueryActivity";
    private TextView tv_table_title_left;
    private LinearLayout right_title_container;
    private List<Temp> tempList = new ArrayList<>();
    private List<Temp> totalList = new ArrayList<>();
    private List<Integer> integerList = new ArrayList<>();
    private MyListView leftlistView;
    private MyListView rightlistView;
    private LeftAdapter leftListAdapter;
    private RightAdapter rightlistAdapter;
    private SyncHorizontalScrollView titleHorScv;
    private SyncHorizontalScrollView contentHorScv;
    private DonutProgress donutProgress;
    private ExecutorService cachedThreadPool;
    private ExecutorService cachedThreadPool_1;
    private int count = 0;
    private int totlacount = 410 ;
    private int page = 0;
    private int pagesize = 400;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 101:
                    Bundle bundle = msg.getData();
                    totalList=bundle.getParcelableArrayList("data");
                    Log.d(TAG,"totallist.size="+totalList.size());
                    setView();
                    break;
                case 102:
                    Bundle bundle2 = msg.getData();
                    count=bundle2.getInt("count");
                    getInt(pagesize);
                    initData();
                    break;
                case 103:
                    Bundle bundle3 = msg.getData();
                    long progress = bundle3.getLong("progress");
                    donutProgress.setProgress(progress);
                    if (progress >= 100){
                        donutProgress.setVisibility(View.GONE);
                    }
                    Log.d(TAG,"progress="+progress);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_data);
        initView();
        getCount();
    }

    private void initView() {
        tv_table_title_left = (TextView) findViewById(R.id.tv_table_title_left);
        tv_table_title_left.setText("序号");
        right_title_container = (LinearLayout) findViewById(R.id.right_title_container);
        getLayoutInflater().inflate(R.layout.table_right_title, right_title_container);
        leftlistView= (MyListView) findViewById(R.id.left_container_listview);
        rightlistView= (MyListView) findViewById(R.id.right_container_listview);
        titleHorScv = (SyncHorizontalScrollView) findViewById(R.id.title_horsv);
        contentHorScv = (SyncHorizontalScrollView) findViewById(R.id.content_horsv);
        donutProgress = findViewById(R.id.donut_progress);
        cachedThreadPool = Executors.newFixedThreadPool(2);
        cachedThreadPool_1 = Executors.newFixedThreadPool(2);
        // 设置两个水平控件的联动
        titleHorScv.setScrollView(contentHorScv);
        contentHorScv.setScrollView(titleHorScv);
    }

    private void setView(){
        addListData(pagesize*page+1,pagesize*(page+1));
        leftListAdapter = new LeftAdapter(integerList,getApplicationContext());
        rightlistAdapter = new RightAdapter(tempList,getApplicationContext());
        leftlistView.setAdapter(leftListAdapter);
        rightlistView.setAdapter(rightlistAdapter);
        leftlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                setSelected(i);
            }
        });
        rightlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                setSelected(i);
            }
        });
    }

    private void setSelected(int i){
        rightlistAdapter.update(i,rightlistView);
        leftListAdapter.update(i,leftlistView);
    }

    private void addListData(int a, int b) {
        for (int i = a ; i <= b ;i++){
            tempList.add(totalList.get(i));
        }
    }

    private void getCount() {
        Runnable runnable = new Runnable() {
            private Connection connection = null;
            private int count = 0 ;
            @Override
            public void run() {
                try {
                    /** 创建数据库对象 */
                    Class.forName("net.sourceforge.jtds.jdbc.Driver");
                    connection = DriverManager.getConnection("jdbc:jtds:sqlserver://183.208.120.208:14333/prd_env_dts;charset=utf8","sa","sa123123");
                    Log.d(TAG,"连接成功");
                    String sql = "select count(*) from WMS_60 where NODEID = '4028812268176ca801688930b0310004'";
                    Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);
                    while (rs.next()){
                        count = rs.getInt(1);
                    }
                    Message message = Message.obtain();
                    message.what = 102;
                    Bundle bundle = new Bundle();
                    bundle.putInt("count",count);
                    Log.d(TAG,"count="+count);
                    message.setData(bundle);
                    handler.sendMessage(message);
                    rs.close();
                    stmt.close();
                    connection.close();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        cachedThreadPool.execute(runnable);
    }

    private void getInt(int count){
        for (int i = 1 ; i <= count ; i ++){
            integerList.add(i);
        }
    }

    private void initData() {
        donutProgress.setVisibility(View.VISIBLE);
        Runnable runnable = new Runnable() {
            private Connection connection = null;
            @Override
            public void run() {
                try {
                    /** 创建数据库对象 */
                    Class.forName("net.sourceforge.jtds.jdbc.Driver");
                    connection = DriverManager.getConnection("jdbc:jtds:sqlserver://183.208.120.208:14333/prd_env_dts;charset=utf8","sa","sa123123");
                    Log.d(TAG,"连接成功");
                    //String sql = "select top 20 * from WMS_60 where NODEID = '4028812268176ca801688930b0310004'";
                    String sql = "select top 410 f_301,f_302,f_315,f_311,f_314,f_313,f_1005,datetime from WMS_60 where NODEID = '4028812268176ca801688930b0310004'order by id desc";
                    Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);
                    List<Temp> list = new ArrayList<>();

                    long step = totlacount / 100 ;
                    long progress = 0 ;
                    while (rs.next()) {
                        Temp temp = new Temp();
                        temp.setTemp(rs.getString("f_301"));
                        temp.setPh(rs.getString("f_302"));
                        temp.setOxygen(rs.getString("f_315"));
                        temp.setNitrogen(rs.getString("f_311"));
                        temp.setPermanganate(rs.getString("f_314"));
                        temp.setPhosphorus(rs.getString("f_313"));
                        temp.setPotential(rs.getString("f_1005"));
                        temp.setTime(rs.getString("datetime"));
                        list.add(temp);
                        long currentsize = list.size();
                        if (currentsize / step != progress){
                            progress = currentsize / step;
                            if (progress % 1 == 0){
                                Message message = Message.obtain();
                                message.what = 103;
                                Bundle bundle = new Bundle();
                                bundle.putLong("progress",progress);
                                message.setData(bundle);
                                handler.sendMessage(message);
                            }
                        }
                    }
                    Message message = Message.obtain();
                    message.what = 101;
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("data", (ArrayList<? extends Parcelable>) list);
                    message.setData(bundle);
                    handler.sendMessage(message);
                    rs.close();
                    stmt.close();
                    connection.close();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        cachedThreadPool_1.execute(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cachedThreadPool.shutdown();
        cachedThreadPool_1.shutdownNow();
        handler.removeCallbacksAndMessages(null);
    }
}
