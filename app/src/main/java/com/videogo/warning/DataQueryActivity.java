package com.videogo.warning;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.videogo.ToastNotRepeat;
import com.videogo.adapter.LeftAdapter;
import com.videogo.adapter.RightAdapter;
import com.videogo.been.Temp;
import com.videogo.ui.util.MyListView;
import com.videogo.ui.util.MySpinner;
import com.videogo.ui.util.SyncHorizontalScrollView;
import com.videogo.ui.util.ToChineseNumUtill;
import com.videogo.util.LogUtil;
import com.videogo.widget.WaitDialog;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private Date queryDate = null;
    private WaitDialog mWaitDlg = null;
    private MySpinner spinner;
    private TextView date;
    private ImageButton back;
    private ImageButton query;
    private String[] mItems;
    private int count = 0;
    private int pagesize = 200;
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
                case 104:
                    if (mWaitDlg != null && mWaitDlg.isShowing()) {
                        mWaitDlg.dismiss();
                    }
                    Bundle bundle4 = msg.getData();
                    tempList.clear();
                    tempList.addAll(bundle4.getParcelableArrayList("data"));
                    getInt(tempList.size());
                    leftListAdapter.notifyDataSetChanged();
                    rightlistAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_data);
        try {
            initView();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        getCount();
    }

    private void initView() throws ParseException {
        tv_table_title_left = (TextView) findViewById(R.id.tv_table_title_left);
        tv_table_title_left.setText("序号");
        right_title_container = (LinearLayout) findViewById(R.id.right_title_container);
        getLayoutInflater().inflate(R.layout.table_right_title, right_title_container);
        leftlistView= (MyListView) findViewById(R.id.left_container_listview);
        rightlistView= (MyListView) findViewById(R.id.right_container_listview);
        titleHorScv = (SyncHorizontalScrollView) findViewById(R.id.title_horsv);
        contentHorScv = (SyncHorizontalScrollView) findViewById(R.id.content_horsv);
        spinner = findViewById(R.id.spinner);
        date = findViewById(R.id.date);
        donutProgress = findViewById(R.id.donut_progress);
        query = findViewById(R.id.query);
        back = findViewById(R.id.back);
        cachedThreadPool = Executors.newFixedThreadPool(5);
        // 设置两个水平控件的联动
        titleHorScv.setScrollView(contentHorScv);
        contentHorScv.setScrollView(titleHorScv);
        long systime = System.currentTimeMillis();
        queryDate = longToDate(systime,"yyyy-MM-dd");
        date.setText(dateToString(queryDate,"yyyy-MM-dd"));
        mWaitDlg = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        mWaitDlg.setCancelable(false);
    }

    private void setView(){
//        String[] mItems = new String[]{"第一页", "第二页", "第三页", "第四页",
//                "第五页", "第六页", "第七页", "第八页", "第九页", "第十页"};
        List<String> arrayList = new ArrayList<>();
        arrayList = getSpinerData();
        mItems = arrayList.toArray(new String[arrayList.size()]);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,R.layout.spinner_item, mItems);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        //绑定 Adapter到控件
        spinner .setAdapter(adapter);

        tempList.addAll(totalList.subList(0,pagesize));
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
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tempList.clear();
                tempList.addAll(totalList.subList(position*pagesize,(position+1)*pagesize));
                getInt(pagesize);
                rightlistAdapter.notifyDataSetChanged();
                leftListAdapter.notifyDataSetChanged();

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG,"noSelected");
            }
        });
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCalendar();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getQueryData(date.getText().toString());
            }
        });
    }
    private List<String> getSpinerData(){
        int a = count/pagesize;
        List<String> list = new ArrayList<>();
        for (int i = 1 ;i <= a ; i++){
            list.add("第"+ ToChineseNumUtill.numberToChinese(i) +"页");
        }
        return list;
    }

    private void setSelected(int i){
        rightlistAdapter.update(i,rightlistView);
        leftListAdapter.update(i,leftlistView);
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
        integerList.clear();
        for (int i = 1 ; i <= count ; i ++){
            integerList.add(i);
        }
    }

    private void getQueryData(String date){
        mWaitDlg.show();
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
                    String sql = "select  f_301,f_302,f_315,f_311,f_314,f_313,f_1005,datetime from WMS_60 where NODEID = '4028812268176ca801688930b0310004'and datetime like '%"+date+"%'order by id desc";
                    Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);
                    List<Temp> list = new ArrayList<>();
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
                    }
                    Message message = Message.obtain();
                    message.what = 104;
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("data", (ArrayList<? extends Parcelable>) list);
                    message.setData(bundle);
                    handler.sendMessage(message);
                    rs.close();
                    stmt.close();
                    connection.close();
                }catch (Exception e){
                    if (mWaitDlg != null && mWaitDlg.isShowing()) {
                        mWaitDlg.dismiss();
                    }
                    e.printStackTrace();
                }
            }
        };
        cachedThreadPool.execute(runnable);
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
                    String sql = "select  f_301,f_302,f_315,f_311,f_314,f_313,f_1005,datetime from WMS_60 where NODEID = '4028812268176ca801688930b0310004'order by id desc";
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
        cachedThreadPool.execute(runnable);
    }
    // 切换到日历界面
    private void goToCalendar() {
        if (getMinDate() != null && new Date().before(getMinDate())) {
            ToastNotRepeat.show(getApplicationContext(),"请先将日期设置到2012/01/01之后");
            return;
        }
        showDatePicker();
    }
    private Date getMinDate() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse("2012-01-01");
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showDatePicker() {
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.setTime(queryDate);
        DatePickerDialog dpd = new DatePickerDialog(this, null, nowCalendar.get(Calendar.YEAR),
                nowCalendar.get(Calendar.MONTH), nowCalendar.get(Calendar.DAY_OF_MONTH));

        dpd.setCancelable(true);
        dpd.setTitle(R.string.select_date);
        dpd.setCanceledOnTouchOutside(true);
        dpd.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.certain),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dg, int which) {
                        DatePicker dp = null;
                        Field[] fields = dg.getClass().getDeclaredFields();
                        for (Field field : fields) {
                            field.setAccessible(true);
                            if (field.getName().equals("mDatePicker")) {
                                try {
                                    dp = (DatePicker) field.get(dg);
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        dp.clearFocus();
                        Calendar selectCalendar = Calendar.getInstance();
                        selectCalendar.set(Calendar.YEAR, dp.getYear());
                        selectCalendar.set(Calendar.MONTH, dp.getMonth());
                        selectCalendar.set(Calendar.DAY_OF_MONTH, dp.getDayOfMonth());
                        queryDate = (Date) selectCalendar.getTime();
                        date.setText(dateToString(queryDate,"yyyy-MM-dd"));
                    }
                });
        dpd.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LogUtil.debugLog("Picker", "Cancel!");
                        if (!isFinishing()) {
                            dialog.dismiss();
                        }

                    }
                });

        dpd.show();
    }
    // currentTime要转换的long类型的时间
    // formatType要转换的时间格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    public static Date longToDate(long currentTime, String formatType)
            throws ParseException {
        Date dateOld = new Date(currentTime); // 根据long类型的毫秒数生命一个date类型的时间
        String sDateTime = dateToString(dateOld, formatType); // 把date类型的时间转换为string
        Date date = stringToDate(sDateTime, formatType); // 把String类型转换为Date类型
        return date;
    }
    // formatType格式为yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    // data Date类型的时间
    public static String dateToString(Date data, String formatType) {
        return new SimpleDateFormat(formatType).format(data);
    }
    // strTime要转换的string类型的时间，formatType要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日
    // HH时mm分ss秒，
    // strTime的时间格式必须要与formatType的时间格式相同
    public static Date stringToDate(String strTime, String formatType)
            throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;
        date = formatter.parse(strTime);
        return date;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cachedThreadPool.shutdown();
        handler.removeCallbacksAndMessages(null);
    }
}
