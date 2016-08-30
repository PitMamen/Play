package com.example.handlerdemo;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main);


        HandlerThread handlertheard = new HandlerThread("handlwetheard");

        handlertheard.start();



        MyHandler handler = new MyHandler(handlertheard.getLooper());


        Message msg= handler.obtainMessage();


        Bundle bundle = new Bundle();

        bundle.putString("hello","你好啊");
        bundle.putInt("Age",12);

        msg.setData(bundle);


        msg.sendToTarget();






    }

    public class MyHandler extends Handler {


        public MyHandler(Looper looper) {

            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Log.d("TAG", "Handler_ID----++:"+Thread.currentThread().getId());
            Log.d("TAG", "Handler_NAME----++:"+Thread.currentThread().getName());



           Bundle b = msg.getData();
            String hello = b.getString("hello");
            int number = b.getInt("Age");

            Log.d("TAG", "hello======: "+hello.toString());
            Log.d("TAG", "number======: "+number);


        }
    }
}
