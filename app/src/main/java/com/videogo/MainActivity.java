package com.videogo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
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
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.raster.Raster;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.TextSymbol;
import com.videogo.constant.IntentConsts;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.ui.realplay.EZRealPlayActivity;
import com.videogo.ui.util.EZUtils;
import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ezviz.ezopensdk.R;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
    private MapView mapView = null;
    private ImageButton change,info,info_sel,warning,robot,measure,measure_sel,zoom_in,zoom_out,position,position_sel;
    private TextView result;
    private ArcGISMap mMap;
    private String path;
    private Point point;
    private TextView title;
    private SharedPreferences sharedPreferences;
    private PointCollection collection ;
    private List<EZDeviceInfo> list_ezdevices;
    public LocationDisplay locationDisplay;
    private List<Graphic> list_graphic = new ArrayList<>();
    private List<GraphicsOverlay> list_graphicsOverlays = new ArrayList<>();
    private List<GraphicsOverlay> list_graphicsOverlays_info = new ArrayList<>();
    private GraphicsOverlay graphicsOverlay_info;

    public final static int REQUEST_CODE = 100;
    public final static int RESULT_CODE = 101;
    private final static int LOAD_MY_DEVICE = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }
    private void initViews() {
        mapView = (MapView) findViewById(R.id.map);
        change = findViewById(R.id.change_ibtn);
        info = findViewById(R.id.info_ibtn);
        info_sel = findViewById(R.id.info_ibtn_sel);
        warning = findViewById(R.id.warning_ibtn);
        robot = findViewById(R.id.robot_ibtn);
        measure = findViewById(R.id.measure_ibtn);
        measure_sel = findViewById(R.id.measure_ibtn_sel);
        zoom_in = findViewById(R.id.zoom_in_ibtn);
        zoom_out = findViewById(R.id.zoom_out_ibtn);
        position = findViewById(R.id.position_ibtn);
        result = findViewById(R.id.result);
        position_sel = findViewById(R.id.position_ibtn_sel);
        title = findViewById(R.id.title_tv);

        change.setOnClickListener(this);
        info.setOnClickListener(this);
        info_sel.setOnClickListener(this);
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
        list_ezdevices = new ArrayList<>();
        Log.i("TAG","path="+path);
        if (path.equals("")||path == null){
            path =Environment.getExternalStorageDirectory().getPath()+"/1.tif";
        }
        //加载tif
        loadlayer(path);
        list_ezdevices = getIntent().getParcelableArrayListExtra("devices_main");
        Log.i("TAG","list.size="+list_ezdevices.size());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.change_ibtn:
                selectfile();
                break;
            case R.id.info_ibtn:
                info.setVisibility(View.GONE);
                info_sel.setVisibility(View.VISIBLE);
                graphicsOverlay_info.setVisible(true);
                break;
            case R.id.info_ibtn_sel:
                info.setVisibility(View.VISIBLE);
                info_sel.setVisibility(View.GONE);
                graphicsOverlay_info.setVisible(false);
                break;
            case R.id.warning_ibtn:
                break;
            case R.id.robot_ibtn:
                break;
            case R.id.measure_ibtn:
                measure.setVisibility(View.GONE);
                measure_sel.setVisibility(View.VISIBLE);
                result.setVisibility(View.VISIBLE);
                title.setVisibility(View.GONE);
                break;
            case R.id.measure_ibtn_sel:
                measure.setVisibility(View.VISIBLE);
                measure_sel.setVisibility(View.GONE);
                result.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                collection.clear();
                list_graphic.clear();
                result.setText("");
                mapView.getGraphicsOverlays().removeAll(list_graphicsOverlays);
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
            ClearMap();
        }else if ((path.substring(path.indexOf(".")-1)).equals("1.tif")){
            path =Environment.getExternalStorageDirectory().getPath()+"/2.tif";
            loadlayer(path);
            mapView.getGraphicsOverlays().clear();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("earthPath", path);
            editor.commit();
            ClearMap();
        }
        list_graphic.clear();
        result.setText("");
        mapView.getGraphicsOverlays().clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                title.setText("南区");
            }else{
                title.setText("西区");
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
                    //String url = Environment.getExternalStorageDirectory().getPath()+"/camera.kml";
                    //String url2 = Environment.getExternalStorageDirectory().getPath()+"/info.kml";
                    String url = "camera.kml";
                    String url2 = "info.kml";
                    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
                    graphicsOverlay_info = new GraphicsOverlay();
                    mapView.getGraphicsOverlays().add(graphicsOverlay);
                    mapView.getGraphicsOverlays().add(graphicsOverlay_info);
                    PictureMarkerSymbol pictureMarkerSymbol = new PictureMarkerSymbol((BitmapDrawable) getResources().getDrawable(R.mipmap.marker));
                    SimpleLineSymbol simpleLineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID,Color.BLACK,2);
                    pictureMarkerSymbol.loadAsync();
                    try {
                        List<String> list_name = new ArrayList<>();
                        List<String> list_des = new ArrayList<>();
                        List<Point> list_point = new ArrayList<>();
                        List<String> list_name_info = new ArrayList<>();
                        List<String> list_des_info = new ArrayList<>();
                        List<PointCollection> list_collection = new ArrayList<>();
                        ReadKml readKml = new ReadKml(url,list_name,list_des,list_point,MainActivity.this);
                        readKml.parseKml();
                        ReadKml readKml1 = new ReadKml(url2,list_name_info,list_des_info,null,list_collection,MainActivity.this);
                        readKml1.parseKml();
                        for (int i = 0 ; i < list_point.size()  ; i++){
                            int finalI = i;
                            pictureMarkerSymbol.addDoneLoadingListener(new Runnable() {
                                @Override
                                public void run() {
                                    Map<String,Object> map = new HashMap<>();
                                    map.put("style","marker");
                                    map.put("name",list_name.get(finalI+2));
                                    map.put("des",list_des.get(finalI));
                                    Graphic ph = new Graphic(list_point.get(finalI), map, pictureMarkerSymbol);
                                    graphicsOverlay.getGraphics().add(ph);
                                    TextSymbol t = new TextSymbol(12f, list_name.get(finalI+2), Color.GREEN, TextSymbol.HorizontalAlignment.LEFT, TextSymbol.VerticalAlignment.TOP);
                                    Map<String,Object> map2 = new HashMap<>();
                                    map2.put("style","text");
                                    Graphic graphic_text = new Graphic(list_point.get(finalI),map2,t);
                                    graphicsOverlay.getGraphics().add(graphic_text);
                                }
                            });
                        }
                        for (int i = 0 ; i < list_collection.size() ; i++){
                            Polyline polyline = new Polyline(list_collection.get(i));
                            Map<String,Object> map = new HashMap<>();
                            map.put("style","line");
                            Graphic line = new Graphic(polyline,map,simpleLineSymbol);
                            graphicsOverlay_info.getGraphics().add(line);
                            TextSymbol textSymbol = new TextSymbol(12f, list_des_info.get(i), Color.BLACK, TextSymbol.HorizontalAlignment.CENTER, TextSymbol.VerticalAlignment.MIDDLE);
                            Map<String,Object> map2 = new HashMap<>();
                            map2.put("style","text");
                            Graphic ts = new Graphic(polyline,map2,textSymbol);
                            graphicsOverlay_info.getGraphics().add(ts);
                        }
                        info.setVisibility(View.GONE);
                        info_sel.setVisibility(View.VISIBLE);
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
                        SimpleFillSymbol s3 = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID,Color.argb(75,255,255,0),null);
                        Point p = mMapView.screenToLocation(new android.graphics.Point((int)e.getX(),(int)e.getY()));
                        collection.add(p);
                        GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
                        list_graphicsOverlays.add(graphicsOverlay);
                        mMapView.getGraphicsOverlays().add(graphicsOverlay);
                        Map<String,Object> map2 = new HashMap<>();
                        map2.put("style","point");
                        graphicsOverlay.getGraphics().add(new Graphic(collection.get(collection.size()-1),map2,s));
                        if (collection.size()==2){
                            //添加线元素
                            Polyline polyline = new Polyline(collection);
                            Map<String,Object> map = new HashMap<>();
                            map.put("style","line");
                            Graphic line = new Graphic(polyline,map,s2);
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
                            Map<String,Object> map = new HashMap<>();
                            map.put("style","polygon");
                            Graphic fill = new Graphic(polygon, map,s3);
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
                    }else{
                        //异步查询
                        android.graphics.Point screenPoint = new android.graphics.Point((int)e.getX(),(int)e.getY());
                        ListenableFuture<List<IdentifyGraphicsOverlayResult>> identifyFuture =mapView.identifyGraphicsOverlaysAsync(screenPoint,30,false,20);
                        identifyFuture.addDoneListener(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    List<IdentifyGraphicsOverlayResult> identifyLayerResults = identifyFuture.get();
                                    for (IdentifyGraphicsOverlayResult identifyGraphicsOverlayResult : identifyLayerResults){
                                        List<Graphic> graphics_resule = identifyGraphicsOverlayResult.getGraphics();
                                        for (Graphic graphic : graphics_resule){
                                            if (graphic.getGeometry().getGeometryType().toString().equals("POINT")){
                                                if (graphic.getAttributes().get("style").equals("marker")){
                                                    showDialog(graphic);
                                                }
                                            }
                                        }
                                    }
                                }catch (Exception e1){
                                    e1.printStackTrace();
                                }
                            }
                        });
                    }
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {

                    super.onLongPress(e);
                }
            });
        }
    }
    private void ClearMap(){
        list_graphic.clear();
        list_graphicsOverlays.clear();
        mapView.getGraphicsOverlays().clear();
    }
    /**
     * marker弹窗
     */
    private void showDialog(Graphic graphic){
        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.marker_dialog,null);
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setTitle("").setView(linearLayout).show();
        TextView tv_name = dialog.findViewById(R.id.name_tv);
        TextView tv_des = dialog.findViewById(R.id.des_tv);
        Button btn_open = dialog.findViewById(R.id.open);
        tv_name.setText(graphic.getAttributes().get("name").toString());
        tv_des.setText(graphic.getAttributes().get("des").toString());
        btn_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0 ; i < list_ezdevices.size() ; i++){
                    if (list_ezdevices.get(i).getDeviceName().equals(tv_name.getText())){
                        EZDeviceInfo deviceInfo = list_ezdevices.get(i);
                        if (deviceInfo.getCameraNum() == 1 && deviceInfo.getCameraInfoList() != null && deviceInfo.getCameraInfoList().size() == 1){
                            EZCameraInfo cameraInfo = EZUtils.getCameraInfoFromDevice(deviceInfo,0);
                            if (cameraInfo == null){
                                return;
                            }
                            Intent intent = new Intent(MainActivity.this , EZRealPlayActivity.class);
                            intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
                            intent.putExtra(IntentConsts.EXTRA_DEVICE_INFO, deviceInfo);
                            startActivityForResult(intent, REQUEST_CODE);
                            return;
                        }
                    }
                }
            }
        });
    }
}
