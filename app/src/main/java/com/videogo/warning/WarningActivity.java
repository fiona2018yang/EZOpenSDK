package com.videogo.warning;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.videogo.ToastNotRepeat;
import com.videogo.adapter.WarningAdapter;

import java.util.ArrayList;
import java.util.List;

import ezviz.ezopensdk.R;

public class WarningActivity  extends Activity {
    private RecyclerView rv;
    private ImageButton back;
    private WarningAdapter adapter;
    private List<String> list;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warning);
        initView();
    }

    private void initView() {
        rv = findViewById(R.id.rv);
        back = findViewById(R.id.back);
        list = new ArrayList<>();
        list.add("垃圾倾倒");
        list.add("违法建筑");
        list.add("违章种植");
        list.add("秸秆焚烧");

        LinearLayoutManager layoutManager =new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);
        //添加分割线
        rv.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        adapter = new WarningAdapter(this, list, new WarningAdapter.setOnclick() {
            @Override
            public void onClick(View view,int position) {
                switch (position){
                    case 0:
                        //垃圾倾倒
                        Intent i1 = new Intent(view.getContext(), GarbageActivity.class);
                        startActivity(i1);
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
                ToastNotRepeat.show(WarningActivity.this,"点击了"+list.get(position));
            }
        });
        rv.setAdapter(adapter);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
