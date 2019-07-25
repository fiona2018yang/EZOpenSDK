package com.videogo.ui.cameralist;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.app.AlertDialog.Builder;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ezviz.ezopensdk.R;

/**
 * Created by 软件组02 on 2018/10/16.
 */

public class GalleryActivity extends Activity {

    private int position = 0;    // 当前显示图片的位置
    private List<String> mList = new ArrayList<>();
    private ImageView gallery_back;
    private ImageView gallery_del;
    private int mPos;
    private ImageAdapter imgAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery);
        MyGallery galllery = (MyGallery) findViewById(R.id.mygallery);
        gallery_back = (ImageView) findViewById(R.id.gallery_back);
        gallery_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);    // 获取GridViewActivity传来的图片位置position
        mList = intent.getStringArrayListExtra("list");
        imgAdapter = new ImageAdapter(this);
        galllery.setAdapter(imgAdapter);        // 设置图片ImageAdapter
        galllery.setSelection(position);        // 设置当前显示图片

        Animation an = AnimationUtils.loadAnimation(this, R.anim.scale);        // Gallery动画
        galllery.setAnimation(an);

        gallery_del = (ImageView) findViewById(R.id.gallery_del);
        gallery_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popLogoutDialog();
            }
        });
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context context) {
            mContext = context;
        }

        public void setOwnposition(int ownposition) {
            mPos = ownposition;
        }

        public int getOwnposition() {
            return mPos;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            mPos = position;
            return position;
        }

        @Override
        public long getItemId(int position) {
            mPos = position;
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            mPos = position;
            ImageView imageview = new ImageView(mContext);
            //imageview.setBackgroundColor(0xFF000000);
            imageview.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageview.setLayoutParams(new MyGallery.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            Picasso.with(GalleryActivity.this).load("file://" + mList.get(position)).into(imageview);
            return imageview;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void popLogoutDialog() {
        Builder exitDialog = new Builder(GalleryActivity.this);
        exitDialog.setTitle("确认删除");
        exitDialog.setMessage("删除后将找不到该图片，确认要删除？");
        exitDialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File file = new File(mList.get(mPos));
                getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + "=?", new String[]{mList.get(mPos)});//删除系统缩略图
                file.delete();//删除SD中图片
                mList.remove(mPos);
                imgAdapter.notifyDataSetChanged();
                if (mList.size() == 0 || mList == null) {
                    finish();
                }
            }
        });
        exitDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        exitDialog.show();
    }
}
