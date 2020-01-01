package com.videogo.warning;

import android.app.Activity;
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
import android.widget.LinearLayout;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.videogo.ToastNotRepeat;
import com.videogo.adapter.LeftAdapter;
import com.videogo.adapter.RightAdapter;
import com.videogo.adapter.RightlistAdapter;
import com.videogo.been.Temp;
import com.videogo.ui.util.MyListView;
import com.videogo.ui.util.SyncHorizontalScrollView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ezviz.ezopensdk.R;

public class DataQueryActivity2 extends Activity {
    private String TAG ="DataQueryActivity2";
    private LinearLayout right_title_container;
    private SyncHorizontalScrollView titleHorScv;
    private SyncHorizontalScrollView contentHorScv;
    private RecyclerView rightlistView;
    private RefreshLayout refreshLayout;
    private RightlistAdapter rightlistAdapter;
    private DonutProgress donutProgress;
    private List<Temp> totalList = new ArrayList<>();
    private List<Temp> tempList = new ArrayList<>();
    private Boolean refreshType = true;
    private int count = 0;
    private int page = 0;
    private int pagesize = 50;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 101:
                    Bundle bundle = msg.getData();
                    totalList=bundle.getParcelableArrayList("data");
                    Log.d(TAG,"templist.size="+totalList.size());
                    setView();
                    break;
                case 102:
                    Bundle bundle2 = msg.getData();
                    count=bundle2.getInt("count");
                    initData();
                    break;
                case 103:
                    Bundle bundle3 = msg.getData();
                    long progress = bundle3.getLong("progress");
                    donutProgress.setVisibility(View.VISIBLE);
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
        setContentView(R.layout.activity_query_data2);
        initView();
        getCount();
    }

    private void initView() {
        right_title_container = (LinearLayout) findViewById(R.id.right_title_container);
        getLayoutInflater().inflate(R.layout.table_right_title, right_title_container);
        rightlistView= (RecyclerView) findViewById(R.id.right_container_listview);
        titleHorScv = (SyncHorizontalScrollView) findViewById(R.id.title_horsv);
        contentHorScv = (SyncHorizontalScrollView) findViewById(R.id.content_horsv);
        donutProgress = findViewById(R.id.donut_progress);
        refreshLayout = findViewById(R.id.refresh_layout);
        // 设置两个水平控件的联动
        titleHorScv.setScrollView(contentHorScv);
        contentHorScv.setScrollView(titleHorScv);
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
        new Thread(runnable).start();
    }
    private void setView(){
        addListData(pagesize*page,pagesize*(page+1));
        LinearLayoutManager layoutManager =new LinearLayoutManager(getApplicationContext());
        rightlistView.setLayoutManager(layoutManager);
        //添加分割线
        rightlistView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),DividerItemDecoration.VERTICAL));
        rightlistAdapter = new RightlistAdapter(tempList,getApplicationContext());
        rightlistView.setAdapter(rightlistAdapter);
        refresh();
    }

    private void refresh() {
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                //下拉刷新
                refreshType = true;
                refreshLayout.finishRefresh(1000);
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                refreshType = false;
                if (false){
                    ToastNotRepeat.show(getApplicationContext(), "暂无更多的数据啦");
                    refreshLayout.finishLoadMoreWithNoMoreData();
                    return;
                }else{
                    refreshLayout.setEnableLoadMore(true);
                    refreshLayout.finishLoadMore(500);
                    page++;
                }
            }
        });
    }

    private void addListData(int a, int b) {
        for (int i = a ; i <= b ;i++){
            tempList.add(totalList.get(i));
        }
        page++;
    }
    private void initData() {
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
                    String sql = "select  * from WMS_60 where NODEID = '4028812268176ca801688930b0310004'";
                    Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);
                    List<Temp> list = new ArrayList<>();

                    long step = count / 100 ;
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
        new Thread(runnable).start();
    }
}
