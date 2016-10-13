package com.example.notifacationdemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    public static final int DOWNIMA = 1;
    private Button btn_imag;
    private ImageView imageView;
    private String imgPath = "http://f.hiphotos.baidu.com/image/w%3D2048/sign=05793c21bba1cd1105b675208d2ac9fc/43a7d933c895d14350ee3c3272f082025aaf0703.jpg";
    private ProgressDialog progressDialog = null;

    Handler handler = new Handler() {


        @Override
        public void handleMessage(Message msg) {

            byte[] data = (byte[]) msg.obj;
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            imageView.setImageBitmap(bitmap);

            if (msg.what == DOWNIMA) {
                progressDialog.dismiss();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponent();


        btn_imag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Thread(new MyTread()).start();

                progressDialog.show();

            }
        });

        // init();
    }

    private void initComponent() {
        btn_imag = (Button) findViewById(R.id.btn_downimg);
        imageView = (ImageView) findViewById(R.id.iv_image);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("提示");
        progressDialog.setMessage("正在下载");
        progressDialog.setCancelable(false);


    }

    private void init() {

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("测试标题")//设置通知栏标题
                .setContentText("测试内容")
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(getDefalutIntent(Notification.FLAG_AUTO_CANCEL)) //设置通知栏点击意图
               /* .setTicker("测试通知来啦") //通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                .setOngoing(false)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                .setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
                //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission*/
                .setSmallIcon(R.mipmap.ic_launcher);//设置通知小ICON

       /* Notification notification = mBuilder.build();
        notification.flags = Notification.FLAG_SHOW_LIGHTS;*/


        Notification notify = mBuilder.build();
        //   notify.flags = Notification.FLAG_SHOW_LIGHTS;
        notify.ledARGB = 0xff0000ff;
        notify.ledOnMS = 300;
        notify.ledOffMS = 300;

        int notifyId = 1;
        mNotificationManager.notify(notifyId, notify);
    }

    public PendingIntent getDefalutIntent(int flags) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, new Intent(), flags);
        return pendingIntent;
    }


    class MyTread implements Runnable {

        @Override
        public void run() {

            HttpClient client = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(imgPath);
            HttpResponse response = null;

            try {
                response = client.execute(httpget);

                if (200 == response.getStatusLine().getStatusCode()) {
                    byte[] data = EntityUtils.toByteArray(response.getEntity());

                    Message.obtain(handler,DOWNIMA,data).sendToTarget();

//                    Message message = Message.obtain(handler);
//                    message.obj = data;
//                    message.what = DOWNIMA;
//
//                    message.sendToTarget();
                   /* message.obj = data;
                    message.what = DOWNIMA;

                    handler.sendMessage(message);*/

                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }


}
