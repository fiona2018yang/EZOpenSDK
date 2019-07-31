package com.videogo;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.baidu.location.Poi;
import com.esri.arcgisruntime.data.TileCache;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.raster.Raster;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

import cn.qqtheme.framework.picker.FilePicker;
import ezviz.ezopensdk.R;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
    private MapView mapView = null;
    private ImageButton qiehuan,xinxi,baojing,jiqiren,liangsuan,fangda,suoxiao,dingwei;
    private ArcGISMap mMap;
    private String path;
    private SharedPreferences sharedPreferences;
    private PointCollection collection ;

    private static String[] allpermissions = {
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private boolean isNeedCheck = true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkpermission();
        initViews();
    }
    private void initViews() {
        mapView = (MapView) findViewById(R.id.map);
        qiehuan = findViewById(R.id.qiehuan);
        xinxi = findViewById(R.id.xinxi);
        baojing = findViewById(R.id.baojing);
        jiqiren = findViewById(R.id.jiqiren);
        liangsuan = findViewById(R.id.liangsuan);
        fangda = findViewById(R.id.fangda);
        suoxiao = findViewById(R.id.suoxiao);
        dingwei = findViewById(R.id.dingwei);

        qiehuan.setOnClickListener(this);
        xinxi.setOnClickListener(this);
        baojing.setOnClickListener(this);
        jiqiren.setOnClickListener(this);
        liangsuan.setOnClickListener(this);
        fangda.setOnClickListener(this);
        suoxiao.setOnClickListener(this);
        dingwei.setOnClickListener(this);


        sharedPreferences = getSharedPreferences("path", 0);
        path = sharedPreferences.getString("earthPath", "");
        Log.i("TAG","path="+path);
        if (path.equals("") || path == null) {
            Toast.makeText(this,"没有TPK地图，请选择文件或者从云端下载！",Toast.LENGTH_LONG).show();
        }else{
            String str = path.substring(path.indexOf("."));
            collection = new PointCollection(mapView.getSpatialReference());
            //加载离线切片地图
            if (str.equals(".tpk")){
                TileCache mainTileCache = new TileCache(path);
                ArcGISTiledLayer arcGISTiledLayer = new ArcGISTiledLayer(mainTileCache);
                arcGISTiledLayer.setDescription("MainLayer");
                mMap = new ArcGISMap(new Basemap(arcGISTiledLayer));
                mapView.setMap(mMap);
            }else if(str.equals(".tif")){
                //加载tif
                Raster raster = new Raster(path);
                RasterLayer rasterLayer = new RasterLayer(raster);
                rasterLayer.setDescription("MainLayer");
                mMap = new ArcGISMap(new Basemap(rasterLayer));
                mapView.setMap(mMap);
                rasterLayer.addDoneLoadingListener(new Runnable() {
                    @Override
                    public void run() {
                        mapView.setViewpointGeometryAsync(rasterLayer.getFullExtent(),50);
                    }
                });
            }
            /**
             * mapview监听
             */
            mapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this,mapView){
                @Override
                //点击屏幕
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    com.esri.arcgisruntime.geometry.Point p = mMapView.screenToLocation(new Point((int)e.getX(),(int)e.getY()));
                    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
                    mMapView.getGraphicsOverlays().add(graphicsOverlay);
                    collection.add(p);
                    SimpleMarkerSymbol s = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.BLACK,5);
                    SimpleLineSymbol s2 = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID,Color.BLACK,2);
                    graphicsOverlay.getGraphics().add(new Graphic(collection.get(collection.size()-1),s));
                    if (collection.size()==2){
                        Polyline polyline = new Polyline(collection);
                        Graphic line =new Graphic(polyline,s2);
                        graphicsOverlay.getGraphics().add(line);
                    }
                    return true;
                }
                //双击屏幕
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return super.onDoubleTap(e);
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.qiehuan:
                selectfile();
                break;
            case R.id.xinxi:
                break;
            case R.id.baojing:
                break;
            case R.id.jiqiren:
                break;
            case R.id.liangsuan:
                MeasureData();
                break;
            case R.id.fangda:
                ZoomIn();
                break;
            case R.id.suoxiao:
                ZoomOut();
                break;
            case R.id.dingwei:
                break;
        }
    }

    /**
     * 测量距离或面积
     */
    private void MeasureData() {

    }

    /**
     * 缩小
     */
    private void ZoomOut() {
        try{
            Double scales=mapView.getMapScale();
            mapView.setViewpointScaleAsync(scales*2);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 放大
     */
    private void ZoomIn() {
        try{
            Double scales=mapView.getMapScale();
            mapView.setViewpointScaleAsync(scales*0.5);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    /**
     *文件选择
     */
    private void selectfile() {
        FilePicker picker = new FilePicker(this, FilePicker.FILE);
        picker.setShowHideDir(false);
        picker.setRootPath(Environment.getExternalStorageDirectory().getPath());
        picker.setAllowExtensions(new String[]{".tif",".tpk"});
        picker.setOnFilePickListener(new FilePicker.OnFilePickListener() {
            @Override
            public void onFilePicked(String currentPath) {
                Log.i("TAG","currentPath="+currentPath);
                sharedPreferences = getSharedPreferences("path", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("earthPath", currentPath);
                editor.commit();
                //加载离线切片地图
                finish();
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
            }
        });
        picker.show();
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
                ActivityCompat.requestPermissions(MainActivity.this,allpermissions,1);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int  i = 0 ;i<grantResults.length;i++){
            if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(MainActivity.this, permissions[i]+"已授权",Toast.LENGTH_SHORT).show();
                isNeedCheck = false;
            }else{
                Toast.makeText(MainActivity.this,permissions[i]+"拒绝授权",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
