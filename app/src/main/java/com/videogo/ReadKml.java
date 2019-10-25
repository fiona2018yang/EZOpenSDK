package com.videogo;

import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;

import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.videogo.been.StyleId;
import com.videogo.been.StyleMap;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ReadKml {
    private String path;
    private List<String> list_name;
    private List<String> list_des;
    private List<Point> list_point;
    private List<List<Point>> list_collection;
    private Context context;
    private String type="";
    private List<StyleId> list_styleid;
    private List<StyleMap> list_stylemap;
    private List<String> style_url;

    public ReadKml(String path, List<String> list_name, List<String> list_des, List<Point> list_point,Context context) {
        this.path = path;
        this.list_name = list_name;
        this.list_des = list_des;
        this.list_point = list_point;
        this.context = context;
    }

    public ReadKml(String path, List<String> list_name, List<String> list_des, List<Point> list_point, List<List<Point>> list_collection, Context context) {
        this.path = path;
        this.list_name = list_name;
        this.list_des = list_des;
        this.list_point = list_point;
        this.list_collection = list_collection;
        this.context = context;
    }

    public ReadKml(String path, List<String> list_name, List<String> list_des, List<Point> list_point, List<List<Point>> list_collection
            ,List<StyleId> list_styleid,List<StyleMap> list_stylemap,List<String> style_url, Context context) {
        this.path = path;
        this.list_name = list_name;
        this.list_des = list_des;
        this.list_point = list_point;
        this.list_collection = list_collection;
        this.context = context;
        this.list_styleid = list_styleid;
        this.list_stylemap = list_stylemap;
        this.style_url = style_url;
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
    public  void parseKml2(){
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
            listNodes2(root);
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
            type = "point";
        }else if("Polygon".equals(node.getName())){
            type = "polygon";
        }else if(!(node.getTextTrim().equals("")) && "coordinates".equals(node.getName())){
            String string = node.getText();
            if (type.equals("point")){
                Point p = new Point(Double.parseDouble(string.substring(0,string.indexOf(","))),Double.parseDouble(string.substring(string.indexOf(",")+1,string.lastIndexOf(","))));
                list_point.add(p);
            }else{
                String[] str = string.trim().split(" ");
                List<Point> list = new ArrayList<>();
                for (int i = 0 ; i < str.length ; i++){
                    String t = str[i];
                    Point p = new Point(Double.parseDouble(t.substring(0,t.indexOf(","))),Double.parseDouble(t.substring(t.indexOf(",")+1,t.lastIndexOf(","))));
                    list.add(p);
                }
                list_collection.add(list);
            }
        }else if("LineStyle".equals(node.getName())){
            Element element = node.element("color");
            //LineColor = element.getText();
            Element element1 = node.element("width");
            //LineWidth = element1.getText();
        }else if ("Style".equals(node.getName())){
            String StyleId = node.attributeValue("id");
            List<Element> element_list = node.elements("LineStyle");
            if (element_list.size()>0){
                Element element = element_list.get(0);
                String LineColor = element.element("color").getText();
                String lineColor = LineColor.substring(2,LineColor.length());
                String LineWidth = element.element("width").getText();
                //Log.i("TAG","LineColor="+LineColor);
                //Log.i("TAG","LineWidth="+LineWidth);
                //Log.i("TAG","StyleId="+StyleId);
                StyleId styleId = new StyleId(StyleId,lineColor,LineWidth);
                list_styleid.add(styleId);
            }
        }else if("StyleMap".equals(node.getName())){
            String StylemapId = node.attributeValue("id");
            Element element = node.element("Pair");
            Element element1 = element.element("styleUrl");
            String url = element1.getText().substring(1,element1.getText().length());
            //Log.i("TAG","stylemapurl="+url);
            //Log.i("TAG","****************");
            StyleMap styleMap = new StyleMap(StylemapId,url);
            list_stylemap.add(styleMap);
        }else if ("Placemark".equals(node.getName())){
            Element element = node.element("styleUrl");
            String styleUrl = element.getText().substring(1,element.getText().length());
            style_url.add(styleUrl);
        }
        //同一时候迭代当前节点以下的全部子节点
        //使用递归
        Iterator<Element> iterator = node.elementIterator();
        while(iterator.hasNext()){
            Element e = iterator.next();
            listNodes(e);
        }
    }
    private  void listNodes2(Element node) {
        //假设当前节点内容不为空，则输出
        if(!(node.getTextTrim().equals("")) && "name".equals(node.getName())){
            //Log.d("当前结点内容：", node.getText());
            //parseHtml(node.getText());
            list_name.add(node.getText());
        }else if (!(node.getTextTrim().equals("")) && "description".equals(node.getName())) {
            list_des.add(node.getText());
        }else if ("Point".equals(node.getName())){
            type = "point";
        }else if("Polygon".equals(node.getName())){
            type = "polygon";
        }else if(!(node.getTextTrim().equals("")) && "coordinates".equals(node.getName())){
            String string = node.getText();
            if (type.equals("point")){
                Point p = new Point(Double.parseDouble(string.substring(0,string.indexOf(","))),Double.parseDouble(string.substring(string.indexOf(",")+1,string.lastIndexOf(","))));
                list_point.add(p);
            }else{
                String[] str = string.trim().split(" ");
                List<Point> list = new ArrayList<>();
                for (int i = 0 ; i < str.length ; i++){
                    String t = str[i];
                    Point p = new Point(Double.parseDouble(t.substring(0,t.indexOf(","))),Double.parseDouble(t.substring(t.indexOf(",")+1,t.lastIndexOf(","))));
                    list.add(p);
                }
                list_collection.add(list);
            }
        }
        //同一时候迭代当前节点以下的全部子节点
        //使用递归
        Iterator<Element> iterator = node.elementIterator();
        while(iterator.hasNext()){
            Element e = iterator.next();
            listNodes2(e);
        }
    }
}
