package com.videogo.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.esri.core.geometry.Point;
import com.squareup.picasso.Picasso;
import com.videogo.been.AlarmMessage;
import com.videogo.been.AsyncImageLoader;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.remoteplayback.list.PlaybackActivity2;
import com.videogo.ui.util.DataUtils;
import com.videogo.ui.util.FTPutils;
import com.videogo.warning.RoundTransform;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ezviz.ezopensdk.R;

public class TitleWarningAdatter extends RecyclerView.Adapter<TitleWarningAdatter.MyViewHolder>implements View.OnClickListener {
    private List<AlarmMessage> alarmMessageList;
    private Context context;
    private ExecutorService cachedThreadPool;
    private OnClickListener OnClickListener;
    private List<EZCameraInfo> cameraInfoList;
    private AsyncImageLoader asyncImageLoader;

    public TitleWarningAdatter(List<AlarmMessage> alarmMessageList,List<EZCameraInfo> cameraInfos,Context context) {
        this.alarmMessageList = alarmMessageList;
        this.cameraInfoList = cameraInfos;
        this.context = context;
        this.cachedThreadPool = Executors.newCachedThreadPool();
        this.asyncImageLoader = new AsyncImageLoader(cachedThreadPool);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data,parent,false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        //绑定监听事件
        view.setOnClickListener(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String path = alarmMessageList.get(position).getImgPath();
        if (path!=null&&!path.equals("")){
            //加载图片
            try {
                String pic_name = DataUtils.getUrlResouse(path).get("pic_name");
                String imagpath = Environment.getExternalStorageDirectory().toString()+"/EZOpenSDK/cash/"+pic_name;
                File imgFile = new File(imagpath);
                if (!imgFile.exists()) {
                    asyncImageLoader.loadDrawable(path, new AsyncImageLoader.ImageCallback() {
                        @Override
                        public void imageLoaded() {
                            Picasso.with(context).load(imgFile).transform(new RoundTransform(10))
                                    .error(context.getResources().getDrawable(R.mipmap.ic_launcher)).into(holder.imageView);
                        }
                    });
                }else{
                    Log.d("TAG","图片存在!");
                    Picasso.with(context).load(imgFile).transform(new RoundTransform(10))
                            .error(context.getResources().getDrawable(R.mipmap.ic_launcher)).into(holder.imageView);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        String camera_name = getCameraInfo(cameraInfoList,alarmMessageList.get(position).getChannelNumber());
        holder.camera_name.setText(camera_name);
        holder.message_text.setText(alarmMessageList.get(position).getMessage());
        holder.time_creat.setText(alarmMessageList.get(position).getCreateTime());
        holder.address.setText(alarmMessageList.get(position).getAddress());
        holder.itemView.setTag(position);
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
    private LatLng tramsform(Point p ){
        LatLng latLng = new LatLng(p.getY(),p.getX());
        return latLng;
    }

    @Override
    public void onClick(View view) {
        //这里使用getTag方法获取position
        OnClickListener.OnItemClick(view, (Integer) view.getTag());
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
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
        void OnItemClick(View view,int position);
    }
}
