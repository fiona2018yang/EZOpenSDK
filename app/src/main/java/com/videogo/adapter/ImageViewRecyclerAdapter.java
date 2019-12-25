package com.videogo.adapter;

import android.content.Context;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;
import com.videogo.been.AsyncImageLoader;
import com.videogo.ui.util.UiUtil;
import com.videogo.warning.RoundTransform;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ezviz.ezopensdk.R;

public class ImageViewRecyclerAdapter extends RecyclerView.Adapter<ImageViewRecyclerAdapter.ImageHolder>{
    private List<HashMap<String,String>> url_list;
    private Context context;
    private OnClickListener OnClickListener;
    private AsyncImageLoader asyncImageLoader;
    private ExecutorService cachedThreadPool;

    public ImageViewRecyclerAdapter(List<HashMap<String, String>> url_list, Context context) {
        this.url_list = url_list;
        this.context = context;
        this.cachedThreadPool = Executors.newCachedThreadPool();
        this.asyncImageLoader = new AsyncImageLoader(cachedThreadPool);
    }

    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item,parent,false);
        ImageHolder imageHolder = new ImageHolder(view);
        return imageHolder;
    }

    @Override
    public void onBindViewHolder(ImageHolder holder, int position) {
        String pic_name = url_list.get(position).get("pic_name");
        String imgpath = Environment.getExternalStorageDirectory().toString()+"/EZOpenSDK/cash/"+pic_name;
        File imgFile = new File(imgpath);
        if (!imgFile.exists()) {
            asyncImageLoader.loadDrawable(url_list.get(position), new AsyncImageLoader.ImageCallback() {
                @Override
                public void imageLoaded() {
                    Picasso.with(context).load(imgFile).transform(new RoundTransform(20))
                            .error(context.getResources().getDrawable(R.mipmap.load_fail)).into(holder.imageView);
                }

                @Override
                public void imageLoadEmpty() {
                    Picasso.with(context).load(R.mipmap.load_fail).transform(new RoundTransform(20))
                            .error(context.getResources().getDrawable(R.mipmap.load_fail)).into(holder.imageView);
                }
            });
        }else{
            Log.d("TAG","图片存在!");
            Picasso.with(context).load(imgFile).transform(new RoundTransform(20))
                    .error(context.getResources().getDrawable(R.mipmap.load_fail)).into(holder.imageView);
        }
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnClickListener.OnItemClick(view,(Integer) view.getTag());
            }
        });
    }

    @Override
    public int getItemCount() {
        return url_list.size();
    }
    public void setSetOnItemClickListener(OnClickListener onClickListener){
        this.OnClickListener = onClickListener;
    }
    public  interface OnClickListener{
        void OnItemClick(View view,int position);
    }

    class ImageHolder extends RecyclerView.ViewHolder{
        private ImageView imageView;
        public ImageHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageview);
            setImageSize(context,imageView);
        }
    }
    private void setImageSize(Context context , ImageView imageView){
        //计算图片左右间距之和
        int padding = 15;
        int spacePx = (int) (UiUtil.dp2px(context, padding) * 2);
        //计算图片宽度
        int imageWidth = UiUtil.getScreenWidth(context) - spacePx;
        //计算宽高比，注意数字后面要加上f表示浮点型数字
        float scale = 16f / 9f;
        //根据图片宽度和比例计算图片高度
        int imageHeight = (int) (imageWidth / scale);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( imageWidth,imageHeight);
        //设置左右边距
        params.leftMargin = (int) UiUtil.dp2px(context, padding);
        params.rightMargin = (int) UiUtil.dp2px(context, padding);
        imageView.setLayoutParams(params);
    }
}
