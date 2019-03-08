package com.example.kkkkkn.communication_file;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.io.IOException;
import java.net.ServerSocket;

public class server_layout extends AppCompatActivity {
    /*变量定义*/
    private Switch switch_flag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_layout);

        //初始化view
        Initview();
    }

    //初始化界面控件+监听
    private void Initview(){
        switch_flag=findViewById(R.id.server_switch);
        switch_flag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //开启服务和端口socket

                }else{
                    //关闭服务和端口socket

                }
            }
        });
    }


}
