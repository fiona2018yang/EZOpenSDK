package com.videogo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.scanpic.ScanPicActivity;
import com.videogo.scanvideo.ScanVideoActivity;
import com.videogo.util.ConnectionDetector;
import com.videogo.warning.WarningActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ezviz.ezopensdk.R;

import static com.videogo.EzvizApplication.getOpenSDK;

public class HomeActivity extends Activity {

    private GridView   HomeGView;
    private ConvenientBanner convenientBanner;
    private List<Map<String, Object>> data_list;
    private List<Integer> imgs=new ArrayList<>();
    public final static int REQUEST_CODE = 100;
    public final static int RESULT_CODE = 101;
    private final static int LOAD_MY_DEVICE = 0;
    private int mLoadType = LOAD_MY_DEVICE;
    private static String[] allpermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_SETTINGS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
    };
    private boolean isNeedCheck = true;
    // 图片封装为一个数组
    private int[] icon = { R.mipmap.home_icon_real_map,R.mipmap.home_icon_preview,R.mipmap.home_icon_baidu_map,
            R.mipmap.home_icon_alarm_information,R.mipmap.home_icon_show_video,R.mipmap.home_icon__show_picture };
    private String[] iconName = { "实景地图", "画面预览", "百度地图", "报警信息", "视频查看", "图片查看"};
    private List<EZDeviceInfo> list_ezdevices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        checkpermission();
        initGridView();
        setConvenientBanner();
    }

    /**
     * 初始化View
     */
    private void initGridView() {
        convenientBanner= (ConvenientBanner) findViewById(R.id.convenientBanner);
        imgs.add(R.mipmap.bg_home_1);
        imgs.add(R.mipmap.bg_home_2);
        imgs.add(R.mipmap.bg_home_3);

        MyTask myTask = new MyTask();
        myTask.execute();

        HomeGView = (GridView) findViewById(R.id.gv_home);
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
                //                img = (ImageView) view.findViewById(R.id.main_title_icon);
//                for (int i = 0; i < parent.getCount(); i++) {
//                    View v = parent.getChildAt(i);
//                    if (position == i) {//当前选中的Item改变背景颜色
//                        if (position != 3 ) {
//                            view.setBackgroundColor(getResources().getColor(R.color.Lightgray));
//                        } else {
//                            view.setBackgroundColor(Color.TRANSPARENT);
//                        }
//                    } else {
//                        v.setBackgroundColor(Color.TRANSPARENT);
//                    }
//                }
                // "实景地图", "画面预览", "百度地图", "报警信息", "视频查看", "图片查看"
                switch (position) {
                    case 0://实景地图
                        Intent iRealMap = new Intent(view.getContext(), MainActivity.class);
                        iRealMap.putParcelableArrayListExtra("devices_main", (ArrayList<? extends Parcelable>) list_ezdevices);
                        startActivity(iRealMap);
                        break;
                    case 1://画面预览
                        Intent iRreview = new Intent(view.getContext(), com.videogo.ui.cameralist.EZCameraListActivity  .class);
                        startActivity(iRreview);
                        break;
                    case 2://百度地图
                        Intent iBaiduMap = new Intent(view.getContext(), BaiduMapActivity.class);
                        iBaiduMap.putParcelableArrayListExtra("devices_baidu", (ArrayList<? extends Parcelable>) list_ezdevices);
                        startActivity(iBaiduMap);
                        break;
                    case 3: //报警信息
                        Intent iWarning = new Intent(view.getContext(), WarningActivity.class);
                        startActivity(iWarning);
                        break;
                    case 4://视频查看
                        Intent ivideoView = new Intent(view.getContext(), ScanVideoActivity.class);
                        ivideoView.putParcelableArrayListExtra("devices_video", (ArrayList<? extends Parcelable>) list_ezdevices);
                        startActivity(ivideoView);
                        break;
                    case 5://图片查看
                        Intent iPicView = new Intent(view.getContext(), ScanPicActivity.class);
                        iPicView.putParcelableArrayListExtra("devices_pic", (ArrayList<? extends Parcelable>) list_ezdevices);
                        startActivity(iPicView);
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
            }catch (BaseException e){
                ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                mErrorCode = errorInfo.errorCode;
                Log.i("TAG","eooro = "+errorInfo.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<EZDeviceInfo> ezDeviceInfos) {
            super.onPostExecute(ezDeviceInfos);
        }
    }
}
