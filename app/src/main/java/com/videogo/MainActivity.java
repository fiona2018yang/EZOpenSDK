package com.videogo;

import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.esri.android.map.FeatureLayer;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.RasterLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnZoomListener;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.core.geodatabase.ShapefileFeatureTable;
import com.esri.core.geometry.AreaUnit;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.raster.FileRasterSource;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.videogo.been.AlarmContant;
import com.videogo.been.Constant;
import com.videogo.been.StyleId;
import com.videogo.been.StyleMap;
import com.videogo.constant.IntentConsts;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.ui.realplay.EZRealPlayActivity;
import com.videogo.ui.util.CopyFontFile;
import com.videogo.ui.util.FTPutils;
import com.videogo.ui.util.ReadUtils;
import com.videogo.widget.WaitDialog;
import org.apache.commons.net.ftp.FTPFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ezviz.ezopensdk.R;

import static com.videogo.been.AlarmContant.fileNum;
import static com.videogo.been.AlarmContant.tifNum;


public class MainActivity extends FragmentActivity implements View.OnClickListener {
    private MapView mapView = null;
    private ImageButton change, info, robot, measure, measure_sel, zoom_in, zoom_out, position, position_sel;
    private TextView result;
    private String path;
    private TextView title;
    private String TAG = "MainActivity";
    private SharedPreferences sharedPreferences;
    private List<EZDeviceInfo> list_ezDevices;
    private List<Graphic> list_graphic = new ArrayList<>();
    private List<Point> pointList = new ArrayList<>();
    private GraphicsLayer graphicsLayer;
    private GraphicsLayer graphicsLayer_camera;
    private GraphicsLayer graphicsLayer_info;
    private GraphicsLayer graphicsLayer_info_text;
    private LocationDisplayManager locationDisplayManager = null;
    public final static int REQUEST_CODE = 100;
    public ExecutorService executorService;
    public CountDownLatch TaskLatch;
    private TextView update;
    private ProgressDialog progressDialog;
    private WaitDialog mWaitDlg = null;
    private FTPutils.FtpProgressListener ftpProgressListener;
    private FTPutils.FtpProgressListener txtProgressListener;
    private FTPutils.FtpProgressListener mapProgressListener;
    private String local_path = Environment.getExternalStorageDirectory().getPath() + "/EZOpenSDK/map";
    private String url_3 = "/west";
    private Handler handler1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (mWaitDlg != null && mWaitDlg.isShowing()) {
                        mWaitDlg.dismiss();
                    }
                    //1.txt解析
                    String s = getTxtContent("/EZOpenSDK/map/version.txt");
                    String local_s = getTxtContent("/EZOpenSDK/map"+url_3+"/version.txt");
                    //2.与本地txt比对
                    String[] num = s.substring(0, s.lastIndexOf(";")).split(",");
                    String[] local_num = local_s.substring(0, local_s.lastIndexOf(";")).split(",");
                    List<String> name_list = Arrays.asList(num);
                    List<String> name_local_list = Arrays.asList(local_num);
                    if (name_list.size()>name_local_list.size()){
                        progressDialog.show();
                        if (name_local_list.size() > 0) {
                            for (int i = 0; i < name_local_list.size(); i++) {
                                name_list.remove(name_local_list.get(i));
                            }
                        }
                        List<String> serverPath_list = new ArrayList<>();
                        List<String> filename_list = new ArrayList<>();
                        for (String str : name_list){
                            serverPath_list.add("node/kaifaqu/map"+url_3+url_3+str+".TIF");
                            serverPath_list.add("node/kaifaqu/map"+url_3+url_3+str+".TIF.aux.xml");
                            serverPath_list.add("node/kaifaqu/map"+url_3+url_3+str+".TIF.ovr");
                            serverPath_list.add("node/kaifaqu/map"+url_3+url_3+str+".tfw");
                            filename_list.add(url_3.substring(1)+str+".TIF");
                            filename_list.add(url_3.substring(1)+str+".TIF.aux.xml");
                            filename_list.add(url_3.substring(1)+str+".TIF.ovr");
                            filename_list.add(url_3.substring(1)+str+".tfw");
                        }
                        //3.根据比对结果下载地图文件
                        UpdateMapTask downMapTask = new UpdateMapTask(filename_list,serverPath_list,path,ftpProgressListener);
                        executorService.execute(downMapTask);
                    }else{
                        ToastNotRepeat.show(getApplicationContext(),"当前版本已是最新版本！");
                    }
                    break;
                case 1:
                    Bundle bundle = msg.getData();
                    String progress = bundle.getString("progress");
                    String currentSize = bundle.getString("currentSize");
                    progressDialog.setProgress(Integer.parseInt(currentSize)/1048576);
                    if (Float.parseFloat(progress)==100){
                        //4.修改本地txt
                        progressDialog.dismiss();
                        String server_path = "node/kaifaqu/map"+url_3+"/version.txt";
                        String file_name = "version.txt";
                        UpdateTask updateTask = new UpdateTask(local_path+url_3, server_path, file_name, txtProgressListener);
                        executorService.execute(updateTask);
                    }
                    break;
                case 3:
                    //5.重新加载地图
                    reLoadClass();
                    break;
                case 4:
                    if (mWaitDlg != null && mWaitDlg.isShowing()) {
                        mWaitDlg.dismiss();
                    }
                    ToastNotRepeat.show(getApplicationContext(),"网络连接错误，请稍后重试...");
                    break;
                case 5:
                    Bundle bundle1 = msg.getData();
                    String progress1 = bundle1.getString("progress");
                    String currentSize1 = bundle1.getString("currentSize");
                    progressDialog.setProgress(Integer.parseInt(currentSize1)/1048576);
                    if (Float.parseFloat(progress1)==100){
                        progressDialog.dismiss();
                        reLoadClass();
                    }
                    break;
                case 6:
                    if (mWaitDlg != null && mWaitDlg.isShowing()) {
                        mWaitDlg.dismiss();
                    }
                    Bundle bundle2 = msg.getData();
                    String serverSize2 = bundle2.getString("serverSize");
                    progressDialog.setProgressNumberFormat("%1d Mb /%2d Mb");
                    progressDialog.setMax(Integer.parseInt(serverSize2)/1048576);
                    progressDialog.show();
                    break;
            }
        }
    };

    private void reLoadClass() {
        recreate();
    }

    private String getTxtContent(String s2) {
        String path = Environment.getExternalStorageDirectory().getPath() + s2;
        String content = ReadUtils.ReadTxtFile(path, MainActivity.this);
        String[] str = content.split(":");
        String s = str[str.length - 1];
        return s;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        //设置授权
        ArcGISRuntime.setClientId("Gxw2gDOFkkdudimV");
        mapView = (MapView) findViewById(R.id.map);
        change = findViewById(R.id.change_ibtn);
        info = findViewById(R.id.info_ibtn);
        robot = findViewById(R.id.robot_ibtn);
        measure = findViewById(R.id.measure_ibtn);
        measure_sel = findViewById(R.id.measure_ibtn_sel);
        zoom_in = findViewById(R.id.zoom_in_ibtn);
        zoom_out = findViewById(R.id.zoom_out_ibtn);
        position = findViewById(R.id.position_ibtn);
        result = findViewById(R.id.result);
        position_sel = findViewById(R.id.position_ibtn_sel);
        title = findViewById(R.id.title_tv);
        update = findViewById(R.id.update);
        update.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        executorService = Executors.newSingleThreadExecutor();
        TaskLatch = new CountDownLatch(1);
        CopyFontFile mCopyData_File = new CopyFontFile(this);
        mCopyData_File.DoCopy();
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("影像文件下载中，请稍候...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);// 设置允许取消


        mapView.setEsriLogoVisible(false);
        change.setOnClickListener(this);
        info.setOnClickListener(this);
        robot.setOnClickListener(this);
        measure.setOnClickListener(this);
        measure_sel.setOnClickListener(this);
        zoom_in.setOnClickListener(this);
        zoom_out.setOnClickListener(this);
        position.setOnClickListener(this);
        position_sel.setOnClickListener(this);
        update.setOnClickListener(this);

        sharedPreferences = getSharedPreferences("path", 0);
        path = sharedPreferences.getString("earthPath", "");
        mWaitDlg = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        list_ezDevices = new ArrayList<>();
        list_ezDevices = getIntent().getParcelableArrayListExtra("devices_main");
        if (path.equals("") || path == null) {
            //path =Environment.getExternalStorageDirectory().getPath()+"/1.tif";
            path = Environment.getExternalStorageDirectory().getPath() + "/EZOpenSDK/map/west";
        }
        //加载tif
        locationDisplayManager = mapView.getLocationDisplayManager();
        try {
            locationDisplayManager.setAllowNetworkLocation(true);
            locationDisplayManager.setAccuracyCircleOn(true);
            locationDisplayManager.setShowLocation(true);
            locationDisplayManager.setAccuracySymbol(new SimpleFillSymbol(Color.GREEN).setAlpha(20));
            locationDisplayManager.setAutoPanMode(LocationDisplayManager.AutoPanMode.OFF);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ftpProgressListener = new FTPutils.FtpProgressListener() {
            @Override
            public void onFtpProgress(int currentStatus, long process, File targetFile,long currentSize,long serverSize) {
                if (currentStatus == Constant.FTP_DOWN_START){
                    Log.d("TAG","serverSize="+serverSize);
                    Message message = Message.obtain();
                    message.what = 6;
                    Bundle bundle = new Bundle();
                    bundle.putString("serverSize", String.valueOf(serverSize));
                    message.setData(bundle);
                    handler.sendMessage(message);
                }
                Message message = Message.obtain();
                message.what = 1;
                Bundle bundle = new Bundle();
                bundle.putString("progress", String.valueOf(process));
                bundle.putString("currentSize", String.valueOf(currentSize));
                message.setData(bundle);
                handler.sendMessage(message);
            }
        };
        txtProgressListener = new FTPutils.FtpProgressListener() {
            @Override
            public void onFtpProgress(int currentStatus, long process, File targetFile,long currentSize,long serverSize) {
                if (currentStatus == Constant.FTP_DOWN_SUCCESS) {
                    Log.d("TAG","txt下载完成");
                    Message message = Message.obtain();
                    message.what = 3;
                    handler.sendMessage(message);
                }
            }
        };
        mapProgressListener = new FTPutils.FtpProgressListener() {
            @Override
            public void onFtpProgress(int currentStatus, long process, File targetFile, long currentSize, long serverSize) {
                if (currentStatus == Constant.FTP_DOWN_START){
                    Log.d("TAG","serverSize="+serverSize);
                    Message message = Message.obtain();
                    message.what = 6;
                    Bundle bundle = new Bundle();
                    bundle.putString("serverSize", String.valueOf(serverSize));
                    message.setData(bundle);
                    handler.sendMessage(message);
                }
                Message message = Message.obtain();
                message.what = 5;
                Bundle bundle = new Bundle();
                bundle.putString("progress", String.valueOf(process));
                bundle.putString("currentSize", String.valueOf(currentSize));
                message.setData(bundle);
                handler.sendMessage(message);
            }
        };
        loadlayer(path);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.update:
                map_update();
                break;
            case R.id.change_ibtn:
                selectfile();
                break;
            case R.id.info_ibtn:
                if (graphicsLayer_info.isVisible()) {
                    graphicsLayer_info.setVisible(false);
                    if (mapView.getScale() < 30000) {
                        graphicsLayer_info_text.setVisible(false);
                    }
                    info.setBackgroundResource(R.mipmap.xinxi);
                } else {
                    graphicsLayer_info.setVisible(true);
                    if (mapView.getScale() < 30000) {
                        graphicsLayer_info_text.setVisible(true);
                    }
                    info.setBackgroundResource(R.mipmap.xinxi_sel);
                }
                break;
            case R.id.robot_ibtn:
                if (graphicsLayer_camera.isVisible()) {
                    graphicsLayer_camera.setVisible(false);
                    robot.setBackgroundResource(R.mipmap.jiqiren);
                } else {
                    graphicsLayer_camera.setVisible(true);
                    robot.setBackgroundResource(R.mipmap.jiqiren_sel);
                }
                break;
            case R.id.measure_ibtn:
                measure.setVisibility(View.GONE);
                measure_sel.setVisibility(View.VISIBLE);
                result.setVisibility(View.VISIBLE);
                title.setVisibility(View.GONE);
                graphicsLayer.removeAll();
                pointList.clear();
                break;
            case R.id.measure_ibtn_sel:
                measure.setVisibility(View.VISIBLE);
                measure_sel.setVisibility(View.GONE);
                result.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                list_graphic.clear();
                result.setText("");
                graphicsLayer.removeAll();
                pointList.clear();
                break;
            case R.id.zoom_in_ibtn:
                mapView.zoomin();
                break;
            case R.id.zoom_out_ibtn:
                mapView.zoomout();
                break;
            case R.id.position_ibtn:
                if (!locationDisplayManager.isStarted()) {
                    locationDisplayManager.start();
                    position.setVisibility(View.GONE);
                    position_sel.setVisibility(View.VISIBLE);
                    handler1 = new Handler();
                    handler1.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                double la = locationDisplayManager.getLocation().getLatitude();
                                double ln = locationDisplayManager.getLocation().getLongitude();
                                Point p = new Point(ln, la);
                                Envelope e = mapView.getMaxExtent();
                                if (!e.contains(p)) {
                                    locationDisplayManager.stop();
                                    position.setVisibility(View.VISIBLE);
                                    position_sel.setVisibility(View.GONE);
                                    ToastNotRepeat.show(getApplicationContext(), "超出地图范围！");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 1500);
                }
                break;
            case R.id.position_ibtn_sel:
                if (locationDisplayManager.isStarted()) {
                    locationDisplayManager.stop();
                    position.setVisibility(View.VISIBLE);
                    position_sel.setVisibility(View.GONE);
                }
                break;
        }
    }

    /**
     * 地图更新
     */
    private void map_update() {
        mWaitDlg.show();
        String server_path = "node/kaifaqu/map"+url_3+"/version.txt";
        String file_name = "version.txt";
        FTPutils.FtpProgressListener listener = new FTPutils.FtpProgressListener() {
            @Override
            public void onFtpProgress(int currentStatus, long process, File targetFile,long currentSize,long serverSize) {
                if (currentStatus == Constant.FTP_DOWN_SUCCESS) {
                    Message message = Message.obtain();
                    message.what = 0;
                    handler.sendMessage(message);
                }
            }
        };
        File file = new File(local_path + "/" + file_name);
        if (file.exists()) {
            file.delete();
        }
        UpdateTask updateTask = new UpdateTask(local_path, server_path, file_name, listener);
        executorService.execute(updateTask);
    }

    /**
     * 地图切换
     */
    private void selectfile() {
        if (path.equals("") || path == null || (path.substring(path.lastIndexOf("/") + 1)).equals("south")) {
            path = Environment.getExternalStorageDirectory().getPath() + "/EZOpenSDK/map/west";
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("earthPath", path);
            editor.commit();
        } else if ((path.substring(path.lastIndexOf("/") + 1)).equals("west")) {
            path = Environment.getExternalStorageDirectory().getPath() + "/EZOpenSDK/map/south";
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("earthPath", path);
            editor.commit();
        }
        reLoadClass();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler!=null){
            handler.removeCallbacksAndMessages(null);
        }
        if (handler1!=null){
            handler1.removeCallbacksAndMessages(null);
        }
        if (locationDisplayManager.isStarted()){
            locationDisplayManager.stop();
        }
        executorService.shutdownNow();
    }

    /**
     * 添加图层
     */
    private void loadlayer(String path) {
        mWaitDlg.show();
        update.setVisibility(View.GONE);
        File file = new File(path);
        String url_2 = "西区.kml";
        if ((path.substring(path.lastIndexOf("/") + 1)).equals("west")) {
            title.setText("西区");
            url_2 = "西区.kml";
            url_3 = "/west";
        } else {
            title.setText("南区");
            url_2 = "南区.kml";
            url_3 = "/south";
        }
        if (!file.exists()) {
            ToastNotRepeat.show(getApplicationContext(), "影像文件下载中，请稍后...");
            //下载地图文件
            String localPath = path;
            List<String> serverPath_list = new ArrayList<>();
            List<String> filename_list = new ArrayList<>();
            for (int i = 0 ; i < tifNum ; i ++){
                serverPath_list.add("node/kaifaqu/map"+url_3+url_3+String.valueOf(i)+".TIF");
                serverPath_list.add("node/kaifaqu/map"+url_3+url_3+String.valueOf(i)+".TIF.aux.xml");
                serverPath_list.add("node/kaifaqu/map"+url_3+url_3+String.valueOf(i)+".TIF.ovr");
                serverPath_list.add("node/kaifaqu/map"+url_3+url_3+String.valueOf(i)+".tfw");
                filename_list.add(url_3.substring(1)+String.valueOf(i)+".TIF");
                filename_list.add(url_3.substring(1)+String.valueOf(i)+".TIF.aux.xml");
                filename_list.add(url_3.substring(1)+String.valueOf(i)+".TIF.ovr");
                filename_list.add(url_3.substring(1)+String.valueOf(i)+".tfw");
            }
            addPathList(serverPath_list, filename_list);
            DownMapTask downMapTask = new DownMapTask(serverPath_list,localPath,filename_list,mapProgressListener);
            executorService.execute(downMapTask);
        } else {
            //判断文件是否完整
            File[] files = file.listFiles();
            if (files.length<fileNum ){
                List<String> l = new ArrayList<>();
                for (File sfile : files){
                    l.add(sfile.getName());
                }
                ComPareMapTask task = new ComPareMapTask(l,path,mapProgressListener);
                executorService.execute(task);

            }else{
                if (mWaitDlg != null && mWaitDlg.isShowing()) {
                    mWaitDlg.dismiss();
                }
                update.setVisibility(View.VISIBLE);
                //加载shp
                try {
                    ShapefileFeatureTable shapefileFeatureTable = new ShapefileFeatureTable(path + "/polyline.shp");
                    FeatureLayer featureLayer = new FeatureLayer(shapefileFeatureTable);
                    featureLayer.setRenderer(new SimpleRenderer(new SimpleFillSymbol(Color.RED)));
                    mapView.addLayer(featureLayer);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                //加载tif
                for (int i = 0; i < tifNum; i++) {
                    executorService.execute(new ImageTask(TaskLatch, path, i, mapView, url_3));
                }
                //加载kml
                LoadKmlTask loadKmlTask = new LoadKmlTask(TaskLatch, "camera.kml", url_2);
                executorService.execute(loadKmlTask);
                graphicsLayer = new GraphicsLayer();
                graphicsLayer_camera = new GraphicsLayer();
                graphicsLayer_info = new GraphicsLayer();
                graphicsLayer_info_text = new GraphicsLayer();
                graphicsLayer_info_text.setVisible(false);
            }

            /**
             * mapview监听
             */
            mapView.setOnSingleTapListener(new OnSingleTapListener() {
                @Override
                public void onSingleTap(float v, float v1) {
                    if (measure_sel.getVisibility() == View.VISIBLE) {
                        Point p = mapView.toMapPoint(v, v1);
                        pointList.add(p);
                        //点，线，面样式
                        SimpleMarkerSymbol simpleMarkerSymbol = new SimpleMarkerSymbol(Color.BLACK, 6, SimpleMarkerSymbol.STYLE.CIRCLE);
                        SimpleLineSymbol simpleLineSymbol = new SimpleLineSymbol(Color.BLACK, 2);
                        SimpleFillSymbol simpleFillSymbol = new SimpleFillSymbol(Color.YELLOW);
                        simpleFillSymbol.setAlpha(90);
                        simpleFillSymbol.setOutline(new SimpleLineSymbol(Color.argb(0, 0, 0, 0), 1));
                        if (pointList.size() == 1) {
                            Graphic point = new Graphic(p, simpleMarkerSymbol);
                            graphicsLayer.addGraphic(point);
                        } else if (pointList.size() == 2) {
                            graphicsLayer.removeAll();
                            Polyline polyline = new Polyline();
                            polyline.startPath(pointList.get(0));
                            polyline.lineTo(p);
                            Graphic line = new Graphic(polyline, simpleLineSymbol);
                            graphicsLayer.addGraphic(line);
                            double distance = GeometryEngine.geodesicDistance(pointList.get(0), pointList.get(1), mapView.getSpatialReference(), new LinearUnit(LinearUnit.Code.METER));
                            double distance_2 = new BigDecimal(distance).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            result.setText("距离为:" + distance_2 + "米");
                        } else if (pointList.size() > 2) {
                            graphicsLayer.removeAll();
                            Polygon polygon = new Polygon();
                            polygon.startPath(pointList.get(0));
                            for (int i = 1; i < pointList.size(); i++) {
                                polygon.lineTo(pointList.get(i));
                            }
                            Graphic gon = new Graphic(polygon, simpleFillSymbol);
                            graphicsLayer.addGraphic(gon);
                            double area = GeometryEngine.geodesicArea(polygon, mapView.getSpatialReference(), new AreaUnit(AreaUnit.Code.SQUARE_METER));
                            double area_2 = new BigDecimal(area).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            double mu = area * 0.0015;
                            BigDecimal b = new BigDecimal(mu);
                            //保留小数点后两位
                            double mu_2 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            result.setText("面积为:" + area_2 + "平方米/" + mu_2 + "亩");
                        }
                    } else {
                        int[] objectIds = graphicsLayer_camera.getGraphicIDs(v, v1, 20);
                        if (objectIds != null && objectIds.length > 0) {
                            for (int i = 0; i < objectIds.length; i++) {
                                Graphic graphic = graphicsLayer_camera.getGraphic(objectIds[i]);
                                if (graphic.getAttributes().get("style").equals("marker")) {
                                    showDialog(graphic);
                                }
                            }
                        }
                    }
                }
            });
            mapView.setOnZoomListener(new OnZoomListener() {
                @Override
                public void preAction(float v, float v1, double v2) {
                    //定义地图默认缩放处理之前的操作。
                }

                @Override
                public void postAction(float v, float v1, double v2) {
                    //定义地图默认缩放处理后的操作。
                    if (mapView.getScale() > 30000) {
                        graphicsLayer_info_text.setVisible(false);
                    } else {
                        graphicsLayer_info_text.setVisible(true);
                    }
                }
            });
        }
    }

    private void addPathList(List<String> serverPath_list, List<String> filename_list) {
        serverPath_list.add("node/kaifaqu/map" + url_3 + "/polyline.dbf");
        serverPath_list.add("node/kaifaqu/map" + url_3 + "/polyline.prj");
        serverPath_list.add("node/kaifaqu/map" + url_3 + "/polyline.sbn");
        serverPath_list.add("node/kaifaqu/map" + url_3 + "/polyline.sbx");
        serverPath_list.add("node/kaifaqu/map" + url_3 + "/polyline.shp");
        serverPath_list.add("node/kaifaqu/map" + url_3 + "/polyline.shp.xml");
        serverPath_list.add("node/kaifaqu/map" + url_3 + "/polyline.shx");
        serverPath_list.add("node/kaifaqu/map" + url_3 + "/version.txt");
        filename_list.add("polyline.dbf");
        filename_list.add("polyline.prj");
        filename_list.add("polyline.sbn");
        filename_list.add("polyline.sbx");
        filename_list.add("polyline.shp");
        filename_list.add("polyline.shp.xml");
        filename_list.add("polyline.shx");
        filename_list.add("version.txt");
    }

    /**
     * 加载机器人信息
     *
     * @param url
     */
    private void loadCamera(String url) {
        PictureMarkerSymbol pictureMarkerSymbol = new PictureMarkerSymbol((BitmapDrawable) getResources().getDrawable(R.mipmap.marker_1));
        List<String> list_name = new ArrayList<>();
        List<String> list_des = new ArrayList<>();
        List<Point> list_point = new ArrayList<>();
        ReadKml readKml = new ReadKml(url, list_name, list_des, list_point, MainActivity.this);
        readKml.parseKml2();
        for (int i = 0; i < list_point.size(); i++) {
            int finalI = i;
            Map<String, Object> map = new HashMap<>();
            map.put("style", "marker");
            map.put("name", list_name.get(finalI + 2));
            map.put("des", list_des.get(finalI));
            Graphic pointGraphic = new Graphic(list_point.get(i), pictureMarkerSymbol, map);
            graphicsLayer_camera.addGraphic(pointGraphic);
            TextSymbol t = new TextSymbol(12, list_name.get(finalI + 2), Color.GREEN);
            t.setFontFamily(new File(CopyFontFile.FONT_PATH).getPath());
            t.setOffsetX(-10);
            t.setOffsetY(-22);
            Map<String, Object> map2 = new HashMap<>();
            map2.put("style", "text");
            Graphic graphic_text = new Graphic(list_point.get(finalI), t, map2);
            graphicsLayer_camera.addGraphic(graphic_text);
        }
    }

    /**
     * 加载地块信息
     *
     * @param url2
     */
    private void loadinfo(String url2) {
        List<String> list_name_info = new ArrayList<>();
        List<String> list_des_info = new ArrayList<>();
        List<List<Point>> list_collection = new ArrayList<>();
        List<StyleId> list_styleid = new ArrayList<>();
        List<StyleMap> list_stylemap = new ArrayList<>();
        List<String> list_style_url = new ArrayList<>();
        ReadKml readKml1 = new ReadKml(url2, list_name_info, list_des_info, null, list_collection, list_styleid, list_stylemap, list_style_url, MainActivity.this);
        readKml1.parseKml();
        //线型
        for (int i = 0; i < list_collection.size(); i++) {
            Polyline polyline = new Polyline();
            polyline.startPath(list_collection.get(i).get(0));
            for (int j = 1; j < list_collection.get(i).size(); j++) {
                polyline.lineTo(list_collection.get(i).get(j));
            }
            Map<String, Object> map = new HashMap<>();
            map.put("style", "line");
            String url = list_style_url.get(i);
            String linecolor = "";
            String linewidth = "";
            for (StyleMap styleMap : list_stylemap) {
                if (styleMap.getId().equals(url)) {
                    String stylemapUrl = styleMap.getStyleUrl();
                    for (StyleId styleid : list_styleid) {
                        if (styleid.getId().equals(stylemapUrl)) {
                            linecolor = styleid.getLineColor();
                            linewidth = styleid.getLineWidth();
                        }
                    }
                }
            }
            SimpleLineSymbol simpleLineSymbol_info = new SimpleLineSymbol(Color.parseColor("#" + linecolor), Integer.parseInt(linewidth));
            Graphic line = new Graphic(polyline, simpleLineSymbol_info, map);
            graphicsLayer_info.addGraphic(line);
            TextSymbol t = new TextSymbol(12, list_name_info.get(i + 1) + list_des_info.get(i), Color.WHITE);
            t.setFontFamily(new File(CopyFontFile.FONT_PATH).getPath());
            t.setOffsetX(-20);
            Map<String, Object> map2 = new HashMap<>();
            map2.put("style", "text");
            Graphic ts = new Graphic(polyline, t, map2);
            graphicsLayer_info_text.addGraphic(ts);
        }
    }

    /**
     * marker弹窗
     */
    private void showDialog(Graphic graphic) {
        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.marker_dialog, null);
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setTitle("").setView(linearLayout).show();
        TextView tv_name = dialog.findViewById(R.id.name_tv);
        TextView tv_des = dialog.findViewById(R.id.des_tv);
        Button btn_open = dialog.findViewById(R.id.open);
        tv_name.setText(graphic.getAttributes().get("name").toString());
        tv_des.setText(graphic.getAttributes().get("des").toString());
        btn_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (EZDeviceInfo ezDeviceInfo : list_ezDevices) {
                    for (EZCameraInfo ezCameraInfo : ezDeviceInfo.getCameraInfoList()) {
                        if (ezCameraInfo == null) {
                            return;
                        } else if (ezCameraInfo.getCameraName().equals(tv_name.getText())) {
                            Intent intent = new Intent(MainActivity.this, EZRealPlayActivity.class);
                            intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, ezCameraInfo);
                            intent.putExtra(IntentConsts.EXTRA_DEVICE_INFO, ezDeviceInfo);
                            startActivityForResult(intent, REQUEST_CODE);
                            return;
                        }
                    }
                }
            }
        });
    }
    private class ComPareMapTask implements Runnable{
        private List<String> list;
        private String localPath;
        private FTPutils.FtpProgressListener downMapProgressListener;

        public ComPareMapTask(List<String> list, String localPath, FTPutils.FtpProgressListener downMapProgressListener) {
            this.list = list;
            this.localPath = localPath;
            this.downMapProgressListener = downMapProgressListener;
        }

        @Override
        public void run() {
            FTPutils ftPutils = new FTPutils();
            Boolean flag = ftPutils.connect(AlarmContant.ftp_ip, Integer.parseInt(AlarmContant.ftp_port), AlarmContant.name, AlarmContant.password);
            if (flag){
                try {
                    FTPFile[] ftpFiles = ftPutils.getmFtpClient().listFiles("node/kaifaqu/map"+url_3);
                    List<String> serverPath_list = new ArrayList<>();
                    List<String> filename_list = new ArrayList<>();
                    for (int i = 0 ; i < ftpFiles.length ; i++){
                        if (!list.contains(ftpFiles[i].getName())){
                            serverPath_list.add("node/kaifaqu/map"+url_3+"/"+ftpFiles[i].getName());
                            filename_list.add(ftpFiles[i].getName());
                        }
                    }
                    ftPutils.downloadMoreFile(serverPath_list,localPath,filename_list,downMapProgressListener);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 地图下载
     */
    private class DownMapTask implements Runnable{
        private List<String> serverPath_list;
        private String localPath;
        private List<String> filename_list;
        private FTPutils.FtpProgressListener downMapProgressListener;

        public DownMapTask(List<String> serverPath_list, String localPath, List<String> filename_list, FTPutils.FtpProgressListener downMapProgressListener) {
            this.serverPath_list = serverPath_list;
            this.localPath = localPath;
            this.filename_list = filename_list;
            this.downMapProgressListener = downMapProgressListener;
        }

        @Override
        public void run() {
            FTPutils ftPutils = new FTPutils();
            Boolean flag = ftPutils.connect(AlarmContant.ftp_ip, Integer.parseInt(AlarmContant.ftp_port), AlarmContant.name, AlarmContant.password);
            if (flag) {
                try {
                    ftPutils.downloadMap(serverPath_list,localPath,filename_list,downMapProgressListener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                Message message = Message.obtain();
                message.what = 4;
                handler.sendMessage(message);
            }
        }
    }
    /**
     * 地图更新
     */
    private class UpdateMapTask implements Runnable {
        private List<String> filename_list;
        private List<String> serverPath_list;
        private String local_path;
        private FTPutils.FtpProgressListener progressListener;

        public UpdateMapTask(List<String> filename_list, List<String> serverPath_list, String local_path, FTPutils.FtpProgressListener progressListener) {
            this.filename_list = filename_list;
            this.serverPath_list = serverPath_list;
            this.local_path = local_path;
            this.progressListener = progressListener;
        }

        @Override
        public void run() {
            FTPutils ftPutils = new FTPutils();
            Boolean flag = ftPutils.connect(AlarmContant.ftp_ip, Integer.parseInt(AlarmContant.ftp_port), AlarmContant.name, AlarmContant.password);
            if (flag) {
                try {
                    ftPutils.downloadMoreFile(serverPath_list,local_path,filename_list,progressListener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                Message message = Message.obtain();
                message.what = 4;
                handler.sendMessage(message);
            }
        }
    }

    /**
     * 检查更新
     */
    private class UpdateTask implements Runnable {
        private String localPath;
        private String serverPath;
        private String fileName;
        private FTPutils.FtpProgressListener listener;

        public UpdateTask(String localPath, String serverPath, String fileName, FTPutils.FtpProgressListener listener) {
            this.localPath = localPath;
            this.serverPath = serverPath;
            this.fileName = fileName;
            this.listener = listener;
        }

        @Override
        public void run() {
            FTPutils ftPutils = new FTPutils();
            Boolean flag = ftPutils.connect(AlarmContant.ftp_ip, Integer.parseInt(AlarmContant.ftp_port), AlarmContant.name, AlarmContant.password);
            if (flag) {
                try {
                    ftPutils.downloadSingleFile2(serverPath, localPath, fileName, listener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                Message message = Message.obtain();
                message.what = 4;
                handler.sendMessage(message);
            }
        }
    }


    private class ImageTask implements Runnable {
        private CountDownLatch endTaskLatch;
        private String img_path;
        private int i;
        private MapView mMapview;
        private String str;

        public ImageTask(CountDownLatch endTaskLatch, String img_path, int i, MapView mMapview, String str) {
            this.endTaskLatch = endTaskLatch;
            this.img_path = img_path;
            this.i = i;
            this.mMapview = mMapview;
            this.str = str;
        }

        @Override
        public void run() {

            FileRasterSource rasterSource = null;
            try {
                rasterSource = new FileRasterSource(img_path + str + i + ".TIF");
                rasterSource.project(mMapview.getSpatialReference());
                Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
                RasterLayer rasterLayer = new RasterLayer(rasterSource);
                mMapview.addLayer(rasterLayer);
                Log.d(TAG, "currentId="+Thread.currentThread().getId());
            } catch (FileNotFoundException e) {
                Log.d(TAG, "message = "+e.getMessage());
            } catch (IllegalArgumentException ie){
                Log.d(TAG, "message = "+ie.getMessage());
            } catch (RuntimeException re){
                Log.d(TAG, "message = "+re.getMessage());
                if (re.getMessage().contains("Failed to open raster dataset")){
                    deleteFile();
                }
            }
            if (rasterSource!=null){
                rasterSource.dispose();
            }
            endTaskLatch.countDown();
        }

        private void deleteFile() {
            File file = new File(img_path+str+i+".TIF");
            File file2 = new File(img_path+str+i+".tfw");
            File file3 = new File(img_path+str+i+".TIF.ovr");
            File file4 = new File(img_path+str+i+".TIF.aux.xml");
            if (file.exists()){
                file.delete();
            }
            if (file2.exists()){
                file2.delete();
            }
            if (file3.exists()){
                file3.delete();
            }
            if (file4.exists()){
                file4.delete();
            }
        }
    }

    private class LoadKmlTask implements Runnable {
        private CountDownLatch endTaskLatch;
        private String url_1;
        private String url_2;

        public LoadKmlTask(CountDownLatch endTaskLatch, String url_1, String url_2) {
            this.endTaskLatch = endTaskLatch;
            this.url_1 = url_1;
            this.url_2 = url_2;
        }

        @Override
        public void run() {
            try {
                endTaskLatch.await();
                loadCamera(url_1);
                loadinfo(url_2);
                mapView.addLayer(graphicsLayer_camera);
                mapView.addLayer(graphicsLayer_info);
                mapView.addLayer(graphicsLayer_info_text);
                mapView.addLayer(graphicsLayer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
