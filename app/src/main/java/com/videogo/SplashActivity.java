package com.videogo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.videogo.main.EzvizWebViewActivity;
import com.videogo.ui.cameralist.CountDownView;

import ezviz.ezopensdk.R;

/**
 * 登录页
 */

public class SplashActivity extends Activity {

    private MyDatabaseHelper dbHelper;
    //启动页面
    EditText etUsername, etPassword;
    Button btnLogin, btnSignup;
    String strUsername, strPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        dbHelper = new MyDatabaseHelper(this, "UserStore.db", null, 1);

        etUsername = (EditText) findViewById(R.id.et_login_username);
        etPassword = (EditText) findViewById(R.id.et_login_password);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnSignup = (Button) findViewById(R.id.btn_signup);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strUsername = etUsername.getText().toString();
                strPassword = etPassword.getText().toString();
                //用户名密码正确跳转
                if (login(strUsername, strPassword)) {
                    Intent it = new Intent(SplashActivity.this, EzvizWebViewActivity.class);//启动MainActivity
                    startActivity(it);
                    SplashActivity.this.finish();//关闭当前Activity，防止返回到此界面
                } else {
                    Toast.makeText(SplashActivity.this, "登陆失败", Toast.LENGTH_SHORT).show();
                }


                //                if (strUsername.equals("用户名")) {
                //                    if (strPassword.equals("密码")){
                //                        Intent it = new Intent(SplashActivity.this, EzvizWebViewActivity.class);//启动MainActivity
                //                        startActivity(it);
                //                        SplashActivity.this.finish();//关闭当前Activity，防止返回到此界面
                //                    }else{
                //                        Toast.makeText(SplashActivity.this, "密码错误", Toast.LENGTH_LONG).show();
                //                    }
                //
                //                }else {
                //                    Toast.makeText(SplashActivity.this, "用户名错误", Toast.LENGTH_LONG).show();
                //                }
            }
        });
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(SplashActivity.this, SignupActivity.class);//启动注册页面
                startActivity(it);
            }
        });


        //        //倒计时球
        //        countDownView = (CountDownView) findViewById(R.id.count_down_view);
        //        countDownView.setOnLoadingFinishListener(new CountDownView.OnLoadingFinishListener() {
        //            @Override
        //            public void finish() {
        //                if (!isIntent) {
        //                    //倒计时结束自然跳转
        //                    Intent it = new Intent(SplashActivity.this, EzvizWebViewActivity.class);//启动MainActivity
        //                    startActivity(it);
        //                    isIntent = true;
        //                    SplashActivity.this.finish();//关闭当前Activity，防止返回到此界面
        //                }
        //            }
        //        });

        //        countDownView.setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View v) {
        //                //点击跳转
        //                Intent it = new Intent(SplashActivity.this, EzvizWebViewActivity.class);//启动MainActivity
        //                startActivity(it);
        //                isIntent = true;
        //                finish();//关闭当前Activity，防止返回到此界面
        //            }
        //        });
        //        countDownView.start();
    }

    //验证登录
    public boolean login(String username, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "select * from userData where name=? and password=?";
        Cursor cursor = db.rawQuery(sql, new String[]{username, password});
        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        }
        return false;
    }
}


