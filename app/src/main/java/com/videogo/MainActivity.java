package com.videogo;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.esri.arcgisruntime.data.TileCache;
import com.esri.arcgisruntime.geometry.AngularUnit;
import com.esri.arcgisruntime.geometry.AngularUnitId;
import com.esri.arcgisruntime.geometry.GeodeticCurveType;
import com.esri.arcgisruntime.geometry.GeodeticDistanceResult;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.LinearUnit;
import com.esri.arcgisruntime.geometry.LinearUnitId;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.KmlLayer;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.ogc.kml.KmlDataset;
import com.esri.arcgisruntime.ogc.kml.KmlNode;
import com.esri.arcgisruntime.raster.Raster;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.videogo.ui.cameralist.EZCameraListActivity;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import ezviz.ezopensdk.R;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
    private MapView mapView = null;
    private TextView titile;
    private ImageButton change,info,warning,robot,measure,measure_sel,zoom_in,zoom_out,position,position_sel;
    private TextView result;
    private ArcGISMap mMap;
    private String path;
    private Point point;
    private SharedPreferences sharedPreferences;
    private PointCollection collection ;
    public LocationDisplay locationDisplay;
    private List<Graphic> list_graphic = new ArrayList<>();

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
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkpermission();
        initViews();
    }
    private void initViews() {
        mapView = (MapView) findViewById(R.id.map);
        change = findViewById(R.id.change_ibtn);
        info = findViewById(R.id.info_ibtn);
        warning = findViewById(R.id.warning_ibtn);
        robot = findViewById(R.id.robot_ibtn);
        measure = findViewById(R.id.measure_ibtn);
        measure_sel = findViewById(R.id.measure_ibtn_sel);
        zoom_in = findViewById(R.id.zoom_in_ibtn);
        zoom_out = findViewById(R.id.zoom_out_ibtn);
        position = findViewById(R.id.position_ibtn);
        result = findViewById(R.id.result);
        position_sel = findViewById(R.id.position_ibtn_sel);
        titile = findViewById(R.id.title);

        change.setOnClickListener(this);
        info.setOnClickListener(this);
        warning.setOnClickListener(this);
        robot.setOnClickListener(this);
        measure.setOnClickListener(this);
        measure_sel.setOnClickListener(this);
        zoom_in.setOnClickListener(this);
        zoom_out.setOnClickListener(this);
        position.setOnClickListener(this);
        position_sel.setOnClickListener(this);

        sharedPreferences = getSharedPreferences("path", 0);
        path = sharedPreferences.getString("earthPath", "");
        Log.i("TAG","path="+path);
        if (path.equals("")||path == null){
            path =Environment.getExternalStorageDirectory().getPath()+"/1.tif";
        }
        //加载tif
        loadlayer(path);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.change_ibtn:
                selectfile();
                break;
            case R.id.info_ibtn:
                break;
            case R.id.warning_ibtn:
                break;
            case R.id.robot_ibtn:
                break;
            case R.id.measure_ibtn:
                measure.setVisibility(View.GONE);
                measure_sel.setVisibility(View.VISIBLE);
                result.setVisibility(View.VISIBLE);
                break;
            case R.id.measure_ibtn_sel:
                measure.setVisibility(View.VISIBLE);
                measure_sel.setVisibility(View.GONE);
                result.setVisibility(View.GONE);
                collection.clear();
                list_graphic.clear();
                result.setText("");
                mapView.getGraphicsOverlays().clear();
                break;
            case R.id.zoom_in_ibtn:
                ZoomIn();
                break;
            case R.id.zoom_out_ibtn:
                ZoomOut();
                break;
            case R.id.position_ibtn:
                Location();
                break;
            case R.id.position_ibtn_sel:
                StopLocation();
                break;
        }
    }
    /**
     * 测量面积
     */
    public String measure_area() {
        DecimalFormat df = new DecimalFormat("0.000");
        List<double[]> points = new ArrayList<double[]>();
        double earthRadiusMeters = 6378137.0;
        double metersPerDegree = 2.0 * Math.PI * earthRadiusMeters / 360.0;
        double radiansPerDegree = Math.PI / 180.0;
        String pt = "";
        //获取图形，并提示
        //if (list_graphic.get(list_graphic.size()-1).getGeometry().getGeometryType() == GeometryType.POLYGON){
            //计算面积
            for (int i = 0 ; i < collection.size() ; i++){
                if ((i+1) >= collection.size()){
                    if ((i+1) == collection.size()){
                        pt = pt + collection.get(i).getX() + "," + collection.get(i).getY() + ",";
                    }
                }else{
                    if (collection.get(i).getX() == collection.get(i+1).getX() && collection.get(i).getY() == collection.get(i+1).getY()){
                    }else{
                        pt = pt + collection.get(i).getX() + "," + collection.get(i).getY() + ",";
                    }
                }
            }
            Log.i("TAG","pt="+pt);
            String pp = pt.substring(0, pt.length() - 1);
            Log.i("TAG","pp="+pp);
            String[] pp1 = pp.split(";");
            for (String ppap : pp1) {
                String[] temp = ppap.split(",");
                for (int i = 0; i < temp.length; ) {
                    double[] point = {Double.parseDouble(temp[i]), Double.parseDouble(temp[i + 1])};
                    points.add(point);
                    i = i + 2;
                }
            }
            //经纬度计算多边形面积
            double a = 0.0;
            for (int i = 0; i < points.size(); ++i) {
                int j = (i + 1) % points.size();
                double xi = points.get(i)[0] * metersPerDegree * Math.cos(points.get(i)[1] * radiansPerDegree);
                double yi = points.get(i)[1] * metersPerDegree;
                double xj = points.get(j)[0] * metersPerDegree * Math.cos(points.get(j)[1] * radiansPerDegree);
                double yj = points.get(j)[1] * metersPerDegree;
                a += xi * yj - xj * yi;
            }
            double s = Math.abs(a / 2.0);
       // }
        return df.format(s);
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
     * 定位
     */
    private void Location(){
        android.graphics.Point p1 = new android.graphics.Point(mapView.getWidth()/2,mapView.getHeight()/2);
        point =mapView.screenToLocation(p1);
        locationDisplay=mapView.getLocationDisplay();
        locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
        locationDisplay.addDataSourceStatusChangedListener(new LocationDisplay.DataSourceStatusChangedListener() {
            @Override
            public void onStatusChanged(LocationDisplay.DataSourceStatusChangedEvent dataSourceStatusChangedEvent) {
                if (dataSourceStatusChangedEvent.isStarted())
                    return;
                if (dataSourceStatusChangedEvent.getError() == null)
                    return;
            }
        });
        locationDisplay.addLocationChangedListener(new LocationDisplay.LocationChangedListener() {
            @Override
            public void onLocationChanged(LocationDisplay.LocationChangedEvent locationChangedEvent) {

            }
        });
        if (!locationDisplay.isStarted()){
            Geometry g=mapView.getMap().getInitialViewpoint().getTargetGeometry();
            Geometry a=GeometryEngine.project(g, mapView.getSpatialReference());
            Geometry p=GeometryEngine.project(locationDisplay.getMapLocation(),mapView.getSpatialReference());
            boolean x=GeometryEngine.within(p,a);
            if (!x){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ToastNotRepeat.show(MainActivity.this,"定位失败！");
                        locationDisplay.stop();
                        mapView.setViewpointCenterAsync(point);
                        position.setVisibility(View.VISIBLE);
                        position_sel.setVisibility(View.GONE);
                    }
                },1200);
            }else {
                locationDisplay.setShowAccuracy(true);
                locationDisplay.setShowLocation(true);
                locationDisplay.setShowPingAnimation(true);
                locationDisplay.setUseCourseSymbolOnMovement(true);
            }
            position.setVisibility(View.GONE);
            position_sel.setVisibility(View.VISIBLE);
            locationDisplay.startAsync();
            Log.i("TAG","startlocation");
        }
    }

    /**
     * 取消定位
     */
    private void StopLocation(){
        try{
            locationDisplay.stop();
            mapView.setViewpointCenterAsync(point);
            position.setVisibility(View.VISIBLE);
            position_sel.setVisibility(View.GONE);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    /**
     *地图切换
     */
    private void selectfile() {
//        FilePicker picker = new FilePicker(this, FilePicker.FILE);
//        picker.setShowHideDir(false);
//        picker.setRootPath(Environment.getExternalStorageDirectory().getPath());
//        picker.setAllowExtensions(new String[]{".tif",".tpk"});
//        picker.setOnFilePickListener(new FilePicker.OnFilePickListener() {
//            @Override
//            public void onFilePicked(String currentPath) {
//                Log.i("TAG","currentPath="+currentPath);
//                sharedPreferences = getSharedPreferences("path", 0);
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                editor.putString("earthPath", currentPath);
//                editor.commit();
//                //加载离线切片地图
//                finish();
//                startActivity(new Intent(MainActivity.this, MainActivity.class));
//                overridePendingTransition(0, 0);
//            }
//        });
//        picker.show();
        if (path.equals("")||path==null||(path.substring(path.indexOf(".")-1)).equals("2.tif")){
            path =Environment.getExternalStorageDirectory().getPath()+"/1.tif";
            loadlayer(path);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("earthPath", path);
            editor.commit();
        }else if ((path.substring(path.indexOf(".")-1)).equals("1.tif")){
            path =Environment.getExternalStorageDirectory().getPath()+"/2.tif";
            loadlayer(path);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("earthPath", path);
            editor.commit();
        }
        list_graphic.clear();
        result.setText("");
        mapView.getGraphicsOverlays().clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isNeedCheck){
            checkpermission();
        }
    }
    /**
     * 添加图层
     */
    private void loadlayer(String path){
        File file = new File(path);
        if (!file.exists()){
            ToastNotRepeat.show(this,"文件不存在！");
        }else{
            if ((path.substring(path.indexOf(".")-1)).equals("2.tif")){
                titile.setText("实景地图(南区)");
            }else{
                titile.setText("实景地图(西区)");
            }
            Raster raster = new Raster(path);
            RasterLayer rasterLayer = new RasterLayer(raster);
            rasterLayer.setDescription("MainLayer");
            mMap = new ArcGISMap(new Basemap(rasterLayer));
            mapView.setMap(mMap);
            rasterLayer.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    mapView.setViewpointGeometryAsync(rasterLayer.getFullExtent(),50);
                    collection = new PointCollection(mapView.getSpatialReference());
                    String url = Environment.getExternalStorageDirectory().getPath()+"/robot.kml";
                    List<String> list_name = new ArrayList<>();
                    List<String> list_des = new ArrayList<>();
                    List<Point> list_point = new ArrayList<>();
                    try { ReadKml.parseKml(url,list_name,list_des,list_point);
                        Log.i("TAG","list_name="+list_name.toString());
                        Log.i("TAG","list_des="+list_des.toString());
                        Log.i("TAG","list_point="+list_point.toString());
                        Log.i("TAG","list_name.size="+list_name.size());
                        Log.i("TAG","list_des.size="+list_des.size());
                        Log.i("TAG","list_point.size="+list_point.size());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            //添加KML图层
//            KmlDataset kmlDataset = new KmlDataset(url);
//            KmlLayer kmlLayer = new KmlLayer(kmlDataset);
//            mMap.getOperationalLayers().add(kmlLayer);
//            mapView.setMap(mMap);
//            kmlDataset.addDoneLoadingListener(new Runnable() {
//                @Override
//                public void run() {
//                    List<KmlNode> list = kmlDataset.getRootNodes();
//                }
//            });
            /**
             * mapview监听
             */
            mapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this,mapView){
                @Override
                //点击屏幕
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    if (measure_sel.getVisibility() == View.VISIBLE){
                        //点，线，面样式
                        SimpleMarkerSymbol s = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.BLACK,3);
                        SimpleLineSymbol s2 = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID,Color.BLACK,2);
                        SimpleFillSymbol s3 = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID,R.color.simple_fill_color,null);

                        Point p = mMapView.screenToLocation(new android.graphics.Point((int)e.getX(),(int)e.getY()));
                        collection.add(p);
                        GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
                        mMapView.getGraphicsOverlays().add(graphicsOverlay);
                        graphicsOverlay.getGraphics().add(new Graphic(collection.get(collection.size()-1),s));
                        if (collection.size()==2){
                            //添加线元素
                            Polyline polyline = new Polyline(collection);
                            Graphic line = new Graphic(polyline,s2);
                            list_graphic.add(line);
                            graphicsOverlay.getGraphics().add(line);
                            //计算距离
                            GeodeticDistanceResult georesult = GeometryEngine.distanceGeodetic(collection.get(0),collection.get(1),new LinearUnit(LinearUnitId.METERS),new AngularUnit(AngularUnitId.DEGREES), GeodeticCurveType.GEODESIC);
                            double distance = georesult.getDistance();
                            result.setText("距离为:"+distance+"米");
                        }else if (collection.size() > 2){
                            //添加面元素
                            list_graphic.get(list_graphic.size()-1).setVisible(false);
                            Polygon polygon=new Polygon(collection);
                            Graphic fill = new Graphic(polygon, s3);
                            list_graphic.add(fill);
                            graphicsOverlay.getGraphics().add(fill);
                            //计算面积
                            //double area = GeometryEngine.areaGeodetic(list_graphic.get(list_graphic.size()-1).getGeometry(), new AreaUnit(AreaUnitId.SQUARE_METERS),GeodeticCurveType.GEODESIC);
                            String area = measure_area();
                            double mu = Double.parseDouble(area)*0.0015;
                            BigDecimal b = new BigDecimal(mu);
                            //保留小数点后两位
                            double mu1 = b.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
                            result.setText("面积为:"+area+"平方米/"+mu1+"亩");
                        }
                    }
                    return true;
                }
            });
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
                initViews();
                isNeedCheck = false;
            }else{
                Toast.makeText(MainActivity.this,permissions[i]+"拒绝授权",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
