package com.videogo.warning;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.videogo.been.WarningData;

import java.util.List;

import ezviz.ezopensdk.R;

public class TitleWarningAdatper extends RecyclerView.Adapter<TitleWarningAdatper.VH> {
    private Context context;
    private List<String>  title_list;
    private List<List<WarningData>> data_list;
    private DataRecyclerAdapter dataRecyclerAdapter;

    public TitleWarningAdatper(Context context, List<String> title_list, List<List<WarningData>> data_list) {
        this.context = context;
        this.title_list = title_list;
        this.data_list = data_list;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_title,parent,false),dataRecyclerAdapter);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        holder.title.setText(title_list.get(position));
        if (holder.dataRecyclerAdapter == null){
            holder.recyclerView.setHasFixedSize(true);
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
            holder.recyclerView.setItemAnimator(new DefaultItemAnimator());
            holder.recyclerView.addItemDecoration(CommItemDecoration.createVertical(context,context.getResources().getColor(R.color.viewfinder_frame),5));
            holder.dataRecyclerAdapter = new DataRecyclerAdapter(context,data_list.get(position));
            holder.recyclerView.setAdapter(holder.dataRecyclerAdapter);
        }
    }

    @Override
    public int getItemCount() {
        return data_list.size();
    }
    public static class VH extends RecyclerView.ViewHolder{

        private TextView title;
        private RecyclerView recyclerView;
        private DataRecyclerAdapter dataRecyclerAdapter;

        public VH(View itemView,DataRecyclerAdapter dataRecyclerAdapter) {
            super(itemView);
            title = itemView.findViewById(R.id.title_tv);
            recyclerView = itemView.findViewById(R.id.recyclerView);
            this.dataRecyclerAdapter = dataRecyclerAdapter;
        }
    }
}

