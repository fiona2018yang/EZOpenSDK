package com.videogo.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class RecyclerViewError extends LinearLayoutManager {
    public RecyclerViewError(Context context) {
        super(context);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        }catch (IndexOutOfBoundsException e){
            e.printStackTrace();
        }
    }
}
