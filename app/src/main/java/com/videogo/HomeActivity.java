package com.videogo;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ezviz.ezopensdk.R;

public class HomeActivity extends Activity {

    private GridView   HomeGView;
    private List<Map<String, Object>> data_list;

    // 图片封装为一个数组
    private int[] icon = { R.mipmap.home_icon_real_map,R.mipmap.home_icon_preview,R.mipmap.home_icon_baidu_map,
            R.mipmap.home_icon_alarm_information,R.mipmap.home_icon_show_video,R.mipmap.home_icon__show_picture };
    private String[] iconName = { "实景地图", "画面预览", "百度地图", "报警信息", "视频查看", "图片查看"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initGridView();

    }

    /**
     * 初始化View
     */
    private void initGridView() {
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
                for (int i = 0; i < parent.getCount(); i++) {
                    View v = parent.getChildAt(i);
                    if (position == i) {//当前选中的Item改变背景颜色
                        if (position != 3 ) {
                            view.setBackgroundColor(getResources().getColor(R.color.Lightgray));
                        } else {
                            view.setBackgroundColor(Color.TRANSPARENT);
                        }
                    } else {
                        v.setBackgroundColor(Color.TRANSPARENT);
                    }
                }
                switch (position) {
                    case 0:
                        //
//                        Intent iSnrSat = new Intent(view.getContext(), com.wonech.wonestar.ActivitySnrSat.class);
//                        startActivity(iSnrSat);
                        //  BatteryInfor();
                        break;
                    case 1:
                        //显示星空图
                        //StarSatelliteSnr();
//                        Intent star = new Intent(view.getContext(), StarChartActivity.class);
//                        startActivity(star);
                        break;
                    case 2:
//                        GNSSInfor();
                        break;
                    case 3: //启动\关闭CROS差分
//                        img = (ImageView) view.findViewById(R.id.main_title_icon);  //放在指定位置才是修改指定位置的图片
//                        StarCors();
                        break;
                    //                    case 4:
                    //                        //显示状态提示信息
                    //                        TipInfor();
                    //                        break;
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
}
