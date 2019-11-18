package com.videogo;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import ezviz.ezopensdk.R;

public class SignupActivity extends Activity {
    Button btnConfirm;
    Spinner spnUsertype;


    private MyDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        btnConfirm = (Button)findViewById(R.id.btn_confirm);
                btnConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        logon(v);
                        SignupActivity.this.finish();//关闭当前Activity，防止返回到此界面
                    }
                });

        dbHelper = new MyDatabaseHelper(this,"UserStore.db",null,1);
    }

    public void logon(View view){
        //SQLiteDatabase db=dbHelper.getWritableDatabase();

        EditText editText3=(EditText)findViewById(R.id.et_signup_username);
        EditText editText4=(EditText)findViewById(R.id.et_signup_password);
        String newname =editText3.getText().toString();
        String password=editText4.getText().toString();
        if (CheckIsDataAlreadyInDBorNot(newname)) {
            Toast.makeText(this,"该用户名已被注册，注册失败",Toast.LENGTH_SHORT).show();
        }
        else {
            if (register(newname, password)) {
                Toast.makeText(this, "插入数据表成功", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //向数据库插入数据
    public boolean register(String username,String password){
        SQLiteDatabase db= dbHelper.getWritableDatabase();
        /*String sql = "insert into userData(name,password) value(?,?)";
        Object obj[]={username,password};
        db.execSQL(sql,obj);*/
        ContentValues values=new ContentValues();
        values.put("name",username);
        values.put("password",password);
        db.insert("userData",null,values);
        db.close();
        //db.execSQL("insert into userData (name,password) values (?,?)",new String[]{username,password});
        return true;
    }
    //检验用户名是否已存在
    public boolean CheckIsDataAlreadyInDBorNot(String value){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        String Query = "Select * from userData where name =?";
        Cursor cursor = db.rawQuery(Query,new String[] { value });
        if (cursor.getCount()>0){
            cursor.close();
            return  true;
        }
        cursor.close();
        return false;
    }


//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_signup);
//        btnConfirm = (Button)findViewById(R.id.btn_confirm);
//        spnUsertype = (Spinner)findViewById(R.id.spn_usertype);
//        spnUsertype.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {//选择item的选择点击监听事件
//            public void onItemSelected(AdapterView<?> arg0, View arg1,
//                                       int arg2, long arg3) {
//                // 将所选mySpinner 的值带入myTextView 中
//              //  myTextView.setText("您选择的是：" + arg2+"个");//文本说明
//            }
//
//            public void onNothingSelected(AdapterView<?> arg0) {
//              //  myTextView.setText("Nothing");
//            }
//        });
//        btnConfirm.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SignupActivity.this.finish();//关闭当前Activity，防止返回到此界面
//            }
//        });
//    }
}
