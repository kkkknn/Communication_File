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
    private TextView server_ip,server_msg;
    private Context mContext;
    private Long start_time,run_time;
    public final String TAG="服务器端";
    private boolean server_Thread_flag;
    private Socket server_socket;
    private Thread  server_thread,receive_thread;
    private ServerSocket mServerSocket;
    private byte[] receive_buffer=new byte[1024];
    private int receive_count,read_count,write_count;
    private boolean read_flag=false;
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
                    server_msg.append("连接成功，开启接收线程等待接收数据\n");
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
                    server_Thread_flag=true;
                    receive_thread=new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(server_Thread_flag){
                                //数据存在buffer中，count为读取到的数据长度。
                                try {
                                    while((read_count=inputStream.available())>0){
                                       /* if(!read_flag){
                                           // Log.i(TAG, "receive_thread: 开始文件传输！");
                                            Message message=mhandler.obtainMessage();
                                            message.what=600;
                                            message.obj="开始文件传输\n";
                                            mhandler.sendMessage(message);
                                            start_time = System.currentTimeMillis();  //開始時間
                                            read_flag=true;
                                        }*/
                                        write_count=read_count;
                                        while(write_count>0){
                                            if(write_count<receive_buffer.length){
                                                receive_count=inputStream.read(receive_buffer,0,write_count);
                                                write_count=0;
                                            }else{
                                                receive_count=inputStream.read(receive_buffer,0,receive_buffer.length);
                                                write_count=write_count-receive_buffer.length;
                                            }
                                            file_out.write(receive_buffer,0,receive_count);
                                            file_out.flush();
                                        }
                                    }
                                  /*  if(read_flag) {
                                       // Log.i(TAG, "receive_thread: 文件传输完成,写入完成！");
                                        Message message=mhandler.obtainMessage();
                                        message.what=600;
                                        message.obj="文件传输完成,写入完成！\n";
                                        mhandler.sendMessage(message);
                                        //计算用时输出
                                        run_time = System.currentTimeMillis()-start_time;  //開始時間
                                        Message message2=mhandler.obtainMessage();
                                        message2.what=600;
                                        message2.obj="用时"+run_time+"ms\n";
                                        mhandler.sendMessage(message2);
                                        read_flag = false;
                                    }*/
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    receive_thread.start();
                    break;
                case 500:
                    //出错
                    Log.e(TAG, "handleMessage: 500:连接失败，出错" );
                    server_Thread_flag=false;
                    break;
                case 600:
                    String text=(String)msg.obj;
                    server_msg.append(text);
                    break;
            }
        }
    };

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
        server_msg=findViewById(R.id.server_msg);
        server_ip=findViewById(R.id.server_ip);
        switch_flag=findViewById(R.id.server_switch);
        switch_flag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //开启服务和端口socket
                    Log.i(TAG, "onCheckedChanged: 打开端口");
                    Open_socket();
                }else{
                    //关闭服务和端口socket
                    Log.i(TAG, "onCheckedChanged: 关闭端口");
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
            Log.i(TAG, "Open_socket: 端口开启成功，开启线程，等待客户端连接！");
            //开启线程，等待客户端的连接
            server_thread=new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        server_socket=mServerSocket.accept();
                        //获取输入流
                        inputStream = server_socket.getInputStream();
                        //获取输出流
                        outputStream = server_socket.getOutputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }

                    //通知handler连接成功，开始收发数据
                    Message msgs=mhandler.obtainMessage();
                    if(server_socket!=null&&inputStream!=null&&outputStream!=null){
                        msgs.what=200;
                        server_Thread_flag=true;
                    }else{
                        msgs.what=500;
                        server_Thread_flag=false;
                    }
                    mhandler.sendMessage(msgs);
                }
            });
            server_thread.start();
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
                server_thread.interrupt();
                //关闭输入输出流
                if(outputStream!=null){
                    outputStream.close();
                }
                if(file_out!=null){
                    file_out.close();
                }
                if(inputStream!=null){
                    inputStream.close();
                }
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
