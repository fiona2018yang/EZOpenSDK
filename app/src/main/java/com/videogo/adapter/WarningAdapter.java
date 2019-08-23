package com.videogo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ezviz.ezopensdk.R;

public class WarningAdapter extends RecyclerView.Adapter<WarningAdapter.WarningHolder> {
    private Context context;
    private List<String> list;
    private setOnclick onclick;

    public WarningAdapter(Context context, List<String> list , setOnclick onclick) {
        this.context = context;
        this.list = list;
        this.onclick = onclick;
    }

    @Override
    public WarningHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new WarningHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.warning_item,parent,false));
    }

    @Override
    public void onBindViewHolder(WarningHolder holder, int position) {
        holder.tv.setText(list.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onclick.onClick(v,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface setOnclick{
        void onClick(View view,int p);
    }

    public static class WarningHolder extends RecyclerView.ViewHolder{
        private TextView tv;
        public WarningHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv);
        }
    }
}
