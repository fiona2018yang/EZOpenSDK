package com.videogo.adapter;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.videogo.been.AlarmContant;
import com.videogo.been.AlarmMessage;
import com.videogo.been.AsyncImageLoader;
import com.videogo.been.SnCal;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.ui.util.DataUtils;
import com.videogo.warning.OkHttpUtil;
import com.videogo.warning.RoundTransform;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import ezviz.ezopensdk.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TitleWarningAdatter extends RecyclerView.Adapter<TitleWarningAdatter.MyViewHolder>{
    private List<AlarmMessage> alarmMessageList;
    private Context context;
    private ExecutorService cachedThreadPool;
    private OnClickListener OnClickListener;
    private List<EZCameraInfo> cameraInfoList;
    private List<String> read_list;
    private AsyncImageLoader asyncImageLoader;
    private String address;
    private boolean isSrolling = false;


    public TitleWarningAdatter(List<AlarmMessage> alarmMessageList,List<EZCameraInfo> cameraInfos,ExecutorService executorService  ,Context context) {
        this.alarmMessageList = alarmMessageList;
        this.cameraInfoList = cameraInfos;
        this.context = context;
        this.cachedThreadPool = executorService;
        this.asyncImageLoader = new AsyncImageLoader(cachedThreadPool);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data,parent,false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        //绑定监听事件
        //view.setOnClickListener(this);
        return viewHolder;
    }
    public void setScrolling(boolean scrolling){
        this.isSrolling = scrolling;
    }

    public void setRead_list(List<String> list){
        this.read_list = list;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String path = alarmMessageList.get(position).getImgPath();
        holder.imageView.setTag(path);
        //加载图片
        if (path != null && !path.equals("")) {
            try {
                List<HashMap<String, String>> list = DataUtils.getUrlResouses(path);
                if (list == null) {
                    Picasso.with(context).load(R.mipmap.load_fail).transform(new RoundTransform(20)).resize(600, 300)
                            .error(context.getResources().getDrawable(R.mipmap.load_fail)).into(holder.imageView);
                } else {
                    String avatarTag = (String) holder.imageView.getTag();
                    HashMap<String, String> map = list.get(0);
                    String pic_name = map.get("pic_name");
                    String imagpath = Environment.getExternalStorageDirectory().toString() + "/EZOpenSDK/cash/" + pic_name;
                    File imgFile = new File(imagpath);
                    if (!imgFile.exists()) {
                        asyncImageLoader.loadDrawable(map, new AsyncImageLoader.ImageCallback() {
                            @Override
                            public void imageLoaded() {
                                if (null == avatarTag || avatarTag.equals(holder.imageView.getTag())) {
                                    Picasso.with(context).load(imgFile).transform(new RoundTransform(20)).resize(600, 300)
                                            .error(context.getResources().getDrawable(R.mipmap.load_fail)).into(holder.imageView);
                                }
                            }

                            @Override
                            public void imageLoadEmpty() {
                                if (null == avatarTag || avatarTag.equals(holder.imageView.getTag())) {
                                    Picasso.with(context).load(R.mipmap.load_fail).transform(new RoundTransform(20)).resize(600, 300)
                                            .error(context.getResources().getDrawable(R.mipmap.load_fail)).into(holder.imageView);
                                }
                            }
                        });
                    } else {
                        if (null == avatarTag || avatarTag.equals(holder.imageView.getTag())) {
                            Picasso.with(context).load(imgFile).transform(new RoundTransform(20)).resize(600, 300)
                                    .error(context.getResources().getDrawable(R.mipmap.ic_launcher)).into(holder.imageView);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Picasso.with(context).load(R.mipmap.load_fail).transform(new RoundTransform(20)).resize(600, 300)
                    .error(context.getResources().getDrawable(R.mipmap.load_fail)).into(holder.imageView);
        }

        //地址
        if (alarmMessageList.get(position).getLatitude() != null || alarmMessageList.get(position).getLongitude() != null) {
            holder.address.setText(alarmMessageList.get(position).getAddress());
        } else {
            holder.address.setText("未知");
        }
        //设置已读
        String id = alarmMessageList.get(position).getId();
        if (read_list.contains(id)) {
            holder.camera_name.setTextColor(context.getResources().getColor(R.color.topBarText));
        } else {
            holder.camera_name.setTextColor(context.getResources().getColor(R.color.a1_blue_color));
        }
        String camera_name = getCameraInfo(cameraInfoList, alarmMessageList.get(position).getChannelNumber());
        holder.camera_name.setText(camera_name);
        holder.message_text.setText(alarmMessageList.get(position).getMessage());
        holder.time_creat.setText(alarmMessageList.get(position).getCreateTime());
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //这里使用getTag方法获取position
                address = holder.address.getText().toString();
                OnClickListener.OnItemClick(view, (Integer) view.getTag(), address);
            }
        });
    }
    private String getCameraInfo(List<EZCameraInfo> cameraInfos , String no){
        if (no!=null&&!no.equals("")){
            for (EZCameraInfo cameraInfo : cameraInfos){
                if (cameraInfo.getCameraNo() == Integer.parseInt(no)){
                    return cameraInfo.getCameraName();
                }
            }
        }
        return "Null";
    }
    @Override
    public int getItemCount() {
        return alarmMessageList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageView;
        public TextView camera_name;
        public TextView message_text;
        public TextView address;
        public TextView time_creat;
        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img);
            camera_name = itemView.findViewById(R.id.camera_name);
            message_text = itemView.findViewById(R.id.message);
            address = itemView.findViewById(R.id.address);
            time_creat = itemView.findViewById(R.id.time_creat);
        }
    }

    public void setSetOnItemClickListener(OnClickListener onClickListener){
        this.OnClickListener = onClickListener;
    }



    public  interface OnClickListener{
        void OnItemClick(View view,int position , String address);
    }
}
