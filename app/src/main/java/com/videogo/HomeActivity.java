package com.videogo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.videogo.been.AlarmContant;
import com.videogo.been.AlarmMessage;
import com.videogo.been.AlarmRead;
import com.videogo.been.Constant;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.jpush.TagAliasOperatorHelper;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.remoteplayback.list.PlaybackActivity2;
import com.videogo.scanpic.ScanPicActivity;
import com.videogo.scanvideo.ScanVideoActivity;
import com.videogo.ui.util.FTPutils;
import com.videogo.ui.util.PackageUtils;
import com.videogo.util.ConnectionDetector;
import com.videogo.warning.OkHttpUtil;
import com.videogo.warning.WarningActivity;
import com.videogo.widget.WaitDialog;

import org.apache.commons.net.ftp.FTPFile;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.jpush.android.api.JPushInterface;
import ezviz.ezopensdk.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.videogo.EzvizApplication.getOpenSDK;
import static com.videogo.jpush.TagAliasOperatorHelper.sequence;

public class HomeActivity extends Activity {
    private SQLiteDatabase db;
    private GridView   HomeGView;
    private String TAG = "HomeActivity";
    private ConvenientBanner convenientBanner;
    private List<Map<String, Object>> data_list;
    private List<Integer> imgs=new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private SharedPreferences sharedPreferences_1;
    private ExecutorService executors_1;
    private final static int LOAD_MY_DEVICE = 0;
    private int mLoadType = LOAD_MY_DEVICE;
    private String userid;
    private String table_name;
    private Cursor cursor;
    private TextView versionName;
    private TextView versionUpdate;
    private int mCurrentVersionCode;
    private ExecutorService cachedThreadPool;
    private WaitDialog mWaitDlg = null;
    private static String[] allpermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.VIBRATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_SETTINGS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.REQUEST_INSTALL_PACKAGES
    };
    private boolean isNeedCheck = true;
    // 图片封装为一个数组
    private int[] icon = { R.mipmap.home_icon_real_map,R.mipmap.home_icon_preview,R.mipmap.home_icon_baidu_map,
            R.mipmap.home_icon_alarm_information,R.mipmap.home_icon_show_video,R.mipmap.home_icon__show_picture };
    private String[] iconName = { "实景地图", "画面预览", "百度地图", "报警信息", "视频查看", "图片查看"};
    private List<EZDeviceInfo> list_ezdevices = new ArrayList<>();
    private List<EZCameraInfo> list_ezCamera = new ArrayList<>();
    private String updateFileName = "";
    private String newVersionName = "";
    private ProgressDialog progressDialog;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 102:
                    if (mWaitDlg != null && mWaitDlg.isShowing()) {
                        mWaitDlg.dismiss();
                    }
                    Bundle bundle = msg.getData();
                    newVersionName = bundle.getString("versionName");
                    int MaxCode = bundle.getInt("versionCode");
                    updateFileName = bundle.getString("fileName");
                    mCurrentVersionCode = PackageUtils.getVersionCode(HomeActivity.this);
                    Log.d(TAG,"mCurrentVersionCode="+mCurrentVersionCode);
                    Log.d(TAG,"MacCode="+MaxCode);
                    if (mCurrentVersionCode>=MaxCode){
                        ToastNotRepeat.show(HomeActivity.this,"当前已经是最新版本");
                    }else{
                        //更新apk
                        showUpdateDialog();
                    }
                    break;
                case 103:
                    if (mWaitDlg != null && mWaitDlg.isShowing()) {
                        mWaitDlg.dismiss();
                    }
                    ToastNotRepeat.show(HomeActivity.this,"网络异常，请稍后重试...");
                    break;
                case 0 :
                    Bundle bundle2 = msg.getData();
                    String progress = bundle2.getString("progress");
                    progressDialog.setProgress(Integer.parseInt(progress));
                    Log.d(TAG,"progress="+progress);
                    if (Float.parseFloat(progress)==100){
                        progressDialog.dismiss();
                        ToastNotRepeat.show(HomeActivity.this,"下载完成");
                        installing();
                    }
                    break;
                case 1 :
                    //下载失败
                    progressDialog.dismiss();
                    ToastNotRepeat.show(HomeActivity.this,"下载失败,请稍后重试...");
                    break;
                case 2 :
                    //下载完成
                    progressDialog.dismiss();
                    ToastNotRepeat.show(HomeActivity.this,"下载完成");
                    //安装apk
                    installing();
                    break;
            }
        }
    };

    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("版本更新");
        builder.setMessage("最新版本："+newVersionName);
        builder.setPositiveButton("现在更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //点击确定的时候进行下载
                progressDialog = new ProgressDialog(HomeActivity.this);
                progressDialog.setTitle("APK文件下载中，请稍候...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(false);// 设置允许取消
                progressDialog.show();
                //开始下载任务
                asyDownLoadFile();
            }
        });
        builder.setNegativeButton("暂不更新",null);
        builder.setCancelable(false);//设置为false时候点击返回键获取屏幕边缘，对话框也无法取消
        builder.show();
    }

    private void asyDownLoadFile() {
        String file_path = Environment.getExternalStorageDirectory().toString()+"/EZOpenSDK/version/"+updateFileName;
        File imgFile = new File(file_path);
        if (imgFile.exists()){
            imgFile.delete();
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                FTPutils ftPutils = new FTPutils();
                String localpath = Environment.getExternalStorageDirectory().toString()+"/EZOpenSDK/version";
                Boolean flag = ftPutils.connect(AlarmContant.ftp_ip,Integer.parseInt(AlarmContant.ftp_port),AlarmContant.name,AlarmContant.password);
                if (flag){
                    try {
                        ftPutils.downloadSingleFile(AlarmContant.apk_path + "/" + updateFileName, localpath, updateFileName, new FTPutils.FtpProgressListener() {
                            @Override
                            public void onFtpProgress(int currentStatus, long process, File targetFile) {
                                if (currentStatus == Constant.FTP_FILE_NOTEXISTS){
                                    Message message = Message.obtain();
                                    message.what = 1;
                                    handler.sendMessage(message);
                                }else if(currentStatus ==Constant.LOCAL_FILE_AIREADY_COMPLETE){
                                    Message message = Message.obtain();
                                    message.what = 2;
                                    handler.sendMessage(message);
                                }else{
                                    Message message = Message.obtain();
                                    message.what = 0;
                                    Bundle bundle = new Bundle();
                                    bundle.putString("progress", String.valueOf(process));
                                    message.setData(bundle);
                                    handler.sendMessage(message);
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    Message message = Message.obtain();
                    message.what = 1;
                    handler.sendMessage(message);
                }
            }
        };
        cachedThreadPool.execute(runnable);
    }

    private void installing() {
        String file_path = Environment.getExternalStorageDirectory().toString()+"/EZOpenSDK/version/"+updateFileName;
        Log.d(TAG,"filepath="+file_path);
        File imgFile = new File(file_path);
        Uri uri = FileProvider.getUriForFile(this, "ezviz.ezopensdk.fileprovider", imgFile);
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
        startActivity(installIntent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        checkpermission();
        initGridView();
        setConvenientBanner();
        //initData();
        setAlias();
        initSql();
        updateApk();
    }

    private void updateApk() {
        mCurrentVersionCode = PackageUtils.getVersionCode(HomeActivity.this);
        mWaitDlg = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        versionName.setText("版本号 "+PackageUtils.getVersionName(this));
        versionUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWaitDlg.show();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        FTPutils ftPutils = new FTPutils();
                        List<Integer> integerList = new ArrayList<>();
                        Boolean flag = ftPutils.connect(AlarmContant.ftp_ip,Integer.parseInt(AlarmContant.ftp_port),AlarmContant.name,AlarmContant.password);
                        Log.d(TAG,"flag = "+flag);
                        if (flag){
                            try {
                                FTPFile[] files = ftPutils.listName(AlarmContant.apk_path);
                                for (int i = 0 ; i < files.length ; i ++){
                                    String[] strings = files[i].getName().split("_");
                                    String versionCode = strings[1].substring(1);
                                    Log.d(TAG,"versionCode = "+versionCode);
                                    integerList.add(Integer.parseInt(versionCode));
                                }
                                int versionCode = Collections.max(integerList);
                                String versionName = files[integerList.indexOf(versionCode)].getName().split("_")[2];
                                String fileName = files[integerList.indexOf(versionCode)].getName();
//                        Log.d(TAG,"files.length = "+files.length);
//                        Log.d(TAG,"file0 = "+files[0].getName());
//                        Log.d(TAG,"file1 = "+files[1].getName());
//                        Log.d(TAG,"versionName = "+versionName);

                                if (versionName!=null){
                                    Message msg = Message.obtain();
                                    msg.what = 102 ;
                                    Bundle bundle = new Bundle();
                                    bundle.putString("versionName",versionName);
                                    bundle.putInt("versionCode",versionCode);
                                    bundle.putString("fileName",fileName);
                                    msg.setData(bundle);
                                    handler.sendMessage(msg);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }else {
                            Message msg = Message.obtain();
                            msg.what = 103 ;
                            handler.sendMessage(msg);
                        }
                    }
                };
                cachedThreadPool.execute(runnable);
            }
        });
    }

    //设置别名
    private void setAlias() {
        String userid = sharedPreferences_1.getString("id","1");
        Boolean isSuccess = sharedPreferences.getBoolean(userid,false);
        if (!isSuccess){
            TagAliasOperatorHelper.getInstance().handleAction(getApplicationContext(),sequence,userid);
        }
    }
    private void initSql() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int number=0;
                Cursor c = db.rawQuery("select * from alarmSize",null);
                number = c.getCount();
                if (number == 0 ){
                    //插入数据
                    ContentValues values = new ContentValues();
                    values.put("size0", "0");
                    values.put("size1", "0");
                    values.put("size2", "0");
                    values.put("size3", "0");
                    values.put("size4", "0");
                    values.put("size5", "0");
                    db.insert("alarmSize", null, values);
                }
            }
        };
        executors_1.execute(runnable);
    }
    private void initData() {
        int number=0;
        Cursor c = db.rawQuery("select * from "+table_name,null);
        number = c.getCount();
        if (number == 0 ){
            //查询数据
            startquerydata();
        }
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 101:
                        Bundle bundle = msg.getData();
                        List<AlarmMessage> list = bundle.getParcelableArrayList("datalist");
                        String sql = "insert into "+table_name+"(message,type,latitude,longitude,altitude,address,imgPath,videoPath,createTime,startTime,endTime,channelNumber) " +
                                "values(?,?,?,?,?,?,?,?,?,?,?,?)";
                        SQLiteStatement stat = db.compileStatement(sql);
                        db.beginTransaction();
                        for (AlarmMessage alarmMessage : list){
                            stat.bindString(1,alarmMessage.getMessage());
                            stat.bindString(2,alarmMessage.getType());
                            stat.bindString(3,alarmMessage.getLatitude());
                            stat.bindString(4,alarmMessage.getLongitude());
                            if (alarmMessage.getAltitude()!=null){
                                stat.bindString(5,alarmMessage.getAltitude());
                            }
                            if (alarmMessage.getAddress()!=null){
                                stat.bindString(6,alarmMessage.getAddress());
                            }
                            if (alarmMessage.getImgPath()!=null){
                                stat.bindString(7,alarmMessage.getImgPath());
                            }
                            if (alarmMessage.getVideoPath()!=null){
                                stat.bindString(8,alarmMessage.getVideoPath());
                            }
                            if (alarmMessage.getCreateTime()!=null){
                                stat.bindString(9,alarmMessage.getCreateTime());
                            }
                            if (alarmMessage.getStartTime()!=null){
                                stat.bindString(10,alarmMessage.getStartTime());
                            }
                            if (alarmMessage.getEndTime()!=null){
                                stat.bindString(11,alarmMessage.getEndTime());
                            }
                            if (alarmMessage.getChannelNumber()!=null){
                                stat.bindString(12,alarmMessage.getChannelNumber());
                            }
                            stat.executeInsert();
                        }
                        db.setTransactionSuccessful();
                        db.endTransaction();
                        Log.d(TAG,"insert success");
                        break;
                }
            }
        };
    }
    private void startquerydata() {
        String url = AlarmContant.service_url+"api/getEarlyWarning";
        Map<String,String> map = new HashMap<>();
        map.put("userId",userid);
        map.put("type","");
        map.put("limit","2000");
        map.put("page",String.valueOf(0));
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
                    if (result.equals("true")){
                        String data = object.get("data").toString();
                        JSONObject objectdata = new JSONObject(data);
                        Gson gson = new Gson();
                        List<JsonObject> list_objects = gson.fromJson(objectdata.get("data").toString(),new TypeToken<List<JsonObject>>(){}.getType());
                        for (JsonObject object1 : list_objects){
                            AlarmMessage alarmMessage = gson.fromJson(object1,AlarmMessage.class);
                            alarmMessageList.add(0,alarmMessage);
                        }
                        Message message = new Message();
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
    /**
     * 初始化View
     */
    private void initGridView() {
        table_name = EzvizApplication.table_name;
        cachedThreadPool = Executors.newFixedThreadPool(3);
        db = ((EzvizApplication)getApplication()).getDatebase();
        sharedPreferences = getSharedPreferences("alias",MODE_PRIVATE);
        sharedPreferences_1 = getSharedPreferences("userid",MODE_PRIVATE);
        executors_1 = Executors.newFixedThreadPool(5);
        userid = sharedPreferences_1.getString("id","1");
        Log.d("TAG","userid="+userid);
        convenientBanner= (ConvenientBanner) findViewById(R.id.convenientBanner);
        imgs.add(R.mipmap.bg_home_1);
        imgs.add(R.mipmap.bg_home_2);
        imgs.add(R.mipmap.bg_home_3);

        MyTask myTask = new MyTask(HomeActivity.this);
        myTask.execute();

        HomeGView = (GridView) findViewById(R.id.gv_home);
        versionName = findViewById(R.id.version_name);
        versionUpdate = findViewById(R.id.update);
        //新建List
        data_list = new ArrayList<>();
        //获取数据
        data_list = getData();
        //新建适配器11
        String[] from = {"icon", "iconName"};
        int[] to = {R.id.grid_icon, R.id.grid_iconName};
        SimpleAdapter sim_adapter = new SimpleAdapter(this, data_list, R.layout.home_grid_item, from, to);
        //配置适配器
        HomeGView.setAdapter(sim_adapter);
        HomeGView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImageView  img = (ImageView) view.findViewById(R.id.grid_icon);
                Animation animation = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.item_img);
                img.startAnimation(animation);
                // "实景地图", "画面预览", "百度地图", "报警信息", "视频查看", "图片查看"
                switch (position) {
                    case 0://实景地图
                        if (list_ezdevices.size()!=0){
                            Intent iRealMap = new Intent(view.getContext(), MainActivity.class);
                            iRealMap.putParcelableArrayListExtra("devices_main", (ArrayList<? extends Parcelable>) list_ezdevices);
                            startActivity(iRealMap);
                        }else {
                            ToastNotRepeat.show(HomeActivity.this,"服务器异常！");
                        }
                        break;
                    case 1://画面预览
                        Intent iRreview = new Intent(view.getContext(), com.videogo.ui.cameralist.EZCameraListActivity  .class);
                        startActivity(iRreview);
                        break;
                    case 2://百度地图
                        if (list_ezdevices.size()!=0){
                            Intent iBaiduMap = new Intent(view.getContext(), BaiduMapActivity.class);
                            iBaiduMap.putParcelableArrayListExtra("devices_baidu", (ArrayList<? extends Parcelable>) list_ezdevices);
                            startActivity(iBaiduMap);
                        }else {
                            ToastNotRepeat.show(HomeActivity.this,"服务器异常！");
                        }
                        break;
                    case 3: //报警信息
                        if (list_ezCamera.size()!=0){
                            Intent iWarning = new Intent(view.getContext(), WarningActivity.class);
                            iWarning.putParcelableArrayListExtra("cameras_pic", (ArrayList<? extends Parcelable>) list_ezCamera);
                            startActivity(iWarning);
                        }else {
                            ToastNotRepeat.show(HomeActivity.this,"服务器异常！");
                        }
                        break;
                    case 4://视频查看
                        if (list_ezCamera.size()!=0){
                            Intent ivideoView = new Intent(view.getContext(), ScanVideoActivity.class);
                            ivideoView.putParcelableArrayListExtra("cameras_pic", (ArrayList<? extends Parcelable>) list_ezCamera);
                            startActivity(ivideoView);
                        }else {
                            ToastNotRepeat.show(HomeActivity.this,"服务器异常！");
                        }
                        break;
                    case 5://图片查看
                        if (list_ezCamera.size()!=0){
                            Intent iPicView = new Intent(view.getContext(), ScanPicActivity.class);
                            iPicView.putParcelableArrayListExtra("cameras_pic", (ArrayList<? extends Parcelable>) list_ezCamera);
                            startActivity(iPicView);
                        }else {
                            ToastNotRepeat.show(HomeActivity.this,"服务器异常！");
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public List<Map<String, Object>> getData(){
        //cion和iconName的长度是相同的，这里任选其一都可以
        for(int i=0;i<icon.length;i++){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("icon", icon[i]);
            map.put("iconName", iconName[i]);
            data_list.add(map);
        }

        return data_list;
    }
//    public List<Map<String, Object>> getData() {
//        //icon和iconName的长度是相同的，这里任选其一都可以
//        Resources res = getResources();
//        String[] TitleIconName = res.getStringArray(R.array.home_grid_text);
//        TypedArray ta = res.obtainTypedArray(R.array.home_grid_icons);
//        for (int i = 0; i < TitleIconName.length; i++) {
//            Map<String, Object> map = new HashMap<>();
//            map.put("icon", ta.getResourceId(i, 0));
//            map.put("iconName", TitleIconName[i]);
//            data_list.add(map);
//        }
//        return data_list;
//    }
    /**
     * 设置广告栏
     */
    private void setConvenientBanner() {
        convenientBanner.setPages(new CBViewHolderCreator() {
            @Override
            public Object createHolder() {
                return new LocalImageHolderView();
            }
        },imgs).setPointViewVisible(true)//设置指示器是否可见
                .setPageIndicator(new int[]{R.mipmap.yuandianbantou,R.mipmap.yuandian});//设置两个点图片作为翻页指示器，不设置则没有指示器，可以根据自己需求自行配合自己的指示器,不需要圆点指示器可用不设
        convenientBanner.setManualPageable(true);//设置手动影响（设置了该项无法手动切换）
        convenientBanner.startTurning(2000);     //设置自动切换（同时设置了切换时间间隔）
        convenientBanner.setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.ALIGN_PARENT_RIGHT);//设置指示器位置（左、中、右）
    }
    public class LocalImageHolderView implements Holder<Integer> {
        private ImageView imageView;
        @Override
        public View createView(Context context) {
            imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            return imageView;
        }
        @Override
        public void UpdateUI(Context context, int position, Integer data) {
            imageView.setImageResource(data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isNeedCheck){
            checkpermission();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executors_1.shutdown();
        cursor.close();
    }

    /**
     * 权限管理
     */
    private void checkpermission() {
        if (Build.VERSION.SDK_INT>=23){
            boolean needapply = false;
            for(int i = 0;i <allpermissions.length;i++ ){
                int checkpermission = ContextCompat.checkSelfPermission(getApplicationContext(),allpermissions[i]);
                if (checkpermission!= PackageManager.PERMISSION_GRANTED){
                    needapply = true;
                }
            }
            if(needapply){
                ActivityCompat.requestPermissions(HomeActivity.this,allpermissions,1);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int  i = 0 ;i<grantResults.length;i++){
            if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                isNeedCheck = false;
            }else{
            }
        }
    }
    /**
     * 获取事件消息任务
     */
    private class MyTask extends AsyncTask<Void, Void, List<EZDeviceInfo>> {
        private int mErrorCode = 0;
        private WeakReference<HomeActivity> activityReference;
        MyTask(HomeActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected List<EZDeviceInfo> doInBackground(Void... voids) {
            if (HomeActivity.this.isFinishing()){
                return null;
            }
            if (!ConnectionDetector.isNetworkAvailable(HomeActivity.this)){
                mErrorCode = ErrorCode.ERROR_WEB_NET_EXCEPTION;
                return null;
            }
            try {
                List<EZDeviceInfo> result = null;
                if (mLoadType == LOAD_MY_DEVICE) {
                    result = getOpenSDK().getDeviceList(0, 30);
                    list_ezdevices.addAll(result);
                }
                return result;
            }catch (BaseException e){
                ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                mErrorCode = errorInfo.errorCode;
                Log.i("TAG","eooro = "+errorInfo.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<EZDeviceInfo> result) {
            HomeActivity activity2 = activityReference.get();
            if (activity2 == null || activity2.isFinishing() || activity2.isDestroyed()){
                return;
            }
            if (result!=null){
                Log.d(TAG,"result.size="+result.size());
                for (EZDeviceInfo ezDeviceInfo : result){
                    for (EZCameraInfo cameraInfo : ezDeviceInfo.getCameraInfoList()){
                        list_ezCamera.add(cameraInfo);
                    }
                }
            }
        }
    }
    /**
     * 菜单、返回键响应
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK){
            exitByTwoClick(); //调用双击退出函数
        }
        return false;
    }
    /**
     * 双击退出函数
     */
    private static Boolean isExit=false;
    private void exitByTwoClick() {
        Timer tExit=null;
        if(isExit==false){
            isExit=true;//准备退出
            ToastNotRepeat.show(this,"再按一次退出程序");
            tExit=new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit=false;//取消退出
                }
            },2000);//// 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
        }else {
            finish();
            System.exit(0);
        }
    }
}
