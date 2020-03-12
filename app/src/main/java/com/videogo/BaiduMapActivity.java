package com.videogo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.DistanceUtil;
import com.esri.core.geometry.Point;
import com.videogo.been.StyleId;
import com.videogo.been.StyleMap;
import com.videogo.constant.IntentConsts;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.ui.realplay.EZRealPlayActivity;
import com.videogo.ui.util.EZUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import ezviz.ezopensdk.R;


public class BaiduMapActivity extends Activity implements View.OnClickListener {
    private MapView mapView = null;
    private BaiduMap mBaiduMap;
    private boolean isFirstLoc = true;
    private MapStatusUpdate mMapStatusUpdate;
    private LocationClient locationClient;
    private MyLocationListener myLocationListener;
    private LatLng latLng ;
    private ImageButton change,info,robot,measure,measure_sel,zoom_in,zoom_out,position,position_sel;
    private TextView result;
    private String style;
    private float angle;
    private BDLocation location;
    private MyOrientationListener myOrientationListener;
    private SharedPreferences sharedPreferences;
    private List<LatLng> points = new ArrayList<LatLng>();
    private List<Overlay> Overlays = new ArrayList<Overlay>();
    private List<Overlay> Overlays_info = new ArrayList<Overlay>();
    private List<Overlay> Overlays_warning = new ArrayList<Overlay>();
    private List<Overlay> Overlays_camera = new ArrayList<Overlay>();
    private List<Overlay> os = new ArrayList<>();
    private List<OverlayOptions> options = new ArrayList<>();
    private List<OverlayOptions> options2 = new ArrayList<>();
    private List<OverlayOptions> options3 = new ArrayList<>();
    private List<OverlayOptions> options_text = new ArrayList<>();
    private List<String> list_name = new ArrayList<>();
    private List<String> list_des = new ArrayList<>();
    private List<Point> list_point = new ArrayList<>();
    private List<String> list_name_info = new ArrayList<>();
    private List<String> list_des_info = new ArrayList<>();
    private List<String> list_name_warning = new ArrayList<>();
    private List<String> list_des_warning = new ArrayList<>();
    private List<List<Point>> list_collection_warning = new ArrayList<>();
    private List<List<Point>> list_collection = new ArrayList<>();
    private List<EZDeviceInfo> list_ezdevices = new ArrayList<>();
    private Boolean show_camera = false;
    private Boolean show_info = false;
    private Boolean show_warning = true;
    private Boolean show_text = false;
    private String TAG = "baiduMapActivity";
    public final static int REQUEST_CODE = 100;
    public final static int RESULT_CODE = 101;
    private final static int LOAD_MY_DEVICE = 0;
    private int mLoadType = LOAD_MY_DEVICE;
    private ExecutorService threadPoolExecutor;
    private ExecutorService threadPoolExecutor_2;
    private String url = "camera.kml";
    private String url2 = "info.kml";
    private String url3 = "违章种植.kml";
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 101:
                    //添加图标
                    addMarker();
                    break;
//                case 102:
//                    Bundle bundle = msg.getData();
//                    String linewidth = bundle.getString("linewidth");
//                    String linecolor = bundle.getString("linecolor");
//                    String des_info = bundle.getString("des_info");
//                    List<LatLng> points  = new ArrayList<>();
//                    points = bundle.getParcelableArrayList("points");
//                    Log.d(TAG,"linewidth="+linewidth);
//                    Log.d(TAG,"linecolor="+linecolor);
//                    Log.d(TAG,"des_info="+des_info);
//                    Log.d(TAG,"points.size="+points.size());
//
//                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_baidu_map);
        initViews();
    }

    private void initViews() {
        mapView = findViewById(R.id.map);
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
        threadPoolExecutor = Executors.newFixedThreadPool(5);
        threadPoolExecutor_2 = Executors.newFixedThreadPool(5);
        sharedPreferences = getSharedPreferences("style", 0);
        style = sharedPreferences.getString("mapstyle", "");
        list_ezdevices = getIntent().getParcelableArrayListExtra("devices_baidu");
        // 不显示缩放比例尺
        mapView.showZoomControls(false);
        // 不显示百度地图Logo
        mapView.removeViewAt(1);
        //初始化位置
        mBaiduMap = mapView.getMap();
        //打开交通图
        mBaiduMap.setTrafficEnabled(false);
        UiSettings settings = mBaiduMap.getUiSettings();
        settings.setCompassEnabled(false);
        settings.setOverlookingGesturesEnabled(false);
        if (style.equals("NORMAL")||style.equals("")||style==null){
            //标准地图
            style = "NORMAL";
            mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        }else{
            //卫星地图
            mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
        }
        mBaiduMap.setMyLocationEnabled(true);
        //初始位置
        //LatLng cenpt = new LatLng(33.935681, 118.289365);
        LatLng cenpt = new LatLng(33.92287826538086, 118.19874572753906);
        MapStatus mMapStatus = new MapStatus.Builder().target(cenpt).zoom(13).build();
        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        //改变地图状态
        mBaiduMap.setMapStatus(mMapStatusUpdate);
        BaiduMapOptions options_statue = new BaiduMapOptions();
        options_statue.zoomGesturesEnabled(true);
        //初始化定位参数配置
        initLocation();
        //添加地块信息
        //addInfo(url2);
        //添加报警信息
        //addWarning();
        addinfo();
        change.setOnClickListener(this);
        info.setOnClickListener(this);
        robot.setOnClickListener(this);
        measure.setOnClickListener(this);
        measure_sel.setOnClickListener(this);
        zoom_in.setOnClickListener(this);
        zoom_out.setOnClickListener(this);
        position.setOnClickListener(this);
        position_sel.setOnClickListener(this);

        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (measure_sel.getVisibility() == View.VISIBLE){
                    points.add(latLng);
                    //构建Marker图标
                    BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.point1);
                    //构建MarkerOption，用于在地图上添加Marker
                    OverlayOptions option = new MarkerOptions().position(latLng).icon(bitmap);
                    Overlay o = mBaiduMap.addOverlay(option);
                    os.add(o);
                    if (points.size() == 2){
                        //添加线元素
                        OverlayOptions mOverlayOptions = new PolylineOptions().width(3).color(Color.BLACK).points(points);
                        Overlay mPolyline = mBaiduMap.addOverlay(mOverlayOptions);
                        Overlays.add(mPolyline);
                        //隐藏marker图标
                        os.get(0).setVisible(false);
                        o.setVisible(false);
                        //计算距离
                        double distance = DistanceUtil. getDistance(points.get(0), points.get(1));
                        BigDecimal b = new BigDecimal(distance);
                        //保留小数点后两位
                        double distances = b.setScale(4,BigDecimal.ROUND_HALF_UP).doubleValue();
                        result.setText("距离为:"+distances+"米");
                    }else if(points.size()>2){
                        //添加面元素
                        PolygonOptions mPolygonOptions = new PolygonOptions().points(points).fillColor(Color.argb(75,255,255,0)).stroke(new Stroke(0, R.color.simple_fill_color)); //边框宽度和颜色
                        Overlays.get(Overlays.size()-1).setVisible(false);
                        Overlay mPolygon = mBaiduMap.addOverlay(mPolygonOptions);
                        Overlays.add(mPolygon);
                        o.setVisible(false);
                        //计算面积
                        String area = measure_area();
                        double mu = Double.parseDouble(area)*0.0015;
                        BigDecimal b = new BigDecimal(mu);
                        //保留小数点后两位
                        double mu1 = b.setScale(4,BigDecimal.ROUND_HALF_UP).doubleValue();
                        result.setText("面积为:"+area+"平方米/"+mu1+"亩");
                    }
                }else{

                }
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Bundle bundle = marker.getExtraInfo();
                Log.d("TAG","Bundel="+bundle);
                if (bundle!=null){
                    showDialog(bundle.getString("name"),bundle.getString("des"));
                }
                return false;
            }
        });
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                float zoom = mapStatus.zoom;
                if (zoom >= 16 && !show_text){
                    mBaiduMap.addOverlays(options_text);
                    show_text = true;
                }else if (zoom < 16 && show_text){
                    mBaiduMap.clear();
                    if (!show_camera){
                        mBaiduMap.addOverlays(options);
                    }
                    if (!show_info){
                        mBaiduMap.addOverlays(options2);
                    }
                    if (!show_warning){
                        mBaiduMap.addOverlays(options3);
                    }
                    show_text = false;
                }
            }
        });
    }
    private void addinfo(){
        try {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    ReadKml readKml = new ReadKml(url,list_name,list_des,list_point,getApplicationContext());
                    ReadKml readKml_warning = new ReadKml(url3,list_name_warning,list_des_warning,null,list_collection_warning,getApplicationContext());
                    readKml.parseKml2();
                    readKml_warning.parseKml2();
                    handler.sendEmptyMessage(101);
                }
            };
            threadPoolExecutor.execute(runnable);
            Runnable runnable1 = new Runnable() {
                @Override
                public void run() {
                    List<String> list_name_info = new ArrayList<>();
                    List<String> list_des_info = new ArrayList<>();
                    List<List<Point>> list_collection = new ArrayList<>();
                    List<StyleId> list_styleid = new ArrayList<>();
                    List<StyleMap> list_stylemap = new ArrayList<>();
                    List<String> list_style_url = new ArrayList<>();
                    ReadKml readKml = new ReadKml(url2,list_name_info,list_des_info,null,list_collection,list_styleid,list_stylemap,list_style_url,getApplicationContext());
                    readKml.parseKml();
                    for (int i = 0 ; i < list_collection.size() ; i++){
                        List<LatLng> points = new ArrayList<LatLng>();
                        for (int j = 0 ; j < list_collection.get(i).size() ; j++){
                            //坐标转换
                            CoordinateConverter converter  = new CoordinateConverter().from(CoordinateConverter.CoordType.GPS).coord(tramsform(list_collection.get(i).get(j)  ));
                            LatLng latLng = converter.convert();
                            points.add(latLng);
                        }
                        String url = list_style_url.get(i);
                        String linecolor="";
                        String linewidth="";
                        for (StyleMap styleMap : list_stylemap){
                            if (styleMap.getId().equals(url)){
                                String stylemapUrl = styleMap.getStyleUrl();
                                for (StyleId styleid : list_styleid){
                                    if (styleid.getId().equals(stylemapUrl)){
                                        linecolor = styleid.getLineColor();
                                        linewidth = styleid.getLineWidth();
                                    }
                                }
                            }
                        }

                        String des_info = list_des_info.get(i);
                        OverlayOptions mOverlayOptions = new PolylineOptions().width(Integer.parseInt(linewidth)).color(Color.parseColor("#"+linecolor)).points(points);
                        options2.add(mOverlayOptions);
                        BitmapDescriptor bitmapDescriptor = stringToBitmapDescriptor(des_info);
                        OverlayOptions option = new MarkerOptions().icon(bitmapDescriptor).position(getInterPosition(points));
                        options_text.add(option);
//                        Message message = Message.obtain();
//                        message.what = 102;
//                        Bundle bundle = new Bundle();
//                        bundle.putString("linewidth",linewidth);
//                        bundle.putString("linecolor",linecolor);
//                        bundle.putString("des_info",des_info);
//                        bundle.putParcelableArrayList("points", (ArrayList<? extends Parcelable>) points);
//                        message.setData(bundle);
//                        handler.sendMessage(message);
                    }
                    Overlays_info = mBaiduMap.addOverlays(options2);
                }
            };
            threadPoolExecutor_2.execute(runnable1);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * marker弹窗
     */
    private void showDialog(String name , String des){
        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.marker_dialog,null);
        AlertDialog dialog = new AlertDialog.Builder(BaiduMapActivity.this).setTitle("").setView(linearLayout).show();
        TextView tv_name = dialog.findViewById(R.id.name_tv);
        TextView tv_des = dialog.findViewById(R.id.des_tv);
        Button btn_open = dialog.findViewById(R.id.open);
        tv_name.setText(name);
        tv_des.setText(des);
        btn_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (EZDeviceInfo ezDeviceInfo : list_ezdevices){
                    for (EZCameraInfo ezCameraInfo : ezDeviceInfo.getCameraInfoList()){
                        if (ezCameraInfo == null){
                            return;
                        }else if(ezCameraInfo.getCameraName().equals(tv_name.getText())){
                            Intent intent = new Intent(BaiduMapActivity.this , EZRealPlayActivity.class);
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
    /**
     * 添加图标
     */
    private void addMarker() {
        for (int i  = 0 ; i < list_point.size() ; i++){
            //坐标转换
            CoordinateConverter converter  = new CoordinateConverter().from(CoordinateConverter.CoordType.GPS).coord(tramsform(list_point.get(i)));
            LatLng latLng = converter.convert();
            Bundle bundle = new Bundle();
            bundle.putString("name",list_name.get(i+2));
            bundle.putString("des",list_des.get(i));
            OverlayOptions marker_option = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker)).extraInfo(bundle);
            OverlayOptions text_option = new TextOptions().text(list_name.get(i+2)).fontSize(25).position(latLng).fontColor(Color.GREEN);
            options.add(marker_option);
            options.add(text_option);
        }
        Overlays_camera =  mBaiduMap.addOverlays(options);
    }

    /**
     * 添加报警信息
     */
    private void addWarning(){
        for (int i = 0 ; i < list_collection_warning.size() ; i++){
            List<LatLng> points = new ArrayList<LatLng>();
            for (int j = 0 ; j < list_collection_warning.get(i).size() ; j++){
                //坐标转换
                CoordinateConverter converter  = new CoordinateConverter().from(CoordinateConverter.CoordType.GPS).coord(tramsform(list_collection_warning.get(i).get(j)  ));
                LatLng latLng = converter.convert();
                points.add(latLng);
            }
            OverlayOptions mOverlayOptions = new PolylineOptions().width(6).color(Color.WHITE).points(points);
            options3.add(mOverlayOptions);
            BitmapDescriptor bitmapDescriptor = stringToBitmapDescriptor(list_des_warning.get(i));
            OverlayOptions option = new MarkerOptions().icon(bitmapDescriptor).position(getInterPosition(points));
            options3.add(option);
        }
        Overlays_warning = mBaiduMap.addOverlays(options3);
        for (Overlay overlay : Overlays_warning){
            overlay.setVisible(false);
        };
    }

    private LatLng tramsform(Point p ){
        LatLng latLng = new LatLng(p.getY(),p.getX());
        return latLng;
    }

    /**
     * String to Bitmap
     * @param string
     * @return
     */
    public BitmapDescriptor stringToBitmapDescriptor(String string) {
        TextView textView = new TextView(this);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(10);
        textView.setTextColor(Color.BLACK);
        textView.setShadowLayer(0, 0, 0, Color.BLACK);
        textView.setText(string);
        textView.destroyDrawingCache();
        textView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());
        textView.setDrawingCacheEnabled(true);
        Bitmap bitmapText = textView.getDrawingCache(true);
        BitmapDescriptor bd = BitmapDescriptorFactory.fromBitmap(bitmapText);
        return bd;
    }
    /**
     * 获取Points集合中心点
     * @param points
     * @return
     */
    private LatLng getInterPosition(List<LatLng> points){
        double x = 0.0, y = 0.0;
        for (int  i = 0 ; i < points.size() ; i++){
            x += points.get(i).latitude;
            y += points.get(i).longitude;
        }
        LatLng latLng = new LatLng(x/points.size(),y/points.size());
        return latLng;
    }
    private void initLocation() {
        //方向传感器
        myOrientationListener = new MyOrientationListener(this);
        myOrientationListener.setmOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                angle = x;
            }
        });
        locationClient = new LocationClient(this);
        LocationClientOption option = new LocationClientOption();
        myLocationListener = new MyLocationListener();
        locationClient.registerLocationListener(myLocationListener);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setIsNeedAddress(true);
        option.setIsNeedLocationDescribe(true);
        option.setIsNeedLocationPoiList(true);
        option.setScanSpan(1000);
        option.setOpenGps(true);
        locationClient.setLocOption(option);
        myOrientationListener.star();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.change_ibtn:
                selectfile();
                break;
            case R.id.info_ibtn:
                if (show_info){
                    mBaiduMap.addOverlays(options2);
                    mBaiduMap.addOverlays(options_text);
                    info.setBackgroundResource(R.mipmap.xinxi_sel);
                    show_info = false;
                    show_text = true;
                }else{
                    mBaiduMap.clear();
                    clearMap();
                    if (!show_camera){
                        mBaiduMap.addOverlays(options);
                    }
//                    if (show_text){
//                        mBaiduMap.addOverlays(options_text);
//                    }
                    info.setBackgroundResource(R.mipmap.xinxi);
                    show_info = true;
                    show_text = false;
                }
                break;
            case R.id.robot_ibtn:
                if (show_camera){
                    mBaiduMap.addOverlays(options);
                    robot.setBackgroundResource(R.mipmap.jiqiren_sel);
                    show_camera = false;
                }else{
                    mBaiduMap.clear();
                    clearMap();
                    if (!show_info){
                        mBaiduMap.addOverlays(options2);
                    }
                    if (show_text){
                        mBaiduMap.addOverlays(options_text);
                    }
                    robot.setBackgroundResource(R.mipmap.jiqiren);
                    show_camera = true;
                }
                break;
            case R.id.measure_ibtn:
                measure.setVisibility(View.GONE);
                measure_sel.setVisibility(View.VISIBLE);
                result.setVisibility(View.VISIBLE);
                break;
            case R.id.measure_ibtn_sel:
                for (Overlay overlay : Overlays){
                    overlay.setVisible(false);
                };
                for (Overlay overlay :os){
                    overlay.setVisible(false);
                };
                measure.setVisibility(View.VISIBLE);
                measure_sel.setVisibility(View.GONE);
                result.setVisibility(View.GONE);
                clearMap();
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

    private void clearMap(){
        points.clear();
        Overlays.clear();
        os.clear();
        result.setText("");
    }
    /**
     *计算面积
     */
    public String measure_area(){
        DecimalFormat df = new DecimalFormat("0.000");
        List<double[]> list_point = new ArrayList<double[]>();
        double earthRadiusMeters = 6378137.0;
        double metersPerDegree = 2.0 * Math.PI * earthRadiusMeters / 360.0;
        double radiansPerDegree = Math.PI / 180.0;
        String pt = "";
        //获取图形，并提示
        for ( int i = 0 ; i < points.size() ; i++){
            if ((i+1) >= points.size()){
                if ((i+1) == points.size()){
                    pt = pt +points.get(i).latitude + "," +points.get(i).longitude +",";
                }
            }else{
                if (points.get(i).latitude == points.get(i+1).latitude && points.get(i).longitude == points.get(i+1).longitude){

                }else{
                    pt = pt + points.get(i).latitude + "," + points.get(i).longitude + ",";
                }
            }
        }
        String pp = pt.substring(0, pt.length() - 1);
        String[] pp1 = pp.split(";");
        for (String ppap : pp1) {
            String[] temp = ppap.split(",");
            for (int i = 0; i < temp.length; ) {
                double[] point = {Double.parseDouble(temp[i]), Double.parseDouble(temp[i + 1])};
                list_point.add(point);
                i = i + 2;
            }
        }
        //经纬度计算多边形面积
        double a = 0.0;
        for (int i = 0; i < list_point.size(); ++i) {
            int j = (i + 1) % list_point.size();
            double xi = list_point.get(i)[0] * metersPerDegree * Math.cos(list_point.get(i)[1] * radiansPerDegree);
            double yi = list_point.get(i)[1] * metersPerDegree;
            double xj = list_point.get(j)[0] * metersPerDegree * Math.cos(list_point.get(j)[1] * radiansPerDegree);
            double yj = list_point.get(j)[1] * metersPerDegree;
            a += xi * yj - xj * yi;
        }
        double s = Math.abs(a / 2.0);
        return df.format(s);
    }
    /**
     * 开始定位
     */
    private void Location() {
        position.setVisibility(View.GONE);
        position_sel.setVisibility(View.VISIBLE);
        if (isFirstLoc){
            locationClient.requestLocation();
            locationClient.start();
        }else{
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(new LatLng(location.getLatitude(),location.getLongitude())).build();
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }
    }

    /**
     * 停止定位
     */
    private void StopLocation() {
            position.setVisibility(View.VISIBLE);
            position_sel.setVisibility(View.GONE);
            MapStatus.Builder builder = new MapStatus.Builder();
            LatLng cenpt = new LatLng(33.935681, 118.289365);
            builder.target(cenpt).zoom(13).build();
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            location = locationClient.getLastKnownLocation();
    }

    /**
     * 放大
     */
    private void ZoomOut() {
        MapStatusUpdate zoomOut = MapStatusUpdateFactory.zoomOut();
        mBaiduMap.setMapStatus(zoomOut);
    }

    /**
     * 缩小
     */
    private void ZoomIn() {
        MapStatusUpdate zoomIn = MapStatusUpdateFactory.zoomIn();
        mBaiduMap.setMapStatus(zoomIn);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        threadPoolExecutor.shutdown();
        threadPoolExecutor_2.shutdown();
        handler.removeCallbacksAndMessages(null);
        locationClient.stop();
        myOrientationListener.stop();
        mapView.onDestroy();
    }
    /**
     *地图切换
     */
    private void selectfile() {
        if (style.equals("NORMAL")){
            //切换成卫星地图
            SharedPreferences.Editor editor3 = sharedPreferences.edit();
            editor3.putString("mapstyle", "SATELLITE");
            editor3.commit();
            mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
            style = "SATELLITE";
        }else if (style.equals("SATELLITE")){
            //切换成标准地图
            SharedPreferences.Editor editor3 = sharedPreferences.edit();
            editor3.putString("mapstyle", "NORMAL");
            editor3.commit();
            mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
            style = "NORMAL";
        }
    }
    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
            String coorType = bdLocation.getCoorType();
            latLng = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
            // 构造定位数据
            MyLocationData locdata = new MyLocationData.Builder()
                    .direction(angle)
                    .accuracy(100)
                    .latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude()).build();
            // 设置定位数据
            mBaiduMap.setMyLocationData(locdata);
            //配置定位图层显示方式,三个参数的构造器
            MyLocationConfiguration configuration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,true,null);
            mBaiduMap.setMyLocationConfiguration(configuration);
            if(isFirstLoc){
                isFirstLoc = false;
                LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(status);
            }
        }
    }
}
