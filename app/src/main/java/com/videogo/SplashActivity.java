package com.videogo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.videogo.been.AlarmContant;
import com.videogo.been.User;
import com.videogo.main.EzvizWebViewActivity;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.ui.util.ExampleUtil;
import com.videogo.warning.OkHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ezviz.ezopensdk.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.videogo.been.AlarmContant.AppKey;
import static com.videogo.been.AlarmContant.Secret;

/**
 * 登录页
 */

public class SplashActivity extends Activity {

    private SharedPreferences.Editor editor;
    private SharedPreferences.Editor editor2;
    private SharedPreferences sharedPreferences;
    private String TAG = "SplashActivity";
    //启动页面
    EditText etUsername, etPassword;
    Button btnLogin;
    String strUsername, strPassword;
    String name ,password;
    private Handler handler ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        editor = getSharedPreferences("userid",MODE_PRIVATE).edit();
        editor2 = getSharedPreferences("user_info",MODE_PRIVATE).edit();
        sharedPreferences = getSharedPreferences("user_info",0);
        name = sharedPreferences.getString("name","");
        password = sharedPreferences.getString("password","");
        etUsername = (EditText) findViewById(R.id.et_login_username);
        etPassword = (EditText) findViewById(R.id.et_login_password);
        btnLogin = (Button) findViewById(R.id.btn_login);
        //btnSignup = (Button) findViewById(R.id.btn_signup);
        getAccessToken();
        if (!name.equals("")&&!password.equals("")){
            etUsername.setText(name);
            etPassword.setText(password);
        }else{
            if (!name.equals("")){
                etUsername.setText(name);
            }
        }

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 102:
                        Bundle bundle = msg.getData();
                        String flag = bundle.getString("flag");
                        //用户名密码正确跳转
                        if (flag.equals("true")) {
                            setUserType(strUsername);
                            User user = (User) bundle.getSerializable("user");
                            editor.putString("id",user.getUserId());
                            editor.commit();
                            editor2.putString("name",strUsername).commit();
                            editor2.putString("password",strPassword).commit();
                            Intent it = new Intent(SplashActivity.this, EzvizWebViewActivity.class);//启动MainActivity
                            startActivity(it);
                            SplashActivity.this.finish();//关闭当前Activity，防止返回到此界面
                        } else {
                            Toast.makeText(SplashActivity.this, "密码错误！", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 103:
                        Toast.makeText(SplashActivity.this, "网络异常！", Toast.LENGTH_LONG).show();
                        break;
                    case 104:
                        Bundle bundle1 = msg.getData();
                        String accessToken = bundle1.getString("accessToken");
                        EZOpenSDK.getInstance().setAccessToken(accessToken);
                        break;
                }
            }
        };
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strUsername = etUsername.getText().toString().trim();
                strPassword = etPassword.getText().toString().trim();
                if(!ExampleUtil.isConnected(SplashActivity.this)){
                    ToastNotRepeat.show(SplashActivity.this,"确认网络是否断开！");
                }else{
                    if (strUsername.trim().equals("")){
                        Toast.makeText(SplashActivity.this, "请您输入用户名！", Toast.LENGTH_SHORT).show();
                    }else{
                        if (strPassword.trim().equals("")){
                            Toast.makeText(SplashActivity.this, "请您输入密码！", Toast.LENGTH_SHORT).show();
                        }else{
                            login(strUsername, strPassword);
//                            Intent it = new Intent(SplashActivity.this, EzvizWebViewActivity.class);//启动MainActivity
//                            startActivity(it);
                        }
                    }
                }
            }
        });
//        btnSignup.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent it = new Intent(SplashActivity.this, SignupActivity.class);//启动注册页面
//                startActivity(it);
//            }
//        });


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
    private void setUserType(String etUsername){
        if (etUsername.equals("chengguan")){
            EzvizApplication.setUser_type(6);
            EzvizApplication.setTable_name("alarmMessage6");
        }else if (etUsername.equals("shiwuju")){
            EzvizApplication.setUser_type(7);
            EzvizApplication.setTable_name("alarmMessage7");
        }else if(etUsername.equals("huanbaoju")){
            EzvizApplication.setUser_type(8);
            EzvizApplication.setTable_name("alarmMessage8");
        }else if (etUsername.equals("zhifaju")){
            EzvizApplication.setUser_type(9);
            EzvizApplication.setTable_name("alarmMessage9");
        }else if (etUsername.equals("fazhanju")){
            EzvizApplication.setUser_type(10);
            EzvizApplication.setTable_name("alarmMessage10");
        }else if (etUsername.equals("admin")){
            EzvizApplication.setUser_type(11);
            EzvizApplication.setTable_name("alarmMessage11");
        }
    }
    //验证登录
    public void login(String username, String password) {
        String url = AlarmContant.service_url+"api/login";
        Map<String,String> map = new HashMap<>();
        map.put("userName",strUsername);
        map.put("password",strPassword);
        OkHttpUtil.post(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: ");
                Message message = new Message();
                message.what = 103;
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d(TAG, "result="+responseBody);
                try {
                    JSONObject object = new JSONObject(responseBody);
                    String result = object.get("success").toString();
                        String data = object.get("data").toString();
                        Gson gson = new Gson();
                        Message message = new Message();
                        message.what = 102;
                        Bundle bundle = new Bundle();
                        bundle.putString("flag",result);
                        if (result.equals("true")){
                            User user = gson.fromJson(data,User.class);
                            bundle.putSerializable("user",user);
                        }
                        message.setData(bundle);
                        handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },map);
    }
    private void getAccessToken(){
        String url = "https://open.ys7.com/api/lapp/token/get";
        Map<String,String> map = new HashMap<>();
        map.put("appKey",AppKey);
        map.put("appSecret",Secret);
        OkHttpUtil.post(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                //Log.d(TAG,"response="+responseBody);
                try {
                    JSONObject object = new JSONObject(responseBody);
                    String data = object.get("data").toString();
                    JSONObject obj = new JSONObject(data);
                    String accessToken = obj.get("accessToken").toString();
                    Message message = new Message();
                    message.what = 104;
                    Bundle bundle = new Bundle();
                    bundle.putString("accessToken",accessToken);
                    message.setData(bundle);
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },map);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}


