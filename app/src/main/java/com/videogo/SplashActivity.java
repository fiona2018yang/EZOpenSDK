package com.videogo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.videogo.main.EzvizWebViewActivity;
import com.videogo.ui.cameralist.CountDownView;

import ezviz.ezopensdk.R;

/**
 * Created by 软件组02 on 2018/10/17.
 */

public class SplashActivity extends Activity {

    //启动页面
    private CountDownView countDownView;
    private boolean isIntent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        //倒计时球
        countDownView = (CountDownView) findViewById(R.id.count_down_view);
        countDownView.setOnLoadingFinishListener(new CountDownView.OnLoadingFinishListener() {
            @Override
            public void finish() {
                if (!isIntent) {
                    //倒计时结束自然跳转
                    Intent it = new Intent(SplashActivity.this, EzvizWebViewActivity.class);//启动MainActivity
                    startActivity(it);
                    isIntent = true;
                    SplashActivity.this.finish();//关闭当前Activity，防止返回到此界面
                }
            }
        });

        countDownView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击跳转
                Intent it = new Intent(SplashActivity.this, EzvizWebViewActivity.class);//启动MainActivity
                startActivity(it);
                isIntent = true;
                finish();//关闭当前Activity，防止返回到此界面
            }
        });
        countDownView.start();
    }
}
