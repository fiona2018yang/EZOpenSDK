package com.videogo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;
import ezviz.ezopensdk.R;

public class LeftListAdapter extends RecyclerView.Adapter<LeftListAdapter.LeftHolder>{
    private List<Integer> sizeList;
    private Context context;
    public static int  selectItem=-1;

    public LeftListAdapter(List<Integer> sizeList,  Context context) {
        this.sizeList = sizeList;
        this.context = context;
    }


    public  void setSelectItem(int selectItem) {
        this.selectItem = selectItem;
    }

    @Override
    public LeftHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new LeftListAdapter.LeftHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.table_left_item,parent,false));
    }

    @Override
    public void onBindViewHolder(LeftHolder holder, int position) {
        holder.tv.setText(String.valueOf(sizeList.get(position)));
        Log.d("TAG", String.valueOf(sizeList.get(position)));
//        if (position == selectItem){
//            holder.itemView.setBackgroundColor(Color.YELLOW);
//        }else{
//            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
//        }
    }

    @Override
    public int getItemCount() {
        return sizeList.size();
    }

    public static class LeftHolder extends RecyclerView.ViewHolder{
        private TextView tv;
        public LeftHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv_table_content_item_left);
        }
    }
}
