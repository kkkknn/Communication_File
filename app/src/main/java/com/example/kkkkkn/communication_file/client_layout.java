package com.example.kkkkkn.communication_file;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class client_layout extends AppCompatActivity {
    //变量声明
    private Context mContext;
    private String filepath=Environment.getExternalStorageDirectory()+"/test.txt";
    private int read_count;
    private byte[] send_byte=new byte[1024];
    private Long start_time,run_time;
    private TextView client_ip,client_msg;
    private EditText editText_ip;
    private String server_ip;
    private Button send_start;
    private boolean connect_flag=false;
    public final String TAG="客户端";
    private Switch client_switch;
    private Socket client_socket;
    private OutputStream outputStream;
    private InputStream inputStream,file_in;
    private Thread send_thread,client_connection;
    private Handler client_handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 200:
                    //连接成功
                    Log.e(TAG, "client_handler: 连接成功");
                    client_msg.append("连接成功\n");
                    connect_flag=true;
                    break;
                case 500:
                    //连接失败
                    Log.e(TAG, "client_handler: 连接失败");
                    client_msg.append("连接失败\n");
                    break;
                case 204:
                    //开始发送文件线程
                    //确认socket已经连接
                    if(client_socket.isConnected()){
                        //开启线程
                        send_thread=new Thread(){
                            @Override
                            public void run() {
                                try {
                                    Log.i(TAG, "file: 文件开始发送!");
                                    start_time = System.currentTimeMillis();  //開始時間
                                    Message message=client_handler.obtainMessage();
                                    message.what=600;
                                    message.obj="文件开始发送!\n";
                                    client_handler.sendMessage(message);
                                    while((read_count = file_in.read(send_byte)) > 0) {
                                        outputStream.write(send_byte, 0, read_count);
                                    }
                                    outputStream.flush();
                                    //结束输出流，让服务端接收到数据
                                    Log.i(TAG, "file: 文件发送成功!");
                                    run_time=System.currentTimeMillis()-start_time;
                                    Message messageend=client_handler.obtainMessage();
                                    messageend.what=600;
                                    messageend.obj="文件发送成功!\n";
                                    client_handler.sendMessage(messageend);
                                    //在页面上输出发送用时
                                    Message messageruntime=client_handler.obtainMessage();
                                    messageruntime.what=600;
                                    messageruntime.obj="用时"+run_time+"ms\n";
                                    client_handler.sendMessage(messageruntime);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        send_thread.start();
                    }
                    break;
                case 600:
                    String text=(String)msg.obj;
                    client_msg.append(text);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_layout);
        //初始化界面
        InitView();

        //显示本机IP地址
        String ip=ShowIp();
        client_ip.setText(ip);
    }

    //初始化界面+控件绑定
    private void InitView(){
        mContext=getApplicationContext();
        client_msg=findViewById(R.id.client_msg);
        send_start=findViewById(R.id.send_start);
        client_ip=findViewById(R.id.client_ip);
        editText_ip=findViewById(R.id.server_ip);
        client_switch=findViewById(R.id.switch_client);
        //switch控件的监听
        client_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //获取输入的IP，开始连接服务器
                    server_ip=editText_ip.getText().toString();
                    //开始连接线程
                    client_connection=new Thread(){
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
                            Log.i(TAG,"Thread  client_connection:连接成功");
                            Message msg=client_handler.obtainMessage();
                            msg.what=200;
                            client_handler.sendMessage(msg);
                        }
                    };
                    //连接要在线程中进行
                    client_connection.start();
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
                        connect_flag=false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //按钮点击事件的监听
        send_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connect_flag){
                    if(send_thread!=null&&send_thread.isAlive()) {
                        Log.i(TAG, "onClick: 文件尚未传输完成，请耐心等待。");
                    }else{
                        //从文件读取到文件流
                        try {
                            File send_file=new File(filepath);
                            file_in=new FileInputStream(send_file);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        if(file_in!=null){
                            //通知handler开启发送线程
                            Message file_start=client_handler.obtainMessage();
                            file_start.what=204;
                            client_handler.sendMessage(file_start);
                        }else{
                            Log.i(TAG, "onClick: 未找到文件路径，请确认路径是否正确");
                        }
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
