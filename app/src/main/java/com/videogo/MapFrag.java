package com.videogo;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.esri.arcgisruntime.data.TileCache;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.raster.Raster;

import cn.qqtheme.framework.picker.FilePicker;
import cn.qqtheme.framework.util.StorageUtils;
import ezviz.ezopensdk.R;

public class MapFrag extends Fragment implements View.OnClickListener {
    private static String TAG = "MapFrag";
    private View view;
    private Context mycontext;
    private Button openfile;
    private MapView mapView;
    private ArcGISMap mMap;
    //地图储存位置
    private String earthPath = "";
    private String path;
    private String str;
    private SharedPreferences sharedPreferences;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.map_fragment,container,false);
        mycontext = view.getContext();
        initView();
        return view;
    }

    private void initView() {
        mapView = view.findViewById(R.id.mapview);
        openfile = view.findViewById(R.id.openfile);
        openfile.setOnClickListener(this);
        sharedPreferences = mycontext.getSharedPreferences("path", 0);
        path = sharedPreferences.getString("earthPath", "");
        path = "/storage/emulated/0/0506.tif";
        Log.i("TAG","path="+path);
        if (path.equals("") || path == null) {
//            ToastNotRepeat.show(this, "没有TPK地图，请选择文件或者从云端下载！");
            Toast.makeText(mycontext,"没有TPK地图，请选择文件",Toast.LENGTH_LONG).show();
        }else{
            str = path.substring(path.indexOf("."));
        }
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.openfile:
                selectfile();
                break;
        }
    }

    //文件选择器
    private void selectfile() {
        FilePicker picker = new FilePicker(getActivity(), FilePicker.FILE);
        picker.setShowHideDir(false);
        picker.setRootPath(Environment.getExternalStorageDirectory().getPath());
        picker.setAllowExtensions(new String[]{".tif"});
        picker.setOnFilePickListener(new FilePicker.OnFilePickListener() {
            @Override
            public void onFilePicked(String currentPath) {
                Log.i("TAG","currentpath="+currentPath);
                //earthPath = "/sdcard/" + currentPath.substring(currentPath.lastIndexOf("/") + 1, currentPath.length());
                sharedPreferences = mycontext.getSharedPreferences("path", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("earthPath", currentPath);
                editor.commit();
                //加载离线切片地图
                //getActivity().finish();
                MainActivity activity = (MainActivity) getActivity();
                //activity.reloadFragView();
                //startActivity(new Intent(mycontext, MainActivity.class));
                //getActivity().overridePendingTransition(0, 0);
            }
        });
        picker.show();
    }
}
