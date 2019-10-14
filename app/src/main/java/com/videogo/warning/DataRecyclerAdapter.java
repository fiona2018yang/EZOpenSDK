package com.videogo.warning;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.videogo.been.WarningData;
import java.io.File;
import java.util.List;
import ezviz.ezopensdk.R;

public class DataRecyclerAdapter extends RecyclerView.Adapter<DataRecyclerAdapter.DataHolder>{
    private Context context;
    private List<WarningData> dataList;

    public DataRecyclerAdapter(Context context, List<WarningData> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public DataHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DataHolder(LayoutInflater.from(context).inflate(R.layout.item_data,parent,false));
    }

    @Override
    public void onBindViewHolder(DataHolder holder, int position) {
        WarningData warningData = dataList.get(position);
        Picasso.with(context).load(new File(warningData.getPath())).resize(400,300).transform(new RoundTransform(context,20)).into(holder.image);
        holder.address.setText(warningData.getAddress());
        holder.location.setText(warningData.getLocation());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class DataHolder extends RecyclerView.ViewHolder{
        private ImageView image;
        private TextView address;
        private TextView location;
        public DataHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.pic);
            address = itemView.findViewById(R.id.address);
            location = itemView.findViewById(R.id.location);
        }
    }
}
