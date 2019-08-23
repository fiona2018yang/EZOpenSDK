package com.videogo;

import android.content.Context;
import android.util.Log;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.mapping.view.MapView;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;


public class ReadKml {
    private String path;
    private List<String> list_name;
    private List<String> list_des;
    private List<Point> list_point;
    private List<PointCollection> list_collection;
    private Context context;
    private String type="";

    public ReadKml(String path, List<String> list_name, List<String> list_des, List<Point> list_point,Context context) {
        this.path = path;
        this.list_name = list_name;
        this.list_des = list_des;
        this.list_point = list_point;
        this.context = context;
    }

    public ReadKml(String path, List<String> list_name, List<String> list_des, List<Point> list_point, List<PointCollection> list_collection,Context context) {
        this.path = path;
        this.list_name = list_name;
        this.list_des = list_des;
        this.list_point = list_point;
        this.list_collection = list_collection;
        this.context = context;
    }

    public  void parseKml(){
        try {
            //InputStream inputStream = new FileInputStream(path);
            InputStream inputStream = context.getAssets().open(path);
            SAXReader reader = new SAXReader();
            Document document = null;
            try {
                document = reader.read(inputStream);
            } catch (DocumentException e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            Element root = document.getRootElement();//获取doc.kml文件的根结点
            listNodes(root);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    private  void listNodes(Element node) {
        //假设当前节点内容不为空，则输出
        if(!(node.getTextTrim().equals("")) && "name".equals(node.getName())){
            //Log.d("当前结点内容：", node.getText());
            //parseHtml(node.getText());
            list_name.add(node.getText());
        }else if (!(node.getTextTrim().equals("")) && "description".equals(node.getName())) {
            list_des.add(node.getText());
        }else if ("Point".equals(node.getName())){
            Log.i("TAG","point");
            type = "point";
        }else if("Polygon".equals(node.getName())){
            Log.i("TAG","polygon");
            type = "polygon";
        }else if(!(node.getTextTrim().equals("")) && "coordinates".equals(node.getName())){
            Log.i("TAG","type="+type);
            String string = node.getText();
            if (type.equals("point")){
                Point p = new Point(Double.parseDouble(string.substring(0,string.indexOf(","))),Double.parseDouble(string.substring(string.indexOf(",")+1,string.lastIndexOf(","))));
                list_point.add(p);
            }else{
                String[] str = string.trim().split(" ");
                PointCollection  collection = new PointCollection(SpatialReference.create(4326));
                for (int i = 0 ; i < str.length ; i++){
                    String t = str[i];
                    Point p = new Point(Double.parseDouble(t.substring(0,t.indexOf(","))),Double.parseDouble(t.substring(t.indexOf(",")+1,t.lastIndexOf(","))),SpatialReference.create(4326));
                    collection.add(p);
                }
                list_collection.add(collection);
            }
        }
        //同一时候迭代当前节点以下的全部子节点
        //使用递归
        Iterator<Element> iterator = node.elementIterator();
        while(iterator.hasNext()){
            Element e = iterator.next();
            listNodes(e);
        }
    }
}
