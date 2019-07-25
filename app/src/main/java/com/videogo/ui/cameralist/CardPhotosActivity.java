package com.videogo.ui.cameralist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ezviz.ezopensdk.R;

/**
 * Created by 软件组02 on 2018/10/16.
 */

public class CardPhotosActivity extends Activity {

    private ImageView card_none;
    private ImageView back;
    private GridView gridView;
    private DisplayMetrics dm;
    private List<String> mList = new ArrayList<>();
    private GridImageAdapter gridImageAdapter;

    private boolean isPhotos = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_photos_activity);
        initView();
    }

    private void initView() {
        back = (ImageView) findViewById(R.id.card_back);
        card_none = (ImageView) findViewById(R.id.card_none);
        gridView = (GridView) findViewById(R.id.myGrid);

        dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //获取图片路径
    private List<String> getPictures(String strPath) {
        // 图片列表
        List<String> imagePathList = new ArrayList<String>();
        // 得到sd卡内image文件夹的路径   File.separator(/)
        String filePath = strPath;
        // 得到该路径文件夹下所有的文件
        File fileAll = new File(filePath);
        File[] files = fileAll.listFiles();
        // 将所有的文件存入ArrayList中,并过滤所有图片格式的文件
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                String filePaths = filePath + file.getName();
                File fileAlls = new File(filePaths);
                File[] files_two = fileAlls.listFiles();
                for (int j = 0; j < files_two.length; j++) {
                    File file1 = files_two[j];
                    if (checkIsImageFile(file1.getPath())) {
                        imagePathList.add(file1.getPath());
                    }
                }
            } else {
                if (checkIsImageFile(file.getPath())) {
                    imagePathList.add(file.getPath());
                }
            }
        }
        // 返回得到的图片列表
        return imagePathList;
    }

    //判断是否有截图文件夹
    private boolean IsPhotosDir(String strPath) {
        //判断是否有图片文件夹
        File photoFile = new File(strPath);
        File[] photoFiles = photoFile.listFiles();
        for (int k = 0; k < photoFiles.length; k++) {
            File file = photoFiles[k];
            if (file.isDirectory()) {
                if (file.getName().equals("CapturePicture")) {
                    isPhotos = true;
                    break;
                } else {
                    isPhotos = false;
                }
            }
        }
        return isPhotos;
    }

    /**
     * 检查扩展名，得到图片格式的文件
     *
     * @param fName 文件名
     * @return
     */
    private boolean checkIsImageFile(String fName) {
        boolean isImageFile = false;
        // 获取扩展名
        String FileEnd = fName.substring(fName.lastIndexOf(".") + 1,
                fName.length()).toLowerCase();
        if (FileEnd.equals("jpg") || FileEnd.equals("png") || FileEnd.equals("gif")
                || FileEnd.equals("jpeg") || FileEnd.equals("bmp")) {
            isImageFile = true;
        } else {
            isImageFile = false;
        }
        return isImageFile;
    }

    AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
            Intent intent = new Intent(CardPhotosActivity.this, GalleryActivity.class);
            intent.putExtra("position", position);
            intent.putStringArrayListExtra("list", (ArrayList<String>) mList);
            startActivity(intent);
        }
    };

    public class GridImageAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mList.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            int space;
            if (convertView == null) {
                holder = new ViewHolder();
                // 初始化绑定控件
                convertView = getLayoutInflater().inflate(R.layout.imageview_item_layout, null);
                holder.imgShow = (ImageView) convertView.findViewById(R.id.img);
                space = dm.widthPixels / 3 - 10;
                holder.imgShow.setLayoutParams(new GridView.LayoutParams(space, space));
                holder.imgShow.setAdjustViewBounds(true);
                holder.imgShow.setScaleType(ImageView.ScaleType.CENTER_CROP);    // 缩放图片使其长和宽一样
                // add to convertView
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Picasso.with(CardPhotosActivity.this).load("file://" + mList.get(position)).into(holder.imgShow);
            return convertView;
        }
    }

    class ViewHolder {
        ImageView imgShow;
    }

    @Override
    protected void onDestroy() {
        overridePendingTransition(0, 0);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        //初始化图片
        if (IsPhotosDir(Environment.getExternalStorageDirectory().getPath() + "/EZOpenSDK/")) {
            List<String> list = getPictures(Environment.getExternalStorageDirectory().getPath() + "/EZOpenSDK/CapturePicture/");
            if (list != null && list.size() != 0) {
                card_none.setVisibility(View.GONE);
                gridView.setVisibility(View.VISIBLE);

                mList.clear();
                for (int i = 0; i < list.size(); i++) {
                    mList.add(list.get(i));
                }

                gridImageAdapter = new GridImageAdapter();
                gridView.setAdapter(gridImageAdapter);
                gridView.setOnItemClickListener(listener); // 设置点击监听事件

            } else {
                card_none.setVisibility(View.VISIBLE);
                gridView.setVisibility(View.GONE);
            }
        } else {
            card_none.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.GONE);
        }
        super.onResume();
    }

    @Override
    protected void onRestart() {
        gridImageAdapter.notifyDataSetChanged();
        super.onRestart();
    }
}
