package com.videogo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.videogo.been.Temp;

import java.util.List;

import ezviz.ezopensdk.R;

public class RightlistAdapter extends RecyclerView.Adapter<RightlistAdapter.RightHolder>  {
    private List<Temp> tempList;
    private Context context;
    public static int  selectItem=-1;

    public RightlistAdapter(List<Temp> tempList, Context context) {
        this.tempList = tempList;
        this.context = context;
    }

    public  void setSelectItem(int selectItem) {
        this.selectItem = selectItem;
    }

    @Override
    public RightHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RightHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.table_right_item,parent,false));
    }

    @Override
    public void onBindViewHolder(RightHolder holder, int position) {
        Temp temp = tempList.get(position);
        holder.temp.setText(temp.getTemp());
        holder.Ph.setText(temp.getPh());
        holder.Oxygen.setText(temp.getOxygen());
        holder.Nitrogen.setText(temp.getNitrogen());
        holder.Permanganate.setText(temp.getPermanganate());
        holder.Phosphorus.setText(temp.getPhosphorus());
        holder.Potential.setText(temp.getPotential());
        holder.Time.setText(temp.getTime());
//        if (position == selectItem){
//            holder.itemView.setBackgroundColor(Color.YELLOW);
//        }else{
//            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
//        }
    }

    @Override
    public int getItemCount() {
        return tempList.size();
    }

    public static class RightHolder extends RecyclerView.ViewHolder{
        private TextView temp;
        private TextView Ph;
        private TextView Oxygen;
        private TextView Nitrogen;
        private TextView Permanganate;
        private TextView Phosphorus;
        private TextView Potential;
        private TextView Time;
        public RightHolder(View itemView) {
            super(itemView);
            temp = itemView.findViewById(R.id.tv_table_content_right_item0);
            Ph = itemView.findViewById(R.id.tv_table_content_right_item1);
            Oxygen = itemView.findViewById(R.id.tv_table_content_right_item2);
            Nitrogen = itemView.findViewById(R.id.tv_table_content_right_item3);
            Permanganate = itemView.findViewById(R.id.tv_table_content_right_item4);
            Phosphorus = itemView.findViewById(R.id.tv_table_content_right_item5);
            Potential = itemView.findViewById(R.id.tv_table_content_right_item6);
            Time = itemView.findViewById(R.id.tv_table_content_right_item7);
        }
    }
}
