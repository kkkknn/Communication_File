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
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class server_layout extends AppCompatActivity {
    /*变量定义*/
    private Switch switch_flag;
    private String filepath=Environment.getExternalStorageDirectory()+"/test.txt";
    private TextView server_ip;
    private Context mContext;
    public final String TAG="服务器端";
    private boolean server_Thread_flag;
    private Socket sercer_socket;
    private Thread  sercer_thread;
    private ServerSocket mServerSocket;
    private byte[] receive_buffer=new byte[1024];
    private int receive_count;
    //输入输出流
    private OutputStream outputStream,file_out;
    private InputStream inputStream;
    private Handler mhandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 200:
                    //连接成功
                    Log.i(TAG, "handleMessage: 200:连接成功，开启接收线程等待接收数据");
                    //开始写入输入的输出流到本地文件
                    try {
                        //创建本地文件
                        File receive_file=new File(filepath);
                        if(receive_file.exists()){
                            Log.i(TAG, "receive_file: 文件已存在！");
                        }else{
                            try {
                                receive_file.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        //确认输出流
                        file_out=new FileOutputStream(receive_file);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    //开启接收写入线程
                    receive_thread.start();
                    break;
                case 500:
                    //出错
                    Log.e(TAG, "handleMessage: 500:连接失败，出错" );
                    break;
            }
        }
    };
    //接收线程
    private Thread receive_thread=new Thread(new Runnable() {
        @Override
        public void run() {
            while(server_Thread_flag){
                //数据存在buffer中，count为读取到的数据长度。
                try {
                    //读取到就循环读取
                    receive_count=inputStream.read(receive_buffer);
                    if(receive_count>0){
                        file_out.write(receive_buffer,0,receive_count);
                    }
                    receive_count=0;
                    //保存数据到文件
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_layout);

        //初始化view
        Initview();
        Log.i(TAG, "onCreate: "+filepath);
        //显示当前设备连接的wifi IP
        String ip=ShowIp();
        if(ip!=null){
            //显示到界面
            server_ip.setText(ip);
        }else{
            server_ip.setText("暂无网络连接");
        }
    }

    //初始化界面控件+监听
    private void Initview(){
        mContext=getApplicationContext();
        server_ip=findViewById(R.id.server_ip);
        switch_flag=findViewById(R.id.server_switch);
        switch_flag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //开启服务和端口socket
                    Open_socket();
                }else{
                    //关闭服务和端口socket
                    Close_socket();
                }
            }
        });
    }
    //开启服务器端口
    private void Open_socket(){
        try {
            //开启端口
            mServerSocket = new ServerSocket(6666);
            //开启线程，等待客户端的连接
            sercer_thread=new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sercer_socket=mServerSocket.accept();
                        //获取输入流
                        inputStream = sercer_socket.getInputStream();
                        //获取输出流
                        outputStream = sercer_socket.getOutputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //通知handler连接成功，开始收发数据
                    Message msgs=mhandler.obtainMessage();
                    if(sercer_socket!=null&&inputStream!=null&&outputStream!=null){
                        msgs.what=200;
                        server_Thread_flag=true;
                    }else{
                        msgs.what=500;
                        server_Thread_flag=false;
                    }
                    mhandler.sendMessage(msgs);
                }
            });
            sercer_thread.start();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Open_socket: 服务器端口开启失败" );
        }
    }
    //关闭服务器端口
    private void Close_socket(){
        if(mServerSocket != null){
            try {
                server_Thread_flag=false;
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //显示获取当前主机IP  仅鉴别wifi
    private String ShowIp(){
        ConnectivityManager  connectivityManager=(ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                return intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
            }
            //无wifi连接，请连接wifi

        } else {
            //当前无网络连接,请在设置中打开网络
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
