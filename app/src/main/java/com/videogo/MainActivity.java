package com.videogo;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.RasterLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.map.event.OnZoomListener;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.core.geometry.AreaUnit;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.raster.FileRasterSource;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.videogo.been.StyleId;
import com.videogo.been.StyleMap;
import com.videogo.constant.IntentConsts;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.ui.realplay.EZRealPlayActivity;
import com.videogo.ui.util.CopyFontFile;
import com.videogo.ui.util.EZUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ezviz.ezopensdk.R;


public class MainActivity extends FragmentActivity implements View.OnClickListener {
    private MapView mapView = null;
    private ImageButton change,info,robot,measure,measure_sel,zoom_in,zoom_out,position,position_sel;
    private TextView result;
    private String path;
    private TextView title;
    private SharedPreferences sharedPreferences;
    private List<EZDeviceInfo> list_ezDevices;
    private List<Graphic> list_graphic = new ArrayList<>();
    private List<Point> pointList = new ArrayList<>();
    private GraphicsLayer graphicsLayer;
    private GraphicsLayer graphicsLayer_camera;
    private GraphicsLayer graphicsLayer_info;
    private GraphicsLayer graphicsLayer_info_text;
    private GraphicsLayer graphicsLayer_warning;
    private LocationDisplayManager locationDisplayManager = null;
    public final static int REQUEST_CODE = 100;


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
        CopyFontFile mCopyData_File = new CopyFontFile(this);
        mCopyData_File.DoCopy();
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

        sharedPreferences = getSharedPreferences("path", 0);
        path = sharedPreferences.getString("earthPath", "");
        list_ezDevices = new ArrayList<>();
        list_ezDevices = getIntent().getParcelableArrayListExtra("devices_main");
        if (path.equals("")||path == null){
            path =Environment.getExternalStorageDirectory().getPath()+"/1.tif";
            //path =Environment.getExternalStorageDirectory().getPath()+"/z_tangxunfu_GPS/tangxunhu(3).tif";
        }
        //加载tif
        loadlayer(path);
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.change_ibtn:
                selectfile();
                break;
            case R.id.info_ibtn:
                if (graphicsLayer_info.isVisible()){
                    graphicsLayer_info.setVisible(false);
                    if (mapView.getScale()<30000){
                        graphicsLayer_info_text.setVisible(false);
                    }
                    info.setBackgroundResource(R.mipmap.xinxi);
                }else{
                    graphicsLayer_info.setVisible(true);
                    if (mapView.getScale()<30000){
                        graphicsLayer_info_text.setVisible(true);
                    }
                    info.setBackgroundResource(R.mipmap.xinxi_sel);
                }
                break;
            case R.id.robot_ibtn:
                if (graphicsLayer_camera.isVisible()){
                    graphicsLayer_camera.setVisible(false);
                    robot.setBackgroundResource(R.mipmap.jiqiren);
                }else{
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
                if (!locationDisplayManager.isStarted()){
                    locationDisplayManager.start();
                    position.setVisibility(View.GONE);
                    position_sel.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                double la = locationDisplayManager.getLocation().getLatitude();
                                double ln = locationDisplayManager.getLocation().getLongitude();
                                Point p = new Point(ln,la);
                                Envelope e = mapView.getMaxExtent();
                                if (!e.contains(p)){
                                    locationDisplayManager.stop();
                                    position.setVisibility(View.VISIBLE);
                                    position_sel.setVisibility(View.GONE);
                                    ToastNotRepeat.show(MainActivity.this,"超出地图范围！");
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    },1500);
                }
                break;
            case R.id.position_ibtn_sel:
                if (locationDisplayManager.isStarted()){
                    locationDisplayManager.stop();
                    position.setVisibility(View.VISIBLE);
                    position_sel.setVisibility(View.GONE);
                }
                break;
        }
    }
    /**
     *地图切换
     */
    private void selectfile() {
        if (path.equals("")||path==null||(path.substring(path.indexOf(".")-1)).equals("2.tif")){
            path =Environment.getExternalStorageDirectory().getPath()+"/1.tif";
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("earthPath", path);
            editor.commit();
        }else if ((path.substring(path.indexOf(".")-1)).equals("1.tif")){
            path =Environment.getExternalStorageDirectory().getPath()+"/2.tif";
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("earthPath", path);
            editor.commit();
        }
        finish();
        Intent i = new Intent(MainActivity.this, MainActivity.class);
        i.putParcelableArrayListExtra("devices_main", (ArrayList<? extends Parcelable>) list_ezDevices);
        startActivity(i);
        overridePendingTransition(0, 0);
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
        String url2 = "";
        if (!file.exists()){
            ToastNotRepeat.show(this,"文件不存在！");
        }else{
            if ((path.substring(path.indexOf(".")-1)).equals("2.tif")){
                title.setText("南区");
                url2 = "南区.kml";
            }else{
                title.setText("西区");
                url2 = "西区.kml";
            }
            FileRasterSource rasterSource = null;
            try {
                rasterSource = new FileRasterSource(path);
                rasterSource.project(mapView.getSpatialReference());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            RasterLayer rasterLayer = new RasterLayer(rasterSource);
            mapView.addLayer(rasterLayer);
            mapView.addLayer(rasterLayer);
            graphicsLayer = new GraphicsLayer();
            graphicsLayer_camera = new GraphicsLayer();
            graphicsLayer_info = new GraphicsLayer();
            graphicsLayer_info_text = new GraphicsLayer();
            graphicsLayer_warning = new GraphicsLayer();
            mapView.addLayer(graphicsLayer);
            mapView.addLayer(graphicsLayer_camera);
            mapView.addLayer(graphicsLayer_info);
            mapView.addLayer(graphicsLayer_info_text);
            mapView.addLayer(graphicsLayer_warning);
            graphicsLayer_info_text.setVisible(false);
            String finalUrl = url2;
            rasterLayer.setOnStatusChangedListener(new OnStatusChangedListener() {
                @Override
                public void onStatusChanged(Object o, STATUS status) {
                    if (status == STATUS.INITIALIZED){
                        Log.i("TAG","加载成功");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String url = "camera.kml";
                                String url3 = "违章种植.kml";
                                loadCamera(url);
                                loadinfo(finalUrl);
                                //loadwarning(url3);
                            }
                        }).start();
                    }else if(status == STATUS.INITIALIZATION_FAILED || status == STATUS.LAYER_LOADING_FAILED){
                        Log.i("TAG","加载失败");
                    }
                }
            });

            /**
             * mapview监听
             */
            mapView.setOnSingleTapListener(new OnSingleTapListener() {
                @Override
                public void onSingleTap(float v, float v1) {
                    if (measure_sel.getVisibility() == View.VISIBLE){
                        Point p = mapView.toMapPoint(v,v1);
                        pointList.add(p);
                        //点，线，面样式
                        SimpleMarkerSymbol simpleMarkerSymbol = new SimpleMarkerSymbol(Color.BLACK, 6, SimpleMarkerSymbol.STYLE.CIRCLE);
                        SimpleLineSymbol simpleLineSymbol = new SimpleLineSymbol(Color.BLACK, 2);
                        SimpleFillSymbol simpleFillSymbol = new SimpleFillSymbol(Color.YELLOW);
                        simpleFillSymbol.setAlpha(90);
                        simpleFillSymbol.setOutline(new SimpleLineSymbol(Color.argb(0,0,0,0),1));
                        if (pointList.size() == 1){
                            Graphic point = new Graphic(p,simpleMarkerSymbol);
                            graphicsLayer.addGraphic(point);
                        }else if (pointList.size() == 2){
                            graphicsLayer.removeAll();
                            Polyline polyline = new Polyline();
                            polyline.startPath(pointList.get(0));
                            polyline.lineTo(p);
                            Graphic line = new Graphic(polyline,simpleLineSymbol);
                            graphicsLayer.addGraphic(line);
                            double distance = GeometryEngine.geodesicDistance(pointList.get(0),pointList.get(1),mapView.getSpatialReference(),new LinearUnit(LinearUnit.Code.METER));
                            double distance_2 = new BigDecimal(distance).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
                            result.setText("距离为:"+distance_2+"米");
                        }else if(pointList.size() > 2){
                            graphicsLayer.removeAll();
                            Polygon polygon = new Polygon();
                            polygon.startPath(pointList.get(0));
                            for (int i = 1 ; i < pointList.size() ; i++){
                                polygon.lineTo(pointList.get(i));
                            }
                            Graphic gon = new Graphic(polygon,simpleFillSymbol);
                            graphicsLayer.addGraphic(gon);
                            double area = GeometryEngine.geodesicArea(polygon,mapView.getSpatialReference(),new AreaUnit(AreaUnit.Code.SQUARE_METER));
                            double area_2 = new BigDecimal(area).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
                            double mu = area*0.0015;
                            BigDecimal b = new BigDecimal(mu);
                            //保留小数点后两位
                            double mu_2 = b.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
                            result.setText("面积为:"+area_2+"平方米/"+mu_2+"亩");
                        }
                    }else{
                        int[] objectIds = graphicsLayer_camera.getGraphicIDs(v,v1,20);
                        if (objectIds != null && objectIds.length > 0){
                            for (int i = 0 ; i < objectIds.length ; i++){
                                Graphic graphic = graphicsLayer_camera.getGraphic(objectIds[i]);
                                if (graphic.getAttributes().get("style").equals("marker")){
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
                    Log.i("TAG","scale="+mapView.getScale());
                    if (mapView.getScale() > 30000){
                        graphicsLayer_info_text.setVisible(false);
                        Log.i("TAG","1");
                    }else{
                        graphicsLayer_info_text.setVisible(true);
                        Log.i("TAG","2");
                    }
                }
            });
        }
    }

    /**
     * 加载机器人信息
     * @param url
     */
    private void loadCamera(String url){
        PictureMarkerSymbol pictureMarkerSymbol = new PictureMarkerSymbol((BitmapDrawable) getResources().getDrawable(R.mipmap.marker_1));
        List<String> list_name = new ArrayList<>();
        List<String> list_des = new ArrayList<>();
        List<Point> list_point = new ArrayList<>();
        ReadKml readKml = new ReadKml(url,list_name,list_des,list_point,MainActivity.this);
        readKml.parseKml2();
        for (int i = 0 ; i < list_point.size()  ; i++){
            int finalI = i;
            Map<String,Object> map = new HashMap<>();
            map.put("style","marker");
            map.put("name",list_name.get(finalI+2));
            map.put("des",list_des.get(finalI));
            Graphic pointGraphic = new Graphic(list_point.get(i), pictureMarkerSymbol,map);
            graphicsLayer_camera.addGraphic(pointGraphic);
            TextSymbol t = new TextSymbol(12, list_name.get(finalI+2), Color.GREEN);
            t.setFontFamily(new File(CopyFontFile.FONT_PATH).getPath());
            t.setOffsetX(-10);
            t.setOffsetY(-22);
            Map<String,Object> map2 = new HashMap<>();
            map2.put("style","text");
            Graphic graphic_text = new Graphic(list_point.get(finalI),t,map2);
            graphicsLayer_camera.addGraphic(graphic_text);
        }
    }

    /**
     * 加载地块信息
     * @param url2
     */
    private void loadinfo(String url2){
        List<String> list_name_info = new ArrayList<>();
        List<String> list_des_info = new ArrayList<>();
        List<List<Point>> list_collection = new ArrayList<>();
        List<StyleId> list_styleid = new ArrayList<>();
        List<StyleMap> list_stylemap = new ArrayList<>();
        List<String> list_style_url = new ArrayList<>();
        ReadKml readKml1 = new ReadKml(url2,list_name_info,list_des_info,null,list_collection,list_styleid,list_stylemap,list_style_url,MainActivity.this);
        readKml1.parseKml();
        //线型
        for (int i = 0 ; i < list_collection.size() ; i++){
            Polyline polyline = new Polyline();
            polyline.startPath(list_collection.get(i).get(0));
            for (int j = 1 ; j < list_collection.get(i).size() ; j++){
                polyline.lineTo(list_collection.get(i).get(j));
            }
            Map<String,Object> map = new HashMap<>();
            map.put("style","line");
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
            SimpleLineSymbol simpleLineSymbol_info = new SimpleLineSymbol(Color.parseColor("#"+linecolor), Integer.parseInt(linewidth));
            Graphic line = new Graphic(polyline,simpleLineSymbol_info,map);
            graphicsLayer_info.addGraphic(line);
            TextSymbol t = new TextSymbol(12, list_des_info.get(i), Color.BLACK);
            t.setFontFamily(new File(CopyFontFile.FONT_PATH).getPath());
            t.setOffsetX(-20);
            Map<String,Object> map2 = new HashMap<>();
            map2.put("style","text");
            Graphic ts = new Graphic(polyline,t,map2);
            graphicsLayer_info_text.addGraphic(ts);
        }
    }
    /**
     * 加载报警信息
     * @param url2
     */
    private void loadwarning(String url2){
        List<String> list_name_info = new ArrayList<>();
        List<String> list_des_info = new ArrayList<>();
        List<List<Point>> list_collection = new ArrayList<>();
        SimpleLineSymbol simpleLineSymbol_warning = new SimpleLineSymbol(Color.WHITE,2);
        ReadKml readKml1 = new ReadKml(url2,list_name_info,list_des_info,null,list_collection,MainActivity.this);
        readKml1.parseKml2();
        for (int i = 0 ; i < list_collection.size() ; i++){
            Polyline polyline = new Polyline();
            polyline.startPath(list_collection.get(i).get(0));
            for (int j = 1 ; j < list_collection.get(i).size() ; j++){
                polyline.lineTo(list_collection.get(i).get(j));
            }
            Map<String,Object> map = new HashMap<>();
            map.put("style","line");
            Graphic line = new Graphic(polyline,simpleLineSymbol_warning,map);
            graphicsLayer_warning.addGraphic(line);
            TextSymbol t = new TextSymbol(12, list_des_info.get(i), Color.YELLOW);
            t.setFontFamily(new File(CopyFontFile.FONT_PATH).getPath());
            t.setOffsetX(-20);
            Map<String,Object> map2 = new HashMap<>();
            map2.put("style","text");
            Graphic ts = new Graphic(polyline,t,map2);
            graphicsLayer_warning.addGraphic(ts);
        }
        graphicsLayer_warning.setVisible(false);
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
                for (EZDeviceInfo ezDeviceInfo : list_ezDevices){
                    for (EZCameraInfo ezCameraInfo : ezDeviceInfo.getCameraInfoList()){
                        if (ezCameraInfo == null){
                            return;
                        }else if (ezCameraInfo.getCameraName().equals(tv_name.getText())){
                            Intent intent = new Intent(MainActivity.this , EZRealPlayActivity.class);
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
}
