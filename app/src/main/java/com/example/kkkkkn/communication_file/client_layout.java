package com.example.kkkkkn.communication_file;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class client_layout extends AppCompatActivity {
    //变量声明
    private Context mContext;
    private EditText editText_ip;
    private String server_ip;
    public final String TAG="客户端";
    private Switch client_switch;
    private Socket client_socket;
    private OutputStream outputStream;
    private InputStream inputStream,file_in;
    private Handler client_handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 200:
                    //连接成功
                    Log.e(TAG, "client_handler: 连接成功a");
                    //从文件读取到文件流
                    try {
                        file_in=openFileInput("aa.text");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case 500:
                    //连接失败
                    Log.e(TAG, "client_handler: 连接失败");
                    break;
            }
        }
    };
    private Thread clien_connection=new Thread(){
        @Override
        public void run() {
            try {
                //指定ip地址和端口号
                client_socket = new Socket(server_ip,6666);
                if(client_socket != null){
                    //获取输出流、输入流
                    outputStream = client_socket.getOutputStream();
                    inputStream = client_socket.getInputStream();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Message msg=client_handler.obtainMessage();
                msg.what=500;
                client_handler.sendMessage(msg);
                return;
            }
            Log.i(TAG,"Thread  clien_connection:连接成功");
            Message msg=client_handler.obtainMessage();
            msg.what=200;
            client_handler.sendMessage(msg);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_layout);

        //初始化界面
        InitView();

        //显示本机IP地址
        ShowIp();
    }

    //初始化界面+控件绑定
    private void InitView(){
        mContext=getApplicationContext();
        editText_ip=findViewById(R.id.server_ip);
        client_switch=findViewById(R.id.server_switch);
        //switch控件的监听
        client_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //获取输入的IP，开始连接服务器
                    server_ip=editText_ip.getText().toString();
                    //连接要在线程中进行
                    clien_connection.start();
                }else{
                    //断开和服务器的连接
                    try {
                        if (outputStream != null) {
                            outputStream.close(); //关闭输出流
                            outputStream = null;
                        }
                        if (inputStream != null) {
                            inputStream.close(); //关闭输入流
                            inputStream = null;
                        }
                        if(client_socket != null){
                            client_socket.close();  //关闭socket
                            client_socket = null;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    //显示获取当前主机IP  仅鉴别wifi
    private String ShowIp(){
        ConnectivityManager connectivityManager=(ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                return intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
            }
            //无wifi连接，请连接wifi
            Log.i(TAG, "ShowIp: 无wifi连接，请连接wifi");
        } else {
            //当前无网络连接,请在设置中打开网络
            Log.i(TAG, "ShowIp: 当前无网络连接，请在设置中打开网络");
        }
        return null;
    }

    //int类型转String类型
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }
}
