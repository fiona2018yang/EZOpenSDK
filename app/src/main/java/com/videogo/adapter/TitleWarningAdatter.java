package com.videogo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.videogo.been.AlarmMessage;
import java.util.List;
import ezviz.ezopensdk.R;

public class TitleWarningAdatter extends RecyclerView.Adapter<TitleWarningAdatter.MyViewHolder> {
    private List<AlarmMessage> alarmMessageList;
    private Context context;

    public TitleWarningAdatter(List<AlarmMessage> alarmMessageList, Context context) {
        this.alarmMessageList = alarmMessageList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data,parent,false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.id_text.setText(alarmMessageList.get(position).getId());
        holder.message_text.setText(alarmMessageList.get(position).getMessage());
        holder.locaton_text.setText(alarmMessageList.get(position).getAddress());
        holder.creattime_text.setText(alarmMessageList.get(position).getCreateTime());
    }

    @Override
    public int getItemCount() {
        return alarmMessageList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageView;
        public TextView id_text;
        public TextView message_text;
        public TextView locaton_text;
        public TextView creattime_text;
        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img);
            id_text = itemView.findViewById(R.id.id);
            message_text = itemView.findViewById(R.id.message);
            locaton_text = itemView.findViewById(R.id.location);
            creattime_text = itemView.findViewById(R.id.creattime);
        }
    }
}
