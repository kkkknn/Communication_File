package com.example.kkkkkn.communication_file;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
   /* 控件声明*/
    private Button btn_server,btn_client;
    /*变量声明*/
    private int device_flag=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化控件
        Initview();
    }
    //初始化控件
    private void Initview(){
        //控件绑定
        btn_server=findViewById(R.id.server);
        btn_client=findViewById(R.id.client);
    }

    //2按钮的点击事件处理
    public void JumpTo(View view){
        switch (view.getId()){
            case R.id.server:
                //修改标识
                device_flag=1;
                //跳转到相应的页面
                Intent server_intent = new Intent(MainActivity.this, server_layout.class);
                startActivity(server_intent);
                break;
            case R.id.client:
                //修改标识
                device_flag=2;
                //跳转到相应的页面
                Intent clinet_intent = new Intent(MainActivity.this, client_layout.class);
                startActivity(clinet_intent);
                break;
        }
    }
}
