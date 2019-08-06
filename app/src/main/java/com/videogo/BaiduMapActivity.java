package com.videogo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
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
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import ezviz.ezopensdk.R;

public class BaiduMapActivity extends Activity implements View.OnClickListener {
    private MapView mapView = null;
    private BaiduMap mBaiduMap;
    private boolean isFirstLoc = true;
    private MapStatusUpdate mMapStatusUpdate;
    private LocationClient locationClient;
    private MyLocationListener myLocationListener;
    private LatLng latLng ;
    private ImageButton change,info,info_sel,warning,robot,measure,measure_sel,zoom_in,zoom_out,position,position_sel;
    private TextView result;
    private String style;
    private float angle;
    private BDLocation location;
    private MyOrientationListener myOrientationListener;
    private SharedPreferences sharedPreferences;
    private List<LatLng> points = new ArrayList<LatLng>();
    private List<Overlay> Overlays = new ArrayList<Overlay>();
    private List<Overlay> Overlays_info = new ArrayList<Overlay>();
    private List<OverlayOptions> options = new ArrayList<>();
    private List<OverlayOptions> options2 = new ArrayList<>();
    private List<String> list_name = new ArrayList<>();
    private List<String> list_des = new ArrayList<>();
    private List<Point> list_point = new ArrayList<>();
    private List<String> list_name_info = new ArrayList<>();
    private List<String> list_des_info = new ArrayList<>();
    private List<PointCollection> list_collection = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_baidu_map);
        initViews();
    }

    private void initViews() {
        mapView = findViewById(R.id.map);
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
        sharedPreferences = getSharedPreferences("style", 0);
        style = sharedPreferences.getString("mapstyle", "");
        String url = Environment.getExternalStorageDirectory().getPath()+"/camera.kml";
        String url2 = Environment.getExternalStorageDirectory().getPath()+"/info.kml";
        try {
            ReadKml readKml = new ReadKml(url,list_name,list_des,list_point);
            ReadKml readKml2 = new ReadKml(url2,list_name_info,list_des_info,null,list_collection);
            readKml.parseKml();
            readKml2.parseKml();
        }catch (Exception e){
            e.printStackTrace();
        }
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
        BaiduMapOptions options = new BaiduMapOptions();
        options.zoomGesturesEnabled(true);
        //初始化定位参数配置
        initLocation();
        //添加图标
        addMarker();
        //添加地块信息
        addInfo();

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

        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (measure_sel.getVisibility() == View.VISIBLE){
                    points.add(latLng);
                    //构建Marker图标
                    BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.point1);
                    //构建MarkerOption，用于在地图上添加Marker
                    OverlayOptions option = new MarkerOptions().position(latLng).icon(bitmap);
                    mBaiduMap.addOverlay(option);
                    if (points.size() == 2){
                        //添加线元素
                        OverlayOptions mOverlayOptions = new PolylineOptions().width(3).color(Color.BLACK).points(points);
                        Overlay mPolyline = mBaiduMap.addOverlay(mOverlayOptions);
                        Overlays.add(mPolyline);
                        //计算距离
                        double distance = DistanceUtil. getDistance(points.get(0), points.get(1));
                        result.setText("距离为:"+distance+"米");
                    }else if(points.size()>2){
                        //添加面元素
                        PolygonOptions mPolygonOptions = new PolygonOptions().points(points).fillColor(Color.argb(75,255,255,0)).stroke(new Stroke(0, R.color.simple_fill_color)); //边框宽度和颜色
                        Overlays.get(Overlays.size()-1).setVisible(false);
                        Overlay mPolygon = mBaiduMap.addOverlay(mPolygonOptions);
                        Overlays.add(mPolygon);
                        //计算面积
                        String area = measure_area();
                        double mu = Double.parseDouble(area)*0.0015;
                        BigDecimal b = new BigDecimal(mu);
                        //保留小数点后两位
                        double mu1 = b.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
                        result.setText("面积为:"+area+"平方米/"+mu1+"亩");
                    }
                }
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
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
            Log.i("TAG","latlng="+latLng);
            OverlayOptions marker_option = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker));
            OverlayOptions text_option = new TextOptions().text(list_name.get(i+2)).fontSize(25).position(latLng).fontColor(Color.GREEN);
            options.add(marker_option);
            options.add(text_option);
        }
        mBaiduMap.addOverlays(options);
    }

    /**
     *添加地块
     */
    private void addInfo(){
        for (int i = 0 ; i < list_collection.size() ; i++){
            List<LatLng> points = new ArrayList<LatLng>();
            for (int j = 0 ; j < list_collection.get(i).size() ; j++){
                //坐标转换
                CoordinateConverter converter  = new CoordinateConverter().from(CoordinateConverter.CoordType.GPS).coord(tramsform(list_collection.get(i).get(j)  ));
                LatLng latLng = converter.convert();
                points.add(latLng);
            }
            OverlayOptions mOverlayOptions = new PolylineOptions().width(6).color(Color.BLACK).points(points);
            options2.add(mOverlayOptions);
            BitmapDescriptor bitmapDescriptor = stringToBitmapDescriptor(list_des_info.get(i));
            OverlayOptions option = new MarkerOptions().icon(bitmapDescriptor).position(getInterPosition(points));
            options2.add(option);
        }
        Overlays_info = mBaiduMap.addOverlays(options2);
        info.setVisibility(View.GONE);
        info_sel.setVisibility(View.VISIBLE);
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
                info.setVisibility(View.GONE);
                info_sel.setVisibility(View.VISIBLE);
                for (Overlay overlay : Overlays_info){
                        overlay.setVisible(true);
                };
                break;
            case R.id.info_ibtn_sel:
                info.setVisibility(View.VISIBLE);
                info_sel.setVisibility(View.GONE);
                for (Overlay overlay : Overlays_info){
                    overlay.setVisible(false);
                };
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
                points.clear();
                Overlays.clear();
                result.setText("");
                mBaiduMap.clear();
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