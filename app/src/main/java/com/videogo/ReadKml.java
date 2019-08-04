package com.videogo;

import com.esri.arcgisruntime.geometry.Point;
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
    public static void parseKml(String path, List<String> list_name, List<String> list_des, List<Point> list_point){
        try {
            InputStream inputStream = new FileInputStream(path);
            SAXReader reader = new SAXReader();
            Document document = null;
            try {
                document = reader.read(inputStream);
            } catch (DocumentException e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            Element root = document.getRootElement();//获取doc.kml文件的根结点
            listNodes(root,list_name,list_des,list_point);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    private static void listNodes(Element node, List<String> list_name, List<String> list_des, List<Point> list_point) {
        //假设当前节点内容不为空，则输出
        if(!(node.getTextTrim().equals("")) && "name".equals(node.getName())){
            //Log.d("当前结点内容：", node.getText());
            //parseHtml(node.getText());
            list_name.add(node.getText());
        }else if (!(node.getTextTrim().equals("")) && "description".equals(node.getName())){
            list_des.add(node.getText());
        }else if (!(node.getTextTrim().equals("")) && "coordinates".equals(node.getName())){
            String string = node.getText();
            Point p = new Point(Double.parseDouble(string.substring(0,string.indexOf(","))),Double.parseDouble(string.substring(string.indexOf(",")+1,string.lastIndexOf(","))));
            list_point.add(p);
        }
        //同一时候迭代当前节点以下的全部子节点
        //使用递归
        Iterator<Element> iterator = node.elementIterator();
        while(iterator.hasNext()){
            Element e = iterator.next();
            listNodes(e,list_name,list_des,list_point);
        }
    }
}
