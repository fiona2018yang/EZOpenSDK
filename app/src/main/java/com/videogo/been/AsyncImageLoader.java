package com.videogo.been;

import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.videogo.ui.util.DataUtils;
import com.videogo.ui.util.FTPutils;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

public class AsyncImageLoader {
    private ExecutorService cachedThreadPool;
    public AsyncImageLoader(ExecutorService cachedThreadPool) {
        this.cachedThreadPool = cachedThreadPool;
    }
    public Drawable loadDrawable(final String imageUrl, final ImageCallback imageCallback) {
//        String imageUrl = "ftp://wh:wanghao@192.168.60.81/uftp/78b8998fe074fcfc708f8d91d93678aa.jpg";
        HashMap<String,String> map = DataUtils.getUrlResouse(imageUrl);
        String ip = map.get("ip");
        String name = map.get("name");
        String password = map.get("password");
        String pic_name = map.get("pic_name");
        String dir_name = map.get("dir_name");
        final Handler handler = new Handler() {
            public void handleMessage(Message message) {
                if (message.what == 0){
                    imageCallback.imageLoaded();
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
                        ftPutils.downloadSingleFile(dir_name+"/"+pic_name, localpath,
                                pic_name, new FTPutils.FtpProgressListener() {
                                    @Override
                                    public void onFtpProgress(int currentStatus, long process, File targetFile) {
                                        Log.d("TAG","currenstatus="+currentStatus);
                                        Log.d("TAG","process="+process);
                                        if (process == 100){
                                            Message message = new Message();
                                            message.what = 0;
                                            handler.sendMessage(message);
                                        }
                                    }
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        cachedThreadPool.execute(runnable);
        return null;
    }

    public interface ImageCallback {
        public void imageLoaded();
    }
}
