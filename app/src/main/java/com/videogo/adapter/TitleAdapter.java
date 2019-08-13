package com.videogo.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.videogo.PictureActivity;

import java.util.ArrayList;
import java.util.List;

import ezviz.ezopensdk.R;

public class TitleAdapter extends RecyclerView.Adapter<TitleAdapter.TitleHolder>{
    private Context context;
    private List<String> titile_list;
    private List<List<String>> file_list;
    private int width;
    private ImageRecyclerAdapter imageRecyclerAdapter;

    public TitleAdapter(Context context,List<String> titile_list, List<List<String>> file_list , int width) {
        this.context = context;
        this.titile_list = titile_list;
        this.file_list = file_list;
        this.width = width;
    }

    @Override
    public TitleHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TitleHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_title,parent,false));
    }

    @Override
    public void onBindViewHolder(TitleHolder holder, int position) {
        holder.title.setText(titile_list.get(position));
        holder.recyclerView.setHasFixedSize(true);
        holder.recyclerView.setLayoutManager(new GridLayoutManager(context,width/350));
        holder.recyclerView.setItemAnimator(new DefaultItemAnimator());
        imageRecyclerAdapter = new ImageRecyclerAdapter(context,file_list.get(position));
        holder.recyclerView.setAdapter(imageRecyclerAdapter);
        imageRecyclerAdapter.setItemClickListener(new OnRecyclerItemClickListener() {
            @Override
            protected void click(View item, int p) {
                startActivity(context, (ArrayList<String>)file_list.get(position),p);
            }
        });
    }

    @Override
    public int getItemCount() {
        return titile_list.size();
    }

    public static class TitleHolder extends RecyclerView.ViewHolder{
        private TextView title;
        private RecyclerView recyclerView;
        public TitleHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title_tv);
            recyclerView = itemView.findViewById(R.id.recyclerView);
        }
    }
    public static void startActivity(Context context, ArrayList<String> list, int position){
        Intent intent = new Intent(context, PictureActivity.class);
        intent.putExtra("position",position);
        intent.putStringArrayListExtra("list", (ArrayList<String>) list);
        context.startActivity(intent);
    }
}
