package com.videogo.adapter;

import android.content.Context;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private AsyncImageLoader asyncImageLoader;
    private String address;
    private boolean isSrolling = false;


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
        //view.setOnClickListener(this);
        return viewHolder;
    }
    public void setScrolling(boolean scrolling){
        this.isSrolling = scrolling;
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String path = alarmMessageList.get(position).getImgPath();
        if (!isSrolling){
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
                                Picasso.with(context).load(imgFile).transform(new RoundTransform(20))
                                        .error(context.getResources().getDrawable(R.mipmap.load_fail)).into(holder.imageView);
                            }
                        });
                    }else{
                        Log.d("TAG","图片存在!");
                        Picasso.with(context).load(imgFile).transform(new RoundTransform(20))
                                .error(context.getResources().getDrawable(R.mipmap.load_fail)).into(holder.imageView);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else{
                Picasso.with(context).load(R.mipmap.load_fail).transform(new RoundTransform(20))
                        .error(context.getResources().getDrawable(R.mipmap.load_fail)).into(holder.imageView);
            }
            if (alarmMessageList.get(position).getLatitude()!=null||alarmMessageList.get(position).getLongitude()!=null){
                String la = alarmMessageList.get(position).getLatitude();
                String ln = alarmMessageList.get(position).getLongitude();
                try {
                    queryLocation(holder.address,la,ln);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }else{
                holder.address.setText("未知");
            }
        }else{
            Picasso.with(context).load(R.mipmap.loading).transform(new RoundTransform(20))
                    .error(context.getResources().getDrawable(R.mipmap.loading)).into(holder.imageView);
            holder.address.setText("加载中...");
        }
        String camera_name = getCameraInfo(cameraInfoList,alarmMessageList.get(position).getChannelNumber());
        holder.camera_name.setText(camera_name);
        holder.message_text.setText(alarmMessageList.get(position).getMessage());
        holder.time_creat.setText(alarmMessageList.get(position).getCreateTime());
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //这里使用getTag方法获取position
                address = holder.address.getText().toString();
                OnClickListener.OnItemClick(view, (Integer) view.getTag(),address);
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
        void OnItemClick(View view,int position , String address);
    }
    public void queryLocation(TextView textView , String la, String ln) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String url = "http://api.map.baidu.com/reverse_geocoding/v3/";
        LinkedHashMap<String,String> map = new LinkedHashMap<>();
        map.put("location",la+","+ln);
        map.put("coordtype","wgs84ll");
        map.put("radius","500");
        map.put("extensions_poi","1");
        map.put("output","json");
        map.put("ak","KNAeq1kjoe2u24PTYfeL4kO0KvGaqNak");
        String sn = SnCal.getSnKry(map);
        OkHttpUtil.get(url, sn,new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("TAG", "onFailure: ",e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                String address = "";
                try {
                    JSONObject object = new JSONObject(responseBody);
                    String status = object.get("status").toString();
                    if (status.equals("0")){
                        String result = object.get("result").toString();
                        JSONObject objectdata = new JSONObject(result);
                        String formatted_address = objectdata.get("formatted_address").toString();
                        String sematic_description = objectdata.get("sematic_description").toString();
                        if (sematic_description==null || sematic_description.equals("")){
                            address = formatted_address;
                        }else{
                            address = formatted_address+"("+sematic_description+")";
                        }
                        if (address.equals("")){
                            textView.setText("未知");
                        }else{
                            textView.setText(address);
                        }
                    }else{
                        textView.setText("未知");
                    }
                    Log.d("TAG","address="+address);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },map);
    }
}
