package com.videogo.warning;

import android.content.Context;
import android.util.Log;

import com.videogo.been.SnCal;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpUtil {
    public static void post(String address, okhttp3.Callback callback, Map<String,String> map)
    {
        OkHttpClient client = new OkHttpClient();
        FormBody.Builder builder = new FormBody.Builder();
        if (map!=null)
        {
            for (Map.Entry<String,String> entry:map.entrySet())
            {
                builder.add(entry.getKey(),entry.getValue());
            }
        }
        FormBody body = builder.build();
        Request request = new Request.Builder()
                .url(address)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }
    public static void get(String address, String sn,okhttp3.Callback callback, LinkedHashMap<String,String> map) throws UnsupportedEncodingException {
        String paramsStr = ToQueryString(map);
        String requestUrl = String.format("%s?%s", address,  paramsStr);
        String totalUrl = requestUrl+"&sn="+sn;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(totalUrl)
                .get()
                .build();
        client.newCall(request).enqueue(callback);
    }
    public static String ToQueryString(Map<?, ?> data)throws UnsupportedEncodingException {
        StringBuffer queryString = new StringBuffer();
        for (Map.Entry<?, ?> pair : data.entrySet()) {
            queryString.append(pair.getKey() + "=");
            String ss[] = pair.getValue().toString().split(",");
            if(ss.length>1){
                for(String s:ss){
                    queryString.append(URLEncoder.encode(s,"UTF-8") + ",");
                }
                queryString.deleteCharAt(queryString.length()-1);queryString.append("&");
            }
            else{
                queryString.append(URLEncoder.encode((String) pair.getValue(), "UTF-8") + "&");
            }
        }
        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }
        return queryString.toString();
    }
}