package com.videogo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.videogo.openapi.bean.EZDeviceInfo;
import java.util.List;
import ezviz.ezopensdk.R;

public class ScanPicAdapter extends RecyclerView.Adapter<ScanPicAdapter.ViewHolder> {
    private List<EZDeviceInfo> list_ezdevices;
    private ItemClickListener listener;

    public ScanPicAdapter(List<EZDeviceInfo> list_ezdevices) {
        this.list_ezdevices = list_ezdevices;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_pic_item,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(list_ezdevices.get(position).getDeviceName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null){
                    listener.onItemClick(v,position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list_ezdevices.size();
    }

    public void setOnItemClickListener(ItemClickListener itemClickListener){
        this.listener = itemClickListener;
    }

    public interface ItemClickListener{
        void onItemClick(View v,int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public final TextView textView;
        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv);
        }
    }
}
