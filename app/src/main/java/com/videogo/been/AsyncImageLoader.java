package com.videogo.been;

import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.videogo.ui.util.FTPutils;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

public class AsyncImageLoader {
    private ExecutorService cachedThreadPool;
    public AsyncImageLoader(ExecutorService cachedThreadPool) {
        this.cachedThreadPool = cachedThreadPool;
    }

//    private static class MyHandler extends Handler{
//        WeakReference<Activity> weakReference;
//        public MyHandler(Activity activity){
//            weakReference = new WeakReference<Activity>(activity);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            final Activity activity = weakReference.get();
//            if (activity!=null){
//                if (msg.what ==0){
//
//                }
//            }
//        }
//    }

    public Drawable loadDrawable(final HashMap<String,String> map, final ImageCallback imageCallback) {
//        String imageUrl = "ftp://wh:wanghao@192.168.60.81/uftp/78b8998fe074fcfc708f8d91d93678aa.jpg";
        //HashMap<String,String> map = DataUtils.getUrlResouse(imageUrl);
        //HashMap<String,String> map = DataUtils.getUrlResouses(imageUrl).get(0);
        String ip = map.get("ip");
        String name = map.get("name");
        String password = map.get("password");
        String pic_name = map.get("pic_name");
        String dir_name = map.get("dir_name");
        String server_name = map.get("server_name");
        Log.d("TAG","server_name="+server_name);
        final Handler handler = new Handler() {
            public void handleMessage(Message message) {
                switch (message.what){
                    case 0:
                        imageCallback.imageLoaded();
                        break;
                    case 1:
                        imageCallback.imageLoadEmpty();
                        break;
                }
            }
        };
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                FTPutils ftPutils = new FTPutils();
                String localpath = Environment.getExternalStorageDirectory().toString()+"/EZOpenSDK/cash";
                Boolean flag = ftPutils.connect(ip,21,name,password);
                if (flag){
                    try {
                        ftPutils.downloadSingleFile(server_name, localpath,
                                pic_name, new FTPutils.FtpProgressListener() {
                                    @Override
                                    public void onFtpProgress(int currentStatus, long process, File targetFile) {
                                        //Log.d("TAG","currenstatus="+currentStatus);
                                        //Log.d("TAG","process="+process);
                                        if (process == 100){
                                            Message message = Message.obtain();
                                            message.what = 0;
                                            handler.sendMessage(message);
                                        }
                                    }
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    Message message = Message.obtain();
                    message.what = 1;
                    handler.sendMessage(message);
                }
            }
        };
        cachedThreadPool.execute(runnable);
        return null;
    }

    public interface ImageCallback {
        public void imageLoaded();
        public void imageLoadEmpty();
    }
}
