package com.videogo.scanpic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.videogo.adapter.PhotoPagerAdapter;
import java.io.File;
import java.util.ArrayList;
import ezviz.ezopensdk.R;

public class PictureActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private TextView tvNum;
    private ArrayList<String> urlList;
    private int position;
    private ImageButton shareBtn;
    private ImageButton delateBtn;
    private Boolean flag = false;
    private Uri uri;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_picture);
        initView();
    }

    private void initView() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        shareBtn = (ImageButton) findViewById(R.id.share);
        delateBtn = (ImageButton) findViewById(R.id.delate);
        //tvNum = (TextView) findViewById(R.id.tv_num);
        urlList = new ArrayList<>();
        Intent intent = getIntent();
        urlList = intent.getStringArrayListExtra("list");
        position = intent.getIntExtra("position",0);
        flag = intent.getBooleanExtra("flag",false);
        PhotoPagerAdapter viewPagerAdapter = new PhotoPagerAdapter(getSupportFragmentManager(), urlList);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setCurrentItem(position);
        uri = Uri.parse(urlList.get(position));
        Log.d("PictureActivity","uri="+uri);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //tvNum.setText(String.valueOf(position + 1) + "/" + urlList.size());
                uri = Uri.parse(urlList.get(position));
                path = urlList.get(position);
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareImg("分享","分享","分享",uri);
            }
        });

        delateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!flag){
                    File file = new File(path);
                    if (file.exists()&&file.isFile()){
                        file.delete();
                        Toast.makeText(PictureActivity.this,"文件已删除!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }else{
                    String path = urlList.get(0);
                    File file = new File(path);
                    if (file.exists()&&file.isFile()){
                        file.delete();
                        Toast.makeText(PictureActivity.this,"文件已删除!", Toast.LENGTH_SHORT).show();
                        Intent intent1 = new Intent();
                        intent1.setAction("com.delate.pic");
                        sendBroadcast(intent1);
                        finish();
                    }
                }
            }
        });
    }
    /**
     * 分享图片和文字内容
     *
     * @param dlgTitle
     *            分享对话框标题
     * @param subject
     *            主题
     * @param content
     *            分享内容（文字）
     * @param uri
     *            图片资源URI
     */
    private void shareImg(String dlgTitle, String subject, String content, Uri uri) {
        if (uri == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        if (subject != null && !"".equals(subject)) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (content != null && !"".equals(content)) {
            intent.putExtra(Intent.EXTRA_TEXT, content);
        }
        // 设置弹出框标题
        if (dlgTitle != null && !"".equals(dlgTitle)) { // 自定义标题
            startActivity(Intent.createChooser(intent, dlgTitle));
        } else { // 系统默认标题
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
