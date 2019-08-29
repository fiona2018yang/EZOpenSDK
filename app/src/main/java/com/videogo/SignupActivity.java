package com.videogo;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import ezviz.ezopensdk.R;

public class SignupActivity extends Activity {
    Button btnConfirm;
    Spinner spnUsertype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        btnConfirm = (Button)findViewById(R.id.btn_confirm);
        spnUsertype = (Spinner)findViewById(R.id.spn_usertype);
        spnUsertype.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {//选择item的选择点击监听事件
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // 将所选mySpinner 的值带入myTextView 中
              //  myTextView.setText("您选择的是：" + arg2+"个");//文本说明
            }

            public void onNothingSelected(AdapterView<?> arg0) {
              //  myTextView.setText("Nothing");
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignupActivity.this.finish();//关闭当前Activity，防止返回到此界面
            }
        });
    }
}
