package com.videogo.been;

import java.util.ArrayList;
import java.util.List;

public class AlarmContant {
    public static final int MESSAGE_TYPE_TRUCK_IDENTITY = 0 ;
    public static final int MESSAGE_TYPE_ILLEGAL_BUILDING= 1 ;
    public static final int MESSAGE_TYPE_ILLEGAL_PLANT= 2 ;
    public static final int MESSAGE_TYPE_STRAW_BURNING= 3 ;
    public static final int MESSAGE_TYPE_RIVER_MONITOR= 4 ;
    public static final int MESSAGE_TYPE_COMPANY_MANAGE= 5 ;

    public static final int USER_TYPE_CHENGGUAN = 6;
    public static final int USER_TYPE_SHIWUJU = 7;
    public static final int USER_TYPE_HUANBAOJU = 8;
    public static final int USER_TYPE_ZHIFAJU = 9;
    public static final int USER_TYPE_FAZHANJU = 10;
    public static final int USER_TYPE_SUPER = 11;

    public static List<String> list_chengguan = new ArrayList<>();
    public static List<String> list_shiwuju = new ArrayList<>();
    public static List<String> list_huanbaoju = new ArrayList<>();
    public static List<String> list_zhifaju = new ArrayList<>();
    public static List<String> list_fazhanju = new ArrayList<>();
    public static List<String> list_super = new ArrayList<>();
    public static final List<String> getList_chengguan(){
        list_chengguan.clear();
        list_chengguan.add("渣土车识别定位跟踪");
        list_chengguan.add("违法乱建");
        list_chengguan.add("违章种植");
        list_chengguan.add("秸秆焚烧");
        return list_chengguan;
    }

    public static final List<String> getList_shiwuju(){
        list_shiwuju.clear();
        list_shiwuju.add("渣土车识别定位跟踪");
        list_shiwuju.add("违法乱建");
        list_shiwuju.add("违章种植");
        list_shiwuju.add("秸秆焚烧");
        return list_shiwuju;
    }
    public static final List<String> getList_huanbaoju(){
        list_huanbaoju.clear();
        list_huanbaoju.add("河道监测");
        return list_huanbaoju;
    }
    public static final List<String> getList_zhifaju(){
        list_zhifaju.clear();
        list_zhifaju.add("渣土车识别定位跟踪");
        list_zhifaju.add("违法乱建");
        list_zhifaju.add("河道监测");
        list_zhifaju.add("园区企业监管");
        return list_zhifaju;
    }
    public static final List<String> getList_fazhanju(){
        list_fazhanju.clear();
        list_fazhanju.add("园区企业监管");
        return list_fazhanju;
    }
    public static final List<String> getList_super(){
        list_super.clear();
        list_super.add("渣土车识别定位跟踪");
        list_super.add("违法乱建");
        list_super.add("违章种植");
        list_super.add("秸秆焚烧");
        list_super.add("河道监测");
        list_super.add("园区企业监管");
        return list_super;
    }

}
